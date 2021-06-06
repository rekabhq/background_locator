package rekab.app.background_locator.pluggables

import android.content.Context
import android.os.Handler
import io.flutter.plugin.common.MethodChannel
import rekab.app.background_locator.IsolateHolderService
import rekab.app.background_locator.Keys
import rekab.app.background_locator.PreferencesManager

class InitPluggable : Pluggable {
    private var isInitCallbackCalled = false

    override fun setCallback(context: Context, callbackHandle: Long) {
        PreferencesManager.setCallbackHandle(context, Keys.INIT_CALLBACK_HANDLE_KEY, callbackHandle)

    }

    override fun onServiceStart(context: Context) {
        if (!isInitCallbackCalled) {
            (PreferencesManager.getCallbackHandle(context, Keys.INIT_CALLBACK_HANDLE_KEY))?.let { initCallback ->
                val initialDataMap = PreferencesManager.getDataCallback(context, Keys.INIT_DATA_CALLBACK_KEY)
                val backgroundChannel = MethodChannel(IsolateHolderService.backgroundEngine?.dartExecutor?.binaryMessenger,
                        Keys.BACKGROUND_CHANNEL_ID)
                Handler(context.mainLooper)
                        .post {
                            backgroundChannel.invokeMethod(Keys.BCM_INIT,
                                    hashMapOf(Keys.ARG_INIT_CALLBACK to initCallback, Keys.ARG_INIT_DATA_CALLBACK to initialDataMap))
                        }
            }
            isInitCallbackCalled = true
        }
    }

    override fun onServiceDispose(context: Context) {
        isInitCallbackCalled = false
    }

    fun setInitData(context: Context, data: Map<*, *>) {
        PreferencesManager.setDataCallback(context, Keys.INIT_DATA_CALLBACK_KEY, data)
    }
}