package com.example.passgplx.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passgplx.data.QuestionRepository
import com.example.passgplx.data.QuestionDataHelper
import com.example.passgplx.data.ReviewHistoryRepository
import com.example.passgplx.models.Category
import com.example.passgplx.models.LicenseType
import com.example.passgplx.models.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryInfo(
    val category: Category,
    val questionCount: Int,
    val answeredCount: Int
)

class ReviewViewModel(
    private val historyRepository: ReviewHistoryRepository = ReviewHistoryRepository()
) : ViewModel() {

    private var pendingAnswerSave: Job? = null

    private val _allQuestions = MutableStateFlow<List<Question>>(emptyList())

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _selectedLicenseType = MutableStateFlow<LicenseType>(LicenseType.B)
    val selectedLicenseType: StateFlow<LicenseType> = _selectedLicenseType.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<String, String>>(emptyMap())
    val selectedAnswers: StateFlow<Map<String, String>> = _selectedAnswers.asStateFlow()

    val activeCategories: StateFlow<List<CategoryInfo>> = combine(
        _selectedLicenseType, _selectedAnswers, _allQuestions
    ) { licenseType, answers, allQs ->
        QuestionDataHelper.categories.mapNotNull { category ->
            val categoryQuestions = allQs.filter { it.licenseClasses.contains(licenseType.name) }
                .filter(category.filter)
            val count = categoryQuestions.size
            if (count > 0) {
                val answeredCount = categoryQuestions.count { answers.containsKey(it.id) }
                CategoryInfo(category, count, answeredCount)
            } else {
                null
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        _selectedAnswers.value = historyRepository.getSavedAnswers()
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _isLoading.value = true
            _allQuestions.value = QuestionRepository.getAllQuestions()
            applyFilters()
            _isLoading.value = false
        }
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        applyFilters()
    }

    fun selectLicenseType(licenseType: LicenseType) {
        _selectedLicenseType.value = licenseType
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = _allQuestions.value.filter { it.licenseClasses.contains(_selectedLicenseType.value.name) }
        
        val category = _selectedCategory.value
        if (category != null) {
            filtered = filtered.filter(category.filter)
        }
        
        _questions.value = filtered
    }

    fun selectAnswer(questionId: String, answerId: String) {
        val newAnswers = _selectedAnswers.value.toMutableMap().also { it[questionId] = answerId }
        _selectedAnswers.value = newAnswers
        
        pendingAnswerSave?.cancel()
        pendingAnswerSave = viewModelScope.launch {
            delay(1500)
            val snapshot = _selectedAnswers.value
            launch(Dispatchers.IO) { historyRepository.saveAnswers(snapshot) }
        }
    }

    fun clearHistoryForCategory(category: Category) {
        val questionsToClear = _allQuestions.value
            .filter { it.licenseClasses.contains(_selectedLicenseType.value.name) }
            .filter(category.filter)
        
        val currentAnswers = _selectedAnswers.value.toMutableMap()
        questionsToClear.forEach { currentAnswers.remove(it.id) }
        
        _selectedAnswers.value = currentAnswers
        historyRepository.saveAnswers(currentAnswers)
    }
}
