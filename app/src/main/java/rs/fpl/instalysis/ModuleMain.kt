@file:SuppressLint("PrivateApi", "DiscouragedPrivateApi")
package rs.fpl.instalysis

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.MethodData
import rs.fpl.instalysis.background.XposedScope
import rs.fpl.instalysis.hookers.ActivityHooker
import rs.fpl.instalysis.hookers.ContextHooker
import rs.fpl.instalysis.hookers.MessageHooker
import java.lang.reflect.Method

class ModuleMain(base: XposedInterface, params: XposedModuleInterface.ModuleLoadedParam): XposedModule(base, params){

    companion object{
        init{
            System.loadLibrary("dexkit")
        }
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        if(param.packageName != "com.instagram.android"){
            return
        }

        val onCreateMethod = Class.forName("android.app.Activity").getDeclaredMethod("onCreate", Bundle::class.java)
        hook(onCreateMethod, ActivityHooker::class.java)

        val attachMethod = Class.forName("android.app.Application").getDeclaredMethod("attach", Context::class.java)
        hook(attachMethod, ContextHooker::class.java)

        cachedHandleMessageMethodHook(param)
    }
    private fun cachedHandleMessageMethodHook(param: XposedModuleInterface.PackageLoadedParam){
        val tag = "FPL_cachedHandleMessageMethodHook"


        val apkPath = param.applicationInfo.sourceDir
        val preferences = getRemotePreferences("rs.fpl.instalysis")
        val currentVersionCode: Long = getVersionCode(param)
        val previousVersionCode: Long? = preferences.getString("cachedVersionCode", "0")?.toLong()
        val cachedClassName: String? = preferences.getString("cachedHandleMessageClassName", null)
        val needsRefreshing = previousVersionCode == null || cachedClassName == null || currentVersionCode != previousVersionCode

        val method: Method = if(needsRefreshing){
            Log.d(tag, "Attempting to find the .handleMessage method")
            val ret = DexKitBridge.create(apkPath).use { bridge ->
                return@use findHandleMessageMethod(bridge, param)
            }
            updatePrefs("cachedHandleMessageClassName", ret.declaringClass.name)
            updatePrefs("cachedVersionCode", currentVersionCode.toString())
            ret
        } else {
            Log.d(tag, "Reusing method from cached class: $cachedClassName")
            Class.forName(cachedClassName, false, param.classLoader).getMethod("handleMessage",
                Message::class.java)
        }
        Log.d(tag, "Found method: $method")
        hook(method, MessageHooker::class.java)
    }
    private fun findHandleMessageMethod(bridge: DexKitBridge, param: XposedModuleInterface.PackageLoadedParam): Method {
        val hookClassData = bridge.findClass {
            searchPackages("X")
            matcher {
                usingStrings("Invalid message.what: ", "Required value was null.")
            }
        }.singleOrNull() ?: error("The class is not unique.")

        val handleMessageMethod: MethodData? = hookClassData.methods.find { it ->
            it.methodName.contains("handleMessage")
        }
        if(handleMessageMethod == null){
            throw Exception("Failed to find the .handleMessage method.")
        }
        Log.d("FPL_FHMM", handleMessageMethod.className)
        return handleMessageMethod.getMethodInstance(param.classLoader)
    }
    private fun updatePrefs(key: String, value: String){
        CoroutineScope(Dispatchers.IO).launch{
            val intent = Intent("rs.fpl.instalysis.UPDATE_PREFS").apply {
                setPackage("rs.fpl.instalysis")
                setComponent(ComponentName("rs.fpl.instalysis", "rs.fpl.instalysis.receivers.UpdatePrefs"))
                putExtra("key", key)
                putExtra("value", value)
            }
            CoroutineScope(Dispatchers.Default).launch {
                XposedScope.awaitContext().sendBroadcast(intent)
                Log.e("FPL", "sent broadcast")
            }
        }
    }
    private fun getVersionCode(param: XposedModuleInterface.PackageLoadedParam): Long{
        val currentActivityThread = Class.forName("android.app.ActivityThread", false, param.classLoader).getMethod("currentActivityThread").invoke(null)
        val context: Context = Class.forName("android.app.ActivityThread", false, param.classLoader).getMethod("getSystemContext").invoke(currentActivityThread) as Context
        return context.packageManager.getPackageInfo(param.packageName, 0).longVersionCode
    }
}