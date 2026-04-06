package com.example.estudapp.ui.feature.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.estudapp.R
import com.example.estudapp.data.model.SimpleChatMessageDTO
import com.example.estudapp.ui.theme.Black
import com.example.estudapp.ui.theme.LightGray

import com.example.estudapp.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    chatViewModel: ChatViewModel = viewModel()
) {
    val messages by chatViewModel.messages.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()
    val errorMessage by chatViewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Ombre brush
    val gradientBrush = Brush.horizontalGradient(listOf(PrimaryBlue, PrimaryGreen))

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Outlined.KeyboardArrowLeft, null, tint = PrimaryBlue, modifier = Modifier.size(35.dp))
                    }
                },
                title = { Text("MonitorIA", color = PrimaryBlue, fontWeight = FontWeight.Black) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(White)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(messages) { msg ->
                    ChatMessageItem(msg)
                }
                if (isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            CircularProgressIndicator(Modifier.size(24.dp), color = PrimaryBlue, strokeWidth = 2.dp)
                        }
                    }
                }
            }

            if (messages.isEmpty() && !isLoading) {
                SuggestionsRow { text -> chatViewModel.sendMessage(context, text) }
            }

            // Input Area with Ombre accent
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
                color = White
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        placeholder = { Text(stringResource(R.string.chat_placeholder)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                chatViewModel.sendMessage(context, textState)
                                textState = ""
                            }
                        },
                        containerColor = Color.Transparent,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(gradientBrush)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: SimpleChatMessageDTO) {
    val isUser = message.sender == "USER"
    val gradientBrush = Brush.horizontalGradient(listOf(PrimaryBlue, PrimaryGreen))
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 0.dp,
                            bottomEnd = if (isUser) 0.dp else 16.dp
                        )
                    )
                    .background(if (isUser) gradientBrush else Brush.linearGradient(listOf(LightGray, LightGray)))
                    .padding(12.dp)
            ) {
                Text(
                    text = message.text ?: "",
                    color = if (isUser) White else Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun SuggestionsRow(onSelect: (String) -> Unit) {
    val suggestions = listOf(
        stringResource(R.string.suggest_1),
        stringResource(R.string.suggest_2),
        stringResource(R.string.suggest_3)
    )
    LazyRow(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suggestions) { text ->
            SuggestionChip(
                onClick = { onSelect(text) },
                label = { Text(text, fontSize = 12.sp) },
                border = BorderStroke(1.dp, PrimaryBlue),
                colors = SuggestionChipDefaults.suggestionChipColors(labelColor = PrimaryBlue)
            )
        }
    }
}
