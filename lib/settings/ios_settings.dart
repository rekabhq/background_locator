import 'package:background_locator/keys.dart';

import 'locator_settings.dart';

class IOSSettings extends LocatorSettings {
  /// [accuracy] The accuracy of location, Default is max accuracy NAVIGATION.
  ///
  /// [distanceFilter] distance in meter to trigger location update, Default is 0 meter.
  ///
  /// [showsBackgroundLocationIndicator] The background location usage indicator is a blue bar or a blue pill in the status bar on iOS. Default is false.

  final bool showsBackgroundLocationIndicator;
  const IOSSettings({
    LocationAccuracy accuracy = LocationAccuracy.NAVIGATION,
    double distanceFilter = 0,
    this.showsBackgroundLocationIndicator = false,
  }) : super(accuracy: accuracy, distanceFilter: distanceFilter); //minutes

  Map<String, dynamic> toMap() {
    return {
      Keys.SETTINGS_ACCURACY: accuracy.value,
      Keys.SETTINGS_DISTANCE_FILTER: distanceFilter,
      Keys.SETTINGS_IOS_SHOWS_BACKGROUND_LOCATION_INDICATOR:
          showsBackgroundLocationIndicator,
    };
  }
}
