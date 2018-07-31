var sageAdmin = angular.module('sage-admin', ['sage-common']);

sageAdmin.controller('DeploymentStatsController', function($scope, $http, dataBus) {

    $scope.init = function() {
        this.getDeploymentStats();
    };

    $scope.getDeploymentStats = function() {
        $http.get(baseAdminApi + "/deployment")
            .success(function(data){
                $scope = angular.extend($scope, data);
            })
            .error(function(data){
                console.log("Error retrieving deployment stats! " + data);
            });
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});