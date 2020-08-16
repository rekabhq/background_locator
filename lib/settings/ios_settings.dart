import 'package:background_locator/keys.dart';

import 'locator_settings.dart';

class IOSSettings extends LocatorSettings {
  /// [accuracy] The accuracy of location, Default is max accuracy NAVIGATION.
  ///
  /// [distanceFilter] distance in meter to trigger location update, Default is 0 meter.
  const IOSSettings(
      {LocationAccuracy accuracy = LocationAccuracy.NAVIGATION,
      double distanceFilter = 0})
      : super(accuracy: accuracy, distanceFilter: distanceFilter); //minutes

  Map<String, dynamic> toMap() {
    return {
      Keys.SETTINGS_ACCURACY: accuracy.value,
      Keys.SETTINGS_DISTANCE_FILTER: distanceFilter,
    };
  }
}
