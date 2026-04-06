package com.example.estudapp.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkManagerHelper {
    private const val WORK_NAME = "study_reminder_work"

    fun scheduleDailyReminder(context: Context) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        var delay = calendar.timeInMillis - System.currentTimeMillis()
        if (delay < 0) {
            // Nếu đã qua 8 giờ sáng, lên lịch cho 8 giờ sáng ngày mai
            delay += TimeUnit.DAYS.toMillis(1)
        }

        val request = PeriodicWorkRequestBuilder<StudyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Tránh tạo trùng lặp nếu đã có lịch
            request
        )
    }
}