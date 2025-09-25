package rs.fpl.instalysis.background

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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


@Suppress("unused", "ObjectPropertyName")
@SuppressLint("StaticFieldLeak")
object XposedScope{

    init {
        XposedServiceHelper.registerListener(object: XposedServiceHelper.OnServiceListener{
            override fun onServiceBind(service: XposedService) {
                setXposedService(service)
                setPreferences(PrefsManager(service))
            }

            override fun onServiceDied(service: XposedService) {
                cleanup()
            }
        })
    }
    private var contextDeferred = CompletableDeferred<Context>()
    var _context: Context? = null

    private var prefsManagerDeferred = CompletableDeferred<PrefsManager>()
    var _prefsManager: PrefsManager? = null


    private var xposedServiceDeferred = CompletableDeferred<XposedService>()
    var _xposedService: XposedService? = null

    fun cleanup(){
        prefsManagerDeferred = CompletableDeferred()
        _prefsManager = null
        xposedServiceDeferred = CompletableDeferred()
        _xposedService = null
    }

    suspend fun awaitContext(): Context{
        _context?.let { return it }
        return contextDeferred.await()
    }
    fun setContext(context: Context){
        this._context = context
        contextDeferred.complete(context)
    }


    suspend fun awaitPrefsManager(): PrefsManager{
        _prefsManager?.let { return it }
        return prefsManagerDeferred.await()
    }
    fun setPreferences(prefsManager: PrefsManager){
        this._prefsManager = prefsManager
        prefsManagerDeferred.complete(prefsManager)
    }


    suspend fun awaitXposedService(): XposedService{
        _xposedService?.let { return it }
        return xposedServiceDeferred.await()
    }
    fun setXposedService(service: XposedService){
        this._xposedService = service
        xposedServiceDeferred.complete(service)
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
        if(XposedScope._prefsManager == null || XposedScope._xposedService == null){
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