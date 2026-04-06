package com.example.estudapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.estudapp.navigate.EPPNavHost
import com.example.estudapp.ui.feature.auth.AuthViewModel
import com.example.estudapp.ui.theme.EstudaTheme
import com.example.estudapp.utils.WorkManagerHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    
    // Xin quyền thông báo cho Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Quyền đã được cấp, lên lịch nhắc nhở
            WorkManagerHelper.scheduleDailyReminder(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Khởi tạo Firebase App Check với Debug Provider để hỗ trợ môi trường phát triển
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = Firebase.appCheck
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
        
        val authViewModel : AuthViewModel by viewModels()

        // Kiểm tra & xin quyền thông báo
        checkNotificationPermission()

        setContent {
            EstudaTheme {
                EPPNavHost(authViewModel = authViewModel)
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Đã có quyền, lên lịch nhắc nhở
                    WorkManagerHelper.scheduleDailyReminder(this)
                }
                else -> {
                    // Xin quyền
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android thấp hơn 13 không cần xin quyền runtime cho thông báo
            WorkManagerHelper.scheduleDailyReminder(this)
        }
    }
}
