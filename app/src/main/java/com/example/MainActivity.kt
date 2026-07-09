package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.DispatchRepository
import com.example.ui.DispatchViewModel
import com.example.ui.DispatchViewModelFactory
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var repository: DispatchRepository
    private lateinit var viewModel: DispatchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize DB and Repository
        database = AppDatabase.getDatabase(this)
        repository = DispatchRepository(database)

        // 2. Instantiate ViewModel
        val factory = DispatchViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[DispatchViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val userSession by viewModel.userSession.collectAsState()

                    if (userSession.isLoggedIn) {
                        MainAppScreen(viewModel = viewModel)
                    } else {
                        LoginScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
