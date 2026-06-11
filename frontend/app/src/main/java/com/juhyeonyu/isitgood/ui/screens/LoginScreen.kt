package com.juhyeonyu.isitgood.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.juhyeonyu.isitgood.ui.theme.Cerulean
import com.juhyeonyu.isitgood.ui.theme.CeruleanAlt
import com.juhyeonyu.isitgood.ui.theme.CoolSteel
import com.juhyeonyu.isitgood.ui.theme.PacificBlue
import com.juhyeonyu.isitgood.ui.theme.Platinum
import com.juhyeonyu.isitgood.ui.viewmodel.AuthState
import com.juhyeonyu.isitgood.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Platinum)
    ) {
        // Branded header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    color = Cerulean,
                    shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "IsItGood?",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your AI Game Guide",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = PacificBlue
                    )
                )
            }
        }

        // Form
        AnimatedContent(
            targetState = isRegistering,
            transitionSpec = {
                val direction = if (targetState) 1 else -1
                slideInHorizontally { it * direction } togetherWith slideOutHorizontally { -it * direction }
            },
            label = "auth_mode"
        ) { registering ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)
                    .padding(top = 268.dp)
            ) {
                Text(
                    text = if (registering) "Create account" else "Welcome back",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Cerulean
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (registering) "Join to track and explore games" else "Sign in to continue",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CeruleanAlt
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible)
                                    "Hide password" else "Show password",
                                tint = Cerulean
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        if (registering) viewModel.register(email, password)
                        else viewModel.login(email, password)
                    },
                    enabled = state !is AuthState.Loading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Cerulean),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (registering) "Create account" else "Log in",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                when (val s = state) {
                    is AuthState.Loading -> CircularProgressIndicator(
                        color = PacificBlue,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    is AuthState.Error -> Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    else -> {}
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = {
                        isRegistering = !isRegistering
                        viewModel.resetState()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = CoolSteel)) {
                                append(
                                    if (registering) "Already have an account?  "
                                    else "New to IsItGood?  "
                                )
                            }
                            withStyle(SpanStyle(color = Cerulean, fontWeight = FontWeight.SemiBold)) {
                                append(if (registering) "Log in" else "Sign up")
                            }
                        },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}