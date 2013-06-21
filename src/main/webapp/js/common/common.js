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
    };
    return dataBus;
});

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

sageCommon.controller('MenuController', function($scope, menuService){
    $scope.toggleMethod = function(index) {
        menuService.toggleMethod(index);
    }
});

function initVerticalMenu() {
    $("p.method-header").click(function(event) {
        if (!$(this).hasClass("active")) {
            $("p.method-header.active").removeClass("active");
            $(this).addClass("active");
        }
    });
    $(".top-method-header li a").click(function(event) {
        if (!$(this).hasClass("active")) {
            $(".top-method-header li a.active").removeClass("active");
            $(this).addClass("active");
        }
    });
}



