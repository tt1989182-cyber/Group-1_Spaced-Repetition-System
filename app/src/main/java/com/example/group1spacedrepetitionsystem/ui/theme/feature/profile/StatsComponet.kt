package com.example.estudapp.ui.feature.profile

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.estudapp.ui.theme.Black
import com.example.estudapp.ui.theme.ErrorRed
import com.example.estudapp.ui.theme.LightGray
import com.example.estudapp.ui.theme.PrimaryBlue
import com.example.estudapp.ui.theme.White
import kotlin.math.roundToInt

@Composable
fun StatsComponent(
    modifier: Modifier = Modifier,
    statsViewModel: StatsViewModel = viewModel()
) {
    val statsState by statsViewModel.statsState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .border(1.dp, LightGray, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Thống kê học tập",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        when (val state = statsState) {
            is StatsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            is StatsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = ErrorRed,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is StatsUiState.Success -> {
                val dataToShow = state.deckStats.map {
                    StatsItem(it.deckName, it.percentage, it.totalScore, it.totalPossible, it.sessionCount)
                }

                if (dataToShow.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bạn chưa có dữ liệu học tập nào",
                            color = Black.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        dataToShow.take(4).forEach { item ->
                            StatBar(
                                label = item.label,
                                percentage = item.percentage,
                                totalScore = item.totalScore,
                                totalPossible = item.totalPossible,
                                sessionCount = item.sessionCount
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBar(
    label: String,
    percentage: Double,
    totalScore: Double,
    totalPossible: Double,
    sessionCount: Int
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedPercentage by animateFloatAsState(
        targetValue = if (animationPlayed) percentage.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "percentage_animation"
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Black,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${animatedPercentage.toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = getColorForPercentage(animatedPercentage)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LightGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (animatedPercentage > 0) animatedPercentage / 100f else 0.01f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(getColorForPercentage(animatedPercentage))
                    .animateContentSize()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Điểm: ${totalScore.roundToInt()}/${totalPossible.roundToInt()}",
                fontSize = 12.sp,
                color = Black.copy(alpha = 0.6f)
            )
            Text(
                text = "Số phiên học: $sessionCount",
                fontSize = 12.sp,
                color = Black.copy(alpha = 0.6f)
            )
        }
    }
}

fun getColorForPercentage(percentage: Float): Color {
    return when {
        percentage < 50f -> Color(0xFFEF5350) // Đỏ
        percentage < 80f -> Color(0xFFFFA726) // Cam/Vàng
        else -> Color(0xFF66BB6A) // Xanh lá
    }
}

data class StatsItem(
    val label: String,
    val percentage: Double,
    val totalScore: Double,
    val totalPossible: Double,
    val sessionCount: Int
)
