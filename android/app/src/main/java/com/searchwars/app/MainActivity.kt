package com.searchwars.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.searchwars.app.network.ApiClient
import com.searchwars.app.network.GuestRequest
import com.searchwars.app.ui.theme.SearchWarsTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SearchWarsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GameScreen()
                }
            }
        }
    }
}

@Composable
fun GameScreen() {
    var status by remember { mutableStateOf("Not connected") }
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "SearchWars", style = MaterialTheme.typography.headlineMedium)
            Text(text = status, modifier = Modifier.padding(top = 16.dp))
            Button(
                onClick = {
                    scope.launch {
                        status = try {
                            val auth = ApiClient.service.playAsGuest(GuestRequest("Guest"))
                            "Connected as ${auth.username}"
                        } catch (e: Exception) {
                            "Error: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Play as Guest")
            }
        }
    }
}
