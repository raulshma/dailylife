package com.raulshma.dailylife

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.raulshma.dailylife.ui.DailyLifeApp
import com.raulshma.dailylife.ui.DailyLifeViewModel
import com.raulshma.dailylife.ui.theme.DailyLifeTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            DailyLifeTheme {
                val viewModel: DailyLifeViewModel = hiltViewModel()
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
