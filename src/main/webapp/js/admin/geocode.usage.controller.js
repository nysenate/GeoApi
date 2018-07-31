var sageAdmin = angular.module('sage-admin', ['sage-common']);

sageAdmin.controller("GeocodeUsageController", function($scope, $http, dataBus){

    $scope.init = function() {
        this.getGeocodeStats();
    };

    $scope.getGeocodeStats = function() {
        $http.get(baseAdminApi + "/geocodeUsage?from=" + (+$scope.from) + "&to=" + (+$scope.to))
            .success(function(data){
                if (data) {
                    $scope = angular.extend($scope, data);
                }
            })
            .error(function(data){
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