var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('ApiUserStatsController', function($scope, $http, dataBus) {
    $scope.apiUserStats = [];

    $scope.init = function() {
        this.getApiUserStats();
    };

    $scope.getApiUserStats = function() {
        $http.get(baseAdminApi + "/apiUserUsage?from=" + (+$scope.from) + "&to=" + (+$scope.to))
            .success(function(data){
                $scope.apiUserStats = data;
            })
            .error(function(){});
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});