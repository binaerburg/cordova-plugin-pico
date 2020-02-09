/********* Pico.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "PicoSDK.h"

@interface PicoPlugin : CDVPlugin <CUPicoConnectorDelegate, CUPicoDelegate>
{
    CUPicoConnector *_picoConnector;
    CUPico *_pico;
}

- (void) init:(CDVInvokedUrlCommand*)command;
- (void) destroy:(CDVInvokedUrlCommand*)command;
- (void) triggerInititalize:(CDVInvokedUrlCommand *)command;
- (void) connect:(CDVInvokedUrlCommand*)command;
- (void) onConnectSuccess:(CUPico *)pico;
- (void) onConnectFail:(NSError *)error;
- (void) disconnect:(CDVInvokedUrlCommand *)command;
- (void) scan:(CDVInvokedUrlCommand *)command;
- (void) calibrate:(CDVInvokedUrlCommand *)command;
- (void) onCalibrationComplete:(CUPico *)pico success:(BOOL)success

@end
