@file:SuppressLint("PrivateApi", "DiscouragedPrivateApi")
package rs.fpl.instalysis

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import rs.fpl.instalysis.hookers.ActivityHooker
import rs.fpl.instalysis.hookers.ContextHooker
import rs.fpl.instalysis.hookers.MessageHooker

class ModuleMain(private val base: XposedInterface, params: XposedModuleInterface.ModuleLoadedParam): XposedModule(base, params){

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

        MessageHooker.hook(base, param)
    }
}