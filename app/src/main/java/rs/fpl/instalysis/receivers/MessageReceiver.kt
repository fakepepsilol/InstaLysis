package rs.fpl.instalysis.receivers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.apache.commons.text.StringEscapeUtils
import rs.fpl.instalysis.permissions.PermissionActivity


class MessageReceiver: BroadcastReceiver() {
    val tag = "FPL_MessageReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {

        if(intent == null){
            Log.e(tag, "intent is null")
            return
        }
        if(intent.action != "rs.fpl.instalysis.MESSAGE_RECEIVE"){
            Log.e(tag, "wrong intent.action")
            return
        }
        if(intent.getStringExtra("serializedMessage") == null){
            Log.e(tag, "no serializedMessage passed")
            return
        }
        val serializedMessage: String = intent.getStringExtra("serializedMessage")!!
        if(!notificationPermissionGranted(context)){
            val intent = Intent().apply {
                setPackage("com.instagram.android")
                setAction("rs.fpl.instalysis.ASK_FOR_PERMISSIONS")
            }
            context!!.sendBroadcast(intent)
        }
        processMessage(serializedMessage, context)
    }
    fun processMessage(serializedMessage: String, context: Context?){
        Log.d(tag, serializedMessage)
        val chatId = Regex("""/direct_v2/threads/(?<ChatId>\d+)/""").find(serializedMessage)?.groups["ChatId"]?.value
        Log.d(tag, "chatId: $chatId")
        val messageContents: String? = Regex(""",\\"text\\":\\"(.+)\\",""").find(serializedMessage)?.groups[1]?.value

        context?.let { ctx ->
            messageContents?.let { msg ->
                val msg = StringEscapeUtils.unescapeJava(StringEscapeUtils.unescapeJava(msg))
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun notificationPermissionGranted(context: Context?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }
}