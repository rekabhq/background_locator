package yukams.app.background_locator_2.pluggables

import android.content.Context
import android.os.Handler
import io.flutter.plugin.common.MethodChannel
import yukams.app.background_locator_2.IsolateHolderService
import yukams.app.background_locator_2.Keys
import yukams.app.background_locator_2.PreferencesManager

class DisposePluggable : Pluggable {
    override fun setCallback(context: Context, callbackHandle: Long) {
        PreferencesManager.setCallbackHandle(context, Keys.DISPOSE_CALLBACK_HANDLE_KEY, callbackHandle)
    }

    override fun onServiceDispose(context: Context) {
        (PreferencesManager.getCallbackHandle(context, Keys.DISPOSE_CALLBACK_HANDLE_KEY))?.let { disposeCallback ->
            IsolateHolderService.getBinaryMessenger(context)?.let { binaryMessenger ->
                val backgroundChannel = MethodChannel(binaryMessenger, Keys.BACKGROUND_CHANNEL_ID)
                Handler(context.mainLooper)
                    .post {
                        backgroundChannel.invokeMethod(
                            Keys.BCM_DISPOSE,
                            hashMapOf(Keys.ARG_DISPOSE_CALLBACK to disposeCallback)
                        )
                    }
            }
        }
    }
}