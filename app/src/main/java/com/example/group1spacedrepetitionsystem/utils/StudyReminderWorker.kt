package com.example.estudapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.estudapp.MainActivity
import com.example.estudapp.R

class StudyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    // Phải có từ khóa suspend ở đây vì dùng CoroutineWorker
    override suspend fun doWork(): Result {
        // Trong thực tế, bạn có thể gọi Firebase ở đây để đếm số flashcard cần học
        // Ở đây tôi giả định là có bài cần học để hiển thị thông báo
        val count = (5..15).random() // Giả lập số lượng bài
        
        showNotification(count)
        
        return Result.success()
    }

    private fun showNotification(count: Int) {
        val channelId = "study_reminder_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Bước 3: Tạo Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở học tập",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh thông báo nhắc nhở ôn tập hàng ngày"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        // Bước 4: Tạo nội dung thông báo
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.logo_white) // Sử dụng logo của app
            .setContentTitle("📚 Đến giờ học rồi!")
            .setContentText("Bạn có $count flashcard cần ôn hôm nay")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1001, notification)
    }
}
