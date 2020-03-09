import 'dart:async';
import 'dart:isolate';
import 'dart:math';
import 'dart:ui';

import 'package:background_locator/background_locator.dart';
import 'package:background_locator/location_dto.dart';
import 'package:background_locator/location_settings.dart';
import 'package:flutter/material.dart';

import 'file_manager.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  ReceivePort port = ReceivePort();

  String logStr = '';

  @override
  void initState() {
    super.initState();

    IsolateNameServer.registerPortWithName(port.sendPort, 'LocatorIsolate');
    port.listen((dynamic data) {
      setLog(data);
    });
    initPlatformState();
  }

  double dp(double val, int places) {
    double mod = pow(10.0, places);
    return ((val * mod).round().toDouble() / mod);
  }

  Future<void> setLog(LocationDto data) async {
    final date = DateTime.now();
    final dateStr = '${date.hour}:${date.minute}:${date.second}';
    FileManager.writeToLogFile(
        '$dateStr --> ${dp(data.latitude, 4)}. ${dp(data.longitude, 4)}\n');
    final log = await FileManager.readLogFile();
    setState(() {
      logStr = log;
    });
  }

  Future<void> initPlatformState() async {
    print('Initializing...');
    await BackgroundLocator.initialize();
    logStr = await FileManager.readLogFile();
    print('Initialization done');
  }

  static void callback(LocationDto locationDto) async {
    print('location in dart: ${locationDto.toString()}');
    final SendPort send = IsolateNameServer.lookupPortByName('LocatorIsolate');
    send?.send(locationDto);
  }

  @override
  Widget build(BuildContext context) {
    final start = SizedBox(
        width: double.maxFinite,
        child: RaisedButton(
          child: Text('Start'),
          onPressed: () {
            BackgroundLocator.registerLocationUpdate(callback,
                settings: LocationSettings(
                    accuracy: LocationAccuracy.NAVIGATION,
                    interval: 5,
                    distanceFilter: 0,
                    requestPermissionMsg:
                        "'registerLocator' requires the ACCESS_FINE_LOCATION permission.",
                    notificationTitle: "Start Location Tracking example",
                    notificationMsg: "Track location in background exapmle",
                    wakeLockTime: 20));
          },
        ));
    final stop = SizedBox(
        width: double.maxFinite,
        child: RaisedButton(
          child: Text('Stop'),
          onPressed: () {
            BackgroundLocator.unRegisterLocationUpdate();
          },
        ));
    final clear = SizedBox(
        width: double.maxFinite,
        child: RaisedButton(
          child: Text('Clear Log'),
          onPressed: () {
            FileManager.clearLogFile();
            setState(() {
              logStr = '';
            });
          },
        ));

    final log = Text(
      logStr,
    );

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Flutter background Locator'),
        ),
        body: Container(
          width: double.maxFinite,
          padding: const EdgeInsets.all(22),
          child: SingleChildScrollView(
              child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: <Widget>[start, stop, clear, log],
          )),
        ),
      ),
    );
  }
}
