package com.juhyeonyu.isitgood.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.juhyeonyu.isitgood.ui.theme.Cerulean
import com.juhyeonyu.isitgood.ui.theme.CoolSteel
import com.juhyeonyu.isitgood.ui.theme.PacificBlue
import com.juhyeonyu.isitgood.ui.theme.Platinum
import com.juhyeonyu.isitgood.ui.viewmodel.AuthViewModel
import com.juhyeonyu.isitgood.ui.viewmodel.ChangePasswordState
import com.juhyeonyu.isitgood.ui.viewmodel.PreferencesLoadState
import com.juhyeonyu.isitgood.ui.viewmodel.PreferencesSaveState
import com.juhyeonyu.isitgood.ui.viewmodel.SettingsViewModel
import com.juhyeonyu.isitgood.ui.viewmodel.UsernameState
import kotlinx.coroutines.delay

private val summaryLengthOptions = listOf("SHORT" to "Short", "MEDIUM" to "Medium", "LONG" to "Long")
private val toneOptions = listOf(
    "CASUAL" to "Casual",
    "BALANCED" to "Balanced",
    "CRITICAL" to "Critical",
    "ENTHUSIASTIC" to "Enthusiastic"
)
private val fontSizeOptions = listOf("SMALL" to "Small", "MEDIUM" to "Medium", "LARGE" to "Large")
private val lookOutForOptions = listOf("Action", "Storyline", "Graphics", "Soundtrack", "Gameplay")
private val dealDisplayOptions = listOf("PRICE" to "Price", "DISCOUNT" to "Discount", "BOTH" to "Both")

private val ContentPadding = 24.dp
private val SectionSpacing = 28.dp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val loadState by viewModel.loadState.collectAsState()
    val prefs by viewModel.prefs.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val changePasswordState by authViewModel.changePasswordState.collectAsState()
    val accountUsername by authViewModel.accountUsername.collectAsState()
    val usernameState by authViewModel.usernameState.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }

    // Local editors for the username + numeric threshold fields, prefilled once loaded.
    var usernameInput by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.load()
        authViewModel.loadAccount()
    }

    LaunchedEffect(accountUsername) {
        accountUsername?.let { usernameInput = it }
    }

    LaunchedEffect(loadState) {
        if (loadState is PreferencesLoadState.Loaded) {
            discountText = prefs.saleAlertDiscount?.toString() ?: ""
            priceText = prefs.saleAlertPrice?.let { formatThresholdPrice(it) } ?: ""
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            state = changePasswordState,
            onConfirm = { current, new -> authViewModel.changePassword(current, new) },
            onDismiss = {
                showPasswordDialog = false
                authViewModel.resetChangePasswordState()
            }
        )
        // Auto-close shortly after a successful change so the user sees the confirmation.
        LaunchedEffect(changePasswordState) {
            if (changePasswordState is ChangePasswordState.Success) {
                delay(900)
                showPasswordDialog = false
                authViewModel.resetChangePasswordState()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Platinum)
            .verticalScroll(rememberScrollState())
    ) {
        // Branded header — scrolls with the rest of the page
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    color = Cerulean,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(24.dp)
            )
        }

        when (val s = loadState) {
            is PreferencesLoadState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Cerulean)
                }
            }

            is PreferencesLoadState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ContentPadding, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.load() },
                        border = BorderStroke(1.5.dp, Cerulean),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Cerulean)
                    ) {
                        Text("Retry")
                    }
                }
            }

            is PreferencesLoadState.Loaded -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ContentPadding, vertical = 20.dp)
                ) {
                    PreferenceSection(title = "Summary length") {
                        SingleChoiceRow(
                            options = summaryLengthOptions,
                            selected = prefs.summaryLength,
                            onSelect = viewModel::setSummaryLength
                        )
                    }

                    PreferenceSection(title = "Tone") {
                        SingleChoiceRow(
                            options = toneOptions,
                            selected = prefs.tone,
                            onSelect = viewModel::setTone
                        )
                    }

                    PreferenceSection(title = "What you look out for") {
                        MultiChoiceRow(
                            options = lookOutForOptions,
                            selected = prefs.lookOutFor,
                            onToggle = viewModel::toggleLookOutFor
                        )
                    }

                    // Mature content toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = SectionSpacing)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mature content (18+)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Cerulean
                                )
                            )
                            Text(
                                text = "Allow frank discussion of mature themes",
                                style = MaterialTheme.typography.bodySmall,
                                color = CoolSteel
                            )
                        }
                        Switch(
                            checked = prefs.allowMatureContent,
                            onCheckedChange = viewModel::setMatureContent,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Cerulean
                            )
                        )
                    }

                    PreferenceSection(title = "Font size") {
                        SingleChoiceRow(
                            options = fontSizeOptions,
                            selected = prefs.fontSize,
                            onSelect = viewModel::setFontSize
                        )
                    }

                    PreferenceSection(title = "Deal display") {
                        SingleChoiceRow(
                            options = dealDisplayOptions,
                            selected = prefs.dealDisplay,
                            onSelect = viewModel::setDealDisplay
                        )
                    }

                    // Sale alerts — highlight a saved game when a deal beats either threshold
                    Column(modifier = Modifier.padding(bottom = SectionSpacing)) {
                        Text(
                            text = "Sale alerts",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Cerulean
                            )
                        )
                        Text(
                            text = "Highlight saved games when a deal beats either threshold. Leave blank to ignore.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoolSteel
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { txt ->
                                val filtered = txt.filter { it.isDigit() }.take(3)
                                discountText = filtered
                                viewModel.setSaleAlertDiscount(filtered.toIntOrNull()?.coerceIn(0, 100))
                            },
                            label = { Text("Min discount (%)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { txt ->
                                val filtered = txt.filter { it.isDigit() || it == '.' }
                                priceText = filtered
                                viewModel.setSaleAlertPrice(filtered.toDoubleOrNull())
                            },
                            label = { Text("Max price") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save status + compact, right-aligned save action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        SaveStatusText(
                            saveState = saveState,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { viewModel.save() },
                            enabled = saveState !is PreferencesSaveState.Saving,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Cerulean)
                        ) {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(SectionSpacing))

                    // Account section
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Cerulean
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = {
                            usernameInput = it
                            authViewModel.resetUsernameState()
                        },
                        label = { Text("Username") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        UsernameStatusText(
                            usernameState = usernameState,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { authViewModel.updateUsername(usernameInput) },
                            enabled = usernameState !is UsernameState.Loading &&
                                usernameInput.isNotBlank() &&
                                usernameInput.trim() != accountUsername,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Cerulean)
                        ) {
                            Text(
                                text = "Update",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(SectionSpacing))

                    // Change password — account action
                    OutlinedButton(
                        onClick = {
                            authViewModel.resetChangePasswordState()
                            showPasswordDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Cerulean),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Cerulean),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Change password",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Logout
                    OutlinedButton(
                        onClick = onLogout,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log out",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SaveStatusText(
    saveState: PreferencesSaveState,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (saveState) {
        is PreferencesSaveState.Saving -> "Saving…" to CoolSteel
        is PreferencesSaveState.Saved -> "Saved ✓" to PacificBlue
        is PreferencesSaveState.Error -> saveState.message to MaterialTheme.colorScheme.error
        is PreferencesSaveState.Idle -> "" to Color.Unspecified
    }
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.End,
        modifier = modifier
    )
}

@Composable
private fun UsernameStatusText(
    usernameState: UsernameState,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (usernameState) {
        is UsernameState.Loading -> "Updating…" to CoolSteel
        is UsernameState.Success -> "Username updated ✓" to PacificBlue
        is UsernameState.Error -> usernameState.message to MaterialTheme.colorScheme.error
        is UsernameState.Idle -> "" to Color.Unspecified
    }
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.End,
        modifier = modifier
    )
}

// Drops a trailing ".0" so a whole-number threshold shows as "20" not "20.0".
private fun formatThresholdPrice(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

@Composable
private fun ChangePasswordDialog(
    state: ChangePasswordState,
    onConfirm: (current: String, new: String) -> Unit,
    onDismiss: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Change password",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Cerulean
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("Current password") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (currentVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { currentVisible = !currentVisible }) {
                            Icon(
                                imageVector = if (currentVisible)
                                    Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = Cerulean
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New password") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (newVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newVisible = !newVisible }) {
                            Icon(
                                imageVector = if (newVisible)
                                    Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = Cerulean
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                when (state) {
                    is ChangePasswordState.Loading -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(
                            color = PacificBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    is ChangePasswordState.Error -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    is ChangePasswordState.Success -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Password updated ✓",
                            color = PacificBlue,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    is ChangePasswordState.Idle -> {}
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(current, newPassword) },
                enabled = state !is ChangePasswordState.Loading
            ) {
                Text("Update", color = Cerulean, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CoolSteel)
            }
        }
    )
}

@Composable
private fun PreferenceSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = SectionSpacing)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Cerulean
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SingleChoiceRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Cerulean,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MultiChoiceRow(
    options: List<String>,
    selected: List<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { item ->
            FilterChip(
                selected = item in selected,
                onClick = { onToggle(item) },
                label = { Text(item) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Cerulean,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
