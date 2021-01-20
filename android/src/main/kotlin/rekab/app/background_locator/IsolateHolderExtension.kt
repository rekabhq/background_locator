package rekab.app.background_locator

import android.content.Context
import android.os.Handler
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterNativeView
import io.flutter.view.FlutterRunArguments
import java.util.HashMap

internal fun IsolateHolderService.startLocatorService(context: Context) {
    // start synchronized block to prevent multiple service instant
    synchronized(IsolateHolderService.serviceStarted) {
        this.context = context
        if (IsolateHolderService.backgroundFlutterView == null) {
            val callbackHandle = context.getSharedPreferences(
                    Keys.SHARED_PREFERENCES_KEY,
                    Context.MODE_PRIVATE)
                    .getLong(Keys.CALLBACK_DISPATCHER_HANDLE_KEY, 0)
            val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)

            // We need flutter view to handle callback, so if it is not available we have to create a
            // Flutter background view without any view
            IsolateHolderService.backgroundFlutterView = FlutterNativeView(context, true)

            val args = FlutterRunArguments()
            args.bundlePath = FlutterMain.findAppBundlePath()
            args.entrypoint = callbackInfo.callbackName
            args.libraryPath = callbackInfo.callbackLibraryPath

            IsolateHolderService.backgroundFlutterView!!.runFromBundle(args)
            IsolateHolderService.setBackgroundFlutterViewManually(IsolateHolderService.backgroundFlutterView)
        }

        IsolateHolderService.pluginRegistrantCallback?.registerWith(IsolateHolderService.backgroundFlutterView!!.pluginRegistry)
    }

    backgroundChannel = MethodChannel(IsolateHolderService.backgroundFlutterView, Keys.BACKGROUND_CHANNEL_ID)
    backgroundChannel.setMethodCallHandler(this)
}
