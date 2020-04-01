
# 🛰️ background_locator

A Flutter plugin for updating location even if the app is killed

## How it work 🙋
 - The plugins register an Isolate as [START_STICKY](https://developer.android.com/reference/android/app/Service.html#START_STICKY) so it can't be killed by Android 

 - It will execute a `static` callback function in your dart code where you will execute everything except any UI operation (`static` because it can't be accessed otherwise 😉)

 - To change any UI element, you will need to listen to the Isolate port that will be called when the callback function send something to this port

> More info on Isolate : [Isolates and Event Loops - Flutter in Focus](https://www.youtube.com/watch?v=vl_AaCgudcY)


#### 📝 Example : 
At every callback, append the location in a file,
on app start, recover the position list from this file and put it on the map as a polyline


## Install 📥:
##### 1. Depend on it
Add this to your package's pubspec.yaml file:
```ruby
dependencies:
  background_locator: ^1.1.2+1
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


## Setup ⚙️:

### 🤖 Android

1) Make sure to have the following permission inside your `AndroidManifest.xml`:
```xml
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```
- **INTERNET** for something 📡
-  **ACCESS_FINE_LOCATION** to have the GPS 🛰️
> Note: you can set ACCESS_COARSE_LOCATION but you will have to modify the accuracy below  `LocationAccuracy.LOW` (I suppose)
- **ACCESS_BACKGROUND_LOCATION** To get update in background :emoji_background: (the plugins really need this since it is a service constantly running?)
- **WAKE_LOCK** to not sleep while getting the GPS 😴
- **FOREGROUND_SERVICE** To let the plugins operate as a service ⚙️


2) Add the following lines to your `AndroidManifest.xml` to give the plugins's services their liberty:
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


3) To work with other plugins, even when the application is killed (using `path_provider` for example), create a new file inside `android/app/src/main/kotlin/com/example/YourFlutterApp/` named `LocationService.kt` and fill it with :
```kotlin
package rekab.app.background_locator_example

import io.flutter.app.FlutterApplication
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.PluginRegistrantCallback
import io.flutter.plugins.pathprovider.PathProviderPlugin
import io.flutter.view.FlutterMain
import rekab.app.background_locator.LocatorService

class LocationService : FlutterApplication(), PluginRegistrantCallback {
    override fun onCreate() {
        super.onCreate()
        LocatorService.setPluginRegistrant(this)
        FlutterMain.startInitialization(this)
    }
    
    override fun registerWith(registry: PluginRegistry?) {
        if (!registry!!.hasPlugin("io.flutter.plugins.pathprovider")) {
            PathProviderPlugin.registerWith(registry!!.registrarFor("io.flutter.plugins.pathprovider"))
        }
    }
}
```


4) Again, inside `AndroidManifest.xml` change `android:name` from `io.flutter.app.FlutterApplication` to `rekab.app.background_locator_example.LocationService` to register the step 3:
```xml
<application
        android:name="rekab.app.background_locator_example.LocationService"
```


**Great ! 👍** Its now your turn, inside your dart file create a callback function that the plugin will call in background


##### 📝 Example :

```dart
import 'package:path_provider/path_provider.dart';

static void callback(LocationDto locationDto) async {
  print('location in dart: ${locationDto.toString()}');
  final SendPort send = IsolateNameServer.lookupPortByName(_isolateName);
  send?.send(locationDto);

  final file = await _getTempLogFile();
  await file.writeAsString(locationDto.toString(), mode: FileMode.append);
}
```


### 🍏 iOS

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


## How to use it 👨‍💻:

> See the best practice here : [PUT EXAMPLE LINK](example)


1) Initialize plugin:
```dart
static const String _isolateName = "LocatorIsolate";
ReceivePort port = ReceivePort();

@override
  void initState() {
    super.initState();
    
    //override the previous port by this one, we can't
    //re-use the previous port
    if (IsolateNameServer.lookupPortByName(_isolateName) != null) {
        IsolateNameServer.removePortNameMapping(_isolateName);
    }
    
    IsolateNameServer.registerPortWithName(port.sendPort, _isolateName);
    port.listen((dynamic data) {
      // do something with data
    });
    initPlatformState();
  }
  
Future<void> initPlatformState() async {
    await BackgroundLocator.initialize();
}
```


2) Create the callback function :
```dart
static void callback(LocationDto locationDto) async {
    final SendPort send = IsolateNameServer.lookupPortByName(_isolateName);
    send?.send(locationDto);
    //the '?' check if send is null before executing it
  }
```


3) Start the location service :
> Before starting the plugin make sure to have the necessary permission

```dart
//Somewhere in your code
startLocationService();

void startLocationService(){
    BackgroundLocator.registerLocationUpdate(
        callback,
        //androidNotificationCallback is executed whenever
        //the user click on the notification
        //(this should be optional)
        androidNotificationCallback: notificationCallback,
        settings: LocationSettings(
            //Scroll down to see the different options
            notificationTitle: "Start Location Tracking example",
            notificationMsg: "Track location in background exapmle",
            wakeLockTime: 20,
            autoStop: false,
            interval: 1
        ),
    );
}
```


3) Unregister the service when you are done:
```dart
@override
void dispose() {
    IsolateNameServer.removePortNameMapping(_isolateName);
    BackgroundLocator.unRegisterLocationUpdate();
    super.dispose();
}
```


### LocationSettings options ⚙️:

**`accuracy`:** Accuracy of location, default : `LocationAccuracy.NAVIGATION`
- `LocationAccuracy.NAVIGATION`
- `LocationAccuracy.HIGH`
- `LocationAccuracy.BALANCED`
- `LocationAccuracy.POWERSAVE`
- `LocationAccuracy.LOW`

**`interval`:** Interval of request the service make in second only for Android, default : `5`

**`distanceFilter`:** distance in meter to trigger the callback, Default : `0`

**`notificationTitle`:** Title of the notification only for Android, default : `'Start Location Tracking'`

**`notificationMsg`:** Message of the notification, only for Android default : `'Track location in background'`

**`notificationIcon`:** Image of the notification, only for Android, the icon should be in 'mipmap' directory. Default : App icon

**`wakeLockTime`:** Timeout in minutes for the service, only for Android, default : `60`

**`autoStop`:** If true the service will stop as soon as app goes to background
