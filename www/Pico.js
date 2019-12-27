var exec = require("cordova/exec");

module.exports.init = function (arg0, success, error) {
    exec(success,error,"Pico", "init")
};

module.exports.destroy = function (arg0, success, error) {
    exec(success,error,"Pico", "destroy")
};

module.exports.connect = function (arg0, success, error) {
    exec(success,error,"Pico", "connect")
};

module.exports.disconnect = function (arg0, success, error) {
    exec(success,error,"Pico", "disconnect")
};

module.exports.scan = function (arg0, success, error) {
    exec(success,error,"Pico", "scan")
};

module.exports.calibrate = function (arg0, success, error) {
    exec(success,error,"Pico", "calibrate")
};

