package rs.fpl.instalysis.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rs.fpl.instalysis.background.XposedScope

class UpdatePrefs: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val tag = "FPL_UpdatePrefsBroadcastReceiver"
        val expectedAction = "rs.fpl.instalysis.UPDATE_PREFS"
        Log.i(tag, "BroadCastReceiver.onReceive")
        if(intent == null){
            Log.e(tag, "Intent is null.")
            return
        }
        if(intent.action != expectedAction){
            Log.e(tag, "Wrong action: expected: \"${expectedAction}\" actual value: \"${intent.action}\".")
            return
        }
        if(intent.getStringExtra("key") == null || intent.getStringExtra("value") == null){
            Log.e(tag, "Broadcast sent without key/value parameters.")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            XposedScope.awaitPrefsManager().addIntent(intent)
        }
    }

}