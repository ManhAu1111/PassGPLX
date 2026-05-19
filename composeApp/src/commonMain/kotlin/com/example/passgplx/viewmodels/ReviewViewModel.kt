package com.example.passgplx.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passgplx.data.QuestionRepository
import com.example.passgplx.data.QuestionDataHelper
import com.example.passgplx.models.Category
import com.example.passgplx.models.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val repository: QuestionRepository = QuestionRepository()
) : ViewModel() {

    private var allQuestions: List<Question> = emptyList()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _categories = MutableStateFlow(QuestionDataHelper.categories)
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _isLoading.value = true
            allQuestions = repository.getAllQuestions()
            _questions.value = allQuestions
            _isLoading.value = false
        }
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        if (category == null) {
            _questions.value = allQuestions
        } else {
            _questions.value = allQuestions.filter(category.filter)
        }
    }
}
