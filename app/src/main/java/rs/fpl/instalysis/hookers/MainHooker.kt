package rs.fpl.instalysis.hookers

import android.app.Activity
import android.content.Context
import android.os.Message
import android.util.Log
import android.widget.Toast
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import rs.fpl.instalysis.ModuleMain
import org.apache.commons.text.StringEscapeUtils

@XposedHooker
class HandleMessageHooker() : XposedInterface.Hooker{
    companion object{
        private val tag = "FPL_HandleMessageHooker"
        @JvmStatic
        @BeforeInvocation
        fun before(callback: XposedInterface.BeforeHookCallback){
            val textPattern = Regex("""\\"text\\":\\"(.+)\\",""")
            if((callback.args[0] as Message).obj == null){
                return
            }
            val escapedMessage: String = (callback.args[0] as Message).obj.toString()

            Log.d(tag, (callback.args[0] as Message).toString())
            val textMatch: MatchGroup? = textPattern.find(escapedMessage)?.groups[1]
            val textContents: String? = textMatch?.value
            if(textContents != null) {
                val message = StringEscapeUtils.unescapeJava(StringEscapeUtils.unescapeJava(textContents))
                Log.i(tag, message)
                Toast.makeText(ModuleMain.context!!, message, Toast.LENGTH_SHORT).show()
            }
        }
        @JvmStatic
        @AfterInvocation
        fun after(callback: XposedInterface.AfterHookCallback){

        }
    }
}