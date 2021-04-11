//
//  PreferencesManager.m
//  background_locator
//
//  Created by Mehdi Sohrabi on 6/28/20.
//

#import "PreferencesManager.h"
#import "Globals.h"

@implementation PreferencesManager

+ (int64_t)getCallbackDispatcherHandle {
    id handle = [[NSUserDefaults standardUserDefaults]
                 objectForKey: kCallbackDispatcherKey];
    if (handle == nil) {
        return 0;
    }
    return [handle longLongValue];
}

+ (void)setCallbackDispatcherHandle:(int64_t)handle {
    [[NSUserDefaults standardUserDefaults]
     setObject:[NSNumber numberWithLongLong:handle]
     forKey:kCallbackDispatcherKey];
}

+ (int64_t)getCallbackHandle:(NSString *)key  {
    id handle = [[NSUserDefaults standardUserDefaults]
                 objectForKey: key];
    if (handle == nil) {
        return 0;
    }
    return [handle longLongValue];
}

+ (void)setCallbackHandle:(int64_t)handle key:(NSString *)key {
    [[NSUserDefaults standardUserDefaults]
     setObject:[NSNumber numberWithLongLong:handle]
     forKey: key];
}

+ (void)saveDistanceFilter:(double)distance {
    [[NSUserDefaults standardUserDefaults] setDouble:distance forKey:kDistanceFilterKey];
}

+ (double)getDistanceFilter {
    return [[NSUserDefaults standardUserDefaults] doubleForKey:kDistanceFilterKey];
}

+ (void)setObservingRegion:(BOOL)observing {
    [[NSUserDefaults standardUserDefaults] setBool:observing forKey:kPrefObservingRegion];
}

+ (BOOL)isObservingRegion {
    return [[NSUserDefaults standardUserDefaults] boolForKey:kPrefObservingRegion];
}

+ (void)setServiceRunning:(BOOL)running {
    [[NSUserDefaults standardUserDefaults] setBool:running forKey:kPrefServiceRunning];
}

+ (BOOL)isServiceRunning {
    return [[NSUserDefaults standardUserDefaults] boolForKey:kPrefServiceRunning];
}

@end
