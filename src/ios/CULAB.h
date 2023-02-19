
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface CULAB : NSObject <NSCoding>

@property (nonatomic, readonly) double l;
@property (nonatomic, readonly) double a;
@property (nonatomic, readonly) double b;

- (instancetype)initWithL:(double)l a:(double)a b:(double)b;

- (double)distance:(CULAB *)lab;
- (double)distanceSquared:(CULAB *)lab;

- (UIColor *)color;
- (NSString *)toString;

@end
