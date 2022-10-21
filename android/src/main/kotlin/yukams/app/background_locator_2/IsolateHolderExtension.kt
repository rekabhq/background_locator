package yukams.app.background_locator_2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.google.android.gms.location.LocationRequest
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import yukams.app.background_locator_2.IsolateHolderService.Companion.isServiceInitialized
import yukams.app.background_locator_2.provider.LocationRequestOptions
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicBoolean

internal fun IsolateHolderService.startLocatorService(context: Context) {

    val serviceStarted = AtomicBoolean(IsolateHolderService.isServiceRunning)
    // start synchronized block to prevent multiple service instant
    synchronized(serviceStarted) {
        this.context = context
        // resetting the background engine to avoid being stuck after an app crash
        IsolateHolderService.backgroundEngine?.destroy();
        IsolateHolderService.backgroundEngine = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                // We need flutter engine to handle callback, so if it is not available we have to create a
                // Flutter engine without any view
                Log.e("IsolateHolderService", "startLocatorService: Start Flutter Engine")
                IsolateHolderService.backgroundEngine = FlutterEngine(context)

                val callbackHandle = context.getSharedPreferences(
                    Keys.SHARED_PREFERENCES_KEY,
                    Context.MODE_PRIVATE
                )
                    .getLong(Keys.CALLBACK_DISPATCHER_HANDLE_KEY, 0)
                val callbackInfo =
                    FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)

                if(callbackInfo == null) {
                    Log.e("IsolateHolderExtension", "Fatal: failed to find callback");
                    return;
                }

                val args = DartExecutor.DartCallback(
                    context.assets,
                    FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                    callbackInfo
                )
                IsolateHolderService.backgroundEngine?.dartExecutor?.executeDartCallback(args)
                isServiceInitialized = true
                Log.e("IsolateHolderExtension", "service initialized")
            }
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    IsolateHolderService.getBinaryMessenger(context)?.let { binaryMessenger ->
        backgroundChannel =
            MethodChannel(
                binaryMessenger,
                Keys.BACKGROUND_CHANNEL_ID
            )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                backgroundChannel.setMethodCallHandler(this)
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }
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