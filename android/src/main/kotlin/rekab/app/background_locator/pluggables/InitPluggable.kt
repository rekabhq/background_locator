package rekab.app.background_locator.pluggables

import android.content.Context
import rekab.app.background_locator.Keys
import rekab.app.background_locator.PreferencesManager

class InitPluggable : Pluggable {
    override fun setCallback(context: Context, callbackHandle: Long) {
        PreferencesManager.setCallbackHandle(context, Keys.INIT_CALLBACK_HANDLE_KEY, callbackHandle)

    }

    override fun onServiceStart() {
        TODO("Not yet implemented")
    }

    fun setInitData(context: Context, data: Map<*, *>) {
        PreferencesManager.setDataCallback(context, Keys.INIT_DATA_CALLBACK_KEY, data)
    }
}