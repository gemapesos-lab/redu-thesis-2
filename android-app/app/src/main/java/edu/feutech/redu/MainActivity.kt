package edu.feutech.redu

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import edu.feutech.redu.ui.ReduAppScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ReduApp
        setContent {
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

    private fun isReduAccessibilityServiceEnabled(): Boolean {
        val expectedService = "$packageName/${packageName}.capture.ReduAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        val splitter = enabledServices.split(':')
        return splitter.any { it.equals(expectedService, ignoreCase = true) }
    }
}
