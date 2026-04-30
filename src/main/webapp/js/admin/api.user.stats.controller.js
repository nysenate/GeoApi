var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('ApiUserStatsController', function($scope, $http, dataBus) {
    $scope.apiUserStats = [];

    $scope.init = function() {
        this.getApiUserStats();
    };

    $scope.getApiUserStats = function() {
        $http.get(baseAdminApi + "/apiUserUsage?from=" + (+$scope.from) + "&to=" + (+$scope.to))
            .then(function(response){
                $scope.apiUserStats = response.data;
            }, function(){});
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});