
#import <Foundation/Foundation.h>
#import "CUPico.h"

@import CoreBluetooth;

@class CUPicoConnector;


@protocol CUPicoConnectorDelegate <NSObject>
@optional
- (void)onConnectSuccess:(CUPico *)pico;
- (void)onConnectFail:(NSError *)error;
@end


@interface CUPicoConnector : NSObject <CBCentralManagerDelegate, CUPicoDelegate>

@property (nonatomic, weak) id<CUPicoConnectorDelegate> delegate;

- (void)connect;
- (void)cancelConnect;

@end
