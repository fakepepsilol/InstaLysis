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
import rs.fpl.instalysis.background.instagram.ServiceHelper
import rs.fpl.instalysis.handlers.MessageHandler
import rs.fpl.instalysis.permissions.Permissions
class Instalysis : Service() {
    companion object{
        @Suppress("MayBeConstant", "RedundantSuppression")
        private val tag = "FPL_Instalysis"
    }
    internal class ServiceMessageHandler(
        private val context: Context
    ) : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            Log.w("FPL_InstalysisService", "handleMessage called")
            when(msg.what){
                ServiceHelper.HANDLE_MESSAGE -> {
                    Log.i(tag, "HANDLE_MESSAGE")
                    MessageHandler.handleMessage(msg.obj as Bundle)
                }
                ServiceHelper.UPDATE_PREFERENCES -> {
                    Log.i(tag, "UPDATE_PREFERENCES")
                }
                ServiceHelper.GET_STATUS -> {
                    Log.i(tag, "GET_STATUS")
                    val status = if(Permissions.checkAllPermissions(context))
                        ServiceHelper.RESPONSE_STATUS_OK
                    else
                        ServiceHelper.RESPONSE_STATUS_MISSING_PERMISSIONS
                    msg.replyTo?.send(Message.obtain(null, status))
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private lateinit var mMessenger: Messenger

    override fun onBind(intent: Intent): IBinder {
        mMessenger = Messenger(ServiceMessageHandler(this))
        return mMessenger.binder
    }
}