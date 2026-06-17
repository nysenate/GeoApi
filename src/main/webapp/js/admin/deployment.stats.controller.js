var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('DeploymentStatsController', function($scope, $http, dataBus) {

    $scope.init = function() {
        this.getDeploymentStats();
    };

    $scope.getDeploymentStats = function() {
        $http.get(baseAdminApi + "/deployment")
            .then(function(response){
                var deployments = response.data.deployments || [];
                // Deployments are returned ordered by deploy time ascending, so the latest is last.
                $scope.deployments = deployments;
                if (deployments.length > 0) {
                    var latest = deployments[deployments.length - 1];
                    $scope.lastDeployment = latest;
                    $scope.requestsSinceLatest = latest.apiRequestsSince;
                }
            }, function(response){
                console.log("Error retrieving deployment stats! " + response.data);
            });
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});