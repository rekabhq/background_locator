import 'locator_settings.dart';

class IOSSettings extends LocatorSettings {
  IOSSettings(
      {LocationAccuracy accuracy = LocationAccuracy.NAVIGATION,
      int interval = 5,
      double distanceFilter = 0,
      bool autoStop = false})
      : super(
            accuracy: accuracy,
            interval: interval,
            distanceFilter: distanceFilter,
            autoStop: autoStop); //minutes
}
