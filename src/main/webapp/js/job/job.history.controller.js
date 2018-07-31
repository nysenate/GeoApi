var sageJob = angular.module('sage-job');

sageJob.controller('JobHistoryController', function($scope, $http, menuService, dataBus) {
    $scope.id = 3;
    $scope.visible = false;
    $scope.allProcesses = [];

    $scope.$on(menuService.menuToggleEvent, function(){
        $scope.visible = menuService.isMethodActive($scope.id);
        if ($scope.visible) {
            $scope.getAllProcesses();
        }
    });

    $scope.getAllProcesses = function() {
        $http.get(statusUrl + "/all")
            .success(function(data, status, headers, config){
                if (data && data.success) {
                    $scope.allProcesses = data.statuses;
                }
            });
    };

    $scope.getConditionStyle = function(condition) {
        var color = "#333";
        switch (condition) {
            case 'WAITING_FOR_CRON' : color = "#333"; break;
            case 'RUNNING' :
            case 'COMPLETED' : color = "#639A00"; break;
            case 'SKIPPED' : color = "orangered"; break;
            case 'COMPLETED_WITH_ERRORS' :
            case 'FAILED' :
            case 'CANCELLED' : color = "red"; break;
        }
        return {"color" : color};
    }

});