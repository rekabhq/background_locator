import 'keys.dart';

class LocationDto {
  final double latitude;
  final double longitude;
  final double accuracy;
  final double altitude;
  final double speed;
  final double speedAccuracy;
  final double heading;

  LocationDto._(this.latitude, this.longitude, this.accuracy, this.altitude,
      this.speed, this.speedAccuracy, this.heading);

  factory LocationDto.fromJson(Map<dynamic, dynamic> json) {
    return LocationDto._(
        json[Keys.ARG_LATITUDE],
        json[Keys.ARG_LONGITUDE],
        json[Keys.ARG_ACCURACY],
        json[Keys.ARG_ALTITUDE],
        json[Keys.ARG_SPEED],
        json[Keys.ARG_SPEED_ACCURACY],
        json[Keys.ARG_HEADING]);
  }

  @override
  String toString() {
    return 'LocationDto{latitude: $latitude, longitude: $longitude, accuracy: $accuracy, altitude: $altitude, speed: $speed, speedAccuracy: $speedAccuracy, heading: $heading}';
  }
}
