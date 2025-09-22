package rs.fpl.instalysis.receivers.instagram

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import rs.fpl.instalysis.hookers.HandleMessageHooker

class AskForPermsReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("FPL_Instagram_AskForPermsReceiver", "onReceive called")
        HandleMessageHooker.Companion.context?.let{
            val intent2 = Intent()
            intent2.component = ComponentName("rs.fpl.instalysis", "rs.fpl.instalysis.permissions.PermissionActivity")
            intent2.setFlags(FLAG_ACTIVITY_NEW_TASK)
            it.startActivity(intent2)
        }
    }
}