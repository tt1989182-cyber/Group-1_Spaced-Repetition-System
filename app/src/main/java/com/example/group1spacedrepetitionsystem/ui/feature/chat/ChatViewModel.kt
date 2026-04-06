package com.example.estudapp.ui.feature.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estudapp.R
import com.example.estudapp.data.model.SimpleChatMessageDTO
import com.example.estudapp.domain.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel : ViewModel() {

    private val repository = FlashcardRepository()

    private val _messages = MutableStateFlow<List<SimpleChatMessageDTO>>(emptyList())
    val messages: StateFlow<List<SimpleChatMessageDTO>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            repository.observeUserMessages().collect { result ->
                result.onSuccess { messageList ->
                    // Sắp xếp tin nhắn theo thời gian
                    _messages.value = messageList.sortedBy {
                        when (val ts = it.timestamp) {
                            is Long -> ts
                            is Map<*, *> -> 0L // Trường hợp đang chờ ServerValue.TIMESTAMP
                            else -> 0L
                        }
                    }
                    // Nếu tin nhắn cuối cùng là của trợ lý, tắt loading
                    if (_messages.value.lastOrNull()?.sender == "ASSISTANT") {
                        _isLoading.value = false
                    }
                }.onFailure {
                    _isLoading.value = false
                }
            }
        }
    }

    fun sendMessage(context: Context, text: String) {
        if (text.isBlank() || _isLoading.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // 1. Gửi tin nhắn của người dùng lên Firebase
                val result = repository.sendDirectMessage(text, "USER")
                
                result.onSuccess {
                    // 2. Giả lập phản hồi từ AI ngay lập tức (Offline Mode)
                    simulateAIResponse(text)
                }.onFailure {
                    _isLoading.value = false
                    _errorMessage.value = "Không thể gửi tin nhắn. Vui lòng kiểm tra kết nối."
                }

            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Lỗi hệ thống: ${e.message}"
            }
        }
    }

    private suspend fun simulateAIResponse(userText: String) {
        // Giả lập thời gian suy nghĩ (1-2 giây)
        delay(1500)

        val responseText = when {
            userText.contains("Chào", ignoreCase = true) || userText.contains("Hi", ignoreCase = true) -> 
                "Xin chào! Tôi là MonitorIA. Tôi có thể giúp gì cho việc học của bạn hôm nay?"
            
            userText.contains("Flashcard", ignoreCase = true) -> 
                "Flashcard là phương pháp học rất hiệu quả. Bạn nên ôn tập chúng theo chu kỳ 1, 3, 7 ngày để nhớ lâu nhất."
            
            userText.contains("mẹo", ignoreCase = true) || userText.contains("bí quyết", ignoreCase = true) -> 
                "Mẹo học nhanh: Hãy chia nhỏ kiến thức, đừng học quá nhiều cùng lúc. Mỗi thẻ chỉ nên chứa một câu hỏi duy nhất."
            
            userText.contains("cảm ơn", ignoreCase = true) -> 
                "Không có gì! Chúc bạn học tập thật tốt nhé. Cần gì cứ hỏi tôi."
            
            else -> "Tôi đã nhận được câu hỏi của bạn. Đây là chế độ trả lời tự động của MonitorIA để đảm bảo hệ thống luôn hoạt động ổn định."
        }

        // Gửi phản hồi giả lập vào Firebase
        repository.sendDirectMessage(responseText, "ASSISTANT")
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
