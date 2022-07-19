package yukams.app.background_locator_2.pluggables

import android.content.Context

interface Pluggable {
    fun setCallback(context: Context, callbackHandle: Long)
    fun onServiceStart(context: Context) { /*optional*/ }
    fun onServiceDispose(context: Context) {/*optional*/ }
}