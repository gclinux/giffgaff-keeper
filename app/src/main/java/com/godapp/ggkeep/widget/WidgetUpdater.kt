package com.godapp.ggkeep.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Triggers a widget update for all placed instances.
 * Call this after any data mutation (create/update/delete/confirm) to refresh the widget.
 */
object WidgetUpdater {

    fun updateAll(context: Context) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                KeepSimWidget().updateAll(context.applicationContext)
            } catch (e: Exception) {
                // Widget not yet placed or other transient error — safe to ignore
            }
        }
    }
}
