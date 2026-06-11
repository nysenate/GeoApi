var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('DashboardController', function($scope, $http, menuService, dataBus) {
    $scope.id = 1;
    $scope.visible = true;
    $scope.now = new Date();
    $scope.lastWeek = new Date(new Date().setDate(new Date().getDate() - 7));

    $scope.from = $scope.lastWeek;
    $scope.to = $scope.now;

    // Bound to the date picker inputs. Kept on an object (rather than bare $scope
    // primitives) so edits made from a child tab's scope mutate these shared values
    // instead of shadowing them.
    $scope.dateRange = {
        fromMonth: $scope.lastWeek.getMonth() + 1,
        fromDate: $scope.lastWeek.getDate(),
        fromYear: $scope.lastWeek.getFullYear(),
        toMonth: $scope.now.getMonth() + 1,
        toDate: $scope.now.getDate(),
        toYear: $scope.now.getFullYear()
    };

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
        $scope.from.setMonth($scope.dateRange.fromMonth - 1);
        $scope.from.setDate($scope.dateRange.fromDate);
        $scope.from.setFullYear($scope.dateRange.fromYear);
        $scope.from.setHours(0);
        $scope.from.setMinutes(0);
        $scope.to.setMonth($scope.dateRange.toMonth - 1);
        $scope.to.setDate($scope.dateRange.toDate);
        $scope.to.setFullYear($scope.dateRange.toYear);
        $scope.to.setHours(23);
        $scope.to.setMinutes(59);
    };
});