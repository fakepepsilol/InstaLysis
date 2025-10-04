package rs.fpl.instalysis.background

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.coroutines.CompletableDeferred


@Suppress("unused", "ObjectPropertyName")
@SuppressLint("StaticFieldLeak")
object XposedScope{

    init {
        XposedServiceHelper.registerListener(object: XposedServiceHelper.OnServiceListener{
            override fun onServiceBind(service: XposedService) {
                setXposedService(service)
                setPreferences(service.getRemotePreferences("rs.fpl.instalysis"))
            }

            override fun onServiceDied(service: XposedService) {
//                cleanup()
            }
        })
    }
    private val contextDeferred = CompletableDeferred<Context>()
    var _context: Context? = null

    private val activityDeferred = CompletableDeferred<Activity>()
    var _activity: Activity? = null
    private val preferencesDeferred = CompletableDeferred<SharedPreferences>()
    var _preferences: SharedPreferences? = null


    private val xposedServiceDeferred = CompletableDeferred<XposedService>()
    var _xposedService: XposedService? = null

    suspend fun awaitContext(): Context{
        _context?.let { return it }
        return contextDeferred.await()
    }
    fun setContext(context: Context){
        _context = context
        contextDeferred.complete(context)
    }


    suspend fun awaitActivity(): Activity {
        _activity?.let { return it }
        return activityDeferred.await()
    }
    fun setActivity(activity: Activity) {
        _activity = activity
        activityDeferred.complete(activity)
    }


    suspend fun awaitPreferences(): SharedPreferences {
        _preferences?.let { return it }
        return preferencesDeferred.await()
    }
    fun setPreferences(preferences: SharedPreferences) {
        _preferences = preferences
        preferencesDeferred.complete(preferences)
    }


    suspend fun awaitXposedService(): XposedService {
        _xposedService?.let { return it }
        return xposedServiceDeferred.await()
    }
    fun setXposedService(service: XposedService) {
        _xposedService = service
        xposedServiceDeferred.complete(service)
    }
}