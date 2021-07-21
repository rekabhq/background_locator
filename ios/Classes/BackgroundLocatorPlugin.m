#import "BackgroundLocatorPlugin.h"
#import "Globals.h"
#import "Utils/Util.h"
#import "Preferences/PreferencesManager.h"
#import "InitPluggable.h"
#import "DisposePluggable.h"

@implementation BackgroundLocatorPlugin {
    FlutterEngine *_headlessRunner;
    FlutterMethodChannel *_callbackChannel;
    FlutterMethodChannel *_mainChannel;
    NSObject<FlutterPluginRegistrar> *_registrar;
    CLLocationManager *_locationManager;
    CLLocation* _lastLocation;
}

static FlutterPluginRegistrantCallback registerPlugins = nil;
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

+ (BackgroundLocatorPlugin *) getInstance {
    return instance;
}

- (void)invokeMethod:(NSString*_Nonnull)method arguments:(id _Nullable)arguments {
    // Return if flutter engine is not ready
    NSString *isolateId = [_headlessRunner isolateId];
    if (_callbackChannel == nil || isolateId == nil) {
        return;
    }
    
    [_callbackChannel invokeMethod:method arguments:arguments];
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
        [self startLocatorService:[PreferencesManager getCallbackDispatcherHandle]];
        [PreferencesManager setObservingRegion:YES];
    } else if([PreferencesManager isObservingRegion]) {
        [self prepareLocationManager];
        [self removeLocator];
        [PreferencesManager setObservingRegion:NO];
        [_locationManager startUpdatingLocation];
    }
    
    // Note: if we return NO, this vetos the launch of the application.
    return YES;
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    if ([PreferencesManager isServiceRunning]) {
        [_locationManager startMonitoringSignificantLocationChanges];
    }
}

-(void)applicationWillTerminate:(UIApplication *)application {
    [self observeRegionForLocation:_lastLocation];
}

- (void) observeRegionForLocation:(CLLocation *)location {
    double distanceFilter = [PreferencesManager getDistanceFilter];
    CLRegion* region = [[CLCircularRegion alloc] initWithCenter:location.coordinate
                                                         radius:distanceFilter
                                                     identifier:@"region"];
    region.notifyOnEntry = false;
    region.notifyOnExit = true;
    [_locationManager startMonitoringForRegion:region];
}

- (void) prepareLocationMap:(CLLocation*) location {
    _lastLocation = location;
    NSDictionary<NSString*,NSNumber*>* locationMap = [Util getLocationMap:location];
    
    [self sendLocationEvent:locationMap];
}

#pragma mark LocationManagerDelegate Methods
- (void)locationManager:(CLLocationManager *)manager
     didUpdateLocations:(NSArray<CLLocation *> *)locations {
    if (locations.count > 0) {
        CLLocation* location = [locations objectAtIndex:0];
        [self prepareLocationMap: location];
        if([PreferencesManager isObservingRegion]) {
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
    NSString *isolateId = [_headlessRunner isolateId];
    if (_callbackChannel == nil || isolateId == nil) {
        return;
    }
    
    NSDictionary *map = @{
                     kArgCallback : @([PreferencesManager getCallbackHandle:kCallbackKey]),
                     kArgLocation: location
                     };
    [_callbackChannel invokeMethod:kBCMSendLocation arguments:map];
}

- (instancetype)init:(NSObject<FlutterPluginRegistrar> *)registrar {
    self = [super init];
    
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
}

#pragma mark MethodCallHelperDelegate

- (void)startLocatorService:(int64_t)handle {
    [PreferencesManager setCallbackDispatcherHandle:handle];
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
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        registerPlugins(_headlessRunner);
    });
    [_registrar addMethodCallDelegate:self channel:_callbackChannel];
}

- (void)registerLocator:(int64_t)callback
           initCallback:(int64_t)initCallback
  initialDataDictionary:(NSDictionary*)initialDataDictionary
        disposeCallback:(int64_t)disposeCallback
               settings: (NSDictionary*)settings {
    [self->_locationManager requestAlwaysAuthorization];
        
    long accuracyKey = [[settings objectForKey:kSettingsAccuracy] longValue];
    CLLocationAccuracy accuracy = [Util getAccuracy:accuracyKey];
    double distanceFilter= [[settings objectForKey:kSettingsDistanceFilter] doubleValue];
    bool  showsBackgroundLocationIndicator=[[settings objectForKey:kSettingsShowsBackgroundLocationIndicator] boolValue];

    _locationManager.desiredAccuracy = accuracy;
    _locationManager.distanceFilter = distanceFilter;
    
    if (@available(iOS 11.0, *)) {
      _locationManager.showsBackgroundLocationIndicator = showsBackgroundLocationIndicator;
    }
    
    if (@available(iOS 9.0, *)) {
        _locationManager.allowsBackgroundLocationUpdates = YES;
    }
    
    [PreferencesManager saveDistanceFilter:distanceFilter];

    [PreferencesManager setCallbackHandle:callback key:kCallbackKey];
    
    InitPluggable *initPluggable = [[InitPluggable alloc] init];
    [initPluggable setCallback:initCallback];
    [initPluggable onServiceStart:initialDataDictionary];
    
    DisposePluggable *disposePluggable = [[DisposePluggable alloc] init];
    [disposePluggable setCallback:disposeCallback];
        
    [_locationManager startUpdatingLocation];
    [_locationManager startMonitoringSignificantLocationChanges];
}

- (void)removeLocator {
    if (_locationManager == nil) {
        return;
    }
    
    @synchronized (self) {
        [_locationManager stopUpdatingLocation];
        
        if (@available(iOS 9.0, *)) {
            _locationManager.allowsBackgroundLocationUpdates = NO;
        }
        
        [_locationManager stopMonitoringSignificantLocationChanges];

        for (CLRegion* region in [_locationManager monitoredRegions]) {
            [_locationManager stopMonitoringForRegion:region];
        }
    }
    
    DisposePluggable *disposePluggable = [[DisposePluggable alloc] init];
    [disposePluggable onServiceDispose];
}

- (void) setServiceRunning:(BOOL) value {
    @synchronized(self) {
        [PreferencesManager setServiceRunning:value];
    }
}

- (BOOL)isServiceRunning{
    return [PreferencesManager isServiceRunning];
}

@end
