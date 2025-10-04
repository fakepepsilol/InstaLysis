package rs.fpl.instalysis.handlers

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

inline fun <T> tryOrNull(block: () -> T): T? =
    try { block() } catch (e: Exception) {
        Log.e("FPL_TryOrNull", e.toString())
        null
    }

object MessageHandler{
    const val TAG = "FPL_MessageHandler"
    var id = 0
    fun handleEvent(bundle: Bundle, context: Context) {
        val message = bundle.getString("serializedMessage")!!
        Log.d(TAG, message)

        if (message.contains("what=2")) return
        context.openFileOutput("${id++}.txt", Context.MODE_PRIVATE).apply {
            write(message.encodeToByteArray())
            close()
        }
        Log.e(TAG, "written: $id.txt")
        val json: String? = Regex("""\[\{.+\}]""").find(message)?.value
        json ?: return fail(context, "json is null")
        Log.d(TAG, "json: $json")

        val eventArray: JSONArray = tryOrNull { JSONArray(json) }
            ?: return fail(context, "eventArray is null")

        for (i in 0..<eventArray.length()) {
            val event = eventArray.getJSONObject(i)
            val eventType = tryOrNull { event.getString("event") }
                ?: return fail(context, "failed to get the EVENT TYPE")
            val dataArray: JSONArray = tryOrNull { event.getJSONArray("data") }
                ?: return fail(context, "failed to get the DATA ARRAY")
            for(j in 0..<dataArray.length()) {
                val data = tryOrNull { dataArray.getJSONObject(j) }
                    ?: return fail(context, "failed to get the DATA")
                val eot = getType(eventType, data, context)
                    ?: return
                Log.i(TAG, eot)
                toast(context, parseEOT(eot) ?: return)
            }
        }
    }

    fun getType(eventType: String, data: JSONObject, context: Context): String? {

        val operationType: String = tryOrNull { data.getString("op") }
            ?: return fain(context, "failed to get the OPERATION TYPE")
        val path: String = tryOrNull { data.getString("path") }
            ?: return fain(context, "failed to get the PATH")

        if(eventType == "patch"){
            when (operationType) {
                "remove" -> {
                    if(path.contains("/reactions/likes")) return "patch|remove|reaction"
                    return "patch|remove"
                }
                "replace" -> {
                    if(path.contains("has_seen")){
                        return "patch|replace|seen"
                    }
                    if (path.contains("update_media_interventions")) {
                        return null
                    }
                }
                "add" -> {
                    if(path.contains("/reactions/likes")) return "patch|add|reaction"
                }
            }
        }

        val valueStr = tryOrNull { data.getString("value") }
            ?: return fain(context, "failed to get the DATA VALUE")

        val value: JSONObject = tryOrNull { JSONObject(valueStr) }
            ?: return fain(context, "failed to deserialize the VALUE STRING")

        val itemType: String = tryOrNull { value.getString("item_type") }
            ?: return fain(context, "failed to get the ITEM TYPE")

        // event-operation-type ("$eventType|$operationType|$itemType")
        return "$eventType|$operationType|$itemType"
    }
    fun parseEOT(eot: String): String?{
        return when(eot){
            "patch|add|text" -> "text"
            "patch|add|reaction" -> "reaction"
            "patch|add|voice_media" -> "voice message"
            "patch|add|animated_media" -> "gif"
            "patch|add|media" -> "image/video"
            "patch|add|generic_xma" -> "multiple images/videos"
            "patch|add|xma_media_share" -> "post"
            "patch|add|xma_clip" -> "reel"
            "patch|add|xma_profile" -> "reel"
            "patch|add|xma_reel_share" -> "story response?"
            "patch|add|action_log" -> "i have no clue, notification?"
            "patch|replace|seen" -> "seen"

            "patch|remove" -> "deletion"
            "patch|remove|reaction" -> "reaction deleted"
            else -> "unknown"
        }
    }



    fun fain(context: Context, message: String): Nothing?{
        toast(context, message)
        return null
    }
    fun fail(context: Context, message: String){
        toast(context, message)
    }
    fun toast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}