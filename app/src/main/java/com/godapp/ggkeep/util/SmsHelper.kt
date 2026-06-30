package com.godapp.ggkeep.util

import android.content.Intent
import android.net.Uri
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SmsHelper {
    fun buildSmsBody(): String {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        return "Hello, today is $today. I am sending this message just to keep in touch—please don't forget me."
    }

    fun buildSmsIntent(phoneNumber: String): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", buildSmsBody())
        }
    }
}
