package rs.fpl.instalysis.background

import android.content.Intent
import android.content.SharedPreferences
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.content.edit
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

object Preferences{

    var prefsManager: PrefsManager? = null
    private var deferred: CompletableDeferred<XposedService>? = null
    var service: XposedService? = null

    suspend fun getXposedService(): XposedService{
        service?.let { return it }
        deferred?.let { return it.await() }
        val newDeferred = CompletableDeferred<XposedService>()
        deferred = newDeferred

        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener{
            override fun onServiceBind(service: XposedService) {
                newDeferred.complete(service)
                Preferences.service = service
            }

            override fun onServiceDied(service: XposedService) {
                deferred = null
                prefsManager?.cleanup()
                prefsManager = null
            }
        })
        return newDeferred.await()
    }
    suspend fun getPreferences(): PrefsManager {
        prefsManager?.let { return it }
        val service = getXposedService()
        prefsManager = PrefsManager(service)
        return prefsManager!!
    }
    suspend fun getChatFile(chatId: String): ParcelFileDescriptor{
        val service = getXposedService()
        return service.openRemoteFile(chatId)
    }
}

class PrefsManager{
    private val tag = "FPL_PrefsManager"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val intentChannel: Channel<Intent> = Channel(Channel.UNLIMITED)

    private val service: XposedService
    constructor(service: XposedService){
        this.service = service
    }

    init {
        scope.launch {
            for(intent in intentChannel){
                processIntent(intent)
            }
        }
    }


    private var prefs: SharedPreferences? = null
    private fun getPrefs(): SharedPreferences {
        prefs?.let { return it }
        service.getRemotePreferences("rs.fpl.instalysis").let { prefs = it; return it }
    }

    private fun processIntent(intent: Intent){
        if(Preferences.prefsManager == null || Preferences.service == null){
            Log.e(tag, "Failed saving preferences. Service not running.")
            return
        }
        val key = intent.getStringExtra("key")
        val value = intent.getStringExtra("value")

        getPrefs().edit(commit = false) {
            putString(key, value)
        }
        Log.i(tag, "Saved $key: $value")
    }
    suspend fun addIntent(intent: Intent){
        intentChannel.send(intent)
    }
    fun cleanup(){
        intentChannel.close()
        scope.cancel()
    }
}