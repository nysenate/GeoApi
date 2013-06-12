/** Shared functionality across SAGE */
var sageCommon = angular.module('sage-common', []);

sageCommon.factory('dataBus', function($rootScope) {
    var dataBus = {
        setBroadcast : function(handle, data) {
            this.handle = handle;
            this.data = data;
            $rootScope.$broadcast(this.handle);
            return this;
        }
    }
    return dataBus;
});

sageCommon.controller('MenuController', function($scope, dataBus){
    $scope.toggleView = function(index) {
        dataBus.setBroadcast('toggleView', index);
    }
});

function initVerticalMenu() {
    $("p.method-header").click(function(event) {
        if (!$(this).hasClass("active")) {
            $(".form-container.active").removeClass("active").slideUp(250);
            $("p.method-header.active").removeClass("active");
            $(this).addClass("active");
            $(this).next(".form-container").slideDown(250).addClass("active");
        }
    });
}



