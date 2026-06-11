var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('AdminPageController', function($scope, $http, dataBus) {
    $scope.activeTab = "api-usage";

    $scope.changeTab = function(newTab) {
        $scope.activeTab = newTab;
    };

    $scope.determineActiveTab = function (inputTab) {
        // console.log(inputTab);
        return $scope.activeTab === inputTab;
    };
});