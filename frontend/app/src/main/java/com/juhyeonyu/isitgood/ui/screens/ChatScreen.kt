package com.juhyeonyu.isitgood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juhyeonyu.isitgood.ui.viewmodel.ChatState
import com.juhyeonyu.isitgood.ui.viewmodel.ChatViewModel
import com.juhyeonyu.isitgood.ui.viewmodel.SharedViewModel

@Composable
fun ChatScreen(
    sharedViewModel: SharedViewModel
) {
    val chatViewModel: ChatViewModel = viewModel()

    var message by remember { mutableStateOf("") }
    val messages = chatViewModel.messages

    val chatState by chatViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        chatViewModel.gameTitle = sharedViewModel.gameTitle
        chatViewModel.summary = sharedViewModel.summary
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            "Chat — ${sharedViewModel.gameTitle}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { (role, content) ->
                val isUser = role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (isUser)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            content,
                            modifier = Modifier.padding(12.dp),
                            color = if (isUser)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
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
                enabled = chatState !is ChatState.Loading
            ) {
                Text("Send")
            }
        }
        when (val s = chatState) {
            is ChatState.Loading -> {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is ChatState.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }
            is ChatState.Idle -> {}
        }
    }
}