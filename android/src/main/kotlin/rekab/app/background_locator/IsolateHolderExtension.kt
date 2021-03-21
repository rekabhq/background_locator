package rekab.app.background_locator

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import java.util.concurrent.atomic.AtomicBoolean


internal fun IsolateHolderService.startLocatorService(context: Context) {

    val serviceStarted = AtomicBoolean(PreferencesManager.isServiceRunning(context))
    // start synchronized block to prevent multiple service instant
    synchronized(serviceStarted) {
        this.context = context
        if (IsolateHolderService.backgroundEngine == null) {

            val callbackHandle = context.getSharedPreferences(
                    Keys.SHARED_PREFERENCES_KEY,
                    Context.MODE_PRIVATE)
                    .getLong(Keys.CALLBACK_DISPATCHER_HANDLE_KEY, 0)
            val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)

            // We need flutter engine to handle callback, so if it is not available we have to create a
            // Flutter engine without any view
            IsolateHolderService.backgroundEngine = FlutterEngine(context)

            val args = DartExecutor.DartCallback(
                    context.assets,
                    FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                    callbackInfo
            )
            IsolateHolderService.backgroundEngine?.dartExecutor?.executeDartCallback(args)
        }
    }

    backgroundChannel =
            MethodChannel(IsolateHolderService.backgroundEngine?.dartExecutor?.binaryMessenger,
                    Keys.BACKGROUND_CHANNEL_ID)
    backgroundChannel.setMethodCallHandler(this)
}

//fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
//    val manager: ActivityManager? = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
//    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
//        if (serviceClass.name == service.service.getClassName()) {
//            return true
//        }
//    }
//    return false
//}

