var sageCommon = angular.module('sage-common', []);

sageCommon.factory('menuService', function(dataBus) {
    var menuService = {
        methodIndex : '',
        menuToggleEvent : 'toggleMethod',

        toggleMethod : function(methodIndex) {
            this.methodIndex = methodIndex;
            dataBus.setBroadcast(this.menuToggleEvent, methodIndex);
        },

        isMethodActive : function(methodIndex) {
            return this.methodIndex == methodIndex;
        }
    };
    return menuService;
});