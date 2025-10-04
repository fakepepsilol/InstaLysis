package rs.fpl.instalysis.hookers

import android.annotation.SuppressLint
import android.content.Context
import io.github.libxposed.api.XposedModuleInterface

object HookUtils {
    @SuppressLint("PrivateApi")
    fun getVersionCode(param: XposedModuleInterface.PackageLoadedParam): Long{
        val currentActivityThread = Class.forName("android.app.ActivityThread", false, param.classLoader).getMethod("currentActivityThread").invoke(null)
        val context: Context = Class.forName("android.app.ActivityThread", false, param.classLoader).getMethod("getSystemContext").invoke(currentActivityThread) as Context
        return context.packageManager.getPackageInfo(param.packageName, 0).longVersionCode
    }
}