package rs.fpl.instalysis.hookers

import android.app.Activity
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import rs.fpl.instalysis.background.XposedScope
import rs.fpl.instalysis.background.instagram.ServiceHelper

@XposedHooker
class ActivityHooker : XposedInterface.Hooker {
    companion object {
        @BeforeInvocation
        @JvmStatic
        fun before(callback: XposedInterface.BeforeHookCallback) {
            XposedScope.setActivity(callback.thisObject as Activity)
            ServiceHelper.sendMessage(ServiceHelper.GET_STATUS)
        }
    }
}