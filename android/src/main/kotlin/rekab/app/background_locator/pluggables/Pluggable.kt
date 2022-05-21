package rekab.app.background_locator.pluggables

import android.content.Context

interface Pluggable {
    fun name(): String
    fun isInitialized(context: Context): Boolean
    fun setCallback(context: Context, callbackHandle: Long)
    fun onServiceStart(context: Context) { /*optional*/ }
    fun onServiceDispose(context: Context) {/*optional*/ }
}