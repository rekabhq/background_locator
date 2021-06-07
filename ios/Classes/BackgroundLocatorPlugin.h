#import <Flutter/Flutter.h>
#import <CoreLocation/CoreLocation.h>
#import "MethodCallHelper.h"

@interface BackgroundLocatorPlugin : NSObject<FlutterPlugin, CLLocationManagerDelegate, MethodCallHelperDelegate>

+ (BackgroundLocatorPlugin*_Nullable) getInstance;
- (void)invokeMethod:(NSString*_Nonnull)method arguments:(id _Nullable)arguments;

@end
