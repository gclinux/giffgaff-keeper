package com.godapp.ggkeep.ui.screens.taskdetail

import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.godapp.ggkeep.util.SmsHelper

@Composable
fun SmsConfirmDialog(
    phoneNumber: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val message = remember(phoneNumber) { SmsHelper.buildSmsBody() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发送保号短信？") },
        text = {
            Text(
                "收件人：$phoneNumber\n\n" +
                "短信内容：\n$message\n\n" +
                "点击「打开短信」将跳转到系统短信应用，你仍需手动确认发送。"
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val intent = SmsHelper.buildSmsIntent(phoneNumber)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                onDismiss()
            }) {
                Text("打开短信", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
