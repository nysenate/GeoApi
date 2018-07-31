var sageCommon = angular.module('sage-common');

sageCommon.factory('dataBus', function($rootScope) {
    var dataBus = {
        viewHandleEvent: 'dataBus_view',
        setBroadcast : function(handle, data) {
            this.handle = handle;
            this.data = data;
            $rootScope.$broadcast(this.handle);
            return this;
        },
        setBroadcastAndView: function(handle, data, viewId) {
            this.setBroadcast(handle, data);
            this.viewId = viewId;
            $rootScope.$broadcast(this.viewHandleEvent);
        }
    };
    return dataBus;
});