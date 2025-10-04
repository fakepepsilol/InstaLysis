package rs.fpl.instalysis.background.instagram

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import rs.fpl.instalysis.handlers.ResponseHandler


enum class ServiceMessageType(val id: Int){
    RESPONSE_STATUS_OPERATIONAL(-2),
    RESPONSE_MISSING_PERMISSIONS(-1),
    RESPONSE_OK(0),

    HANDLE_MESSAGE(1),
    UPDATE_PREFERENCES(2),
    GET_STATUS(3);
    companion object{
        fun fromInt(id: Int) = entries.first { it.id == id }
    }
}

object ServiceMessageQueue{
    const val TAG = "FPL_ServiceMessageQueue"
    private lateinit var channel: Channel<Message>
    fun begin(){
        if(!IServiceConnection.connected){
            Log.e(TAG, "Cannot start MessageQueue if the Service is not running.")
            return
        }
        channel = Channel()
        CoroutineScope(Dispatchers.Default).launch {
            for(message in channel){
                if(!IServiceConnection.connected){
                    channel.close()
                }
                IServiceConnection.service?.send(message)
            }
        }
    }
    fun push(message: Message){
        CoroutineScope(Dispatchers.Default).launch {
            channel.send(message)
        }
    }
}
@Suppress("unused")
object ServiceHelper {
    @Suppress("MayBeConstant", "RedundantSuppression")
    const val TAG = "FPL_ServiceHelper"
    val responseMessenger: Messenger = Messenger(ResponseHandler)
    fun sendMessage(type: Int, obj: Object? = null){
        CoroutineScope(Dispatchers.Default).launch {
            IServiceConnection.connect()
            ServiceMessageQueue.begin()
            ServiceMessageQueue.push(
                Message.obtain(null, type, obj)
                    .apply { replyTo = responseMessenger }
            )
        }
    }
    fun sendMessage(type: ServiceMessageType, bundle: Bundle) = sendMessage(type.id, bundle as Object)
    fun sendMessage(type: Int, bundle: Bundle) = sendMessage(type, bundle as Object)
    fun sendMessage(type: ServiceMessageType, obj: Object? = null) = sendMessage(type.id, obj)
}