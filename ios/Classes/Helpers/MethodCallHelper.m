//
//  MethodCallHelper.m
//  background_locator_2
//
//  Created by Mehdi Sohrabi on 6/28/20.
//

#import "MethodCallHelper.h"
#import "Globals.h"

@implementation MethodCallHelper

- (void)handleMethodCall:(FlutterMethodCall *)call
                  result:(FlutterResult)result
                delegate:(id <MethodCallHelperDelegate>)delegate {
    NSDictionary *arguments = call.arguments;
    if ([kMethodPluginInitializeService isEqualToString:call.method]) {
        int64_t callbackDispatcher = [[arguments objectForKey:kArgCallbackDispatcher] longLongValue];
        [delegate startLocatorService:callbackDispatcher];
        result(@(YES));
    } else if ([kMethodServiceInitialized isEqualToString:call.method]) {
        result(nil);
    } else if ([kMethodPluginRegisterLocationUpdate isEqualToString:call.method]) {
        int64_t callbackHandle = [[arguments objectForKey:kArgCallback] longLongValue];
        int64_t initCallbackHandle = [[arguments objectForKey:kArgInitCallback] longLongValue];
        NSDictionary *initialDataDictionary = [arguments objectForKey:kArgInitDataCallback];
        int64_t disposeCallbackHandle = [[arguments objectForKey:kArgDisposeCallback] longLongValue];
        [delegate setServiceRunning:true];
        [delegate registerLocator:callbackHandle initCallback:initCallbackHandle initialDataDictionary:initialDataDictionary disposeCallback:disposeCallbackHandle settings:arguments];
        result(@(YES));
    } else if ([kMethodPluginUnRegisterLocationUpdate isEqualToString:call.method]) {
        [delegate removeLocator];
        [delegate setServiceRunning:false];
        result(@(YES));
    } else if ([kMethodPluginIsRegisteredLocationUpdate isEqualToString:call.method]) {
        BOOL val = [delegate isServiceRunning];
        result(@(val));
    }else if ([kMethodPluginIsServiceRunning isEqualToString:call.method]) {
        BOOL val = [delegate isServiceRunning];
        result(@(val));
    } else if([kMethodPluginUpdateNotification isEqualToString:call.method]) {
        // updating notification's text is just for android
        result(nil);
    } else {
        result(FlutterMethodNotImplemented);
    }
}

@end
