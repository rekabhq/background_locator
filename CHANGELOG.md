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
