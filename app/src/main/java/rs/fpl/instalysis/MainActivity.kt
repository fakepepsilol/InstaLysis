package rs.fpl.instalysis

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rs.fpl.instalysis.ui.theme.InstaLysisTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InstaLysisTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ){
                        val context = LocalContext.current
                        val activity = this@MainActivity
                        Button(
                            onClick = {
                                clearPreferences(context, activity)
                            },
                        ) {
                            Text("Clear Preferences")
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        finishAndRemoveTask()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}

fun clearPreferences(context: Context, activity: Activity){
    CoroutineScope(Dispatchers.Default).launch {
        XposedServiceHelper.registerListener(object: XposedServiceHelper.OnServiceListener{
            override fun onServiceBind(service: XposedService) {
                service.deleteRemotePreferences("rs.fpl.instalysis")
                activity.runOnUiThread {
                    Toast.makeText(context, "preferences cleared", Toast.LENGTH_SHORT).show()
                }
                activity.finishAndRemoveTask()
            }

            override fun onServiceDied(service: XposedService) {
            }
        })
    }
}
