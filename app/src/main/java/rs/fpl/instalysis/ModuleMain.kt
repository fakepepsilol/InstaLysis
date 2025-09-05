package rs.fpl.instalysis

import android.annotation.SuppressLint
import android.app.Application
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


    @XposedHooker
    class MyHooker(private val magic: Int) : XposedInterface.Hooker{
        companion object{

            @JvmStatic
            @BeforeInvocation
            fun before(callback: XposedInterface.BeforeHookCallback){
                val context: Context = callback.args[0] as Context
                Toast.makeText(context, "hello world", Toast.LENGTH_SHORT).show()
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
        val attachMethod = Application::class.java.getDeclaredMethod("attach", Context::class.java)
        hook(attachMethod, MyHooker::class.java)

//        Log.d("WHY", "NIGGER")

//        super.onPackageLoaded(param)
//
    }
}