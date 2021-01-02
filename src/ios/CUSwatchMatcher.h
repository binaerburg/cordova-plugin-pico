
#import <Foundation/Foundation.h>

@class CULAB;
@class CUMatch;

@interface CUSwatchMatcher : NSObject

+ (CUMatch *)getMatch:(CULAB *)lab swatches:(NSArray *)swatches;
+ (NSArray *)getMatches:(CULAB *)lab swatches:(NSArray *)swatches numMatches:(NSUInteger)numMatches;

@end
