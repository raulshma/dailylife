package com.raulshma.dailylife

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raulshma.dailylife.ui.DailyLifeApp
import com.raulshma.dailylife.ui.DailyLifeViewModel
import com.raulshma.dailylife.ui.theme.DailyLifeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        val viewModelFactory = DailyLifeViewModel.Factory(applicationContext)
        setContent {
            DailyLifeTheme {
                val viewModel: DailyLifeViewModel = viewModel(factory = viewModelFactory)
                DailyLifeApp(viewModel = viewModel)
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permission), NotificationPermissionRequestCode)
        }
    }
}

private const val NotificationPermissionRequestCode = 42
