import 'dart:ui';

import 'package:background_locator/location_dto.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'keys.dart';

@pragma('vm:entry-point')
void callbackDispatcher() {
  const MethodChannel _backgroundChannel =
      MethodChannel(Keys.BACKGROUND_CHANNEL_ID);
  WidgetsFlutterBinding.ensureInitialized();

  _backgroundChannel.setMethodCallHandler((MethodCall call) async {
    if (Keys.BCM_SEND_LOCATION == call.method) {
      final Map<dynamic, dynamic> args = call.arguments;
      final Function callback = PluginUtilities.getCallbackFromHandle(
          CallbackHandle.fromRawHandle(args[Keys.ARG_CALLBACK]));
      final LocationDto location =
          LocationDto.fromJson(args[Keys.ARG_LOCATION]);
      callback(location);
    } else if (Keys.BCM_NOTIFICATION_CLICK == call.method) {
      final Map<dynamic, dynamic> args = call.arguments;
      final Function notificationCallback =
          PluginUtilities.getCallbackFromHandle(CallbackHandle.fromRawHandle(
              args[Keys.ARG_NOTIFICATION_CALLBACK]));
      notificationCallback();
    }
  });
  _backgroundChannel.invokeMethod(Keys.METHOD_SERVICE_INITIALIZED);
}
