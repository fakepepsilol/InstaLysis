package rs.fpl.instalysis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import rs.fpl.instalysis.ui.theme.InstaLysisTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InstaLysisTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Text(modifier = Modifier.padding(innerPadding),
                        text = "hello world")
                }
            }
        }
    }
}
