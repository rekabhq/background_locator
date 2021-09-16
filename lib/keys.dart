class Keys {
  static const String CHANNEL_ID = 'app.rekab/locator_plugin';
  static const String BACKGROUND_CHANNEL_ID =
      'app.rekab/locator_plugin_background';

  static const String METHOD_SERVICE_INITIALIZED = 'LocatorService.initialized';
  static const String METHOD_PLUGIN_INITIALIZE_SERVICE =
      'LocatorPlugin.initializeService';
  static const String METHOD_PLUGIN_REGISTER_LOCATION_UPDATE =
      'LocatorPlugin.registerLocationUpdate';
  static const String METHOD_PLUGIN_UN_REGISTER_LOCATION_UPDATE =
      'LocatorPlugin.unRegisterLocationUpdate';
  static const String METHOD_PLUGIN_IS_REGISTER_LOCATION_UPDATE =
      'LocatorPlugin.isRegisterLocationUpdate';
  static const String METHOD_PLUGIN_IS_SERVICE_RUNNING =
      'LocatorPlugin.isServiceRunning';
  static const String METHOD_PLUGIN_UPDATE_NOTIFICATION =
      'LocatorPlugin.updateNotification';

  static const String ARG_IS_MOCKED = 'is_mocked';
  static const String ARG_LATITUDE = 'latitude';
  static const String ARG_LONGITUDE = 'longitude';
  static const String ARG_ALTITUDE = 'altitude';
  static const String ARG_ACCURACY = 'accuracy';
  static const String ARG_SPEED = 'speed';
  static const String ARG_SPEED_ACCURACY = 'speed_accuracy';
  static const String ARG_HEADING = 'heading';
  static const String ARG_TIME = 'time';
  static const String ARG_PROVIDER = 'provider';
  static const String ARG_CALLBACK = 'callback';
  static const String ARG_NOTIFICATION_CALLBACK = 'notificationCallback';
  static const String ARG_INIT_CALLBACK = 'initCallback';
  static const String ARG_INIT_DATA_CALLBACK = 'initDataCallback';
  static const String ARG_DISPOSE_CALLBACK = 'disposeCallback';
  static const String ARG_LOCATION = 'location';
  static const String ARG_SETTINGS = 'settings';
  static const String ARG_CALLBACK_DISPATCHER = 'callbackDispatcher';

  static const String SETTINGS_ACCURACY = 'settings_accuracy';
  static const String SETTINGS_INTERVAL = 'settings_interval';
  static const String SETTINGS_DISTANCE_FILTER = 'settings_distanceFilter';
  static const String SETTINGS_AUTO_STOP = 'settings_autoStop';
  static const String SETTINGS_ANDROID_NOTIFICATION_CHANNEL_NAME =
      'settings_android_notificationChannelName';
  static const String SETTINGS_ANDROID_NOTIFICATION_TITLE =
      'settings_android_notificationTitle';
  static const String SETTINGS_ANDROID_NOTIFICATION_MSG =
      'settings_android_notificationMsg';
  static const String SETTINGS_ANDROID_NOTIFICATION_BIG_MSG =
      'settings_android_notificationBigMsg';
  static const String SETTINGS_ANDROID_NOTIFICATION_ICON =
      'settings_android_notificationIcon';
  static const String SETTINGS_ANDROID_NOTIFICATION_ICON_COLOR =
      'settings_android_notificationIconColor';
  static const String SETTINGS_ANDROID_WAKE_LOCK_TIME =
      'settings_android_wakeLockTime';
  static const String SETTINGS_ANDROID_LOCATION_CLIENT =
      "settings_android_location_client";

  static const String SETTINGS_IOS_SHOWS_BACKGROUND_LOCATION_INDICATOR =
      'settings_ios_showsBackgroundLocationIndicator';

  static const String BCM_SEND_LOCATION = 'BCM_SEND_LOCATION';
  static const String BCM_NOTIFICATION_CLICK = 'BCM_NOTIFICATION_CLICK';
  static const String BCM_INIT = 'BCM_INIT';
  static const String BCM_DISPOSE = 'BCM_DISPOSE';
}
