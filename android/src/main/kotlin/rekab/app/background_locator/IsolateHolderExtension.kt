package rekab.app.background_locator

import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationRequest
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import rekab.app.background_locator.provider.LocationRequestOptions
import java.util.concurrent.atomic.AtomicBoolean

internal fun IsolateHolderService.startLocatorService(context: Context) {

    val serviceStarted = AtomicBoolean(IsolateHolderService.isServiceRunning)
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

fun getLocationRequest(intent: Intent): LocationRequestOptions {
    val interval: Long = (intent.getIntExtra(Keys.SETTINGS_INTERVAL, 10) * 1000).toLong()
    val accuracyKey = intent.getIntExtra(Keys.SETTINGS_ACCURACY, 4)
    val accuracy = getAccuracy(accuracyKey)
    val distanceFilter = intent.getDoubleExtra(Keys.SETTINGS_DISTANCE_FILTER, 0.0)

    return LocationRequestOptions(interval, accuracy, distanceFilter.toFloat())
}

fun getAccuracy(key: Int): Int {
    return when (key) {
        0 -> LocationRequest.PRIORITY_NO_POWER
        1 -> LocationRequest.PRIORITY_LOW_POWER
        2 -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        3 -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        4 -> LocationRequest.PRIORITY_HIGH_ACCURACY
        else -> LocationRequest.PRIORITY_HIGH_ACCURACY
    }
}