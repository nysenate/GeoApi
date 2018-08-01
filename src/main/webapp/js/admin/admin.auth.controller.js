var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('AdminAuthController', function($scope, $http) {
    $scope.visible = true;
    $scope.email = "";
    $scope.password = "";
    $scope.error = false;
    $scope.errorMessage = "";
});
