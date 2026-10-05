package edu.feutech.redu

import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import edu.feutech.redu.ui.ReduAppScreen
import edu.feutech.redu.ui.theme.ReduTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // The platform exit scales the launcher icon. Removing it here lets the
            // animated mascot be the first blob, with no static face underneath.
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenView.remove()
            }
        }
        val app = application as ReduApp
        setContent {
            ReduTheme {
                ReduAppScreen(
                    database = app.database,
                    isAccessibilityServiceEnabled = ::isReduAccessibilityServiceEnabled,
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    context = this,
                )
            }
        }
    }

    private fun isReduAccessibilityServiceEnabled(): Boolean {
        val expectedService = ComponentName(this, edu.feutech.redu.capture.ReduAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return enabledServices.split(':')
            .mapNotNull(ComponentName::unflattenFromString)
            .any { it == expectedService }
    }

}
