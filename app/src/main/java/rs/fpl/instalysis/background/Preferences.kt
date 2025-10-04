package rs.fpl.instalysis.background

import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Preferences {
    const val TAG = "FPL_Preferences"
    fun processBundle(bundle: Bundle) {
        CoroutineScope(Dispatchers.IO).launch {
            val preferences = XposedScope.awaitPreferences()
            preferences.edit {
                for(key in bundle.keySet()) {
                    @Suppress("deprecation") // not deprecated, just "not type-safe"
                    val value: Any? = bundle.get(key)
                    Log.i(TAG, "Saving: $key -> $value")
                    when(value){
                        is Int -> { putInt(key, value) }
                        is Long -> { putLong(key, value) }
                        is String -> { putString(key, value) }
                        is Boolean -> { putBoolean(key, value) }
                    }
                    Log.i(TAG, "Saved.")
                }
            }
        }
    }
}