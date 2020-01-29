/********* Pico.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "PicoSDK.h"

@interface PicoPlugin : CDVPlugin <CUPicoConnectorDelegate, CUPicoDelegate>
{
    CUPicoConnector *_picoConnector;
    CUPico *_pico;
}

- (void) pluginInitialize:(CDVInvokedUrlCommand*)command;
- (void) connect:(CDVInvokedUrlCommand*)command;
- (void) onConnectSuccess:(CUPico *)pico;
- (void) onConnectFail:(NSError *)error;
- (void) disconnect:(CDVInvokedUrlCommand *)command;

@end
