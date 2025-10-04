package rs.fpl.instalysis.background

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import rs.fpl.instalysis.background.instagram.ServiceMessageType
import rs.fpl.instalysis.handlers.MessageHandler
import rs.fpl.instalysis.permissions.Permissions
class Instalysis : Service() {
    companion object{
        @Suppress("MayBeConstant", "RedundantSuppression")
        private val tag = "FPL_Instalysis"
        private var pid: Int? = null
    }
    internal class ServiceMessageHandler(
        private val context: Context
    ) : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            Log.w("FPL_InstalysisService", "handleMessage called")
            when(ServiceMessageType.fromInt(msg.what)){
                ServiceMessageType.HANDLE_MESSAGE -> {
                    Log.d(tag, "HANDLE_MESSAGE")
                    MessageHandler.handleMessage(msg.obj as Bundle)
                }
                ServiceMessageType.UPDATE_PREFERENCES -> {
                    Log.d(tag, "UPDATE_PREFERENCES")
                    Preferences.processBundle(msg.obj as Bundle)
                }
                ServiceMessageType.GET_STATUS -> {
                    Log.d(tag, "GET_STATUS")
                    val status = if(Permissions.checkAllPermissions(context))
                        ServiceMessageType.RESPONSE_STATUS_OPERATIONAL
                    else
                        ServiceMessageType.RESPONSE_MISSING_PERMISSIONS
                    msg.replyTo?.send(Message.obtain(null, status.id))
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private lateinit var mMessenger: Messenger
    override fun onBind(intent: Intent): IBinder? {
        val callerPid: Int? = intent.getIntExtra("pid", -1).takeIf { it != -1 }
        if(callerPid != null){
            Log.e(tag, "Service started by pid: $callerPid")
            pid = callerPid
        }
        mMessenger = Messenger(ServiceMessageHandler(this))
        return mMessenger.binder
    }
}