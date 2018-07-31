var sage = angular.module('sage', ['sage-common']);

sage.controller("UspsLookupController", function($scope, $http, dataBus, mapService, menuService, uiBlocker) {
    $scope.id = 6;
    $scope.visible = false;

    $scope.$on(menuService.menuToggleEvent, function() {
        $scope.visible = menuService.isMethodActive($scope.id);
        if ($scope.visible) {
            dataBus.setBroadcast("hideResultTab", false);
            mapService.toggleMap(true);
        }
    });
});