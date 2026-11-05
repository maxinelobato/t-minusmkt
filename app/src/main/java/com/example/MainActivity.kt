package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TMinusMainScreen
import com.example.ui.TMinusViewModel
import com.example.ui.components.GoogleAccountConfirmDialog
import com.example.ui.screens.NeumorphicLoginScreen
import com.example.ui.theme.TMinusMktTheme
import com.example.util.BiometricAuthHelper
import com.example.util.GoogleAuthHelper
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val viewModel: TMinusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()
            var showManualGoogleDialog by remember { mutableStateOf(false) }

            TMinusMktTheme(darkTheme = uiState.isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!uiState.isLocked) {
                        TMinusMainScreen(
                            viewModel = viewModel,
                            onLockApp = {
                                viewModel.setLocked(true)
                            }
                        )
                    } else {
                        NeumorphicLoginScreen(
                            isPt = uiState.isPortuguese,
                            isDarkMode = uiState.isDarkMode,
                            hasExistingSession = uiState.hasStoredSession,
                            isAuthenticated = uiState.isAuthenticated,
                            userEmail = uiState.userSession?.email,
                            timeDisplay = uiState.timeDisplay,
                            amPmDisplay = uiState.amPmDisplay,
                            dateDisplay = uiState.dateDisplay,
                            onGoogleSignInClick = {
                                scope.launch {
                                    val session = GoogleAuthHelper.signInWithGoogle(this@MainActivity)
                                    if (session != null) {
                                        viewModel.onGoogleSignInSuccess(
                                            email = session.email,
                                            displayName = session.displayName,
                                            photoUrl = session.photoUrl,
                                            idToken = session.idToken
                                        )
                                        viewModel.setLocked(false)
                                    } else {
                                        // Open Google Account confirmation modal for seamless access
                                        showManualGoogleDialog = true
                                    }
                                }
                            },
                            onBiometricUnlockClick = {
                                triggerBiometricPrompt()
                            },
                            onUnlockClick = {
                                viewModel.setLocked(false)
                            },
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onToggleTheme = { viewModel.toggleTheme() },
                            onVoiceClick = {
                                viewModel.openAddDialog()
                            },
                            onNotificationClick = {
                                Toast.makeText(
                                    this@MainActivity,
                                    if (uiState.isPortuguese) "T-Minus Mkt: ${uiState.daysUntilDeadline} dias até o lançamento!" else "T-Minus Mkt: ${uiState.daysUntilDeadline} days until launch!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onLockClick = {
                                viewModel.lockSession()
                                viewModel.setLocked(true)
                            }
                        )

                        if (showManualGoogleDialog) {
                            GoogleAccountConfirmDialog(
                                isPt = uiState.isPortuguese,
                                onDismiss = { showManualGoogleDialog = false },
                                onConfirm = { googleEmail ->
                                    showManualGoogleDialog = false
                                    viewModel.onGoogleSignInSuccess(
                                        email = googleEmail,
                                        displayName = googleEmail.substringBefore("@")
                                            .replace(".", " ")
                                            .replaceFirstChar { it.uppercase() }
                                    )
                                    viewModel.setLocked(false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // If the user has a stored Google session but app is locked, prompt biometric unlock
        val state = viewModel.uiState.value
        if (state.hasStoredSession && state.isLocked) {
            triggerBiometricPrompt()
        }
    }

    private fun triggerBiometricPrompt() {
        val isPt = viewModel.uiState.value.isPortuguese
        BiometricAuthHelper.authenticate(
            activity = this,
            title = if (isPt) "T-Minus Mkt • Autenticação" else "T-Minus Mkt • Authentication",
            subtitle = if (isPt) "Confirme sua identidade com biometria ou senha" else "Confirm identity with biometrics or PIN",
            onSuccess = {
                viewModel.onBiometricSuccess()
                viewModel.setLocked(false)
            },
            onError = { _, errString ->
                Toast.makeText(this@MainActivity, errString, Toast.LENGTH_SHORT).show()
            },
            onFailed = {
                Toast.makeText(
                    this@MainActivity,
                    if (isPt) "Biometria não reconhecida" else "Biometrics not recognized",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}
