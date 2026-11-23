package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TMinusMainScreen
import com.example.ui.TMinusViewModel
import com.example.ui.screens.NeumorphicLoginScreen
import com.example.ui.theme.TMinusMktTheme
import com.example.util.BiometricAuthHelper
import com.example.util.GoogleAuthHelper
import com.example.util.GoogleSignInResult
import com.example.util.NotificationHelper
import kotlinx.coroutines.launch

private enum class AppRootRoute {
    LOGIN,
    HOME
}

class MainActivity : FragmentActivity() {

    private val viewModel: TMinusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.util.configureAppCheck(this, intent)
        enableEdgeToEdge()
        viewModel.setLocked(false)
        handleUnlockIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()

            val notifPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    com.example.service.SystemLockScreenService.start(
                        context = this@MainActivity,
                        syncWallpaper = true
                    )
                }
            }

            // Request notification permission and activate Android System Lock Screen
            // only after user is authenticated on Home so Login is never interrupted
            LaunchedEffect(isAuthenticated) {
                if (!isAuthenticated) return@LaunchedEffect
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    com.example.service.SystemLockScreenService.start(
                        context = this@MainActivity,
                        syncWallpaper = true
                    )
                }
            }

            val currentRoute = if (isAuthenticated) {
                AppRootRoute.HOME
            } else {
                AppRootRoute.LOGIN
            }

            TMinusMktTheme(darkTheme = uiState.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(
                        targetState = currentRoute,
                        animationSpec = tween(280),
                        label = "root_navigation_transition"
                    ) { route ->
                        when (route) {
                            AppRootRoute.LOGIN -> {
                                NeumorphicLoginScreen(
                                    isPt = uiState.isPortuguese,
                                    isDarkMode = uiState.isDarkMode,
                                    hasExistingSession = uiState.hasStoredSession,
                                    isAuthenticated = isAuthenticated,
                                    userEmail = uiState.userSession?.email,
                                    timeDisplay = uiState.timeDisplay,
                                    dateDisplay = uiState.dateDisplay,
                                    activeTask = uiState.activeTask,
                                    tasks = uiState.allTasks,
                                    onGoogleSignInClick = {
                                        scope.launch {
                                            when (val result = GoogleAuthHelper.signInWithGoogle(this@MainActivity)) {
                                                is GoogleSignInResult.Success -> {
                                                    val s = result.session
                                                    viewModel.onGoogleSignInSuccess(
                                                        email = s.email,
                                                        displayName = s.displayName,
                                                        photoUrl = s.photoUrl,
                                                        idToken = s.idToken
                                                    )
                                                }
                                                is GoogleSignInResult.Cancelled -> {
                                                    // User dismissed the Google credential sheet
                                                }
                                                is GoogleSignInResult.Error -> {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        if (uiState.isPortuguese) {
                                                            "Google Sign-In: ${result.message}. Use a biometria abaixo se disponível."
                                                        } else {
                                                            "Google Sign-In: ${result.message}. Try biometric unlock below."
                                                        },
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        }
                                    },
                                    onBiometricUnlockClick = {
                                        if (BiometricAuthHelper.canAuthenticate(this@MainActivity)) {
                                            BiometricAuthHelper.authenticate(
                                                activity = this@MainActivity,
                                                title = if (uiState.isPortuguese) {
                                                    "T-Minus Mkt • Desbloqueio"
                                                } else {
                                                    "T-Minus Mkt • Unlock"
                                                },
                                                subtitle = if (uiState.isPortuguese) {
                                                    "Confirme sua identidade para abrir o Planejamento de Marketing"
                                                } else {
                                                    "Confirm your identity to open Marketing Planning"
                                                },
                                                onSuccess = {
                                                    viewModel.onBiometricSuccess()
                                                },
                                                onError = { errorCode, errString ->
                                                    if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
                                                        errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL ||
                                                        errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT
                                                    ) {
                                                        viewModel.onBiometricSuccess()
                                                    } else if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                                                    ) {
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            errString,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                },
                                                onFailed = {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        if (uiState.isPortuguese) {
                                                            "Biometria não reconhecida. Tente novamente."
                                                        } else {
                                                            "Biometric not recognized. Please try again."
                                                        },
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                        } else {
                                            // Device/emulator has no biometric or PIN enrolled: unlock gracefully
                                            viewModel.onBiometricSuccess()
                                        }
                                    },
                                    onUnlockClick = {
                                        viewModel.onBiometricSuccess()
                                    },
                                    onToggleLanguage = { viewModel.toggleLanguage() },
                                    onToggleTheme = { viewModel.toggleTheme() }
                                )
                            }

                            AppRootRoute.HOME -> {
                                TMinusMainScreen(
                                    viewModel = viewModel,
                                    onLockApp = { viewModel.lockSession() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.setLocked(false)
        handleUnlockIntent(intent)
    }

    private fun handleUnlockIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("EXTRA_UNLOCK_DIRECT", false) == true) {
            viewModel.onBiometricSuccess()
        }
    }
}
