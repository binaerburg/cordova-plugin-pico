
#import <Foundation/Foundation.h>

@class CUSwatch;

@interface CUMatch : NSObject <NSCoding>

@property (nonatomic, readonly) CUSwatch *swatch;
@property (nonatomic, readonly) double distance;
@property (nonatomic, readonly) double distanceSquared;

- (instancetype)initWithSwatch:(CUSwatch *)swatch distanceSquared:(double)distanceSquared;

- (NSComparisonResult)compare:(CUMatch *)match;

@end
