package rekab.app.background_locator.pluggables

import android.content.Context

interface Pluggable {
    fun setCallback(context: Context, callbackHandle: Long)
    fun onServiceStart() { /*optional*/ }
    fun onServiceDispose() {/*optional*/ }
}