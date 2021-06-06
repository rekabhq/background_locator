package rekab.app.background_locator.pluggables

import android.content.Context

class DisposePluggable: Pluggable {
    override fun setCallback(context: Context, callbackHandle: Long) {

    }

    override fun onServiceDispose() {
        
    }
}