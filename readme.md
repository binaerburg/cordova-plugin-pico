[![MIT License](https://img.shields.io/apm/l/atomic-design-ui.svg?)](https://github.com/tterb/atomic-design-ui/blob/master/LICENSEs)
# cordova-plugin-pico [![npm version](https://badge.fury.io/gh/binaerburg%2Fcordova-plugin-pico.svg)](//npmjs.com/package/@binaerburg/cordova-plugin-pico)

This plugin integrates an interface in order to interact with the `Pico Colour Scanner` from [Palette](https://palette.com/pico/)
You can integrate the cordova plugin into your `IONIC`- or `CORDOVA`-APP in order to connect to a Pico sensors directly within your app.

# Supported Platforms

- Android

- iOS

# Installation

```
cordova plugin add cordova-plugin-pico
```
```
ionic cordova plugin add cordova-plugin-pico
```


# Usage

Use the Wrapper [ionic-pico](https://github.com/binaerburg/pico-ionic) for an ionic application.

```js
var exec = require("cordova/exec");

module.exports.init = function (arg0, success, error) {
    exec(success,error,"PicoPlugin","init")
};

module.exports.destroy = function (arg0, success, error) {
    exec(success,error,"PicoPlugin", "destroy")
};

module.exports.triggerInitialize = function (arg0, success, error) {
    exec(success,error,"PicoPlugin", "triggerInitialize")
};

module.exports.connect = function (arg0, success, error) {
    exec(success,error,"PicoPlugin", "connect")
};

module.exports.disconnect = function (arg0, success, error) {
    exec(success,error,"PicoPlugin", "disconnect")
};

module.exports.scan = function (arg0, success, error) {
    exec(success,error,"PicoPlugin", "scan")
};

module.exports.calibrate = function (arg0, success, error) {
    exec(success,error,"PicoPlugin", "calibrate")
};
```


# Credits / Native library links

Pico Palette :- [Pico Palette](https://palette.com/pico/) <br/>


#Tutorial
Base Structure
https://www.youtube.com/watch?v=w9zYXelkl6I&t=2318s

Integration
https://www.youtube.com/watch?v=Q6PaFEDonac


# More about us!

Find out more or contact us directly here :- http://www.binaerburg.de
