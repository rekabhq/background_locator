package rekab.app.background_locator

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import rekab.app.background_locator.Keys.Companion.ARG_ACCURACY
import rekab.app.background_locator.Keys.Companion.ARG_CALLBACK
import rekab.app.background_locator.Keys.Companion.ARG_CALLBACK_DISPATCHER
import rekab.app.background_locator.Keys.Companion.ARG_DISTANCE_FILTER
import rekab.app.background_locator.Keys.Companion.ARG_INTERVAL
import rekab.app.background_locator.Keys.Companion.ARG_LOCATION_PERMISSION_MSG
import rekab.app.background_locator.Keys.Companion.ARG_NOTIFICATION_MSG
import rekab.app.background_locator.Keys.Companion.ARG_NOTIFICATION_TITLE
import rekab.app.background_locator.Keys.Companion.ARG_SETTINGS
import rekab.app.background_locator.Keys.Companion.ARG_WAKE_LOCK_TIME
import rekab.app.background_locator.Keys.Companion.CALLBACK_DISPATCHER_HANDLE_KEY
import rekab.app.background_locator.Keys.Companion.CALLBACK_HANDLE_KEY
import rekab.app.background_locator.Keys.Companion.CHANNEL_ID
import rekab.app.background_locator.Keys.Companion.METHOD_PLUGIN_INITIALIZE_SERVICE
import rekab.app.background_locator.Keys.Companion.METHOD_PLUGIN_REGISTER_LOCATION_UPDATE
import rekab.app.background_locator.Keys.Companion.METHOD_PLUGIN_UN_REGISTER_LOCATION_UPDATE
import rekab.app.background_locator.Keys.Companion.SHARED_PREFERENCES_KEY


class BackgroundLocatorPlugin(private val context: Context, private val activity: Activity?) : MethodCallHandler {
    private val locatorClient = LocationServices.getFusedLocationProviderClient(context)

    companion object {
        @JvmStatic
        private var channel: MethodChannel? = null

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val plugin = BackgroundLocatorPlugin(registrar.context(), registrar.activity())
            channel = MethodChannel(registrar.messenger(), CHANNEL_ID)
            channel?.setMethodCallHandler(plugin)
        }

        @JvmStatic
        private fun registerLocator(context: Context,
                                    client: FusedLocationProviderClient,
                                    args: Map<Any, Any>,
                                    result: Result?) {
            if (IsolateHolderService.isRunning) {
                // The service is running already
                Log.d("BackgroundLocatorPlugin", "Locator service is already running")
                return
            }

            val callbackHandle = args[ARG_CALLBACK] as Long
            setCallbackHandle(context, callbackHandle)

            val settings = args[ARG_SETTINGS] as Map<*, *>

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_DENIED) {

                val msg = settings[ARG_LOCATION_PERMISSION_MSG] as String
                result?.error(msg, null, null)
            }

            startIsolateService(context, settings)

            client.requestLocationUpdates(getLocationRequest(settings),
                    getLocatorPendingIndent(context))
        }

        @JvmStatic
        private fun startIsolateService(context: Context, settings: Map<*, *>) {
            val intent = Intent(context, IsolateHolderService::class.java)
            intent.action = IsolateHolderService.ACTION_START
            intent.putExtra(ARG_NOTIFICATION_TITLE, settings[ARG_NOTIFICATION_TITLE] as String)
            intent.putExtra(ARG_NOTIFICATION_MSG, settings[ARG_NOTIFICATION_MSG] as String)

            if (settings.containsKey(ARG_WAKE_LOCK_TIME)) {
                intent.putExtra(ARG_WAKE_LOCK_TIME, settings[ARG_WAKE_LOCK_TIME] as Int)
            }

            ContextCompat.startForegroundService(context, intent)
        }

        @JvmStatic
        private fun stopIsolateService(context: Context) {
            val intent = Intent(context, IsolateHolderService::class.java)
            intent.action = IsolateHolderService.ACTION_SHUTDOWN
            ContextCompat.startForegroundService(context, intent)
        }

        @JvmStatic
        private fun initializeService(context: Context, args: Map<Any, Any>) {
            val callbackHandle: Long = args[ARG_CALLBACK_DISPATCHER] as Long
            setCallbackDispatcherHandle(context, callbackHandle)
        }

        @JvmStatic
        private fun getLocatorPendingIndent(context: Context): PendingIntent {
            val intent = Intent(context, LocatorBroadcastReceiver::class.java)
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        @JvmStatic
        private fun getLocationRequest(settings: Map<*, *>): LocationRequest {
            val locationRequest = LocationRequest()

            val interval: Long = (settings[ARG_INTERVAL] as Int * 1000).toLong()
            locationRequest.interval = interval
            locationRequest.fastestInterval = interval
            locationRequest.maxWaitTime = interval

            val accuracyKey = settings[ARG_ACCURACY] as Int
            locationRequest.priority = getAccuracy(accuracyKey)

            val distanceFilter = settings[ARG_DISTANCE_FILTER] as Double
            locationRequest.smallestDisplacement = distanceFilter.toFloat()

            return locationRequest
        }

        @JvmStatic
        private fun getAccuracy(key: Int): Int {
            return when (key) {
                0 -> LocationRequest.PRIORITY_NO_POWER
                1 -> LocationRequest.PRIORITY_LOW_POWER
                2 -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                3 -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                4 -> LocationRequest.PRIORITY_HIGH_ACCURACY
                else -> LocationRequest.PRIORITY_HIGH_ACCURACY
            }
        }

        @JvmStatic
        private fun removeLocator(context: Context,
                                  client: FusedLocationProviderClient) {
            if (!IsolateHolderService.isRunning) {
                // The service is not running
                Log.d("BackgroundLocatorPlugin", "Locator service is not running, nothing to stop")
                return
            }

            client.removeLocationUpdates(getLocatorPendingIndent(context))
            stopIsolateService(context)
        }

        @JvmStatic
        private fun setCallbackDispatcherHandle(context: Context, handle: Long) {
            context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(CALLBACK_DISPATCHER_HANDLE_KEY, handle)
                    .apply()
        }

        @JvmStatic
        fun setCallbackHandle(context: Context, handle: Long) {
            context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(CALLBACK_HANDLE_KEY, handle)
                    .apply()
        }

        @JvmStatic
        fun getCallbackHandle(context: Context): Long {
            return context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .getLong(CALLBACK_HANDLE_KEY, 0)
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            METHOD_PLUGIN_INITIALIZE_SERVICE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity?.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            12312)
                }
                val args: Map<Any, Any> = call.arguments()
                initializeService(context, args)
                result.success(true)
            }
            METHOD_PLUGIN_REGISTER_LOCATION_UPDATE -> {
                val args: Map<Any, Any> = call.arguments()
                registerLocator(context,
                        locatorClient,
                        args,
                        result)
            }
            METHOD_PLUGIN_UN_REGISTER_LOCATION_UPDATE -> removeLocator(context,
                    locatorClient)
            else -> result.notImplemented()
        }
    }
}
