package com.godapp.ggkeep

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.godapp.ggkeep.ui.navigation.KeepSimNavigation
import com.godapp.ggkeep.ui.theme.KeepSimTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        val taskId = intent?.getLongExtra(EXTRA_TASK_ID, -1L) ?: -1L
        val triggerSms = intent?.getBooleanExtra(EXTRA_TRIGGER_SMS, false) ?: false

        setContent {
            KeepSimTheme {
                KeepSimNavigation(
                    initialTaskId = taskId,
                    triggerSms = triggerSms
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Recreate to pick up new intent extras (task_id / trigger_sms).
        // Simple and correct; the brief recreation flash is acceptable for notification taps.
        if (intent.hasExtra(EXTRA_TASK_ID)) {
            recreate()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TRIGGER_SMS = "trigger_sms"
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }
}
