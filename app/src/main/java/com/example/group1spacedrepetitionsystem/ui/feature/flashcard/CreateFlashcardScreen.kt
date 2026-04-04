package com.example.estudapp.ui.feature.flashcard

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.estudapp.R
import com.example.estudapp.data.model.AlternativaDTO
import com.example.estudapp.data.model.FlashcardDTO
import com.example.estudapp.data.model.FlashcardTypeEnum
import com.example.estudapp.ui.theme.LightGray
import com.example.estudapp.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFlashcardScreen(
    navController: NavHostController,
    flashcardViewModel: FlashcardViewModel = viewModel(),
    deckId: String,
    flashcardId: String?
) {
    val isEditMode = flashcardId != null
    val saveStatus by flashcardViewModel.saveStatus.collectAsState()
    val cardToEdit by flashcardViewModel.cardToEdit.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(FlashcardTypeEnum.FRENTE_VERSO) }

    LaunchedEffect(flashcardId) {
        if (isEditMode) {
            flashcardViewModel.loadFlashcardForEditing(deckId, flashcardId!!)
        } else {
            flashcardViewModel.clearCardToEdit()
        }
    }

    LaunchedEffect(cardToEdit) {
        val current = cardToEdit
        if (isEditMode && current != null) {
            val typeEnum = runCatching { FlashcardTypeEnum.valueOf(current.type) }
                .getOrElse { FlashcardTypeEnum.FRENTE_VERSO }
            selectedTab = typeEnum
        }
    }

    LaunchedEffect(saveStatus) {
        when (val status = saveStatus) {
            is SaveStatus.Success -> {
                Toast.makeText(context, status.message, Toast.LENGTH_SHORT).show()
                flashcardViewModel.resetSaveStatus()
                navController.popBackStack()
            }
            is SaveStatus.Error -> {
                Toast.makeText(context, status.message, Toast.LENGTH_SHORT).show()
                flashcardViewModel.resetSaveStatus()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Outlined.KeyboardArrowLeft, "quay lại", tint = PrimaryBlue, modifier = Modifier.size(35.dp))
                    }
                },
                title = { Text(if (isEditMode) "Sửa thẻ" else "Tạo thẻ mới", color = PrimaryBlue, fontWeight = FontWeight.Black) },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                FlashcardTypeEnum.values().forEach { tabType ->
                    Tab(
                        selected = selectedTab == tabType,
                        enabled = !isEditMode,
                        onClick = { selectedTab = tabType },
                        text = {
                            val label = when(tabType) {
                                FlashcardTypeEnum.FRENTE_VERSO -> "Cơ bản"
                                FlashcardTypeEnum.CLOZE -> "Điền từ"
                                FlashcardTypeEnum.DIGITE_RESPOSTA -> "Nhập liệu"
                                FlashcardTypeEnum.MULTIPLA_ESCOLHA -> "Trắc nghiệm"
                            }
                            Text(label, fontSize = 12.sp)
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isEditMode && cardToEdit == null) {
                    CircularProgressIndicator()
                } else {
                    when (selectedTab) {
                        FlashcardTypeEnum.FRENTE_VERSO -> FormFrenteVerso(navController, deckId, flashcardViewModel, saveStatus, isEditMode, cardToEdit)
                        FlashcardTypeEnum.CLOZE -> FormCloze(navController, deckId, flashcardViewModel, saveStatus, isEditMode, cardToEdit)
                        FlashcardTypeEnum.DIGITE_RESPOSTA -> FormDigiteResposta(navController, deckId, flashcardViewModel, saveStatus, isEditMode, cardToEdit)
                        FlashcardTypeEnum.MULTIPLA_ESCOLHA -> FormMultiplaEscolha(navController, deckId, flashcardViewModel, saveStatus, isEditMode, cardToEdit)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormFrenteVerso(navController: NavHostController, deckId: String, viewModel: FlashcardViewModel, saveStatus: SaveStatus, isEditMode: Boolean, cardToEdit: FlashcardDTO?) {
    var frente by remember { mutableStateOf("") }
    var verso by remember { mutableStateOf("") }
    var imagemUri by remember { mutableStateOf<Uri?>(null) }
    var audioUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(cardToEdit) {
        if (isEditMode && cardToEdit != null) {
            frente = cardToEdit.frente.orElseEmpty()
            verso = cardToEdit.verso.orElseEmpty()
        }
    }

    Text("Mặt trước (Câu hỏi)", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), color = PrimaryBlue)
    OutlinedTextField(value = frente, onValueChange = { frente = it }, placeholder = { Text("Nhập câu hỏi") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(30f))
    
    Spacer(Modifier.height(16.dp))
    
    Text("Mặt sau (Câu trả lời)", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), color = PrimaryBlue)
    OutlinedTextField(value = verso, onValueChange = { verso = it }, placeholder = { Text("Nhập câu trả lời") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(30f))

    Spacer(Modifier.height(50.dp))
    SaveButton(isEditMode, saveStatus !is SaveStatus.Loading, saveStatus is SaveStatus.Loading) {
        if (isEditMode && cardToEdit != null) viewModel.updateFrenteVerso(deckId, cardToEdit, frente, verso)
        else viewModel.saveFrenteVerso(deckId, frente, verso, imagemUri, audioUri)
    }
    Spacer(Modifier.height(16.dp))
    GenerateCardButton(navController, deckId)
}

@Composable
private fun FormCloze(navController: NavHostController, deckId: String, viewModel: FlashcardViewModel, saveStatus: SaveStatus, isEditMode: Boolean, cardToEdit: FlashcardDTO?) {
    var texto by remember { mutableStateOf("") }
    LaunchedEffect(cardToEdit) {
        if (isEditMode && cardToEdit != null) texto = cardToEdit.textoComLacunas.orElseEmpty()
    }
    OutlinedTextField(value = texto, onValueChange = { texto = it }, label = { Text("Văn bản có ô trống") }, placeholder = { Text("VD: Thủ đô Việt Nam là {{c1::Hà Nội}}.") }, shape = RoundedCornerShape(30f), modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(20.dp))
    Text("Mẹo: 'c1' là ô trống 1. Dùng {{c1::nội dung}} để tạo ô trống.", color = Color.Gray, fontStyle = FontStyle.Italic, fontSize = 12.sp)
    Spacer(Modifier.height(60.dp))
    SaveButton(isEditMode, texto.isNotBlank() && saveStatus !is SaveStatus.Loading, saveStatus is SaveStatus.Loading) {
        val regex = Regex("\\{\\{(c\\d+)::(.*?)\\}\\}")
        val respostasMap = regex.findAll(texto).associate { it.groupValues[1] to it.groupValues[2] }
        if (isEditMode && cardToEdit != null) viewModel.updateCloze(deckId, cardToEdit, texto, respostasMap)
        else viewModel.saveCloze(deckId, texto, respostasMap)
    }
    Spacer(Modifier.height(16.dp))
    GenerateCardButton(navController, deckId)
}

@Composable
private fun FormDigiteResposta(navController: NavHostController, deckId: String, viewModel: FlashcardViewModel, saveStatus: SaveStatus, isEditMode: Boolean, cardToEdit: FlashcardDTO?) {
    var pergunta by remember { mutableStateOf("") }
    var respuestas by remember { mutableStateOf("") }
    LaunchedEffect(cardToEdit) {
        if (isEditMode && cardToEdit != null) {
            pergunta = cardToEdit.pergunta.orElseEmpty()
            respuestas = cardToEdit.respostasValidas?.joinToString(", ") ?: ""
        }
    }
    Text("Câu hỏi", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth())
    OutlinedTextField(value = pergunta, onValueChange = { pergunta = it }, placeholder = { Text("VD: Ai là người phát minh ra bóng đèn?") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(30f))
    Spacer(Modifier.height(16.dp))
    Text("Đáp án đúng", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth())
    OutlinedTextField(value = respuestas, onValueChange = { respuestas = it }, placeholder = { Text("Cách nhau bằng dấu phẩy") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(30f))
    Spacer(Modifier.height(60.dp))
    SaveButton(isEditMode, pergunta.isNotBlank() && respuestas.isNotBlank() && saveStatus !is SaveStatus.Loading, saveStatus is SaveStatus.Loading) {
        val respuestasList = respuestas.split(',').map { it.trim() }.filter { it.isNotBlank() }
        if (isEditMode && cardToEdit != null) viewModel.updateDigiteResposta(deckId, cardToEdit, pergunta, respuestasList)
        else viewModel.saveDigiteResposta(deckId, pergunta, respuestasList, null, null)
    }
    Spacer(Modifier.height(16.dp))
    GenerateCardButton(navController, deckId)
}

@Composable
private fun FormMultiplaEscolha(navController: NavHostController, deckId: String, viewModel: FlashcardViewModel, saveStatus: SaveStatus, isEditMode: Boolean, cardToEdit: FlashcardDTO?) {
    var pergunta by remember { mutableStateOf("") }
    var alt1Texto by remember { mutableStateOf("") }
    var alt2Texto by remember { mutableStateOf("") }
    var alt3Texto by remember { mutableStateOf("") }
    var alt4Texto by remember { mutableStateOf("") }
    var respuestaCorretaIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(cardToEdit) {
        if (isEditMode && cardToEdit != null) {
            pergunta = cardToEdit.pergunta.orElseEmpty()
            val alts = cardToEdit.alternativas ?: emptyList()
            alt1Texto = alts.getOrNull(0)?.text.orElseEmpty()
            alt2Texto = alts.getOrNull(1)?.text.orElseEmpty()
            alt3Texto = alts.getOrNull(2)?.text.orElseEmpty()
            alt4Texto = alts.getOrNull(3)?.text.orElseEmpty()
            val correta = cardToEdit.respostaCorreta
            respuestaCorretaIndex = alts.indexOfFirst { it == correta }.takeIf { it >= 0 } ?: 0
        }
    }
    Text("Câu hỏi trắc nghiệm", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth())
    OutlinedTextField(value = pergunta, onValueChange = { pergunta = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(30f))
    
    Spacer(Modifier.height(16.dp))
    Text("Các phương án chọn", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth())
    
    @Composable
    fun AltIn(v: String, onChange: (String) -> Unit, label: String, idx: Int) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = respuestaCorretaIndex == idx, onClick = { respuestaCorretaIndex = idx })
            OutlinedTextField(value = v, onValueChange = onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(30f))
        }
        Spacer(Modifier.height(8.dp))
    }
    
    AltIn(alt1Texto, { alt1Texto = it }, "Phương án 1", 0)
    AltIn(alt2Texto, { alt2Texto = it }, "Phương án 2", 1)
    AltIn(alt3Texto, { alt3Texto = it }, "Phương án 3", 2)
    AltIn(alt4Texto, { alt4Texto = it }, "Phương án 4", 3)

    Spacer(Modifier.height(40.dp))
    SaveButton(isEditMode, saveStatus !is SaveStatus.Loading, saveStatus is SaveStatus.Loading) {
        val alternativas = listOf(AlternativaDTO(alt1Texto), AlternativaDTO(alt2Texto), AlternativaDTO(alt3Texto), AlternativaDTO(alt4Texto))
        if (isEditMode && cardToEdit != null) viewModel.updateMultiplaEscolha(deckId, cardToEdit, pergunta, alternativas, respuestaCorretaIndex)
        else viewModel.saveMultiplaEscolha(deckId, pergunta, null, null, listOf(alt1Texto to null, alt2Texto to null, alt3Texto to null, alt4Texto to null), respuestaCorretaIndex)
    }
    Spacer(Modifier.height(16.dp))
    GenerateCardButton(navController, deckId)
}

@Composable
private fun SaveButton(isEditMode: Boolean, enabled: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(60.dp), enabled = enabled, shape = RoundedCornerShape(30f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF454F63))) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
        else Text(if (isEditMode) "Cập nhật" else "Lưu lại", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
    }
}

@Composable
private fun GenerateCardButton(navController: NavHostController, deckId: String) {
    Button(onClick = { navController.navigate("generate_flashcard/${deckId}?flashcardId=") }, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(30f), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tạo thẻ với MonitorIA", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Spacer(Modifier.width(8.dp))
            Icon(painter = painterResource(id = R.drawable.icon_generate), contentDescription = null, tint = Color.White)
        }
    }
}

private fun String?.orElseEmpty(): String = this ?: ""