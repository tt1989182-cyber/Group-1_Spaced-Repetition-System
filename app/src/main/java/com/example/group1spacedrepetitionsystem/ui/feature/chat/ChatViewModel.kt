package com.example.estudapp.ui.feature.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estudapp.R
import com.example.estudapp.data.model.SimpleChatMessageDTO
import com.example.estudapp.domain.repository.FlashcardRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ChatViewModel : ViewModel() {

    private val repository = FlashcardRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<SimpleChatMessageDTO>>(emptyList())
    val messages: StateFlow<List<SimpleChatMessageDTO>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var awaitingAIResponse = false
    private var lastUserMessageCount = 0

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            repository.observeUserMessages().collect { result ->
                result.onSuccess { messageList ->
                    val sortedMessages = messageList.sortedBy {
                        (it.timestamp as? Long) ?: 0L
                    }

                    if (awaitingAIResponse && sortedMessages.size > lastUserMessageCount) {
                        val lastMessage = sortedMessages.lastOrNull()
                        if (lastMessage?.sender == "ASSISTANT") {
                            _isLoading.value = false
                            awaitingAIResponse = false
                        }
                    }

                    _messages.value = sortedMessages
                }.onFailure { error ->
                    _isLoading.value = false
                    awaitingAIResponse = false
                }
            }
        }
    }

    fun sendMessage(context: Context, text: String) {
        if (_isLoading.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                awaitingAIResponse = true
                lastUserMessageCount = _messages.value.size + 1

                val result = repository.sendDirectMessage(text)
                result.onSuccess {
                    callChatAPI(context)
                }.onFailure { error ->
                    _isLoading.value = false
                    awaitingAIResponse = false
                    _errorMessage.value = context.getString(R.string.error_ai_process)
                }

            } catch (e: Exception) {
                _isLoading.value = false
                awaitingAIResponse = false
                _errorMessage.value = context.getString(R.string.error_ai_process)
            }
        }
    }

    private suspend fun callChatAPI(context: Context) {
        withContext(Dispatchers.IO) {
            val user = auth.currentUser
            if (user == null) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = context.getString(R.string.error_no_auth)
                    _isLoading.value = false
                    awaitingAIResponse = false
                }
                return@withContext
            }

            try {
                val tokenTask = user.getIdToken(true)
                val token = com.google.android.gms.tasks.Tasks.await(tokenTask).token

                if (token == null) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = context.getString(R.string.error_no_auth)
                        _isLoading.value = false
                        awaitingAIResponse = false
                    }
                    return@withContext
                }

                val url = URL("https://estudapp-api-293741035243.southamerica-east1.run.app/chat/respond")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true
                connection.connectTimeout = 30000
                connection.readTimeout = 60000

                val outputStreamWriter = OutputStreamWriter(connection.outputStream)
                outputStreamWriter.write("{}")
                outputStreamWriter.flush()
                outputStreamWriter.close()

                val responseCode = connection.responseCode
                Log.d("ChatViewModel", "API Response Code: $responseCode")

                // CHỈNH SỬA: Chấp nhận cả 200 (OK) và 202 (Accepted)
                val isSuccess = responseCode == HttpURLConnection.HTTP_OK ||
                                responseCode == HttpURLConnection.HTTP_ACCEPTED

                if (!isSuccess) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = context.getString(R.string.error_server, responseCode)
                        _isLoading.value = false
                        awaitingAIResponse = false
                    }
                } else {
                    Log.d("ChatViewModel", "Yêu cầu đã được gửi thành công, đang chờ AI trả lời qua Firebase...")
                }

                connection.disconnect()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = context.getString(R.string.error_ai_process)
                    _isLoading.value = false
                    awaitingAIResponse = false
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}