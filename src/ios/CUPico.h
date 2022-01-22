
#import <Foundation/Foundation.h>

@import CoreBluetooth;

@class CULAB;
@class CUPico;
@class CUSensorData;


static NSString * const ColorServiceUuid = @"0x54AA5561-9FA7-417A-90AA-AF6EAA357506";
static NSString * const ColorServiceLabDataCharUuid = @"0x54AA5563-9FA7-417A-90AA-AF6EAA357506";

static NSString * const BasicControlServiceStatusResponseCharUuid = @"0x54AAFAF2-9FA7-417A-90AA-AF6EAA357506";

static NSString * const UartService096Uuid = @"0x6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
static NSString * const UartService096TxCharUuid = @"0x6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
static NSString * const UartService096RxCharUuid = @"0x6E400002-B5A3-F393-E0A9-E50E24DCCA9E";

static NSString * const UartService098Uuid = @"0x54AAB9A1-9FA7-417A-90AA-AF6EAA357506";
static NSString * const UartService098TxCharUuid = @"0x54AAB9A2-9FA7-417A-90AA-AF6EAA357506";
static NSString * const UartService098RxCharUuid = @"0x54AAB9A3-9FA7-417A-90AA-AF6EAA357506";


typedef NS_ENUM(NSUInteger, CUPicoState)
{
    CUPicoStateDiscovered,
    CUPicoStateSetupServices,
    CUPicoStateConnected,
    CUPicoStateDisconnected,
};

typedef NS_ENUM(NSUInteger, CUBatteryStatus)
{
    CUBatteryStatusUnknown = 0,
    CUBatteryStatusNotCharging,
    CUBatteryStatusCharging,
    CUBatteryStatusCharged
};


@protocol CUPicoDelegate <NSObject>
@optional
- (void)onConnectSuccess:(CUPico *)pico;
- (void)onConnectFail:(NSError *)error;

- (void)onDisconnect:(CUPico *)pico error:(NSError *)error;

- (void)onFetchLabData:(CUPico *)pico lab:(CULAB *)lab;
- (void)onFetchSensorData:(CUPico *)pico sensorData:(CUSensorData *)sensorData;
- (void)onFetchRawData:(CUPico *)pico rawData:(NSArray *)data;

- (void)onCalibrationComplete:(CUPico *)pico success:(BOOL)success;

- (void)onFetchBatteryLevel:(CUPico *)pico level:(NSInteger)level;
- (void)onFetchBatteryStatus:(CUPico *)pico status:(CUBatteryStatus)status;
@end


@interface CUPico : NSObject <CBPeripheralDelegate>

@property (nonatomic, weak) id<CUPicoDelegate> delegate;

@property (nonatomic, readonly) NSString *name;
@property (nonatomic, readonly) NSString *serial;

@property (nonatomic, readonly) NSString *uartServiceUuid;
@property (nonatomic, readonly) NSString *uartServiceTxUuid;
@property (nonatomic, readonly) NSString *uartServiceRxUuid;

- (instancetype)initWithManager:(CBCentralManager *)manager peripheral:(CBPeripheral *)peripheral serial:(NSString *)serial;
- (void)discoverServices;

- (void)disconnect;

- (void)sendLabDataRequest;
- (void)sendSensorDataRequest;
- (void)sendRawDataRequest;

- (void)sendCalibrationRequest;
- (BOOL)sendBatteryLevelRequest;
- (BOOL)sendBatteryStatusRequest;

- (CUPicoState)state;
- (void)setScanRawAdjustment:(float)a b:(float)b c:(float)c;

@end
