package com.example.passgplx.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passgplx.data.QuestionRepository
import com.example.passgplx.models.LicenseType
import com.example.passgplx.models.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.passgplx.data.ReviewHistoryRepository
import com.example.passgplx.models.MockExamStateData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock
import com.example.passgplx.models.MockExamRecord

data class MockExamState(
    val selectedLicenseType: LicenseType = LicenseType.B,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<String, String> = emptyMap(), // Map of Question ID -> Answer ID
    val isSubmitted: Boolean = false,
    val score: Int = 0,
    val isLoading: Boolean = false,
    val isExamStarted: Boolean = false
)

class MockExamViewModel(
    private val historyRepository: ReviewHistoryRepository = ReviewHistoryRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(MockExamState())
    val state: StateFlow<MockExamState> = _state.asStateFlow()

    private val _hasSavedExam = MutableStateFlow(false)
    val hasSavedExam: StateFlow<Boolean> = _hasSavedExam.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(0)
    val timeRemainingSeconds: StateFlow<Int> = _timeRemainingSeconds.asStateFlow()

    private var timerJob: Job? = null
    private var pendingSave: Job? = null

    init {
        checkSavedExam()
    }

    private fun checkSavedExam() {
        _hasSavedExam.value = historyRepository.getSavedMockExam() != null
    }

    fun continueExam() {
        viewModelScope.launch {
            val json = historyRepository.getSavedMockExam()
            if (json != null) {
                try {
                    val savedData = Json.decodeFromString<MockExamStateData>(json)
                    val licenseType = LicenseType.valueOf(savedData.licenseType)
                    _timeRemainingSeconds.value = savedData.timeRemainingSeconds
                    _state.update { 
                        it.copy(
                            selectedLicenseType = licenseType,
                            questions = savedData.questions,
                            selectedAnswers = savedData.selectedAnswers,
                            currentIndex = savedData.currentIndex,
                            isSubmitted = savedData.isSubmitted,
                            score = savedData.score,
                            isExamStarted = true,
                            isLoading = false
                        ) 
                    }
                    if (!savedData.isSubmitted) {
                        startTimer()
                    }
                } catch (e: Exception) {
                    historyRepository.clearMockExam()
                    _hasSavedExam.value = false
                }
            }
        }
    }

    private fun flushSave() {
        val currentState = _state.value
        if (!currentState.isExamStarted) return
        val data = MockExamStateData(
            licenseType = currentState.selectedLicenseType.name,
            questions = currentState.questions,
            selectedAnswers = currentState.selectedAnswers,
            timeRemainingSeconds = _timeRemainingSeconds.value,
            currentIndex = currentState.currentIndex,
            isSubmitted = currentState.isSubmitted,
            score = currentState.score
        )
        if (!currentState.isSubmitted) {
            historyRepository.saveMockExam(Json.encodeToString(data))
            _hasSavedExam.value = true
        }
    }

    private fun scheduleSave() {
        pendingSave?.cancel()
        pendingSave = viewModelScope.launch(Dispatchers.IO) {
            delay(1500)
            flushSave()
        }
    }

    fun startNewExam() {
        timerJob?.cancel()
        historyRepository.clearMockExam()
        _hasSavedExam.value = false
        viewModelScope.launch {
            val licenseType = _state.value.selectedLicenseType
            _timeRemainingSeconds.value = licenseType.timeMinutes * 60
            _state.update { it.copy(isLoading = true, isSubmitted = false, isExamStarted = true, selectedAnswers = emptyMap(), score = 0, currentIndex = 0) }
            val randomQuestions = QuestionRepository.getRandomQuestions(_state.value.selectedLicenseType)
            _state.update { it.copy(questions = randomQuestions, isLoading = false) }
            scheduleSave()
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_timeRemainingSeconds.value > 0 && !_state.value.isSubmitted) {
                delay(1000)
                val newTime = _timeRemainingSeconds.value - 1
                _timeRemainingSeconds.value = newTime
                if (newTime % 30 == 0) {
                    flushSave()
                }
            }
            if (_timeRemainingSeconds.value == 0 && !_state.value.isSubmitted) {
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
        scheduleSave()
    }

    fun selectLicenseType(licenseType: LicenseType) {
        _state.update { it.copy(selectedLicenseType = licenseType) }
    }
    
    fun quitExam() {
        timerJob?.cancel()
        pendingSave?.cancel()
        if (!_state.value.isSubmitted) {
            flushSave()
        }
        _state.update { it.copy(isExamStarted = false) }
        checkSavedExam()
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
        pendingSave?.cancel()
        
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
        
        val currentState = _state.value
        val data = MockExamStateData(
            licenseType = currentState.selectedLicenseType.name,
            questions = currentState.questions,
            selectedAnswers = currentState.selectedAnswers,
            timeRemainingSeconds = _timeRemainingSeconds.value,
            currentIndex = currentState.currentIndex,
            isSubmitted = true,
            score = calculatedScore
        )
        
        val record = MockExamRecord(
            id = Clock.System.now().toEpochMilliseconds().toString(),
            timestamp = Clock.System.now().toEpochMilliseconds(),
            data = data
        )
        
        viewModelScope.launch(Dispatchers.IO) {
            historyRepository.addCompletedMockExam(record)
            historyRepository.clearMockExam() // Clear the in-progress exam
        }
        _hasSavedExam.value = false
    }

    fun loadPastExam(record: MockExamRecord) {
        timerJob?.cancel()
        pendingSave?.cancel()
        val savedData = record.data
        val licenseType = LicenseType.valueOf(savedData.licenseType)
        _timeRemainingSeconds.value = savedData.timeRemainingSeconds
        _state.update { 
            it.copy(
                selectedLicenseType = licenseType,
                questions = savedData.questions,
                selectedAnswers = savedData.selectedAnswers,
                currentIndex = 0,
                isSubmitted = true,
                score = savedData.score,
                isExamStarted = true,
                isLoading = false
            ) 
        }
    }
}
