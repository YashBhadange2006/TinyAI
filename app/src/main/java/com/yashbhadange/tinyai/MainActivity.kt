package com.yashbhadange.tinyai

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.yashbhadange.tinyai.navigation.PocketAINavigation
import com.yashbhadange.tinyai.ui.theme.LocalModelAITheme
import android.widget.Toast

private enum class ThemeMode {
    LIGHT,
    DARK
}

private const val APP_PREFERENCES = "app_preferences"
private const val THEME_MODE_KEY = "theme_mode"

class MainActivity : ComponentActivity() {
    private val updateLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                updatePromptInProgress = true
            }
        }

    private lateinit var appUpdateManager: AppUpdateManager
    private var updatePromptInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var themeMode by rememberSaveable {
                mutableStateOf(
                    appPreferences.getString(THEME_MODE_KEY, null)
                        ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                        ?: if (systemDark) ThemeMode.DARK else ThemeMode.LIGHT
                )
            }

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            LocalModelAITheme(darkTheme = darkTheme) {
                PocketAINavigation(
                    isDarkTheme = darkTheme,
                    themeModeLabel = when (themeMode) {
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                    },
                    onToggleTheme = {
                        val nextThemeMode = when (themeMode) {
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.LIGHT
                        }
                        themeMode = nextThemeMode
                        appPreferences.edit().putString(THEME_MODE_KEY, nextThemeMode.name).apply()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkForAppUpdate()
    }

    private fun checkForAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                info.installStatus() == InstallStatus.DOWNLOADED -> {
                    Toast.makeText(
                        this,
                        "Update downloaded. Restarting to apply it.",
                        Toast.LENGTH_LONG
                    ).show()
                    appUpdateManager.completeUpdate()
                }

                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) &&
                    !updatePromptInProgress -> {
                    updatePromptInProgress = true
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LocalModelAITheme {
        Greeting("Android")
    }
}
