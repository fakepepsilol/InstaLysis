package rs.fpl.instalysis.receivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import kotlin.jvm.Throws


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
//        val serializedMessage: String = StringEscapeUtils.unescapeJava(serializedMessage)
        val jsonData: String? = Regex(""".+obj=(?<jsonData>\[\{.+\}]).+\}.*""").find(serializedMessage)?.groups["jsonData"]?.value
//        if(jsonData == null){
//            Log.e(tag, "jsonData is null.")
//        }
        jsonData?.let { jsonData ->
            val jsonArray = JSONArray(jsonData)
            Log.w(tag, "jsonArray.length: ${jsonArray.length()}")
            val jsonObject = jsonArray.getJSONObject(0)
            val dataObject =
                JSONObject(jsonObject.getJSONArray("data").getJSONObject(0).getString("value"))
            Log.e(tag, dataObject.toString())
            context?.let { ctx ->
                handleMessage(jsonObject, ctx)
            }
        }

    }
    fun notificationPermissionGranted(context: Context?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }
    inline fun <T> tryOrNull(block: () -> T): T? =
        try { block() } catch (_: Exception) { null }


    @OptIn(ExperimentalSerializationApi::class)
    fun handleMessage(jsonObject: JSONObject, context: Context) {

        val dataObject = jsonObject.getJSONArray("data").getJSONObject(0)

        val path = dataObject.getString("path")
        val chatId: String? = tryOrNull { Regex("/(?<chatId>[0-9]+)/").find(path)?.groups["chatId"]?.value }
        val shortChatId: String? = chatId?.substring(chatId.length - 10)
        val textObject = JSONObject(dataObject.getString("value"))

        val senderId: Long? = tryOrNull { textObject.getLong("user_id") }
        val text: String? = tryOrNull { textObject.getString("text") } ?: tryOrNull{ textObject.getJSONObject("link").getString("text") }
        if(text?.toCharArray()[0] != '$'){
            return
        }

        when{
            t(text, "me") -> {
                val file = openFileOut("ownerId.txt", context)
                file.write((senderId.toString().encodeToByteArray()))
                file.close()
                toast("new owner: $senderId", context)
            }
            t(text, arrayOf("ow", "ownerId", "owie")) -> {
                try {
                    val file = openFileIn("ownerId.txt", context)
                    val ownerId = file.readBytes().decodeToString()
                    toast("owner: $ownerId", context)
                    file.close()
                }catch (_: FileNotFoundException){
                    toast("no owner yet.", context)
                }
            }
            t(text, arrayOf("ci", "chatId", "chat", "id", "path")) -> {
                toast("Chat Id: $chatId", context)
            }
            t(text, arrayOf("status")) -> {
                toast("Status: Operational.", context)
            }
            t(text, arrayOf("fn", "filename", "fd")) -> {
                val parts = text.replace(Regex("\\s+"), " ").split(' ')
                if(parts.count() > 2){
                    toast("Usage: \"fn <filename>(.txt)\"", context)
                    return
                }
                chatId?.let { id ->
                    if(parts.count() == 1){
                        try {
                            val idToFile: Map<String,String> = openFileIn("chatIdsToFiles.json", context).use { stream ->
                                Json.decodeFromStream<Map<String, String>>(stream)
                            }
                            if(idToFile[id] != null){
                                toast("Chat: ...$shortChatId -> FileName: ${idToFile[id]}", context)
                            }else{
                                toast("No file associated with this chat.", context)
                            }
                        } catch (_: Exception){
                            toast("No file associated with this chat.", context)
                        }
                        return
                    }


                    val filename = if(parts[1].contains('.')) parts[1] else "${parts[1]}.txt"

                    val idToFile: MutableMap<String, String> = try {
                        openFileIn("chatIdsToFiles.json", context).use { stream ->
                            Json.decodeFromStream<MutableMap<String, String>>(stream)
                        }
                    } catch (_: FileNotFoundException){
                        mutableMapOf()
                    }
                    idToFile[id] = filename
                    Log.e(tag, idToFile.toString())
                    openFileOut("chatIdsToFiles.json", context).use { stream ->
                        Json.encodeToStream<Map<String, String>>(idToFile, stream)
                    }
                    toast("Chat: ...$shortChatId -> FileName: $filename", context)
                }
            }

        }
    }
    @Throws(FileNotFoundException::class)
    fun openFileIn(filename: String, context: Context): FileInputStream{
        return context.openFileInput(filename)
    }
    fun openFileOut(filename: String, context: Context): FileOutputStream{
        return context.openFileOutput(filename, Context.MODE_PRIVATE)
    }
    fun t(text: String?): String?{
        return text?.split(' ')[0]?.trim()?.lowercase()
    }
    fun t(text: String?, command: String): Boolean{
        return t(text) == t("$$command")
    }
    fun t(text: String?, commands: Array<String>): Boolean{
        commands.forEach { cmd ->
            if(t(text) == t("$$cmd")){
                return true
            }
        }
        return false
    }
    fun toast(message: String, context: Context){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}