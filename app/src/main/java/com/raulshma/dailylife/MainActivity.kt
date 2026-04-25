package com.raulshma.dailylife

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
        val viewModelFactory = DailyLifeViewModel.Factory(applicationContext)
        setContent {
            DailyLifeTheme {
                val viewModel: DailyLifeViewModel = viewModel(factory = viewModelFactory)
                DailyLifeApp(viewModel = viewModel)
            }
        }
    }
}
