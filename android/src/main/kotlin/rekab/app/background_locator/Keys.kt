package rekab.app.background_locator

class Keys {
    companion object {
        @JvmStatic
        val SHARED_PREFERENCES_KEY = "SHARED_PREFERENCES_KEY"

        @JvmStatic
        val CALLBACK_DISPATCHER_HANDLE_KEY = "CALLBACK_DISPATCHER_HANDLE_KEY"

        @JvmStatic
        val CALLBACK_HANDLE_KEY = "CALLBACK_HANDLE_KEY"

        @JvmStatic
        val NOTIFICATION_CALLBACK_HANDLE_KEY = "NOTIFICATION_CALLBACK_HANDLE_KEY"

        @JvmStatic
        val INIT_CALLBACK_HANDLE_KEY = "INIT_CALLBACK_HANDLE_KEY"

        @JvmStatic
        val INIT_DATA_CALLBACK_KEY = "INIT_DATA_CALLBACK_KEY"

        @JvmStatic
        val DISPOSE_CALLBACK_HANDLE_KEY = "DISPOSE_CALLBACK_HANDLE_KEY"

        @JvmStatic
        val CHANNEL_ID = "app.rekab/locator_plugin"

        @JvmStatic
        val BACKGROUND_CHANNEL_ID = "app.rekab/locator_plugin_background"

        @JvmStatic
        val METHOD_SERVICE_INITIALIZED = "LocatorService.initialized"

        @JvmStatic
        val METHOD_PLUGIN_INITIALIZE_SERVICE = "LocatorPlugin.initializeService"

        @JvmStatic
        val METHOD_PLUGIN_REGISTER_LOCATION_UPDATE = "LocatorPlugin.registerLocationUpdate"

        @JvmStatic
        val METHOD_PLUGIN_UN_REGISTER_LOCATION_UPDATE = "LocatorPlugin.unRegisterLocationUpdate"

        @JvmStatic
        val METHOD_PLUGIN_IS_REGISTER_LOCATION_UPDATE = "LocatorPlugin.isRegisterLocationUpdate"

        @JvmStatic
        val METHOD_PLUGIN_IS_SERVICE_RUNNING = "LocatorPlugin.isServiceRunning"

        @JvmStatic
        val METHOD_PLUGIN_UPDATE_NOTIFICATION = "LocatorPlugin.updateNotification"

        @JvmStatic
        val ARG_INIT_CALLBACK = "initCallback"

        @JvmStatic
        val ARG_INIT_DATA_CALLBACK = "initDataCallback"

        @JvmStatic
        val ARG_DISPOSE_CALLBACK = "disposeCallback"

        @JvmStatic
        val ARG_IS_MOCKED = "is_mocked"

        @JvmStatic
        val ARG_LATITUDE = "latitude"

        @JvmStatic
        val ARG_LONGITUDE = "longitude"

        @JvmStatic
        val ARG_ACCURACY = "accuracy"

        @JvmStatic
        val ARG_ALTITUDE = "altitude"

        @JvmStatic
        val ARG_SPEED = "speed"

        @JvmStatic
        val ARG_SPEED_ACCURACY = "speed_accuracy"

        @JvmStatic
        val ARG_HEADING = "heading"

        @JvmStatic
        val ARG_TIME = "time"

        @JvmStatic
        val ARG_PROVIDER = "provider"

        @JvmStatic
        val ARG_CALLBACK = "callback"

        @JvmStatic
        val ARG_NOTIFICATION_CALLBACK = "notificationCallback"

        @JvmStatic
        val ARG_LOCATION = "location"

        @JvmStatic
        val ARG_SETTINGS = "settings"

        @JvmStatic
        val ARG_CALLBACK_DISPATCHER = "callbackDispatcher"


        @JvmStatic
        val SETTINGS_ACCURACY = "settings_accuracy"

        @JvmStatic
        val SETTINGS_INTERVAL = "settings_interval"

        @JvmStatic
        val SETTINGS_DISTANCE_FILTER = "settings_distanceFilter"

        @JvmStatic
        val SETTINGS_ANDROID_NOTIFICATION_CHANNEL_NAME = "settings_android_notificationChannelName"

        @JvmStatic
        val SETTINGS_ANDROID_NOTIFICATION_TITLE = "settings_android_notificationTitle"

        @JvmStatic
        val SETTINGS_ANDROID_NOTIFICATION_MSG = "settings_android_notificationMsg"

        @JvmStatic
        val SETTINGS_ANDROID_NOTIFICATION_BIG_MSG = "settings_android_notificationBigMsg"

        @JvmStatic
        val SETTINGS_ANDROID_NOTIFICATION_ICON = "settings_android_notificationIcon"

        @JvmStatic
        val SETTINGS_ANDROID_NOTIFICATION_ICON_COLOR = "settings_android_notificationIconColor"

        @JvmStatic
        val SETTINGS_ANDROID_WAKE_LOCK_TIME = "settings_android_wakeLockTime"

        @JvmStatic
        val SETTINGS_ANDROID_LOCATION_CLIENT = "settings_android_location_client"

        @JvmStatic
        val SETTINGS_INIT_PLUGGABLE = "settings_init_pluggable"

        @JvmStatic
        val SETTINGS_DISPOSABLE_PLUGGABLE = "settings_disposable_pluggable"

        @JvmStatic
        val BCM_SEND_LOCATION = "BCM_SEND_LOCATION"

        @JvmStatic
        val BCM_NOTIFICATION_CLICK = "BCM_NOTIFICATION_CLICK"

        @JvmStatic
        val BCM_INIT = "BCM_INIT"

        @JvmStatic
        val BCM_DISPOSE = "BCM_DISPOSE"

        @JvmStatic
        val NOTIFICATION_ACTION = "com.rekab.background_locator.notification"
    }
}