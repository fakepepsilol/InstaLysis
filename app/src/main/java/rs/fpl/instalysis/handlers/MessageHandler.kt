package rs.fpl.instalysis.handlers

import android.os.Bundle
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

inline fun <T> tryOrNull(block: () -> T): T? =
    try { block() } catch (_: Exception) { null }

object MessageHandler{
    val tag = "FPL_MessageHandler"
    fun handleMessage(bundle: Bundle){
        val obj = bundle.getString("serializedMessage")!!
        Log.w(tag, obj)
        val jsonData: String? = Regex(""".+obj=(?<jsonData>\[\{.+\}]).+\}.*""").find(obj)?.groups["jsonData"]?.value
        if(jsonData == null){ return }


        // crashes sometimes idk why

        val dataObject = JSONObject(JSONArray(jsonData).getJSONObject(0).getJSONArray("data").getJSONObject(0).getString("value"))
        Log.i(tag, dataObject.toString())
    }
}
