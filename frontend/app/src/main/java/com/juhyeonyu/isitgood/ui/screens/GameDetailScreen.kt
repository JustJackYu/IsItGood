package com.juhyeonyu.isitgood.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juhyeonyu.isitgood.ui.viewmodel.GameDetailViewModel
import com.juhyeonyu.isitgood.ui.viewmodel.GameState
import com.juhyeonyu.isitgood.ui.viewmodel.PricesState
import com.juhyeonyu.isitgood.ui.viewmodel.SaveState
import com.juhyeonyu.isitgood.ui.viewmodel.SummaryState

@Composable
fun GameDetailScreen(
    rawgId: Int,
    title: String,
    onChatClick: (rawgId: Int) -> Unit
) {
    val viewModel: GameDetailViewModel = viewModel()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val summaryState by viewModel.summaryState.collectAsState()
    val pricesState by viewModel.pricesState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val gameState by viewModel.gameState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadGame(rawgId)
        viewModel.loadSummary(rawgId, title)
        viewModel.loadPrices(rawgId, title)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Text("AI Summary", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        when (val state = summaryState) {
            is SummaryState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is SummaryState.Success -> {
                Text(state.summary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sources", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                state.sources.forEach { url ->
                    Text(
                        text = url,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                            .padding(vertical = 2.dp)
                    )
                }
            }
            is SummaryState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is SummaryState.Idle -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Prices", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        when (val state = pricesState) {
            is PricesState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is PricesState.Success -> {
                if (state.prices.isEmpty()) {
                    Text("No prices found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    state.prices.forEach { deal ->
                        val price = "CA$%.2f".format(deal.price)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deal.url))
                                    context.startActivity(intent)
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(deal.store, style = MaterialTheme.typography.bodyMedium)
                            Text(price, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            is PricesState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is PricesState.Idle -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))

        val game = (gameState as? GameState.Success)?.game
        Button(
            onClick = {
                viewModel.saveGame(
                    id = rawgId,
                    name = title,
                    coverImage = game?.backgroundImage,
                    rating = game?.rating,
                    released = game?.released
                )
            },
            enabled = saveState !is SaveState.Loading && saveState !is SaveState.Success,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when (saveState) {
                    is SaveState.Loading -> "Saving..."
                    is SaveState.Success -> "Saved ✓"
                    is SaveState.Error -> "Save Failed — Retry"
                    else -> "Save Game"
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (summaryState is SummaryState.Success) {
            OutlinedButton(
                onClick = { onChatClick(rawgId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chat about this game")
            }
        }
    }
}