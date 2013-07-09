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

sageCommon.controller('MenuController', function($scope, menuService, dataBus){
    $scope.index = 1;

    $scope.toggleMethod = function(index) {
        this.index = index;
        menuService.toggleMethod(index);
    };

    $scope.$on(menuService.menuToggleEvent, function(){
        $scope.index = dataBus.data;
    });

    $scope.getMethodClass = function(index) {
        return (this.index === index) ? "active" : "";
    };
});

function initVerticalMenu() {
    $(".top-method-header li a").click(function(event) {
        if (!$(this).hasClass("active")) {
            $(".top-method-header li a.active").removeClass("active");
            $(this).addClass("active");
        }
    });
}



