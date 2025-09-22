@file:Suppress("unused")

package rs.fpl.instalysis.hookers

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Message
import android.util.Log
import android.widget.Toast
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.text.StringEscapeUtils
import rs.fpl.instalysis.ModuleMain

@XposedHooker
class HandleMessageHooker() : XposedInterface.Hooker{
    companion object{
        @Suppress("ConstPropertyName")
        const val tag = "FPL_HandleMessageHooker"
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
        init {
            CoroutineScope(Dispatchers.Main).launch {
                context = ModuleMain.getContext()
            }
        }

        @JvmStatic
        @BeforeInvocation
        fun before(callback: XposedInterface.BeforeHookCallback){
            if(context == null){
                return
            }
            val message = callback.args[0] as Message
            if(!message.toString().contains(Regex("""obj=.*\{.+ target"""))){
                return
            }
            val intent = Intent("rs.fpl.instalysis.MESSAGE_RECEIVE").apply {
                setPackage("rs.fpl.instalysis")
                setComponent(ComponentName("rs.fpl.instalysis", "rs.fpl.instalysis.receivers.MessageReceiver"))
                putExtra("serializedMessage", message.toString())
            }
            context?.sendBroadcast(intent)
            Log.i(tag, "Sent MESSAGE_RECEIVE broadcast")


        }
        @JvmStatic
        @AfterInvocation
        fun after(callback: XposedInterface.AfterHookCallback){

        }
    }
}