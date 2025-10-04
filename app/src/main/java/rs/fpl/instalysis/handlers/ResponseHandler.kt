package rs.fpl.instalysis.handlers

import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rs.fpl.instalysis.background.XposedScope
import rs.fpl.instalysis.background.instagram.ServiceMessageType

object ResponseHandler: Handler(Looper.getMainLooper()){
    override fun handleMessage(msg: Message) {
        when (ServiceMessageType.fromInt(msg.what)){
            ServiceMessageType.RESPONSE_STATUS_OPERATIONAL -> toastStatus("Operational")
            ServiceMessageType.RESPONSE_MISSING_PERMISSIONS -> requestPermissions()
            else -> super.handleMessage(msg)
        }
    }
}


fun toastStatus(status: String){
    CoroutineScope(Dispatchers.Default).launch {
        val context = XposedScope.awaitContext()
        val activity = XposedScope.awaitActivity()
        activity.runOnUiThread {
            Toast.makeText(context, "Service status: $status", Toast.LENGTH_SHORT).show()
        }
    }
}

fun requestPermissions(){
    toastStatus("Missing permissions")

    CoroutineScope(Dispatchers.Default).launch {

        val context = XposedScope.awaitContext()
        Intent().apply {
            setPackage("rs.fpl.instalysis")
            setComponent(ComponentName("rs.fpl.instalysis", "rs.fpl.instalysis.permissions.PermissionActivity"))
            setFlags(FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            context.startActivity(intent)
        }
    }
}