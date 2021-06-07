//
//  Pluggable.h
//  Pods
//
//  Created by Mehdok on 6/6/21.
//

#ifndef Pluggable_h
#define Pluggable_h

@protocol Pluggable <NSObject>
- (void) setCallback:(int64_t) callbackHandle;
- (void) onServiceStart: (NSDictionary*)initialDataDictionary;
- (void) onServiceDispose;
@end

#endif /* Pluggable_h */
