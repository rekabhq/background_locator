#import "BackgroundLocatorPlugin.h"

@implementation BackgroundLocatorPlugin {
    NSMutableArray<NSDictionary<NSString*,NSNumber*>*> *_eventQueue;
    FlutterEngine *_headlessRunner;
    FlutterMethodChannel *_callbackChannel;
    FlutterMethodChannel *_mainChannel;
    NSObject<FlutterPluginRegistrar> *_registrar;
    CLLocationManager *_locationManager;
}

static FlutterPluginRegistrantCallback registerPlugins = nil;
static BOOL initialized = NO;
static BackgroundLocatorPlugin *instance = nil;

NSString *_kCallbackDispatcherKey = @"callback_dispatcher_handle_key";
NSString *_kCallbackKey = @"callback_handle_key";
NSString *_kInitCallbackKey = @"init_callback_handle_key";
NSString *_kInitDataCallbackKey = @"init_data_callback_key";
NSString *_kDisposeCallbackKey = @"dispose_callback_handle_key";

NSString *CHANNEL_ID = @"app.rekab/locator_plugin";
NSString *BACKGROUND_CHANNEL_ID = @"app.rekab/locator_plugin_background";
NSString *METHOD_SERVICE_INITIALIZED = @"LocatorService.initialized";
NSString *METHOD_PLUGIN_INITIALIZE_SERVICE = @"LocatorPlugin.initializeService";
NSString *METHOD_PLUGIN_REGISTER_LOCATION_UPDATE = @"LocatorPlugin.registerLocationUpdate";
NSString *METHOD_PLUGIN_UN_REGISTER_LOCATION_UPDATE = @"LocatorPlugin.unRegisterLocationUpdate";
NSString *METHOD_PLUGIN_IS_REGISTER_LOCATION_UPDATE = @"LocatorPlugin.isRegisterLocationUpdate";
NSString *ARG_LATITUDE = @"latitude";
NSString *ARG_LONGITUDE = @"longitude";
NSString *ARG_ACCURACY = @"accuracy";
NSString *ARG_ALTITUDE = @"altitude";
NSString *ARG_SPEED = @"speed";
NSString *ARG_SPEED_ACCURACY = @"speed_accuracy";
NSString *ARG_HEADING = @"heading";
NSString *ARG_TIME = @"time";
NSString *ARG_CALLBACK = @"callback";
NSString *ARG_INIT_CALLBACK = @"initCallback";
NSString *ARG_INIT_DATA_CALLBACK = @"initDataCallback";
NSString *ARG_DISPOSE_CALLBACK = @"disposeCallback";
NSString *ARG_LOCATION = @"location";
NSString *ARG_SETTINGS = @"settings";
NSString *ARG_CALLBACK_DISPATCHER = @"callbackDispatcher";
NSString *ARG_INTERVAL = @"interval";
NSString *ARG_DISTANCE_FILTER = @"distanceFilter";
NSString *BCM_SEND_LOCATION = @"BCM_SEND_LOCATION";
NSString *BCM_INIT = @"BCM_INIT";
NSString *BCM_DISPOSE = @"BCM_DISPOSE";


#pragma mark FlutterPlugin Methods

+ (void)registerWithRegistrar:(nonnull NSObject<FlutterPluginRegistrar> *)registrar {
    @synchronized(self) {
        if (instance == nil) {
            instance = [[BackgroundLocatorPlugin alloc] init:registrar];
            [registrar addApplicationDelegate:instance];
        }
    }
}

+ (void)setPluginRegistrantCallback:(FlutterPluginRegistrantCallback)callback {
    registerPlugins = callback;
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    NSDictionary *arguments = call.arguments;
    if ([METHOD_PLUGIN_INITIALIZE_SERVICE isEqualToString:call.method]) {
        int64_t callbackDispatcher = [[arguments objectForKey:ARG_CALLBACK_DISPATCHER] longLongValue];
        [self startLocatorService: callbackDispatcher];
        result(@(YES));
    } else if ([METHOD_SERVICE_INITIALIZED isEqualToString:call.method]) {
        @synchronized(self) {
            initialized = YES;

            while ([_eventQueue count] > 0) {
                NSDictionary<NSString*,NSNumber*>* event = _eventQueue[0];
                [_eventQueue removeObjectAtIndex:0];
                [self sendLocationEvent:event];
            }
        }
        result(nil);
    } else if ([METHOD_PLUGIN_REGISTER_LOCATION_UPDATE isEqualToString:call.method]) {
        int64_t callbackHandle = [[arguments objectForKey:ARG_CALLBACK] longLongValue];
        int64_t initCallbackHandle = [[arguments objectForKey:ARG_INIT_CALLBACK] longLongValue];
        NSDictionary *initialDataDictionary = [arguments objectForKey:ARG_INIT_DATA_CALLBACK];
        int64_t disposeCallbackHandle = [[arguments objectForKey:ARG_DISPOSE_CALLBACK] longLongValue];
        NSDictionary *settings = [arguments objectForKey:ARG_SETTINGS];

        [self registerLocator:callbackHandle initCallback:initCallbackHandle initialDataDictionary:initialDataDictionary disposeCallback:disposeCallbackHandle settings:settings];
        result(@(YES));
    } else if ([METHOD_PLUGIN_UN_REGISTER_LOCATION_UPDATE isEqualToString:call.method]) {
        [self removeLocator];
        result(@(YES));
    } else if ([METHOD_PLUGIN_IS_REGISTER_LOCATION_UPDATE isEqualToString:call.method]) {
        BOOL val = [self isRegisterLocator];
        result(@(val));
    } else {
        result(FlutterMethodNotImplemented);
    }
}

//https://medium.com/@calvinlin_96474/ios-11-continuous-background-location-update-by-swift-4-12ce3ac603e3
// iOS will launch the app when new location received
- (BOOL)application:(UIApplication *)application
didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Check to see if we're being launched due to a location event.
    if (launchOptions[UIApplicationLaunchOptionsLocationKey] != nil) {
        // Restart the headless service.
        [self startLocatorService:[self getCallbackDispatcherHandle]];
    }
    
    // Note: if we return NO, this vetos the launch of the application.
    return YES;
}

#pragma mark LocationManagerDelegate Methods
- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray<CLLocation *> *)locations {
    for (int i = 0; i < locations.count; i++) {
        CLLocation *location = [locations objectAtIndex:i];
        NSTimeInterval timeInSeconds = [location.timestamp timeIntervalSince1970];
        NSDictionary<NSString*,NSNumber*>* locationMap = @{
                                                           ARG_LATITUDE: @(location.coordinate.latitude),
                                                           ARG_LONGITUDE: @(location.coordinate.longitude),
                                                           ARG_ACCURACY: @(location.horizontalAccuracy),
                                                           ARG_ALTITUDE: @(location.altitude),
                                                           ARG_SPEED: @(location.speed),
                                                           ARG_SPEED_ACCURACY: @(0.0),
                                                           ARG_HEADING: @(location.course),
                                                           ARG_TIME: @(((double) timeInSeconds) * 1000.0)  // in milliseconds since the epoch
                                                           };
        if (initialized) {
            [self sendLocationEvent:locationMap];
        } else {
            [_eventQueue addObject:locationMap];
        }
    }
}

#pragma mark LocatorPlugin Methods
- (void) sendLocationEvent: (NSDictionary<NSString*,NSNumber*>*)location {
    NSDictionary *map = @{
                     ARG_CALLBACK : @([self getCallbackHandle:_kCallbackKey]),
                     ARG_LOCATION: location
                     };
    [_callbackChannel invokeMethod:BCM_SEND_LOCATION arguments:map];
}

- (instancetype)init:(NSObject<FlutterPluginRegistrar> *)registrar {
    self = [super init];
    _eventQueue = [[NSMutableArray alloc] init];
    _locationManager = [[CLLocationManager alloc] init];
    [_locationManager setDelegate:self];
    _locationManager.pausesLocationUpdatesAutomatically = NO;
    if (@available(iOS 9.0, *)) {
        _locationManager.allowsBackgroundLocationUpdates = YES;
    }
    
    _headlessRunner = [[FlutterEngine alloc] initWithName:@"LocatorIsolate" project:nil allowHeadlessExecution:YES];
    _registrar = registrar;
    
    _mainChannel = [FlutterMethodChannel methodChannelWithName:CHANNEL_ID
                                               binaryMessenger:[registrar messenger]];
    [registrar addMethodCallDelegate:self channel:_mainChannel];
    
    _callbackChannel =
    [FlutterMethodChannel methodChannelWithName:BACKGROUND_CHANNEL_ID
                                binaryMessenger:[_headlessRunner binaryMessenger] ];
    return self;
}

- (void)startLocatorService:(int64_t)handle {
    [self setCallbackDispatcherHandle:handle];
    FlutterCallbackInformation *info = [FlutterCallbackCache lookupCallbackInformation:handle];
    NSAssert(info != nil, @"failed to find callback");
    
    NSString *entrypoint = info.callbackName;
    NSString *uri = info.callbackLibraryPath;
    [_headlessRunner runWithEntrypoint:entrypoint libraryURI:uri];
    NSAssert(registerPlugins != nil, @"failed to set registerPlugins");
    
    // Once our headless runner has been started, we need to register the application's plugins
    // with the runner in order for them to work on the background isolate. `registerPlugins` is
    // a callback set from AppDelegate.m in the main application. This callback should register
    // all relevant plugins (excluding those which require UI).
    registerPlugins(_headlessRunner);
    [_registrar addMethodCallDelegate:self channel:_callbackChannel];
}

- (void)registerLocator:(int64_t)callback initCallback:(int64_t)initCallback initialDataDictionary:(NSDictionary*)initialDataDictionary disposeCallback:(int64_t)disposeCallback settings: (NSDictionary*)settings {
    [self->_locationManager requestAlwaysAuthorization];
        
    long accuracyKey = [[settings objectForKey:ARG_ACCURACY] longValue];
    CLLocationAccuracy accuracy = [self getAccuracy:accuracyKey];
    double distanceFilter = [[settings objectForKey:ARG_DISTANCE_FILTER] doubleValue];

    _locationManager.desiredAccuracy = accuracy;
    _locationManager.distanceFilter = distanceFilter;

    [self setCallbackHandle:callback key:_kCallbackKey];
    [self setCallbackHandle:initCallback key:_kInitCallbackKey];
    [self setCallbackHandle:disposeCallback key:_kDisposeCallbackKey];
    NSDictionary *map = @{
                     ARG_INIT_CALLBACK : @([self getCallbackHandle:_kInitCallbackKey]),
                     ARG_INIT_DATA_CALLBACK: initialDataDictionary
                     };
    [_callbackChannel invokeMethod:BCM_INIT arguments:map];
    [_locationManager startUpdatingLocation];
    [_locationManager startMonitoringSignificantLocationChanges];
}

- (void)removeLocator {
    [_locationManager stopUpdatingLocation];
    NSDictionary *map = @{
                     ARG_DISPOSE_CALLBACK : @([self getCallbackHandle:_kDisposeCallbackKey])
                     };
    [_callbackChannel invokeMethod:BCM_DISPOSE arguments:map];
}

- (BOOL)isRegisterLocator{
    return initialized;
}

- (int64_t)getCallbackDispatcherHandle {
    id handle = [[NSUserDefaults standardUserDefaults]
                 objectForKey: _kCallbackDispatcherKey];
    if (handle == nil) {
        return 0;
    }
    return [handle longLongValue];
}

- (void)setCallbackDispatcherHandle:(int64_t)handle {
    [[NSUserDefaults standardUserDefaults]
     setObject:[NSNumber numberWithLongLong:handle]
     forKey:_kCallbackDispatcherKey];
}

- (int64_t)getCallbackHandle:(NSString *)key  {
    id handle = [[NSUserDefaults standardUserDefaults]
                 objectForKey: key];
    if (handle == nil) {
        return 0;
    }
    return [handle longLongValue];
}

- (void)setCallbackHandle:(int64_t)handle key:(NSString *)key {
    //TODO
    [[NSUserDefaults standardUserDefaults]
     setObject:[NSNumber numberWithLongLong:handle]
     forKey: key];
}

- (CLLocationAccuracy) getAccuracy:(long)key {
    switch (key) {
        case 0:
            return kCLLocationAccuracyKilometer;
        case 1:
            return kCLLocationAccuracyHundredMeters;
        case 2:
            return kCLLocationAccuracyNearestTenMeters;
        case 3:
            return kCLLocationAccuracyBest;
        case 4:
            return kCLLocationAccuracyBestForNavigation;
        default:
            return kCLLocationAccuracyBestForNavigation;
    }
}

@end
