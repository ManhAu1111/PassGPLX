package com.example.passgplx.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passgplx.data.QuestionRepository
import com.example.passgplx.models.Question
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MockExamState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<String, String> = emptyMap(), // Map of Question ID -> Answer ID
    val timeRemainingSeconds: Int = 19 * 60, // 19 minutes
    val isSubmitted: Boolean = false,
    val score: Int = 0,
    val isLoading: Boolean = true
)

class MockExamViewModel(
    private val repository: QuestionRepository = QuestionRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(MockExamState())
    val state: StateFlow<MockExamState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        startNewExam()
    }

    fun startNewExam() {
        timerJob?.cancel()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isSubmitted = false, selectedAnswers = emptyMap(), score = 0, currentIndex = 0, timeRemainingSeconds = 19 * 60) }
            val randomQuestions = repository.getRandomQuestions(25)
            _state.update { it.copy(questions = randomQuestions, isLoading = false) }
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_state.value.timeRemainingSeconds > 0 && !_state.value.isSubmitted) {
                delay(1000)
                _state.update { it.copy(timeRemainingSeconds = it.timeRemainingSeconds - 1) }
            }
            if (_state.value.timeRemainingSeconds == 0 && !_state.value.isSubmitted) {
                submitExam()
            }
        }
    }

    fun selectAnswer(questionId: String, answerId: String) {
        if (_state.value.isSubmitted) return
        _state.update {
            val newAnswers = it.selectedAnswers.toMutableMap()
            newAnswers[questionId] = answerId
            it.copy(selectedAnswers = newAnswers)
        }
    }

    fun goToQuestion(index: Int) {
        if (index in _state.value.questions.indices) {
            _state.update { it.copy(currentIndex = index) }
        }
    }
    
    fun nextQuestion() {
        if (_state.value.currentIndex < _state.value.questions.size - 1) {
            _state.update { it.copy(currentIndex = it.currentIndex + 1) }
        }
    }
    
    fun previousQuestion() {
        if (_state.value.currentIndex > 0) {
            _state.update { it.copy(currentIndex = it.currentIndex - 1) }
        }
    }

    fun submitExam() {
        if (_state.value.isSubmitted) return
        timerJob?.cancel()
        
        var calculatedScore = 0
        val currentQuestions = _state.value.questions
        val currentAnswers = _state.value.selectedAnswers
        
        for (q in currentQuestions) {
            val selectedAnswerId = currentAnswers[q.id]
            val correctAnswer = q.answers.find { it.correct }
            if (selectedAnswerId == correctAnswer?.id) {
                calculatedScore++
            }
        }
        
        _state.update { it.copy(isSubmitted = true, score = calculatedScore) }
    }
}
