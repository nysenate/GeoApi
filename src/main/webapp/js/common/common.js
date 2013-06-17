/** Shared functionality across SAGE */
var sageCommon = angular.module('sage-common', []);

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
    }
    return dataBus;
});

sageCommon.controller('MenuController', function($scope, dataBus){
    $scope.toggleView = function(index) {
        dataBus.setBroadcast('toggleView', index);
    };

    $scope.toggleMethod = function(index) {
        dataBus.setBroadcast('toggleMethod', index);
    }
});

function initVerticalMenu() {
    $("p.method-header").click(function(event) {
        if (!$(this).hasClass("active")) {
            $("p.method-header.active").removeClass("active");
            $(this).addClass("active");
        }
    });
}



