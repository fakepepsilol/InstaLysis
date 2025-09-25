package rs.fpl.instalysis.receivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CheckPermissions: BroadcastReceiver() {
    val tag = "FPL_CheckPermissionsBroadcastReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null){
            Log.e(tag, "Context is null.")
            return
        }
        if(!isGranted(Manifest.permission.POST_NOTIFICATIONS, context)){ requestAllPermissions(context) }
    }
    private fun requestAllPermissions(context: Context){
        val intent = Intent().apply {
            setPackage("com.instagram.android")
            setAction("rs.fpl.instalysis.ASK_FOR_PERMISSIONS")
        }
        context.sendBroadcast(intent)
    }
    private fun isGranted(permission: String, context: Context): Boolean{
        if(permission == Manifest.permission.POST_NOTIFICATIONS) return isGrantedPushNotifs(context)
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    private fun isGrantedPushNotifs(context: Context): Boolean{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else{
            true
        }
    }
}