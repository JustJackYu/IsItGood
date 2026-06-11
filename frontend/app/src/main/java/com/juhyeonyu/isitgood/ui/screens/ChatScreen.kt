package com.juhyeonyu.isitgood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.delay
import com.juhyeonyu.isitgood.ui.viewmodel.ChatState
import com.juhyeonyu.isitgood.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(rawgId: Int) {
    val chatViewModel: ChatViewModel = viewModel()
    var message by remember { mutableStateOf("") }
    val messages = chatViewModel.messages
    val chatState by chatViewModel.state.collectAsState()
    val gameTitle by chatViewModel.gameTitle.collectAsState()
    val listState = rememberLazyListState()
    val isLoading = chatState is ChatState.Loading

    LaunchedEffect(Unit) {
        chatViewModel.loadGameContext(rawgId)
    }

    LaunchedEffect(messages.size, isLoading) {
        val total = messages.size + if (isLoading) 1 else 0
        if (total > 0) listState.animateScrollToItem(total - 1)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = "Chat — $gameTitle",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(messages) { (role, content) ->
                ChatBubble(role = role, content = content)
            }
            if (isLoading) {
                item { TypingIndicator() }
            }
        }

        if (chatState is ChatState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (chatState as ChatState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Ask something...") },
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
                enabled = !isLoading
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
private fun ChatBubble(role: String, content: String) {
    val isUser = role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            if (isUser) {
                Text(
                    text = content,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                MarkdownText(
                    markdown = content,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
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
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = ".".repeat(dotCount),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}