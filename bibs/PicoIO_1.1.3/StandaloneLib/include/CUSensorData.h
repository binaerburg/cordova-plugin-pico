
#import <Foundation/Foundation.h>


@interface CUSensorData : NSObject <NSCoding>

@property (nonatomic, readonly) NSInteger r;
@property (nonatomic, readonly) NSInteger g;
@property (nonatomic, readonly) NSInteger b;

- (instancetype)initWithR:(NSInteger)r a:(NSInteger)g b:(NSInteger)b;

- (NSString *)toString;

@end
