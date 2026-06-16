package com.juhyeonyu.isitgood.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.delay
import com.juhyeonyu.isitgood.data.model.Deal
import com.juhyeonyu.isitgood.ui.theme.Cerulean
import com.juhyeonyu.isitgood.ui.theme.CoolSteel
import com.juhyeonyu.isitgood.ui.theme.NegativeRed
import com.juhyeonyu.isitgood.ui.theme.PacificBlue
import com.juhyeonyu.isitgood.ui.theme.Platinum
import com.juhyeonyu.isitgood.ui.theme.PositiveGreen
import com.juhyeonyu.isitgood.ui.viewmodel.GameDetailViewModel
import com.juhyeonyu.isitgood.ui.viewmodel.GameState
import com.juhyeonyu.isitgood.ui.viewmodel.PricesState
import com.juhyeonyu.isitgood.ui.viewmodel.SaveState
import com.juhyeonyu.isitgood.ui.viewmodel.SummaryState

private const val DEAL_FOLD_THRESHOLD = 7
private const val DEAL_PREVIEW_COUNT = 5

// Long-form text uses the same sans-serif family as the rest of the app, just a touch larger.
private val ReadingFont = FontFamily.SansSerif

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
    val game = (gameState as? GameState.Success)?.game

    var dealsExpanded by remember { mutableStateOf(false) }
    var summaryExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadGame(rawgId)
        viewModel.checkIfSaved(rawgId)
        viewModel.loadSummary(rawgId, title)
        viewModel.loadPrices(rawgId, title)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Platinum)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Cover art
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Cerulean),
            contentAlignment = Alignment.Center
        ) {
            if (game?.backgroundImage != null) {
                AsyncImage(
                    model = game.backgroundImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Cerulean
            )
        )

        if (game != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = PacificBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = game.rating.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                game.released?.let { released ->
                    Text(
                        text = "  ·  $released",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoolSteel
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Summary
        SectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("AI Summary")
                Spacer(modifier = Modifier.weight(1f))
                if (summaryState is SummaryState.Success) {
                    IconButton(onClick = { viewModel.regenerateSummary(rawgId, title) }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Regenerate summary",
                            tint = Cerulean
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            when (val state = summaryState) {
                is SummaryState.Loading -> SummaryLoading()
                is SummaryState.Success -> {
                    if (summaryExpanded) {
                        MarkdownText(
                            markdown = state.summary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = ReadingFont,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                        )
                        if (state.sources.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sources",
                                style = MaterialTheme.typography.labelMedium,
                                color = CoolSteel
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.sources.forEach { url ->
                                    SourceItem(url = url) { context.openUrl(url) }
                                }
                            }
                        }
                    }
                    TextButton(onClick = { summaryExpanded = !summaryExpanded }) {
                        Text(
                            text = if (summaryExpanded) "Show less" else "Show more",
                            color = Cerulean,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                is SummaryState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is SummaryState.Idle -> {}
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Deals
        SectionCard {
            SectionTitle("Deals")
            Spacer(modifier = Modifier.height(8.dp))
            when (val state = pricesState) {
                is PricesState.Loading -> CircularProgressIndicator(
                    color = Cerulean,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                is PricesState.Success -> {
                    val sorted = state.prices.sortedBy { it.price }
                    if (sorted.isEmpty()) {
                        Text("No deals found.", color = CoolSteel)
                    } else {
                        val foldable = sorted.size > DEAL_FOLD_THRESHOLD
                        val shown = if (foldable && !dealsExpanded) sorted.take(DEAL_PREVIEW_COUNT) else sorted
                        shown.forEachIndexed { i, deal ->
                            if (i > 0) HorizontalDivider(color = Platinum, thickness = 1.dp)
                            DealRow(deal) { context.openUrl(deal.url) }
                        }
                        if (foldable) {
                            TextButton(onClick = { dealsExpanded = !dealsExpanded }) {
                                Text(
                                    text = if (dealsExpanded) "Show less"
                                    else "Show all ${sorted.size} deals",
                                    color = Cerulean,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                is PricesState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is PricesState.Idle -> {}
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SaveButton(saveState = saveState, onSave = {
            viewModel.saveGame(
                id = rawgId,
                name = title,
                coverImage = game?.backgroundImage,
                rating = game?.rating,
                released = game?.released
            )
        }, onUnsave = { viewModel.unsaveGame(rawgId) })

        Spacer(modifier = Modifier.height(12.dp))

        // Chat — always visible, disabled until the summary is ready
        val chatEnabled = summaryState is SummaryState.Success
        Button(
            onClick = { onChatClick(rawgId) },
            enabled = chatEnabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Cerulean,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (chatEnabled) "Chat about this game" else "Chat (summary loading…)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SummaryLoading() {
    val messages = listOf(
        "Searching the web…",
        "Reading recent reviews…",
        "Weighing the verdicts…",
        "Writing your summary…",
        "Almost there…"
    )
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (index < messages.lastIndex) {
            delay(2800)
            index++
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            color = Cerulean,
            strokeWidth = 2.dp,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = messages[index],
            style = MaterialTheme.typography.bodyMedium,
            color = CoolSteel
        )
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Cerulean
        )
    )
}

@Composable
private fun SourceItem(url: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Platinum,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = ReadingFont,
                    fontSize = 14.sp
                ),
                color = PacificBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = CoolSteel,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun DealRow(deal: Deal, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = deal.store,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (deal.discountPercent > 0) {
            DiscountBadge(deal.discountPercent)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = "CA$%.2f".format(deal.price),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = PacificBlue
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open store",
            tint = CoolSteel,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DiscountBadge(percent: Int) {
    Surface(color = PositiveGreen, shape = RoundedCornerShape(6.dp)) {
        Text(
            text = "-$percent%",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun SaveButton(saveState: SaveState, onSave: () -> Unit, onUnsave: () -> Unit) {
    AnimatedContent(
        targetState = saveState,
        transitionSpec = {
            (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.92f)) togetherWith
                (fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.92f))
        },
        label = "save_button"
    ) { state ->
        val shape = RoundedCornerShape(12.dp)
        val modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
        val labelStyle = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
        when (state) {
            is SaveState.Idle, is SaveState.Error -> Button(
                onClick = onSave,
                shape = shape,
                colors = ButtonDefaults.buttonColors(containerColor = PositiveGreen, contentColor = Color.White),
                modifier = modifier
            ) {
                Text(
                    text = if (state is SaveState.Error) "Save failed — Retry" else "Save game",
                    style = labelStyle
                )
            }
            is SaveState.Saving -> Button(
                onClick = {}, enabled = false, shape = shape, modifier = modifier,
                colors = ButtonDefaults.buttonColors(containerColor = PositiveGreen, contentColor = Color.White)
            ) { Text("Saving…", style = labelStyle) }
            is SaveState.Saved -> Button(
                onClick = {}, enabled = false, shape = shape, modifier = modifier,
                colors = ButtonDefaults.buttonColors(containerColor = PositiveGreen, contentColor = Color.White)
            ) { Text("Saved ✓", style = labelStyle) }
            is SaveState.SavedPermanent -> Button(
                onClick = onUnsave,
                shape = shape,
                colors = ButtonDefaults.buttonColors(containerColor = NegativeRed, contentColor = Color.White),
                modifier = modifier
            ) { Text("Unsave game", style = labelStyle) }
            is SaveState.Unsaving -> Button(
                onClick = {}, enabled = false, shape = shape, modifier = modifier,
                colors = ButtonDefaults.buttonColors(containerColor = NegativeRed, contentColor = Color.White)
            ) { Text("Unsaving…", style = labelStyle) }
            is SaveState.Unsaved -> Button(
                onClick = {}, enabled = false, shape = shape, modifier = modifier,
                colors = ButtonDefaults.buttonColors(containerColor = NegativeRed, contentColor = Color.White)
            ) { Text("Unsaved ✓", style = labelStyle) }
        }
    }
}

private fun android.content.Context.openUrl(url: String) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
