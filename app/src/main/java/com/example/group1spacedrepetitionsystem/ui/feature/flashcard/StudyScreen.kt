package com.example.estudapp.ui.feature.flashcard

import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.estudapp.R
import com.example.estudapp.data.model.AlternativaDTO
import com.example.estudapp.data.model.FlashcardDTO
import com.example.estudapp.data.model.FlashcardTypeEnum
import com.example.estudapp.ui.theme.LightGray
import com.example.estudapp.ui.theme.PrimaryBlue
import com.example.estudapp.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    navController: NavHostController,
    studyViewModel: StudyViewModel = viewModel(),
    deckId: String,
    deckName: String
) {
    LaunchedEffect(deckId) {
        studyViewModel.startStudySession(deckId)
    }

    val uiState by studyViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(Icons.Outlined.KeyboardArrowLeft, "quay lại", tint = PrimaryBlue, modifier = Modifier.size(35.dp))
                    }
                },
                title = { Text(stringResource(id = R.string.study_title), color = PrimaryBlue, fontWeight = FontWeight.Black) },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(deckName ?: "Bộ thẻ", fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Start))

            when (val state = uiState) {
                is StudyUiState.Loading -> CircularProgressIndicator()
                is StudyUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Warning, contentDescription = null, tint = PrimaryBlue)
                        Text(state.message, color = PrimaryBlue, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
                is StudyUiState.EmptyDeck -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(10.dp))
                        Text(stringResource(id = R.string.empty_deck_message), color = PrimaryBlue, textAlign = TextAlign.Center)
                    }
                }
                is StudyUiState.SessionFinished -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎉", fontSize = 40.sp)
                        Text(stringResource(id = R.string.session_finished_title), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(id = R.string.session_finished_msg))
                    }
                    Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                        Text(stringResource(id = R.string.back_to_list))
                    }
                }
                is StudyUiState.Studying -> {
                    StudyCardContent(
                        state = state,
                        onCheckAnswer = { studyViewModel.checkAnswer(it) },
                        onCheckCloze = { studyViewModel.checkAnswer("", it) },
                        onCheckMulti = { studyViewModel.checkAnswer("", emptyMap(), it) },
                        onShowAnswer = { studyViewModel.showAnswer() },
                        onDifficultySelected = { studyViewModel.onDifficultySelected(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ColumnScope.StudyCardContent(
    state: StudyUiState.Studying,
    onCheckAnswer: (String) -> Unit,
    onCheckCloze: (Map<String, String>) -> Unit,
    onCheckMulti: (AlternativaDTO) -> Unit,
    onShowAnswer: () -> Unit,
    onDifficultySelected: (Int) -> Unit
) {
    val card = state.card
    val context = LocalContext.current
    var inputAnswer by remember(card.id) { mutableStateOf("") }

    // Khởi tạo TTS
    val tts = remember {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
            }
        }
        textToSpeech
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LightGray, RoundedCornerShape(20.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val questionText = when (card.type) {
                    FlashcardTypeEnum.FRENTE_VERSO.name -> card.frente ?: ""
                    FlashcardTypeEnum.CLOZE.name -> {
                        val text = card.textoComLacunas ?: ""
                        if (state.isShowingAnswer) {
                            text.replace(Regex("\\{\\{c\\d+::(.*?)\\}\\}"), "$1")
                        } else {
                            text.replace(Regex("\\{\\{c\\d+::.*?\\}\\}"), "[...]")
                        }
                    }
                    FlashcardTypeEnum.DIGITE_RESPOSTA.name -> card.pergunta ?: ""
                    FlashcardTypeEnum.MULTIPLA_ESCOLHA.name -> card.pergunta ?: ""
                    else -> ""
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(questionText, fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f, fill = false))
                    IconButton(onClick = {
                        tts?.speak(questionText, TextToSpeech.QUEUE_FLUSH, null, null)
                    }) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Phát âm", tint = PrimaryBlue)
                    }
                }

                if (state.isShowingAnswer && card.type == FlashcardTypeEnum.FRENTE_VERSO.name) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(card.verso ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (!state.isShowingAnswer) {
            when (card.type) {
                FlashcardTypeEnum.DIGITE_RESPOSTA.name -> {
                    OutlinedTextField(
                        value = inputAnswer,
                        onValueChange = { inputAnswer = it },
                        label = { Text("Nhập câu trả lời của bạn") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { onCheckAnswer(inputAnswer) }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                        Text("Kiểm tra")
                    }
                }
                FlashcardTypeEnum.MULTIPLA_ESCOLHA.name -> {
                    card.alternativas?.forEach { alt ->
                        Button(
                            onClick = { onCheckMulti(alt) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = White),
                            border = BorderStroke(1.dp, PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(alt.text ?: "", color = PrimaryBlue)
                        }
                    }
                }
                else -> {
                    Button(onClick = { onShowAnswer() }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                        Text("Hiện đáp án")
                    }
                }
            }
        } else {
            if (card.type == FlashcardTypeEnum.DIGITE_RESPOSTA.name || card.type == FlashcardTypeEnum.MULTIPLA_ESCOLHA.name) {
                val isCorrect = state.wasCorrect ?: false
                Text(
                    text = if (isCorrect) "Chính xác! 🎉" else "Chưa đúng rồi. Đáp án là: ${if (card.type == FlashcardTypeEnum.DIGITE_RESPOSTA.name) card.respostasValidas?.firstOrNull() else card.respostaCorreta?.text}",
                    color = if (isCorrect) Color(0xFF4CAF50) else Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Text("Bạn thấy thẻ này thế nào?", color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                DifficultyBtn("Khó", 1, Color(0xFFFF5252), onDifficultySelected)
                DifficultyBtn("Trung bình", 3, Color(0xFFFFB74D), onDifficultySelected)
                DifficultyBtn("Dễ", 5, Color(0xFF81C784), onDifficultySelected)
            }
        }
    }
}

@Composable
fun DifficultyBtn(label: String, days: Int, color: Color, onClick: (Int) -> Unit) {
    Button(
        onClick = { onClick(days) },
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier.width(90.dp).height(50.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("+$days ngày", fontSize = 9.sp)
        }
    }
}
