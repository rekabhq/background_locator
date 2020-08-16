
import 'package:background_locator/settings/locator_settings.dart';
import 'package:flutter/material.dart';

class AndroidNotificationSettings {
  final String notificationChannelName;
  final String notificationTitle;
  final String notificationMsg;
  final String notificationBigMsg;
  final String notificationIcon;
  final Color notificationIconColor;

  /// [notificationTitle] Title of the notification. Only applies for android. Default is 'Start Location Tracking'.
  ///
  /// [notificationMsg] Message of notification. Only applies for android. Default is 'Track location in background'.
  ///
  /// [notificationBigMsg] Message to be displayed in the expanded content area of the notification. Only applies for android. Default is 'Background location is on to keep the app up-tp-date with your location. This is required for main features to work properly when the app is not running.'.
  ///
  /// [notificationIcon] Icon name for notification. Only applies for android. The icon should be in 'mipmap' Directory.
  /// Default is app icon. Icon must comply to android rules to be displayed (transparent background and black/white shape)
  ///
  /// [notificationIconColor] Icon color for notification from notification drawer. Only applies for android. Default color is grey.
  ///
  AndroidNotificationSettings(
      {this.notificationChannelName = 'Location tracking',
      this.notificationTitle = 'Start Location Tracking',
      this.notificationMsg = 'Track location in background',
      this.notificationBigMsg = 'Background location is on to keep the app up-tp-date with your location. This is required for main features to work properly when the app is not running.',
      this.notificationIcon = '',
      this.notificationIconColor = Colors.grey});
}

class AndroidSettings extends LocatorSettings {
  final AndroidNotificationSettings androidNotificationSettings;
  final int wakeLockTime;

  AndroidSettings(
      {LocationAccuracy accuracy = LocationAccuracy.NAVIGATION,
      int interval = 5,
      double distanceFilter = 0,
      bool autoStop = false,
      this.androidNotificationSettings,
      this.wakeLockTime = 60})
      : super(
            accuracy: accuracy,
            interval: interval,
            distanceFilter: distanceFilter,
            autoStop: autoStop); //minutes
}
