# background_locator

A Flutter plugin for updating location in background.

## Install
##### 1. Depend on it
Add this to your package's pubspec.yaml file:
```ruby
dependencies:
  background_locator: ^1.1.1+1
  ```

##### 2. Install it
You can install packages from the command line:

with Flutter:
```
$ flutter pub get
```

Alternatively, your editor might support `flutter pub get`. Check the docs for your editor to learn more.

##### 3. Import it
Now in your Dart code, you can use:
```dart
import 'package:background_locator/background_locator.dart';
```

## Setup

### Android

1) Add the following permission to `AndroidManifest.xml`:
```xml
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```


2) Add the following lines to your `AndroidManifest.xml` to register the Services and BroadcastReceiver:
```xml
        <receiver android:name="rekab.app.background_locator.LocatorBroadcastReceiver"
            android:enabled="true"
            android:exported="true"/>
        <service android:name="rekab.app.background_locator.LocatorService"
            android:permission="android.permission.BIND_JOB_SERVICE"
            android:exported="true"/>
        <service android:name="rekab.app.background_locator.IsolateHolderService"
            android:permission="android.permission.FOREGROUND_SERVICE"
            android:exported="true"
            />
        <meta-data
            android:name="flutterEmbedding"
            android:value="2" />
```

### iOS

1) Add the following lines to `AppDelegate` class:
```swift
import background_locator

func registerPlugins(registry: FlutterPluginRegistry) -> () {
    GeneratedPluginRegistrant.register(with: registry)
}

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GeneratedPluginRegistrant.register(with: self)
    BackgroundLocatorPlugin.setPluginRegistrantCallback(registerPlugins)
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}

```

2) In app setting enable `Background Modes` and check `Location Updates`.

3) In `Info.plist` add Key for using location service:

```
NSLocationAlwaysAndWhenInUseUsageDescription
NSLocationWhenInUseUsageDescription
```

## Usage
1) Initialize plugin:
```dart
@override
  void initState() {
    super.initState();

    IsolateNameServer.registerPortWithName(port.sendPort, 'LocatorIsolate');
    port.listen((dynamic data) {
      // do something with data
    });
    initPlatformState();
  }
  
Future<void> initPlatformState() async {
    await BackgroundLocator.initialize();
  }
```

2) Call `BackgroundLocator.registerLocationUpdate(callback);` with similar callback:
```dart
static void callback(LocationDto locationDto) async {
    final SendPort send = IsolateNameServer.lookupPortByName('LocatorIsolate');
    send?.send(locationDto);
  }
```

**Note:** Before starting the plugin you have to get location permission.

3) Don't forget to unregister the locator when you are done:
```dart
BackgroundLocator.unRegisterLocationUpdate();
```

**LocationSettings** options:

`accuracy`: The accuracy of location, Default is max accuracy NAVIGATION.

`interval`: Interval of retrieving location update in second. Only applies for android. Default is 5 second.

`distanceFilter`: distance in meter to trigger location update, Default is 0 meter.

`notificationTitle`: Title of the notification. Only applies for android. Default is 'Start Location Tracking'.

`notificationMsg`: Message of notification. Only applies for android. Default is 'Track location in background'.

`notificationIcon`: Icon name for notification. Only applies for android. The icon should be in 'mipmap' Directory. Default is app icon.

`wakeLockTime`: Time for living service in background in meter. Only applies in android. Default is 60 minute.

`autoStop`: If true locator will stop as soon as app goes to background.
