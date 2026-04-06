package com.example.estudapp.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estudapp.data.model.DeckPlayStatDTO
import com.example.estudapp.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class DeckStatistics(
    val deckId: String,
    val deckName: String,
    val totalScore: Double,
    val totalPossible: Double,
    val percentage: Double,
    val sessionCount: Int
)

sealed class StatsUiState {
    object Loading : StatsUiState()
    data class Success(
        val deckStats: List<DeckStatistics>
    ) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}

class StatsViewModel : ViewModel() {

    private val repository = FlashcardRepository()

    private val _statsState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val statsState: StateFlow<StatsUiState> = _statsState.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _statsState.value = StatsUiState.Loading

            try {
                // Busca todas as estatísticas do usuário
                val allStatsResult = repository.getAllUserStats()
                if (allStatsResult.isFailure) {
                    _statsState.value = StatsUiState.Error("Lỗi khi tải thống kê")
                    return@launch
                }

                val allStats = allStatsResult.getOrNull() ?: emptyList()

                // Processa estatísticas por deck
                val deckStatsMap = mutableMapOf<String, MutableList<DeckPlayStatDTO>>()
                allStats.forEach { stat ->
                    val deckId = stat.deckId ?: return@forEach
                    deckStatsMap.getOrPut(deckId) { mutableListOf() }.add(stat)
                }

                val deckStatistics = deckStatsMap.map { (deckId, sessions) ->
                    val totalScore = sessions.sumOf { it.totalScore ?: 0.0 }
                    val totalPossible = sessions.sumOf { it.totalPossible ?: 0.0 }
                    val percentage = if (totalPossible > 0) (totalScore / totalPossible * 100) else 0.0

                    // Busca nome do deck
                    val deckInfo = repository.getDeckInfo(deckId).getOrNull()
                    val deckName = deckInfo?.name ?: "Bộ thẻ $deckId"

                    DeckStatistics(
                        deckId = deckId,
                        deckName = deckName,
                        totalScore = totalScore,
                        totalPossible = totalPossible,
                        percentage = percentage.roundToInt().toDouble(),
                        sessionCount = sessions.size
                    )
                }.sortedByDescending { it.percentage }

                _statsState.value = StatsUiState.Success(
                    deckStats = deckStatistics
                )

            } catch (e: Exception) {
                _statsState.value = StatsUiState.Error("Lỗi khi xử lý thống kê: ${e.message}")
            }
        }
    }
}
