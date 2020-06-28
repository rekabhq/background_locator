#import "BackgroundLocatorPlugin.h"
#import "Globals.h"

@implementation BackgroundLocatorPlugin {
    NSMutableArray<NSDictionary<NSString*,NSNumber*>*> *_eventQueue;
    FlutterEngine *_headlessRunner;
    FlutterMethodChannel *_callbackChannel;
    FlutterMethodChannel *_mainChannel;
    NSObject<FlutterPluginRegistrar> *_registrar;
    CLLocationManager *_locationManager;
    CLLocation* _lastLocation;
}

static FlutterPluginRegistrantCallback registerPlugins = nil;
static BOOL initialized = NO;
static BOOL observingRegions = NO;
static BackgroundLocatorPlugin *instance = nil;

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

- (void)handleMethodCall:(FlutterMethodCall *)call
                  result:(FlutterResult)result {
    MethodCallHelper *callHelper = [[MethodCallHelper alloc] init];
    [callHelper handleMethodCall:call result:result delegate:self];
}

//https://medium.com/@calvinlin_96474/ios-11-continuous-background-location-update-by-swift-4-12ce3ac603e3
// iOS will launch the app when new location received
- (BOOL)application:(UIApplication *)application
didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Check to see if we're being launched due to a location event.
    if (launchOptions[UIApplicationLaunchOptionsLocationKey] != nil) {
        // Restart the headless service.
        [self startLocatorService:[self getCallbackDispatcherHandle]];
        observingRegions = YES;
    } else {
        if(observingRegions == YES) {
            [self prepareLocationManager];
            [self removeLocator];
            observingRegions = NO;
            [_locationManager startUpdatingLocation];
        }
    }
    
    // Note: if we return NO, this vetos the launch of the application.
    return YES;
}

-(void)applicationWillTerminate:(UIApplication *)application {
    [self observeRegionForLocation:_lastLocation];
}

- (void) observeRegionForLocation:(CLLocation *)location {
    CLRegion* region = [[CLCircularRegion alloc] initWithCenter:location.coordinate radius:0 identifier:@"region"];
    region.notifyOnEntry = false;
    region.notifyOnExit = true;
    [_locationManager startMonitoringForRegion:region];
}

- (void) prepareLocationMap:(CLLocation*) location {
    _lastLocation = location;
    NSTimeInterval timeInSeconds = [location.timestamp timeIntervalSince1970];
    NSDictionary<NSString*,NSNumber*>* locationMap = @{
                                                       kArgLatitude: @(location.coordinate.latitude),
                                                       kArgLongitude: @(location.coordinate.longitude),
                                                       kArgAccuracy: @(location.horizontalAccuracy),
                                                       kArgAltitude: @(location.altitude),
                                                       kArgSpeed: @(location.speed),
                                                       kArgSpeedAccuracy: @(0.0),
                                                       kArgHeading: @(location.course),
                                                       kArgTime: @(((double) timeInSeconds) * 1000.0)  // in milliseconds since the epoch
                                                       };
    if (initialized) {
        [self sendLocationEvent:locationMap];
    } else {
        [_eventQueue addObject:locationMap];
    }
}

#pragma mark LocationManagerDelegate Methods
- (void)locationManager:(CLLocationManager *)manager
     didUpdateLocations:(NSArray<CLLocation *> *)locations {
    if (locations.count > 0) {
        CLLocation* location = [locations objectAtIndex:0];
        [self prepareLocationMap: location];
        if(observingRegions) {
            [self observeRegionForLocation: location];
            [_locationManager stopUpdatingLocation];
        }
    }
}

- (void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region {
    [_locationManager stopMonitoringForRegion:region];
    [_locationManager startUpdatingLocation];
}

#pragma mark LocatorPlugin Methods
- (void) sendLocationEvent: (NSDictionary<NSString*,NSNumber*>*)location {
    NSDictionary *map = @{
                     kArgCallback : @([self getCallbackHandle:kCallbackKey]),
                     kArgLocation: location
                     };
    [_callbackChannel invokeMethod:kBCMSendLocation arguments:map];
}

- (instancetype)init:(NSObject<FlutterPluginRegistrar> *)registrar {
    self = [super init];
    _eventQueue = [[NSMutableArray alloc] init];
    
    _headlessRunner = [[FlutterEngine alloc] initWithName:@"LocatorIsolate" project:nil allowHeadlessExecution:YES];
    _registrar = registrar;
    [self prepareLocationManager];
    
    _mainChannel = [FlutterMethodChannel methodChannelWithName:kChannelId
                                               binaryMessenger:[registrar messenger]];
    [registrar addMethodCallDelegate:self channel:_mainChannel];
    
    _callbackChannel =
    [FlutterMethodChannel methodChannelWithName:kBackgroundChannelId
                                binaryMessenger:[_headlessRunner binaryMessenger] ];
    return self;
}

- (void) prepareLocationManager {
    _locationManager = [[CLLocationManager alloc] init];
    [_locationManager setDelegate:self];
    _locationManager.pausesLocationUpdatesAutomatically = NO;
    if (@available(iOS 9.0, *)) {
        _locationManager.allowsBackgroundLocationUpdates = YES;
    }
}


- (int64_t)getCallbackDispatcherHandle {
    id handle = [[NSUserDefaults standardUserDefaults]
                 objectForKey: kCallbackDispatcherKey];
    if (handle == nil) {
        return 0;
    }
    return [handle longLongValue];
}

- (void)setCallbackDispatcherHandle:(int64_t)handle {
    [[NSUserDefaults standardUserDefaults]
     setObject:[NSNumber numberWithLongLong:handle]
     forKey:kCallbackDispatcherKey];
}

- (int64_t)getCallbackHandle:(NSString *)key  {
    id handle = [[NSUserDefaults standardUserDefaults]
                 objectForKey: key];
    if (handle == nil) {
        return 0;
    }
    return [handle longLongValue];
}

- (void)setCallbackHandle:(int64_t)handle
                      key:(NSString *)key {
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

#pragma mark MethodCallHelperDelegate

- (void)startLocatorService:(int64_t)handle {
    [self setCallbackDispatcherHandle:handle];
    FlutterCallbackInformation *info = [FlutterCallbackCache lookupCallbackInformation:handle];
    NSAssert(info != nil, @"failed to find callback");
    
    NSString *entrypoint = info.callbackName;
    NSString *uri = info.callbackLibraryPath;
    [_headlessRunner runWithEntrypoint:entrypoint libraryURI:uri];
    NSAssert(registerPlugins != nil, @"failed to set registerPlugins");
    
    // Once our hremoveLocatoreadless runner has been started, we need to register the application's plugins
    // with the runner in order for them to work on the background isolate. `registerPlugins` is
    // a callback set from AppDelegate.m in the main application. This callback should register
    // all relevant plugins (excluding those which require UI).
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        registerPlugins(_headlessRunner);
    });
    [_registrar addMethodCallDelegate:self channel:_callbackChannel];
}

- (void) setInitialized {
    @synchronized(self) {
        initialized = YES;
    }
}

- (void)registerLocator:(int64_t)callback
           initCallback:(int64_t)initCallback
  initialDataDictionary:(NSDictionary*)initialDataDictionary
        disposeCallback:(int64_t)disposeCallback
               settings: (NSDictionary*)settings {
    [self->_locationManager requestAlwaysAuthorization];
        
    long accuracyKey = [[settings objectForKey:kArgAccuracy] longValue];
    CLLocationAccuracy accuracy = [self getAccuracy:accuracyKey];
    double distanceFilter= [[settings objectForKey:kArgDistanceFilter] doubleValue];

    _locationManager.desiredAccuracy = accuracy;
    _locationManager.distanceFilter = distanceFilter;

    [self setCallbackHandle:callback key:kCallbackKey];
    [self setCallbackHandle:initCallback key:kInitCallbackKey];
    [self setCallbackHandle:disposeCallback key:kDisposeCallbackKey];
    NSDictionary *map = @{
                     kArgInitCallback : @([self getCallbackHandle:kInitCallbackKey]),
                     kArgInitDataCallback: initialDataDictionary
                     };
    [_callbackChannel invokeMethod:kBCMInit arguments:map];
    [_locationManager startUpdatingLocation];
}

- (void)removeLocator {
    @synchronized (self) {
        if(initialized){
            [_locationManager stopUpdatingLocation];
            for (CLRegion* region in [_locationManager monitoredRegions]) {
                [_locationManager stopMonitoringForRegion:region];
            }
            NSDictionary *map = @{
                             kArgDisposeCallback : @([self getCallbackHandle:kDisposeCallbackKey])
                             };
            [_callbackChannel invokeMethod:kBCMDispose arguments:map];
        }
    }
}

- (BOOL)isLocatorRegistered{
    return initialized;
}

@end
