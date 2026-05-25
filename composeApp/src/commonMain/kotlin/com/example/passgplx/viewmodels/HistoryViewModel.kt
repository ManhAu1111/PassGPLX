package com.example.passgplx.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passgplx.data.ReviewHistoryRepository
import com.example.passgplx.models.MockExamRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: ReviewHistoryRepository = ReviewHistoryRepository()
) : ViewModel() {

    private val _completedExams = MutableStateFlow<List<MockExamRecord>>(emptyList())
    val completedExams: StateFlow<List<MockExamRecord>> = _completedExams.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _completedExams.value = historyRepository.getCompletedMockExams()
            _isLoading.value = false
        }
    }
}
