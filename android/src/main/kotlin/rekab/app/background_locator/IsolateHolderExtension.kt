package rekab.app.background_locator

import android.content.Context
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation

internal fun IsolateHolderService.startLocatorService(context: Context) {
    // start synchronized block to prevent multiple service instant
    synchronized(IsolateHolderService.serviceStarted) {
        this.context = context
        if (IsolateHolderService.backgroundEngine == null) {
            val callbackHandle = context.getSharedPreferences(
                    Keys.SHARED_PREFERENCES_KEY,
                    Context.MODE_PRIVATE)
                    .getLong(Keys.CALLBACK_DISPATCHER_HANDLE_KEY, 0)
            val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)

            // We need flutter view to handle callback, so if it is not available we have to create a
            // Flutter background view without any view
            IsolateHolderService.backgroundEngine = FlutterEngine(context)

            val args = DartExecutor.DartCallback(
                    context.assets,
                    FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                    callbackInfo
            )
            IsolateHolderService.backgroundEngine?.dartExecutor?.executeDartCallback(args)
        }
    }

    backgroundChannel = MethodChannel(IsolateHolderService.backgroundEngine?.dartExecutor?.binaryMessenger, Keys.BACKGROUND_CHANNEL_ID)
    backgroundChannel.setMethodCallHandler(this)
}
