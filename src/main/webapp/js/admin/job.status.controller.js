var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('JobStatusController', function($scope, $http, dataBus){
    $scope.jobStatuses = [];

    $scope.init = function() {
        this.getJobStatuses();
    };

    $scope.getJobStatuses = function() {
        $http.get(baseAdminApi + "/jobStatuses?from=" + (+$scope.from) + "&to=" + (+$scope.to))
            .then(function(response){
                var data = response.data;
                if (data) {
                    $scope.jobStatuses = data;
                }
            }, function(){
                console.log("Failed to retrieve job statuses!")
            });
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});