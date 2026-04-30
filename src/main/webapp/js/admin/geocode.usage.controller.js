var sageAdmin = angular.module('sage-admin');

sageAdmin.controller("GeocodeUsageController", function($scope, $http, dataBus){

    $scope.init = function() {
        this.getGeocodeStats();
    };

    $scope.getGeocodeStats = function() {
        $http.get(baseAdminApi + "/geocodeUsage?from=" + (+$scope.from) + "&to=" + (+$scope.to))
            .then(function(response){
                if (response.data) {
                    $scope = angular.extend($scope, response.data);
                }
            }, function(response){
                console.log("Failed to retrieve geocode usage response!");
            });
    };

    $scope.getBarStyle = function(hits, total) {
        return {width: (hits/total) * 100 + "%"}
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});