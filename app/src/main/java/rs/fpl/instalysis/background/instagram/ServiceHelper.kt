package rs.fpl.instalysis.background.instagram

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rs.fpl.instalysis.background.XposedScope

object ServiceHelper {
    const val HANDLE_MESSAGE: Int = 1
    const val UPDATE_PREFERENCES: Int = 2
    const val GET_STATUS: Int = 3

    const val RESPONSE_STATUS_OK = 1
    const val RESPONSE_STATUS_MISSING_PERMISSIONS = 2

    @Suppress("MayBeConstant", "RedundantSuppression")
    val tag = "FPL_ServiceHelper"
    var serviceMessenger: Messenger? = null
    val connectedToService: Boolean
        get() {
            return serviceMessenger != null
        }

    val clientMessageHandler = object: Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            when (msg.what){
                RESPONSE_STATUS_OK -> {
                    Log.w(tag, "The service is operational.")
                    CoroutineScope(Dispatchers.Default).launch {
                        val context = XposedScope.awaitContext()
                        val activity = XposedScope.awaitActivity()
                        activity.runOnUiThread {
                            Toast.makeText(context, "Service status: Operational", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                RESPONSE_STATUS_MISSING_PERMISSIONS -> {
                    Log.w(tag, "The service is missing permissions.")
                    CoroutineScope(Dispatchers.Default).launch {
                        val context = XposedScope.awaitContext()
                        val activity = XposedScope.awaitActivity()
                        activity.runOnUiThread {
                            Toast.makeText(context, "Service status: Missing permissions", Toast.LENGTH_SHORT).show()
                        }
                        Intent().apply {
                            setPackage("rs.fpl.instalysis")
                            setComponent(ComponentName("rs.fpl.instalysis", "rs.fpl.instalysis.permissions.PermissionActivity"))
                            setFlags(FLAG_ACTIVITY_NEW_TASK)
                        }.also { intent ->
                            context.startActivity(intent)
                        }
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }
    var clientMessenger: Messenger = Messenger(clientMessageHandler)
    val mConnection = object: ServiceConnection{
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            Log.i(tag, "Connected to the Instalysis service.")
            Messenger(service).let {
                serviceMessenger = it
                val message = Message.obtain(clientMessageHandler, ServiceHelper.GET_STATUS)
                message.replyTo = clientMessenger
                it.send(message)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(tag, "The Instalysis service disconnected. (service crashed?)")
            serviceMessenger = null
        }

    }
    fun connect(context: Context){
        Log.w(tag, this.toString())
        if(connectedToService) {
            Log.w(tag, "Already connected to the service.")
            return
        }
        Log.i(tag, "Attempting to connect to the Instalysis service.")
        Intent().apply{
            setPackage("rs.fpl.instalysis")
            setComponent(ComponentName("rs.fpl.instalysis","rs.fpl.instalysis.background.Instalysis"))
        }.also { intent ->
            context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun sendMessage(id: Int, obj: Object? = null){
        Log.w(tag, "sending message (id -> $id)")
        if(serviceMessenger == null){
            Log.e(tag, "Failed to send Message, service not running.")
            XposedScope._context.let { ctx ->
                Log.i(tag, "Attempting to start the service.")
                if(ctx != null){
                    connect(ctx)
                }else{
                    CoroutineScope(Dispatchers.Default).launch {
                        connect(XposedScope.awaitContext())
                    }
                }
            }
            return
        }
        serviceMessenger?.let {
            val message = if(obj == null){
                Message.obtain(null, id)
            }else{
                Message.obtain(null, id, obj)
            }
            message.replyTo = clientMessenger
            it.send(message)
        }
    }
}