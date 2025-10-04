@file:Suppress("unused")

package rs.fpl.instalysis.hookers

import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.core.os.bundleOf
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.MethodData
import rs.fpl.instalysis.background.instagram.ServiceHelper
import rs.fpl.instalysis.background.instagram.ServiceMessageType
import java.lang.reflect.Method


object MessageHooker{
    const val TAG = "FPL_MessageHooker"
    fun hook(base: XposedInterface, param: XposedModuleInterface.PackageLoadedParam){
        val method: Method = findMethod(base, param) ?: run{
            Log.e(TAG, "MessageHooker failed.")
            return
        }
        base.hook(method, Hooker::class.java)
        Log.i(TAG, "Hooked: $method")
    }
    private fun findMethod(base: XposedInterface, param: XposedModuleInterface.PackageLoadedParam): Method?{

        val preferences = base.getRemotePreferences("rs.fpl.instalysis")

        val currentVersionCode: Long = HookUtils.getVersionCode(param)
        val cachedVersionCode: Long = preferences.getLong("versionCode", 0L)
        if(currentVersionCode == cachedVersionCode){
            preferences.getString("handleMessageClass", null)?.let{
                return Class.forName(it, false, param.classLoader)
                    .getDeclaredMethod("handleMessage", Message::class.java)
            }
        }

        ServiceHelper.sendMessage(
            ServiceMessageType.UPDATE_PREFERENCES.id,
            bundleOf(Pair("versionCode", currentVersionCode)))

        val apkPath = param.applicationInfo.sourceDir
        return DexKitBridge.create(apkPath).use { bridge ->
            val methodData = findMethodData(bridge) ?: return null
            methodData.getMethodInstance(param.classLoader)
        }
    }
    private fun findMethodData(bridge: DexKitBridge): MethodData?{
        return bridge.findMethod {
            searchPackages("X")

            matcher {
                name = "handleMessage"
                usingStrings("Invalid message.what: ", "Required value was null.")
            }
        }.singleOrNull() ?: run {
            Log.e("FPL_MessageHooker", "Multiple 'handleMessage' methods found.")
            return null
        }
    }
    @XposedHooker
    class Hooker : XposedInterface.Hooker{
        companion object{
            @Suppress("ConstPropertyName")
            const val tag = "FPL_HandleMessageHooker"

            @JvmStatic
            @BeforeInvocation
            fun before(callback: XposedInterface.BeforeHookCallback){
                val message = callback.args[0] as Message
                if(!message.toString().contains(Regex("""obj=.*\{.+ target"""))){
                    return
                }
                val bundle = Bundle().apply {
                    putString("serializedMessage", message.toString())
                }
                ServiceHelper.sendMessage(ServiceMessageType.HANDLE_MESSAGE.id, bundle as Object)
            }
        }
    }
}