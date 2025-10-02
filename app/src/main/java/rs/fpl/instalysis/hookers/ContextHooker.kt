package rs.fpl.instalysis.hookers

import android.content.Context
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import rs.fpl.instalysis.background.XposedScope

@XposedHooker
class ContextHooker : XposedInterface.Hooker {
    companion object {
        @BeforeInvocation
        @JvmStatic
        fun before(callback: XposedInterface.BeforeHookCallback) {
            XposedScope.setContext(callback.args[0] as Context)
        }
    }
}