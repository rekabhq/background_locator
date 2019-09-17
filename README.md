# background_locator

A Flutter plugin for updating location in background.

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
```

3) Create an `Application` class and add the following lines:
```kotlin
class Application: FlutterApplication(), PluginRegistry.PluginRegistrantCallback {
    override fun onCreate() {
        super.onCreate()
        LocatorService.setPluginRegistrant(this)
    }
    override fun registerWith(p0: PluginRegistry?) {
        GeneratedPluginRegistrant.registerWith(p0)
    }
}
```

**Note**: Don't forget to reference your `Application` class in `AndroidManifest.xml`:
```xml
<application
        android:name=".Application"
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

3) Don't forget to unregister the locator when you are done:
```dart
BackgroundLocator.registerLocationUpdate(callback);
```
