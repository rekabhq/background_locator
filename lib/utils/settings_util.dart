import 'dart:io';
import 'dart:ui';

import 'package:background_locator/keys.dart';
import 'package:background_locator/location_dto.dart';
import 'package:background_locator/settings/android_settings.dart';
import 'package:background_locator/settings/ios_settings.dart';

class SettingsUtil {
  static Map<String, dynamic> getArgumentsMap(
      {required void Function(LocationDto) callback,

      AndroidSettings androidSettings = const AndroidSettings(),
      IOSSettings iosSettings = const IOSSettings()}) {
    final args = _getCommonArgumentsMap(
        callback: callback);

    if (Platform.isAndroid) {
      args.addAll(_getAndroidArgumentsMap(androidSettings));
    } else if (Platform.isIOS) {
      args.addAll(_getIOSArgumentsMap(iosSettings));
    }

    return args;
  }

  static Map<String, dynamic> _getCommonArgumentsMap({
    required void Function(LocationDto) callback,
  }) {
    final Map<String, dynamic> args = {
      Keys.ARG_CALLBACK:
          PluginUtilities.getCallbackHandle(callback)!.toRawHandle(),
    };

    return args;
  }

  static Map<String, dynamic> _getAndroidArgumentsMap(
      AndroidSettings androidSettings) {
    final Map<String, dynamic> args = {
      Keys.ARG_SETTINGS: androidSettings.toMap()
    };

    if (androidSettings.androidNotificationSettings.notificationTapCallback !=
        null) {
      args[Keys.ARG_NOTIFICATION_CALLBACK] = PluginUtilities.getCallbackHandle(
              androidSettings
                  .androidNotificationSettings.notificationTapCallback!)!
          .toRawHandle();
    }

    return args;
  }

  static Map<String, dynamic> _getIOSArgumentsMap(IOSSettings iosSettings) {
    return iosSettings.toMap();
  }
}
