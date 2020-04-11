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
        val ARG_CALLBACK = "callback"
        @JvmStatic
        val ARG_LOCATION = "location"
        @JvmStatic
        val ARG_SETTINGS = "settings"
        @JvmStatic
        val ARG_CALLBACK_DISPATCHER = "callbackDispatcher"
        @JvmStatic
        val ARG_INTERVAL = "interval"
        @JvmStatic
        val ARG_DISTANCE_FILTER = "distanceFilter"
        @JvmStatic
        val ARG_NOTIFICATION_TITLE = "notificationTitle"
        @JvmStatic
        val ARG_NOTIFICATION_MSG = "notificationMsg"
        @JvmStatic
        val ARG_NOTIFICATION_ICON = "notificationIcon"
        @JvmStatic
        val ARG_WAKE_LOCK_TIME = "wakeLockTime"
        @JvmStatic
        val ARG_NOTIFICATION_CALLBACK = "notificationCallback"

        @JvmStatic
        val BCM_SEND_LOCATION = "BCM_SEND_LOCATION"
        @JvmStatic
        val BCM_NOTIFICATION_CLICK = "BCM_NOTIFICATION_CLICK"
    }
}