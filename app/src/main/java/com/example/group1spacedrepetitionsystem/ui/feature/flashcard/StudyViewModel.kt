package com.example.estudapp.ui.feature.flashcard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estudapp.data.model.AlternativaDTO
import com.example.estudapp.data.model.FlashcardDTO
import com.example.estudapp.data.model.FlashcardTypeEnum
import com.example.estudapp.data.model.SimpleChatMessageDTO
import com.example.estudapp.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class StudyViewModel : ViewModel() {

    private val repository = FlashcardRepository()
    private val flashcardViewModel = FlashcardViewModel()

    private val _uiState = MutableStateFlow<StudyUiState>(StudyUiState.Loading)
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private var allCardsInDeck: List<FlashcardDTO> = emptyList()
    private var currentCardIndex = 0

    private val _messages = MutableStateFlow<List<SimpleChatMessageDTO>>(emptyList())
    val messages: StateFlow<List<SimpleChatMessageDTO>> = _messages.asStateFlow()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            repository.observeUserMessages().collect { result ->
                result.onSuccess { messageList ->
                    _messages.value = messageList
                }.onFailure {
                    Log.e("Chat", "Lỗi quan sát tin nhắn: ${it.message}")
                }
            }
        }
    }

    fun startStudySession(deckId: String) {
        viewModelScope.launch {
            _uiState.value = StudyUiState.Loading
            val result = repository.getFlashcardsOnce(deckId)

            result.onSuccess { flashcards ->
                // Cộng thêm 1 phút để đảm bảo các thẻ vừa tạo (nextReview = now) được bao gồm
                val todayWithBuffer = System.currentTimeMillis() + 60000 
                
                // Lọc thẻ đến hạn hoặc thẻ mới chưa bao giờ ôn tập
                allCardsInDeck = flashcards.filter { 
                    it.reviewCount == 0 || it.nextReview <= todayWithBuffer 
                }.shuffled()
                
                currentCardIndex = 0
                if (allCardsInDeck.isNotEmpty()) {
                    flashcardViewModel.startDeckSession(deckId)
                    _uiState.value = StudyUiState.Studying(card = allCardsInDeck[currentCardIndex], isShowingAnswer = false)
                } else {
                    _uiState.value = StudyUiState.EmptyDeck
                }
            }.onFailure {
                _uiState.value = StudyUiState.Error(it.message ?: "Lỗi tải thẻ.")
            }
        }
    }

    fun checkAnswer(
        userAnswer: String,
        clozeAnswers: Map<String, String> = emptyMap(),
        multipleChoiceAnswer: AlternativaDTO? = null
    ) {
        val currentState = _uiState.value
        if (currentState is StudyUiState.Studying && !_isProcessing.value) {
            _isProcessing.value = true
            viewModelScope.launch {
                try {
                    val card = currentState.card
                    var isCorrect = false

                    when (card.type) {
                        FlashcardTypeEnum.MULTIPLA_ESCOLHA.name -> {
                            // So sánh ID hoặc text nếu ID null
                            isCorrect = card.respostaCorreta?.text == multipleChoiceAnswer?.text
                        }
                        FlashcardTypeEnum.CLOZE.name -> {
                            // Ở phiên bản đơn giản, chúng ta chỉ hiện đáp án cho Cloze
                            isCorrect = true 
                        }
                        FlashcardTypeEnum.DIGITE_RESPOSTA.name -> {
                            isCorrect = card.respostasValidas?.any { it.equals(userAnswer, ignoreCase = true) } == true
                        }
                        else -> isCorrect = true
                    }

                    _uiState.value = currentState.copy(
                        isShowingAnswer = true,
                        wasCorrect = isCorrect
                    )
                } finally {
                    _isProcessing.value = false
                }
            }
        }
    }

    fun showAnswer() {
        val currentState = _uiState.value
        if (currentState is StudyUiState.Studying) {
            _uiState.value = currentState.copy(isShowingAnswer = true)
        }
    }

    fun onDifficultySelected(difficulty: Int) {
        val currentState = _uiState.value
        if (currentState is StudyUiState.Studying) {
            val card = currentState.card
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, difficulty)

            val updatedCard = card.copy(
                nextReview = calendar.timeInMillis,
                reviewCount = card.reviewCount + 1
            )

            viewModelScope.launch {
                repository.updateFlashcard(card.deckId, updatedCard)
                nextCard()
            }
        }
    }

    private fun nextCard() {
        currentCardIndex++
        if (currentCardIndex < allCardsInDeck.size) {
            _uiState.value = StudyUiState.Studying(
                card = allCardsInDeck[currentCardIndex],
                isShowingAnswer = false
            )
        } else {
            flashcardViewModel.finishAndSaveDeckSession()
            _uiState.value = StudyUiState.SessionFinished
        }
    }
}

sealed class StudyUiState {
    object Loading : StudyUiState()
    data class Studying(
        val card: FlashcardDTO,
        val isShowingAnswer: Boolean,
        val wasCorrect: Boolean? = null
    ) : StudyUiState()
    object EmptyDeck : StudyUiState()
    object SessionFinished : StudyUiState()
    data class Error(val message: String) : StudyUiState()
}