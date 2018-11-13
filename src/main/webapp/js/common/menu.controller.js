var sageCommon = angular.module('sage-common');

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