package rs.fpl.instalysis.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.core.content.ContextCompat

object Permissions {

    fun checkAllPermissions(context: Context): Boolean{
        return if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            true
        } else ContextCompat
            .checkSelfPermission(
                context,Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED
//        return checkPermission(Manifest.permission.POST_NOTIFICATIONS, context)
    }
//    fun checkPermission(permission: String, context: Context): Boolean{
//        if(permission == Manifest.permission.POST_NOTIFICATIONS) return checkPermissionPostNotifications(context)
//        return ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
//    }
//    fun checkPermissionPostNotifications(context: Context): Boolean{
//        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
//            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED
//        } else true
//    }
}