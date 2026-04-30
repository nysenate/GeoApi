var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('DeploymentStatsController', function($scope, $http, dataBus) {

    $scope.init = function() {
        this.getDeploymentStats();
    };

    $scope.getDeploymentStats = function() {
        $http.get(baseAdminApi + "/deployment")
            .then(function(response){
                $scope = angular.extend($scope, response.data);
            }, function(response){
                console.log("Error retrieving deployment stats! " + response.data);
            });
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});