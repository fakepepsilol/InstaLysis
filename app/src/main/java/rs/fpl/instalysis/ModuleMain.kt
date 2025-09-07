package rs.fpl.instalysis

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker


class ModuleMain(base: XposedInterface, params: XposedModuleInterface.ModuleLoadedParam): XposedModule(base, params){

    companion object{
        var context: Context? = null
    }
    @XposedHooker
    class MyHooker(private val magic: Int) : XposedInterface.Hooker{
        companion object{

            @JvmStatic
            @BeforeInvocation
            fun before(callback: XposedInterface.BeforeHookCallback){
                if(context == null){
                    context = callback.args[0] as Context
                    try {
                        context?.packageName.equals("")
                    }catch (e: Exception){
                        context = null
                        Log.e("nigga", "wrong thingy")
                    }
                    return
                }
//                val context: Context = callback.args[0] as Context
                Toast.makeText(context, "Activity started: " + (callback.thisObject as Activity)::class.qualifiedName.toString(), Toast.LENGTH_SHORT).show()
                Log.d("nigga", (callback.thisObject as Activity)::class.qualifiedName.toString())
            }
            @JvmStatic
            @AfterInvocation
            fun after(callback: XposedInterface.AfterHookCallback){

            }
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        log("Package loaded: ${param.packageName}")
//        if(param.packageName != "mobi.globaltel.selfcare"){
//            return
//        }
        val attachMethod = Class.forName("android.app.Application").getDeclaredMethod("attach", Context::class.java)
        val onCreateMethod = Class.forName("android.app.Activity").getDeclaredMethod("onCreate", Class.forName("android.os.Bundle"))
        hook(onCreateMethod, MyHooker::class.java)
        hook(attachMethod, MyHooker::class.java)

    }
}