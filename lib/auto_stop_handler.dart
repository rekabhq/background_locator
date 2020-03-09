import 'package:background_locator/background_locator.dart';
import 'package:flutter/material.dart';

class AutoStopHandler extends WidgetsBindingObserver {
  @override
  Future<void> didChangeAppLifecycleState(AppLifecycleState state) async {
    switch (state) {
      case AppLifecycleState.inactive:
      case AppLifecycleState.paused:
      case AppLifecycleState.detached:
      await BackgroundLocator.unRegisterLocationUpdate();
        break;
      case AppLifecycleState.resumed:
        break;
    }
  }
}
