import 'keys.dart';

class LocationAccuracy {
  const LocationAccuracy._internal(this.value);

  final int value;

  static const POWERSAVE = LocationAccuracy._internal(0);
  static const LOW = LocationAccuracy._internal(1);
  static const BALANCED = LocationAccuracy._internal(2);
  static const HIGH = LocationAccuracy._internal(3);
  static const NAVIGATION = LocationAccuracy._internal(4);
}

class LocationSettings {
  final LocationAccuracy accuracy;
  final int interval; //seconds
  final double distanceFilter;
  final String notificationTitle;
  final String notificationMsg;
  final String notificationIcon;
  final int wakeLockTime; //minutes
  final bool autoStop;

  LocationSettings(
      {this.accuracy = LocationAccuracy.NAVIGATION,
      this.interval = 5,
      this.distanceFilter = 0,
      this.notificationTitle = 'Start Location Tracking',
      this.notificationMsg = 'Track location in background',
      this.notificationIcon = '',
      this.wakeLockTime = 60,
      this.autoStop = false});

  Map<String, dynamic> toMap() {
    return {
      Keys.ARG_ACCURACY: accuracy.value,
      Keys.ARG_INTERVAL: interval,
      Keys.ARG_DISTANCE_FILTER: distanceFilter,
      Keys.ARG_NOTIFICATION_TITLE: notificationTitle,
      Keys.ARG_NOTIFICATION_MSG: notificationMsg,
      Keys.ARG_NOTIFICATION_ICON: notificationIcon,
      Keys.ARG_WAKE_LOCK_TIME: wakeLockTime,
      Keys.ARG_AUTO_STOP: autoStop,
    };
  }
}
