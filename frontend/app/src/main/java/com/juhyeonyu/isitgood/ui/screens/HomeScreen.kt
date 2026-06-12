package com.juhyeonyu.isitgood.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.juhyeonyu.isitgood.data.model.BestDeal
import com.juhyeonyu.isitgood.data.model.DealGame
import com.juhyeonyu.isitgood.data.model.SavedGame
import com.juhyeonyu.isitgood.data.model.UserPreferences
import com.juhyeonyu.isitgood.ui.theme.Cerulean
import com.juhyeonyu.isitgood.ui.theme.CeruleanAlt
import com.juhyeonyu.isitgood.ui.theme.CoolSteel
import com.juhyeonyu.isitgood.ui.theme.PacificBlue
import com.juhyeonyu.isitgood.ui.theme.Platinum
import com.juhyeonyu.isitgood.ui.viewmodel.DealsState
import com.juhyeonyu.isitgood.ui.viewmodel.HomeViewModel
import com.juhyeonyu.isitgood.ui.viewmodel.SavedGameState
import com.juhyeonyu.isitgood.utils.Greetings
import java.time.LocalTime

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSearchClick: () -> Unit,
    onGameClick: (rawgId: Int, title: String) -> Unit
) {
    val savedGameState by viewModel.savedGameState.collectAsState()
    val dealsState by viewModel.dealsState.collectAsState()
    val savedDeals by viewModel.savedDeals.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val username by viewModel.username.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    // Pick a greeting once per username resolution so it stays stable across recompositions.
    val greeting = remember(username) { Greetings.pick(username, LocalTime.now().hour) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Platinum)
    ) {
        item {
            HomeHeader(greeting = greeting, onSearchClick = onSearchClick)
        }

        item { SectionTitle("Your Saved Games", topPadding = 24.dp) }

        when (val s = savedGameState) {
            is SavedGameState.Idle -> {}
            is SavedGameState.Loading -> item { SectionSpinner() }
            is SavedGameState.Error -> item { SectionMessage(s.message, MaterialTheme.colorScheme.error) }
            is SavedGameState.Success -> {
                if (s.games.isEmpty()) {
                    item { SectionMessage("No saved games yet", CoolSteel) }
                } else {
                    items(s.games, key = { it.rawgId }) { game ->
                        SavedGameCard(
                            game = game,
                            deal = savedDeals[game.rawgId],
                            prefs = preferences,
                            onClick = { onGameClick(game.rawgId, game.title) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        item { SectionTitle("Popular Games on Sale", topPadding = 28.dp) }

        when (val d = dealsState) {
            is DealsState.Loading -> item { SectionSpinner() }
            is DealsState.Error -> item { SectionMessage(d.message, MaterialTheme.colorScheme.error) }
            is DealsState.Success -> {
                if (d.deals.isEmpty()) {
                    item { SectionMessage("No deals right now", CoolSteel) }
                } else {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(d.deals, key = { it.rawgId }) { deal ->
                                DealCard(
                                    deal = deal,
                                    dealDisplay = preferences.dealDisplay,
                                    onClick = { onGameClick(deal.rawgId, deal.title) }
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun HomeHeader(greeting: String, onSearchClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Cerulean,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(start = 24.dp, end = 24.dp, top = 36.dp, bottom = 24.dp)
    ) {
        Column {
            Text(
                text = "IsItGood?",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSearchClick() },
                shape = RoundedCornerShape(28.dp),
                color = Color.White
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Cerulean)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search games...", color = CoolSteel)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, topPadding: Dp = 0.dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Cerulean
        ),
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = topPadding, bottom = 10.dp)
    )
}

@Composable
private fun SectionSpinner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Cerulean)
    }
}

@Composable
private fun SectionMessage(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
private fun SavedGameCard(
    game: SavedGame,
    deal: BestDeal?,
    prefs: UserPreferences,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = game.coverImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = CeruleanAlt,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⭐ ${game.rating ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoolSteel
                )
                if (deal != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    DealBadge(
                        label = dealLabel(prefs.dealDisplay, deal.cut, deal.price),
                        accent = meetsThreshold(deal, prefs)
                    )
                }
            }
        }
    }
}

@Composable
private fun DealCard(
    deal: DealGame,
    dealDisplay: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = deal.coverImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = deal.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = CeruleanAlt,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⭐ ${deal.rating ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoolSteel
                )
                Spacer(modifier = Modifier.height(6.dp))
                DealBadge(
                    label = dealLabel(dealDisplay, deal.discountPercent, deal.price),
                    accent = true
                )
            }
        }
    }
}

@Composable
private fun DealBadge(label: String, accent: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (accent) PacificBlue else CoolSteel.copy(alpha = 0.30f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (accent) Color.White else CeruleanAlt,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun dealLabel(dealDisplay: String, cut: Int, price: Double): String {
    val priceStr = "$" + String.format("%.2f", price)
    return when (dealDisplay) {
        "PRICE" -> priceStr
        "DISCOUNT" -> "-$cut%"
        else -> "-$cut%  ·  $priceStr"
    }
}

private fun meetsThreshold(deal: BestDeal, prefs: UserPreferences): Boolean {
    val byDiscount = prefs.saleAlertDiscount?.let { deal.cut >= it } ?: false
    val byPrice = prefs.saleAlertPrice?.let { deal.price <= it } ?: false
    return byDiscount || byPrice
}
