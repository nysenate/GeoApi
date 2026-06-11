var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('DashboardController', function($scope, $http, menuService, dataBus) {
    $scope.id = 1;
    $scope.visible = true;

    var now = new Date();
    var lastWeek = new Date(new Date().setDate(now.getDate() - 7));

    // Format a Date as a local yyyy-MM-dd string (as <input type="date"> expects).
    var toIsoDate = function(d) {
        return [d.getFullYear(),
                String(d.getMonth() + 1).padStart(2, '0'),
                String(d.getDate()).padStart(2, '0')].join('-');
    };

    // Upper bound for the pickers: stats can't extend past today.
    $scope.maxDate = toIsoDate(now);

    // Bound to the date picker inputs (native <input type="date">, so these are
    // Date objects). Kept on an object rather than bare $scope properties so edits
    // made from a child tab's scope mutate these shared values instead of shadowing them.
    $scope.dateRange = {
        from: lastWeek,
        to: now
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

    // Derive the query bounds from the picked dates, widening them to cover the full
    // first and last day.
    $scope.init = function() {
        $scope.from = new Date($scope.dateRange.from);
        $scope.from.setHours(0, 0, 0, 0);
        $scope.to = new Date($scope.dateRange.to);
        $scope.to.setHours(23, 59, 59, 999);
    };

    $scope.init();
});
