## 1.6.12
* Fixes onStatusChanged crash;
* Fixed issue #94;
* Fix importing path_provider in ios example;
* Fix issue #266;
* Fix Android 12 location permission handling;
* Add network location provider even gps location data not update;

## 1.6.6
* Fix invoking method on flutter channel when engine is not ready; (#254)

## 1.6.5
* Fix returning result for unRegisterPlugin method on Android; (#262)

## 1.6.4
* Fix triggering location update when plugin is stopped; (#258)
* Fix saving service start stop status; (#259)

## 1.6.3
* Bug fixes;

## 1.6.2+1-beta
* Bring back init and dispose callback;

## 1.6.1+1-beta
* Fix crash on Android location client causing by change in status of location provider;

## 1.6.0+2-beta
* Fix crash on start;

## 1.6.0+1-beta
* Use new flutter engine;
* Fix start stop bug which prevents correct state in plugin;

## 1.5.0+1
* Add null safety support;

## 1.4.0+1
* Set default value for autoStop;
* Fix register and unregister futures never complete on Android;
* Fix Doze problem for Android >= 10;

## 1.3.2+1
* Fix compile error on sdk 30;
* Fix app stop locating on android sdk 30 in background;

## 1.3.0+1
* Add google location client as option;
* Several bug fixes;

## 1.2.2+1
* Add platform specific settings;
* Add ability to update android notification;
* Ability to showsBackgroundLocationIndicator on iOS;

## 1.1.13+1
* add isServiceRunning method;

## 1.1.12+1
* Added support for big text in Android notification;

## 1.1.11+1
* Fix getCallbackHandle bug which caused some callbacks not getting executed;

## 1.1.10+1
* Add region monitoring for iOS to get location info while app is terminated;
* Minor iOS bug fix;
* Add a way to use 3rd party plugins while app is terminated in iOS;

## 1.1.7+1
* Add notification icon color;
* Add isMocked property on location model;
* Add channel name property on location dto;

## 1.1.5+1
* Fix crash in onStartCommand caused by null intent on Android;
* Fix getting several unwanted position on iOS;

## 1.1.3+1
* Add possibility to restart locator service after reboot;
* Fix triggering android notification callback with wrong notification;

## 1.1.2+2
* Fix optional android notification callback.

## 1.1.2+1
* Fix accessing other plugins when app is terminated.

## 1.1.1+1
* ‌Fix Callback is not triggered in iOS.

## 1.1.0+1
* Add callback for android notification.

## 1.0.1+2
* Fix crash on detach.

## 1.0.1+1
* Add isRegistered method.
* Bug fixes.

## 1.0.0+1
* Add auto stop feature.
* Update flutter plugin library to version 2.

## 0.0.4-beta
* Add parameter to setting to change android wakelock time.
* Prevent service from registering twice.

## 0.0.3-beta
Change where location access requested.

## 0.0.2-beta

* Improvements.

## 0.0.1-beta

* initial release.
