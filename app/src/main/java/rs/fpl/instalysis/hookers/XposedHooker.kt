package rs.fpl.instalysis.hookers

import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker

@XposedHooker
interface XposedHooker : XposedInterface.Hooker {
    companion object {
        @BeforeInvocation
        @JvmStatic
        fun before(callback: XposedInterface.BeforeHookCallback) {
        }

        @AfterInvocation
        @JvmStatic
        fun after(callback: XposedInterface.AfterHookCallback) {

        }
    }
}