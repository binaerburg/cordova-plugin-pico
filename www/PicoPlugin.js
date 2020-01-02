var exec = require("cordova/exec");

module.exports.destroy = function (arg0, success, error) {
    exec(success,error,"PicoPlugin", "destroy")
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

