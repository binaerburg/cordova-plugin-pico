/********* Pico.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "PicoSDK.h"

@interface PicoPlugin : CDVPlugin{
    CUPicoConnector *_picoConnector;
    CUPico *_pico;
}
-(void)connect;
@end
