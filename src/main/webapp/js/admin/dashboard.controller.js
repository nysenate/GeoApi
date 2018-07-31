var sageAdmin = angular.module('sage-admin', ['sage-common']);

sageAdmin.controller('DashboardController', function($scope, $http, menuService, dataBus) {
    $scope.id = 1;
    $scope.visible = true;
    $scope.now = new Date();
    $scope.lastWeek = new Date(new Date().setDate(new Date().getDate() - 7));

    $scope.from = $scope.lastWeek;
    $scope.to = $scope.now;

    $scope.fromMonth = $scope.lastWeek.getMonth() + 1;
    $scope.fromDate = $scope.lastWeek.getDate();
    $scope.fromYear = $scope.lastWeek.getFullYear();

    $scope.toMonth = $scope.now.getMonth() + 1;
    $scope.toDate = $scope.now.getDate();
    $scope.toYear = $scope.now.getFullYear();

    $scope.$on(menuService.menuToggleEvent, function(){
        $scope.visible = ($scope.id == dataBus.data);
    });

    $scope.update = function() {
        dataBus.setBroadcast("update", true);
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init = function() {
        $scope.from.setMonth($scope.fromMonth - 1);
        $scope.from.setDate($scope.fromDate);
        $scope.from.setFullYear($scope.fromYear);
        $scope.from.setHours(0);
        $scope.from.setMinutes(0);
        $scope.to.setMonth($scope.toMonth - 1);
        $scope.to.setDate($scope.toDate);
        $scope.to.setFullYear($scope.toYear);
        $scope.to.setHours(23);
        $scope.to.setMinutes(59);
    };
});