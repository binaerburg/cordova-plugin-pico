var exec = require("cordova/exec");

module.exports.coolMethod = function (arg0, success, error) {
    exec(success, error, "Pico", "coolMethod", [arg0]);
};


module.exports.connect = function (arg0, success, error) {
    exec(success,error,"Pico", "connect", [arg0])
};

