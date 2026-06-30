package com.godapp.ggkeep.ui.theme

import androidx.compose.ui.graphics.Color
import com.godapp.ggkeep.domain.model.ColorLevel
import com.godapp.ggkeep.domain.model.TaskStatus

/** Maps a TaskStatus to its representative Compose Color. (保留兼容，但 UI 应优先用 ColorLevel) */
fun TaskStatus.color(): Color = when (this) {
    TaskStatus.EXPIRED -> Color(0xFFE53935)  // red
    TaskStatus.URGENT -> Color(0xFFFF9800)   // orange
    TaskStatus.OK -> Color(0xFF4CAF50)       // green
}

/** Light tint (15% alpha) for card backgrounds. */
fun TaskStatus.tint(): Color = color().copy(alpha = 0.15f)

// ===== 固定阈值颜色（不受 remindDaysBefore 设置影响）=====

/** Maps a ColorLevel to its representative Compose Color. */
fun ColorLevel.color(): Color = when (this) {
    ColorLevel.EXPIRED  -> Color(0xFF9E9E9E)  // gray
    ColorLevel.CRITICAL -> Color(0xFFE53935)  // red
    ColorLevel.WARNING  -> Color(0xFFFF9800)  // orange
    ColorLevel.NORMAL   -> Color(0xFF4CAF50)  // green
}

/** Light tint (15% alpha) for card backgrounds. */
fun ColorLevel.tint(): Color = color().copy(alpha = 0.15f)
