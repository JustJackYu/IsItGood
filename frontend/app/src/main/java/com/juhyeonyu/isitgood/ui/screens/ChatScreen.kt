package com.juhyeonyu.isitgood.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.delay
import com.juhyeonyu.isitgood.ui.theme.Cerulean
import com.juhyeonyu.isitgood.ui.theme.CoolSteel
import com.juhyeonyu.isitgood.ui.theme.PacificBlue
import com.juhyeonyu.isitgood.ui.theme.Platinum
import com.juhyeonyu.isitgood.ui.viewmodel.ChatState
import com.juhyeonyu.isitgood.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(rawgId: Int, onBack: () -> Unit) {
    val chatViewModel: ChatViewModel = viewModel()
    var message by remember { mutableStateOf("") }
    val messages = chatViewModel.messages
    val chatState by chatViewModel.state.collectAsState()
    val gameTitle by chatViewModel.gameTitle.collectAsState()
    val listState = rememberLazyListState()
    val isLoading = chatState is ChatState.Loading

    // Bubbles cap at ~85% of the screen so they never span edge-to-edge.
    val maxBubbleWidth = (LocalConfiguration.current.screenWidthDp * 0.85f).dp

    LaunchedEffect(Unit) {
        chatViewModel.loadGameContext(rawgId)
    }

    LaunchedEffect(messages.size, isLoading) {
        val total = messages.size + if (isLoading) 1 else 0
        if (total > 0) listState.animateScrollToItem(total - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Platinum)
    ) {
        // Branded header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Cerulean,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to game",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Chat",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    if (gameTitle.isNotBlank()) {
                        Text(
                            text = gameTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PacificBlue
                        )
                    }
                }
            }
        }

        // Chat canvas — the distinct conversation area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { (role, content) ->
                ChatBubble(role = role, content = content, maxWidth = maxBubbleWidth)
            }
            if (isLoading) {
                item { TypingIndicator(maxWidth = maxBubbleWidth) }
            }
        }

        if (chatState is ChatState.Error) {
            Text(
                text = (chatState as ChatState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Input bar — elevated white zone, distinct from the canvas
        Surface(color = Color.White, shadowElevation = 8.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Ask something…") },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            chatViewModel.sendMessage(message)
                            message = ""
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cerulean,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Send", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(role: String, content: String, maxWidth: androidx.compose.ui.unit.Dp) {
    val isUser = role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) Cerulean else Color.White,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            shadowElevation = if (isUser) 0.dp else 1.dp,
            modifier = Modifier.widthIn(max = maxWidth)
        ) {
            if (isUser) {
                Text(
                    text = content,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                MarkdownText(
                    markdown = content,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator(maxWidth: androidx.compose.ui.unit.Dp) {
    var dotCount by remember { mutableIntStateOf(1) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            dotCount = if (dotCount >= 3) 1 else dotCount + 1
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = maxWidth)
        ) {
            Text(
                text = ".".repeat(dotCount),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = CoolSteel,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
