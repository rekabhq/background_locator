class LocationAccuracy {
  const LocationAccuracy._internal(this.value);

  final int value;

  static const POWERSAVE = LocationAccuracy._internal(0);
  static const LOW = LocationAccuracy._internal(1);
  static const BALANCED = LocationAccuracy._internal(2);
  static const HIGH = LocationAccuracy._internal(3);
  static const NAVIGATION = LocationAccuracy._internal(4);
}

class LocatorSettings {
  final LocationAccuracy accuracy;
  final int interval; //seconds
  final double distanceFilter;
  final bool autoStop;

  /// [accuracy] The accuracy of location, Default is max accuracy NAVIGATION.
  ///
  /// [interval] Interval of retrieving location update in second. Only applies for android. Default is 5 second.
  ///
  /// [distanceFilter] distance in meter to trigger location update, Default is 0 meter.
  ///
  /// [autoStop] If true locator will stop as soon as app goes to background.
  LocatorSettings(
      {this.accuracy, this.interval, this.distanceFilter, this.autoStop});
}
