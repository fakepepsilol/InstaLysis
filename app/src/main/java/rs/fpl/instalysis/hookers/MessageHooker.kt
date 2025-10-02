@file:Suppress("unused")

package rs.fpl.instalysis.hookers

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rs.fpl.instalysis.background.XposedScope
import rs.fpl.instalysis.background.instagram.ServiceHelper

@XposedHooker
class MessageHooker() : XposedInterface.Hooker{
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
            ServiceHelper.sendMessage(ServiceHelper.HANDLE_MESSAGE, bundle as Object)
        }
        @JvmStatic
        @AfterInvocation
        fun after(callback: XposedInterface.AfterHookCallback){

        }
    }
}