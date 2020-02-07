/********* Pico.m Cordova Plugin Implementation *******/
#import "PicoPlugin.h"
#import <Cordova/CDV.h>

@implementation PicoPlugin

- (void)init:(CDVInvokedUrlCommand*)command
{
    NSLog(@"Inititalize...");
    if (_picoConnector == nil)
   {
      _picoConnector = CUPicoConnector.alloc.init;
      _picoConnector.delegate = self;
   }
}


- (void)connect:(CDVInvokedUrlCommand*)command
{
   /*if (_picoConnector == nil)
   {
      _picoConnector = CUPicoConnector.alloc.init;
      _picoConnector.delegate = self;
   }*/

  [_picoConnector connect];
}

- (void)onConnectSuccess:(CUPico *)pico
{
    _pico = pico;

    NSLog(@"Pico conntected");

    _pico.delegate = self;

    [_pico sendBatteryLevelRequest];
    [_pico sendBatteryStatusRequest];
}

- (void)onConnectFail:(NSError *)error
{
    NSLog(@"Failed to connect to Pico: %@", error.description);
}


- (void)disconnect:(CDVInvokedUrlCommand *)command;
{
    if (_pico != nil)
    {
        [_pico disconnect];
        _pico = nil;
    }
}
/*
- (void)scan
{
    [_pico sendLabDataRequest];
}

- (void)calibrate
{
     [_pico sendCalibrationRequest];
}

- (void)onDisconnect:(CUPico *)pico error:(NSError *)error
{
    NSLog(@"Pico disconnected");
}

- (void)onFetchLabData:(CUPico *)pico lab:(CULAB *)lab
{
    [self log:[NSString stringWithFormat:@"Received LAB: %@", lab.toString]];

    // Convert lab to rgb and display it.
    _lblScan.backgroundColor = lab.color;

    // Find matches and display them.
    NSArray *swatches = self.getSampleColors;
    NSArray *matches = [CUSwatchMatcher getMatches:lab swatches:swatches numMatches:3];

    for (NSInteger i = 0; i < matches.count; i++)
    {
        CUMatch *match = matches[i];

        UILabel *lbl = _lblMatches[i];
        lbl.backgroundColor = match.swatch.lab.color;
        lbl.text = [NSString stringWithFormat:@"MATCH %d\n%@\ndE %.2f", i + 1, match.swatch.name, match.distance];
    }
}

- (void)onCalibrationComplete:(CUPico *)pico success:(BOOL)success
{
    NSString *result = success ? @"success" : @"failure";
    [self log:[NSString stringWithFormat:@"Calibration complete: %@", result]];
}

- (void)onFetchBatteryLevel:(CUPico *)pico level:(NSInteger)level
{
    [self log:[NSString stringWithFormat:@"Battery level: %d", level]];
}

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

    [self log:[NSString stringWithFormat:@"Battery status: %@", str]];
}
*/

@end
