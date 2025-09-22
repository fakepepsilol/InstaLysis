package rs.fpl.instalysis

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.ClassData
import org.luckypray.dexkit.result.MethodData
import rs.fpl.instalysis.hookers.HandleMessageHooker
import rs.fpl.instalysis.receivers.instagram.AskForPermsReceiver
import java.lang.reflect.Method


class ModuleMain(base: XposedInterface, params: XposedModuleInterface.ModuleLoadedParam): XposedModule(base, params){

    companion object{
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
        val contextDeferred = CompletableDeferred<Context>()
        suspend fun getContext(): Context {
            context?.let{return it}
            context = contextDeferred.await()
            return context!!
        }

        init{
            System.loadLibrary("dexkit")
        }
        var classData: ClassData? = null
    }


    @Suppress("unused")
    @XposedHooker
    class MyHooker() : XposedInterface.Hooker{
        companion object{

            @JvmStatic
            @BeforeInvocation
            fun before(callback: XposedInterface.BeforeHookCallback){
                Log.e("FPL_MyHooker", "${callback.member.name} called")
                if(callback.member.name == "attach"){
                    val context = callback.args[0] as Context
                    contextDeferred.complete(context)
                    ModuleMain.context = context
                    return
                }
                Toast.makeText(context, "Activity started: " + (callback.thisObject as Activity)::class.qualifiedName.toString(), Toast.LENGTH_SHORT).show()
                Log.d("FPL_MyHooker", "Activity started: ${(callback.thisObject as Activity)::class.qualifiedName.toString()}")
            }
            @JvmStatic
            @AfterInvocation
            fun after(callback: XposedInterface.AfterHookCallback){

            }
        }
    }

    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        log("Package loaded: ${param.packageName}")

        val attachMethod = Class.forName("android.app.Application").getDeclaredMethod("attach", Context::class.java)
        val onCreateMethod = Class.forName("android.app.Activity").getDeclaredMethod("onCreate", Class.forName("android.os.Bundle"))
        hook(onCreateMethod, MyHooker::class.java)
        hook(attachMethod, MyHooker::class.java)



        if(param.packageName != "com.instagram.android"){
            return
        }

        cachedHandleMessageMethodHook(param)


        CoroutineScope(Dispatchers.Main).launch {
            val filter = IntentFilter(
                "rs.fpl.instalysis.ASK_FOR_PERMISSIONS"
            )
            ContextCompat.registerReceiver(
                getContext(),
                AskForPermsReceiver(),
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            Log.d("FPL_OnPackageLoaded", "registered broadcast receiver in the instagram app.")
        }
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
        hook(method, HandleMessageHooker::class.java)
    }
    private fun findHandleMessageMethod(bridge: DexKitBridge, param: XposedModuleInterface.PackageLoadedParam): Method {
        val hookClassData = bridge.findClass {
            searchPackages("X")
            matcher {
                usingStrings("Invalid message.what: ", "Required value was null.")
            }
        }.singleOrNull() ?: error("The class is not unique.")
        classData = hookClassData

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
            while(context == null){
                Thread.sleep(250)
                Log.d("FPL", "waiting")
            }
            context?.sendBroadcast(intent)
            Log.e("FPL", "sent broadcast")
        }
    }
    @SuppressLint("PrivateApi")
    private fun getVersionCode(param: XposedModuleInterface.PackageLoadedParam): Long{
        val currentActivityThread = Class.forName("android.app.ActivityThread", false, param.classLoader).getMethod("currentActivityThread").invoke(null)
        val context: Context = Class.forName("android.app.ActivityThread", false, param.classLoader).getMethod("getSystemContext").invoke(currentActivityThread) as Context
        return context.packageManager.getPackageInfo(param.packageName, 0).longVersionCode
    }
}