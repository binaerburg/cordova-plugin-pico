/********* Pico.m Cordova Plugin Implementation *******/
#import "PicoPlugin.h"
#import <Cordova/CDV.h>

@implementation PicoPlugin

//intialize connector
- (void)init:(CDVInvokedUrlCommand*)command
{
    NSLog(@"Inititalize...");
    if (_picoConnector == nil)
   {
      _picoConnector = CUPicoConnector.alloc.init;
      _picoConnector.delegate = self;
   }
}

//destroy the connection
- (void) destroy:(CDVInvokedUrlCommand*)command {
    if (_pico != nil)
        {
            [_pico disconnect];
            _pico = nil;
            [self triggerInititalize:command];
        }
    NSLog(@"destroyed");
}

//second connector init, usually not required
- (void)triggerInititalize:(CDVInvokedUrlCommand*)command
{
    NSLog(@"Trigger Inititalize...");
    if (_picoConnector == nil)
   {
      _picoConnector = CUPicoConnector.alloc.init;
      _picoConnector.delegate = self;
   }
}

//establish connection to pico
- (void)connect:(CDVInvokedUrlCommand*)command
{
   if (_picoConnector == nil)
   {
      NSLog(@"connect: _picoConnector == nil");
      _picoConnector = CUPicoConnector.alloc.init;
      _picoConnector.delegate = self;
      NSLog(@"start wait");
      [NSThread sleepForTimeInterval:1.0f];
      NSLog(@"lets go");
   }

  [_picoConnector connect];
}

//connection established successfully
- (void)onConnectSuccess:(CUPico *)pico
{
    NSLog(@"Pico conntected");

    _pico = pico;
    _pico.delegate = self;

    [_pico sendBatteryLevelRequest];
    [_pico sendBatteryStatusRequest];

    NSDictionary * payload = @{
            @"connection": [NSNumber numberWithBool:YES]
        };

   [[NSNotificationCenter defaultCenter] postNotificationName:@"connection" object:nil userInfo:payload];
   NSLog(@"Pico%@", _pico);
   NSLog(@"Pico%@", _pico.serial);
   NSLog(@"Pico%@", _pico.name);
   NSDictionary * payloadInfo = @{
                      @"name": [NSString stringWithFormat:@"%@", _pico.name],
                      @"serial": [NSString stringWithFormat:@"%@", _pico.serial],
                      @"bluetoothAddress": @"unknown"
                  };

   [[NSNotificationCenter defaultCenter] postNotificationName:@"picoInfo" object:nil userInfo:payloadInfo];
}

//connection failed
- (void)onConnectFail:(NSError *)error
{
    NSDictionary * payload = @{
            @"connection": [NSString stringWithFormat:@"Failed to connect to Pico: %@", error.description]
        };
        [[NSNotificationCenter defaultCenter] postNotificationName:@"connection" object:nil userInfo:payload];
}

//disconnect from pico
- (void)disconnect:(CDVInvokedUrlCommand *)command;
{
    if (_pico != nil)
    {
        [_pico disconnect];
        _pico = nil;
        NSDictionary * payload = @{
                    @"connection": [NSNumber numberWithBool:NO]
                };

            [[NSNotificationCenter defaultCenter] postNotificationName:@"connection" object:nil userInfo:payload];
    }
}

//scan colours with pico
- (void)scan:(CDVInvokedUrlCommand *)command
{
    [_pico sendLabDataRequest];
}

//scan raw data from pico with firing LED's
- (void)scanRaw:(CDVInvokedUrlCommand *)command
{
    NSLog(@"Scan Raw Data Request");
    [_pico sendRawDataRequest];
}

- (void)calibrate:(CDVInvokedUrlCommand *)command
{
     [_pico sendCalibrationRequest];
}

- (void)onCalibrationComplete:(CUPico *)pico success:(BOOL)success
{
   NSDictionary * payload = @{
               @"calibrationResult":  success ? @"success" : @"failure"
           };

        [[NSNotificationCenter defaultCenter] postNotificationName:@"calibration" object:nil userInfo:payload];

}

// on Disconnect
- (void)onDisconnect:(CUPico *)pico error:(NSError *)error
{
     NSDictionary * payload = @{
        @"connection": [NSNumber numberWithBool:NO]
    };

    [[NSNotificationCenter defaultCenter] postNotificationName:@"connection" object:nil userInfo:payload];
}

//getting the LAB colours from PICO
- (void)onFetchLabData:(CUPico *)pico lab:(CULAB *)lab
{
    NSDictionary * payload = @{
        @"lab": @{ @"l": [NSNumber numberWithFloat:(double) lab.l], @"a": [NSNumber numberWithFloat:(double) lab.a],  @"b": [NSNumber numberWithFloat:(double) lab.b]}
    };

    [[NSNotificationCenter defaultCenter] postNotificationName:@"labScan" object:nil userInfo:payload];
}

//get Raw Data from pico
- (void)onFetchRawData:(CUPico *)pico rawData:(NSArray *)data
{
    NSLog(@"On Fetch Raw Data");
    NSDictionary * payload = @{
            @"raw": data,
        };

    [[NSNotificationCenter defaultCenter] postNotificationName:@"rawScan" object:nil userInfo:payload];
}


//get Battery level of pico
- (void)onFetchBatteryLevel:(CUPico *)pico level:(NSInteger)level
{

    NSDictionary * payload = @{
             @"batteryLevel": [NSNumber numberWithInteger:level]
         };

     [[NSNotificationCenter defaultCenter] postNotificationName:@"batteryLevel" object:nil userInfo:payload];
}


//get Battery status from pico
- (void)onFetchBatteryStatus:(CUPico *)pico status:(CUBatteryStatus)status
{
    NSString *str = @"Unknown";
    switch (status)
    {
        case CUBatteryStatusNotCharging:
            str = @"NotCharging";
            break;
        case CUBatteryStatusCharging:
            str = @"Charging";
            break;
        case CUBatteryStatusCharged:
            str = @"Charged";
            break;
    }
    NSDictionary * payload = @{
        @"batteryStatus": str
        };
        [[NSNotificationCenter defaultCenter] postNotificationName:@"batteryStatus" object:nil userInfo:payload];
}

@end
