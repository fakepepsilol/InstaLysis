package rs.fpl.instalysis.background.instagram

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Messenger
import android.util.Log
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import rs.fpl.instalysis.background.XposedScope
import rs.fpl.instalysis.background.instagram.IServiceConnection.connectJob
import rs.fpl.instalysis.background.instagram.IServiceConnection.connected
import rs.fpl.instalysis.background.instagram.IServiceConnection.service

object IServiceConnection {
    var connected: Boolean = false
    var connectJob: CompletableJob = Job()
    var service: Messenger? = null
        get() { return if(connected) field else null }

    suspend fun connect(){
        if(connectJob.isCompleted){
            return
        }
        Intent().apply{
            setPackage("rs.fpl.instalysis")
            setComponent(ComponentName("rs.fpl.instalysis","rs.fpl.instalysis.background.Instalysis"))
            putExtra("pid", android.os.Process.myPid())
        }.also { intent ->
            XposedScope.awaitContext().bindService(intent, ServiceConnectionHandler, Context.BIND_AUTO_CREATE)
        }
        connectJob.join()
    }
}

object ServiceConnectionHandler: ServiceConnection{
    const val TAG = "FPL_ServiceConnectionHandler"
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        service ?: run { Log.w(TAG, "IBinder is null"); return }

        IServiceConnection.service = Messenger(service)
        connected = true
        connectJob.complete()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        connectJob = Job()
        connected = false
        service = null
    }
}