var exec = require('cordova/exec');

var MyPlugin = {
    showMainScreen: function(url, apiKey, success, error) {
        exec(success, error, 'MyPlugin', 'showMainScreen', [url, apiKey]);
    }
};

module.exports = MyPlugin;
