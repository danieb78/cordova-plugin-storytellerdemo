var exec = require('cordova/exec');

var MyPlugin = {
    showMainScreen: function(url, apiKey, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'MyPlugin', 'showMainScreen', [url, apiKey]);
    },
    
    dismissDialog: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'MyPlugin', 'dismissDialog', []);
    }
};

module.exports = MyPlugin;
