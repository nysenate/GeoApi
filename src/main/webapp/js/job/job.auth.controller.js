var sageJob = angular.module('sage-job');

sageJob.controller('JobAuthController', function($scope, $http) {
    $scope.visible = true;
    $scope.email = "";
    $scope.password = "";
    $scope.error = false;
    $scope.errorMessage = "";
});
