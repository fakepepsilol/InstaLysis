package rs.fpl.instalysis.background

import android.content.Intent
import android.os.Handler
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Preferences{
    var prefsManager: PrefsManager? = null
    private var deferred: CompletableDeferred<PrefsManager>? = null

    suspend fun getPreferences(): PrefsManager {
        prefsManager?.let { return prefsManager!! }
        deferred?.let { return it.await() }

        val newDeferred = CompletableDeferred<PrefsManager>()
        deferred = newDeferred

        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener{
            override fun onServiceBind(service: XposedService) {
                prefsManager = PrefsManager(service)
                newDeferred.complete(prefsManager!!)
            }

            override fun onServiceDied(service: XposedService) {
                prefsManager?.cleanup()
                prefsManager = null
                deferred = null
            }
        })
        return newDeferred.await()
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
    private fun processIntent(intent: Intent){
        service.getRemotePreferences("rs.fpl.instalysis").edit{
            val key = intent.getStringExtra("key")
            val value = intent.getStringExtra("value")
            putString(key, value)
            Log.i(tag, "Saved $key: $value")
        }
    }
    suspend fun addIntent(intent: Intent){
        intentChannel.send(intent)
    }
    fun cleanup(){
        intentChannel.close()
        scope.cancel()
    }
}