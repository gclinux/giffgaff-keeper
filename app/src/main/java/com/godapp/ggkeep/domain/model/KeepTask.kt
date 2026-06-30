package com.godapp.ggkeep.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class TaskStatus { EXPIRED, URGENT, OK }

/**
 * Fixed-threshold color level for display, independent of user's remindDaysBefore setting.
 * - EXPIRED  (≤0 days)    → gray
 * - CRITICAL (1-7 days)   → red
 * - WARNING  (8-15 days)  → orange
 * - NORMAL   (>15 days)   → green
 */
enum class ColorLevel { EXPIRED, CRITICAL, WARNING, NORMAL }

/**
 * Domain model for a SIM keep-alive task.
 *
 * The 180-day rule: a SIM card must have at least one billable event
 * (call/SMS/data) every 180 days to keep the number active.
 *
 * @param lastConsumeDate epoch millis at local start-of-day of the last consume event
 * @param reminderHour    hour of day (0-23) to fire the daily reminder
 * @param remindDaysBefore start reminding when remaining days <= this value (7 or 15)
 * @param smsToNumber     optional target number for the keep-alive SMS
 */
data class KeepTask(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val lastConsumeDate: Long,
    val reminderHour: Int,
    val remindDaysBefore: Int,
    val smsToNumber: String? = null
) {
    private fun lastConsumeLocalDate(): LocalDate =
        Instant.ofEpochMilli(lastConsumeDate).atZone(ZoneId.systemDefault()).toLocalDate()

    /** Days remaining before the 180-day window expires. Can be negative if expired. */
    val remainingDays: Int
        get() = TOTAL_DAYS - ChronoUnit.DAYS.between(
            lastConsumeLocalDate(),
            LocalDate.now(ZoneId.systemDefault())
        ).toInt()

    val isExpired: Boolean
        get() = remainingDays <= 0

    val isUrgent: Boolean
        get() = remainingDays in 1..remindDaysBefore

    val status: TaskStatus
        get() = when {
            isExpired -> TaskStatus.EXPIRED
            isUrgent -> TaskStatus.URGENT
            else -> TaskStatus.OK
        }

    /**
     * Display color level based on FIXED thresholds (not affected by remindDaysBefore setting):
     * - ≤0 → EXPIRED (gray)
     * - 1..7 → CRITICAL (red)
     * - 8..15 → WARNING (orange)
     * - >15 → NORMAL (green)
     */
    val colorLevel: ColorLevel
        get() = when {
            remainingDays <= 0 -> ColorLevel.EXPIRED
            remainingDays <= 7 -> ColorLevel.CRITICAL
            remainingDays <= 15 -> ColorLevel.WARNING
            else -> ColorLevel.NORMAL
        }

    companion object {
        const val TOTAL_DAYS = 180
    }
}
