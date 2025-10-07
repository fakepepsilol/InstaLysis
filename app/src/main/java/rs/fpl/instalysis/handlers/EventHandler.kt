package rs.fpl.instalysis.handlers

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

inline fun <T> tryOrNull(block: () -> T): T? =
    try { block() } catch (e: Exception) {
        Log.e("FPL_TryOrNull", e.toString())
        null
    }

object EventHandler{
    const val TAG = "FPL_MessageHandler"
    fun handleEvent(bundle: Bundle, context: Context) {
        val message = bundle.getString("serializedMessage")!!
        Log.d(TAG, message)

        if (message.contains("what=2")) return

        val json: String? = Regex("""\[\{.+\}]""").find(message)?.value
        json ?: return toast(context, "json is null")
        Log.d(TAG, "json: $json")


        val events = JSONArray(json)
        for(i in 0..<events.length()){
            val event = Event(events.getJSONObject(i))
            for(operation in event.operations){
                Log.i(TAG, "EOT: ${operation.getEOT()}")
                when(operation.eot){
                    else -> {
                        toast(context, operation.getEOT())
                        logJson(context, json)
                    }
                }
            }
        }
    }


//            "patch|add|text" -> "text"
//            "patch|add|reaction" -> "reaction"
//            "patch|add|voice_media" -> "voice message"
//            "patch|add|animated_media" -> "gif"
//            "patch|add|media" -> "image/video"
//            "patch|add|generic_xma" -> "multiple images/videos or a cutout (collision)"
//            "patch|add|xma_media_share" -> "post"
//            "patch|add|xma_clip" -> "reel"
//            "patch|add|xma_profile" -> "reel"
//            "patch|add|xma_reel_share" -> "story response?"
//            "patch|add|action_log" -> "i have no clue, notification?"
//            "patch|replace|has_seen" -> "seen"
//
//            "patch|remove" -> "deletion"
//            "patch|remove|reaction" -> "reaction deleted"
//            else -> "unknown"

    var fileId: Int = 0
    private fun logJson(context: Context, json: String){
        val dir = File(context.filesDir, "logs").apply { mkdirs() }
        File(dir, "$fileId.txt").writeBytes(json.encodeToByteArray())
        fileId++
    }
    fun toast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
class Event{
    val eventObject: JSONObject
    val eventJson: String
    val eventType: String
    val operations: MutableList<Operation> = mutableListOf()

    constructor(eventObject: JSONObject){
        this.eventObject = eventObject
        this.eventJson = eventObject.toString()
        this.eventType = eventObject.getString("event")
        val dataJSONArray = eventObject.getJSONArray("data")
        for(i in 0..<dataJSONArray.length()){
            operations.add(Operation(dataJSONArray.getJSONObject(i), this))
        }
    }
    class Operation{
        val event: Event
        val operationObject: JSONObject
        val operationJson: String
        val operationType: String
        val eo: String
        var eot: String? = null
            private set
        val operationPath: String
        val valueObject: JSONObject?

        constructor(dataObject: JSONObject, parent: Event){
            this.event = parent
            this.operationObject = dataObject
            this.operationJson = dataObject.toString()
            this.operationType = dataObject.getString("op")
            this.eo = "${parent.eventType}|$operationType"
            this.operationPath = dataObject.getString("path")
            this.valueObject = tryOrNull { JSONObject(dataObject.getString("value")) }
            valueObject?.let {
                this.eot = tryOrNull { "$eo|${valueObject.getString("item_type")}" }
            }
            setEOT()
        }
        private fun setEOT(){
            if(this.eot != null) return
            when(event.eventType){
                "patch" -> {
                    when(operationType){
                        "replace" -> {
                            if(operationPath.contains("has_seen"))
                                this.eot = "$eo|has_seen"
                            if(operationPath.contains("update_media_interventions"))
                                this.eot = "$eo|update_media_interventions"
                        }
                        "add" -> {
                            if(operationPath.contains("reactions/likes"))
                                this.eot = "$eo|reaction"
                        }
                        "remove" -> {
                            if (operationPath.contains("reactions/likes"))
                                this.eot = "$eo|reaction"
                        }
                    }
                }
            }
        }
        fun getEOT(): String{
            eot?.let { return it }
            return eo
        }
    }
}
