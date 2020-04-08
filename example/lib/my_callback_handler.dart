import 'dart:isolate';
import 'dart:math';
import 'dart:ui';
import 'file_manager.dart';
import 'package:background_locator/location_dto.dart';

class MyCallbackHandler {
  static const String isolateName = 'LocatorIsolate';
  static int count = 0;

  static Future<void> init() async {
  }

  static Future<void> dispose() async {
  }

  static void callback(LocationDto locationDto) async {
    count++;
    print('$count location in dart: ${locationDto.toString()}');
    await setLog(locationDto);
    final SendPort send = IsolateNameServer.lookupPortByName(isolateName);
    send?.send(locationDto);
  }

  static void notificationCallback() {
    print('notificationCallback');
  }

  static Future<void> setLog(LocationDto data) async {
    final date = DateTime.now();
    await FileManager.writeToLogFile(
        '${formatDateLog(date)} --> ${formatLog(data)}\n');
  }

  static double dp(double val, int places) {
    double mod = pow(10.0, places);
    return ((val * mod).round().toDouble() / mod);
  }

  static String formatDateLog(DateTime date) {
    return date.hour.toString() +
        ":" +
        date.minute.toString() +
        ":" +
        date.second.toString();
  }

  static String formatLog(LocationDto locationDto) {
    return dp(locationDto.latitude, 4).toString() +
        " " +
        dp(locationDto.longitude, 4).toString();
  }

}