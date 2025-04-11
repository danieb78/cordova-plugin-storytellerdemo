var exec = require('cordova/exec');

var MyPlugin = {
    showMainScreen: function(success, error) {
        exec(success, error, 'MyPlugin', 'showMainScreen', []);
    }
};

module.exports = MyPlugin;
