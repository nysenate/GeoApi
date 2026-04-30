var sage = angular.module('sage');

sage.controller("StreetLookupController", function($scope, $http, dataBus, mapService, menuService, uiBlocker) {
    $scope.id = 3;
    $scope.visible = false;
    $scope.showFilter = false;
    $scope.zip5 = "";

    $scope.$on(menuService.menuToggleEvent, function() {
        $scope.visible = menuService.isMethodActive($scope.id);
        if ($scope.visible) {
            mapService.toggleMap(true);
            dataBus.setBroadcast("hideResultTab", false);
        }
    });

    $scope.lookup = function () {
        uiBlocker.block("Loading street information.");
        $http.get(this.getStreetLookupUrl())
            .then(function(response) {
                dataBus.setBroadcastAndView("street", response.data, "street");
                $scope.showFilter = true;
            }, function(response) {
            uiBlocker.unBlock();
        });
    };

    $scope.getStreetLookupUrl = function () {
        return contextPath + baseApi + "/street/lookup?zip5=" + this.zip5;
    };
});