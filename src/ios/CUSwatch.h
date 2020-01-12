
#import <Foundation/Foundation.h>

@class CULAB;

@interface CUSwatch : NSObject <NSCoding>

@property (nonatomic, readonly) NSString *name;
@property (nonatomic, readonly) NSString *code;
@property (nonatomic, readonly) CULAB *lab;

- (instancetype)initWithName:(NSString *)name code:(NSString *)code lab:(CULAB *)lab;

- (NSString *)toString;

@end
