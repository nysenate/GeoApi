var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('AdminPageController', function($scope, $http, dataBus) {
    $scope.activeTab = "exceptions";

    $scope.exceptionTab = true;

    $scope.changeTab = function(newTab) {
        $scope.activeTab = newTab;
    };

    $scope.determineActiveTab = function (inputTab) {
        // console.log(inputTab);
        return $scope.activeTab === inputTab;
    };
});