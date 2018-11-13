var sage = angular.module('sage');

sage.controller("RevGeoController", function($scope, $http, mapService, menuService, dataBus) {
    $scope.id = 4;
    $scope.visible = false;
    $scope.lat = "";
    $scope.lon = "";

    $scope.$on(menuService.menuToggleEvent, function() {
        $scope.visible = menuService.isMethodActive($scope.id);
        if ($scope.visible) {
            mapService.toggleMap(true);
        }
    });

    $scope.lookup = function() {
        $http.get(this.getRevGeoUrl())
            .success(function(data, status, headers, config) {
                dataBus.setBroadcastAndView("revgeo", data, "revgeo");
            });
    };

    $scope.getRevGeoUrl = function() {
        return contextPath + baseApi + "/geo/revgeocode?lat=" + this.lat + "&lon=" + this.lon;
    };
});