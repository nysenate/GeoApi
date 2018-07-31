var sageJob = angular.module('sage-job', ['sage-common']);

sageJob.controller('JobStatusController', function($scope, $http, menuService, dataBus) {
    $scope.id = 2;
    $scope.visible = false;
    $scope.rpInterval = 0;  // Running processes interval id
    $scope.cpInterval = 0;  // Completed processes interval id
    $scope.apInterval = 0;  // Active/Queued processes interval id

    $scope.runningProcesses = [];
    $scope.activeProcesses = [];
    $scope.completedProcesses = [];

    $scope.processorRunning = false;

    $scope.showProcessQueue = function() {
        return (this.activeProcesses.length > 0);
    };

    $scope.lastCompletedRecords = 0;

    $scope.$on(menuService.menuToggleEvent, function(){
        $scope.visible = menuService.isMethodActive($scope.id);
        if ($scope.visible) {
            $scope.getActiveProcesses();
            $scope.getRunningProcesses();
            $scope.getCompletedProcesses();

            clearInterval($scope.rpInterval);
            clearInterval($scope.cpInterval);
            clearInterval($scope.apInterval);

            $scope.cpInterval = setInterval(function() {$scope.getActiveProcesses()}, 5000);
            $scope.cpInterval = setInterval(function() {$scope.getCompletedProcesses()}, 6000);
            $scope.rpInterval = setInterval(function() {$scope.getRunningProcesses()}, 3000);
        }
        else {
            clearInterval($scope.intervalId);
        }
    });

    $scope.$on("jobDone", function(){
        $scope.getActiveProcesses();
    });

    $scope.getRunningProcesses = function() {
        $http.get(statusUrl + "/running")
            .success(function(data, status, headers, config) {
                if (data && data.success) {
                    $scope.runningProcesses = data.statuses;
                    $scope.computeProgress();
                }
            }).error(function(data) {
            console.log("Failed to retrieve running job processes " + data);
        });
    };

    $scope.computeProgress = function() {
        $.each(this.runningProcesses, function(){
            var total = this.process.recordCount;
            var current = this.completedRecords;
            if (total > 0) {
                this.progressStyle = {'width' : ((current/total) * 100) + "%"};
            }
        });
    };

    $scope.getActiveProcesses = function() {
        $http.get(statusUrl + "/active")
            .success(function(data, status, headers, config) {
                if (data && data.success) {
                    $scope.activeProcesses = data.statuses;
                    $scope.processorRunning = data.processorRunning;
                }
                else {
                    console.log("Active processes: " + data);
                }
            }).error(function(data) {
            console.log("Error retrieving active processes. " + data);
        });
    };

    $scope.getCompletedProcesses = function() {
        $http.get(statusUrl + "/completed")
            .success(function(data, status, headers, config) {
                if (data && data.success) {
                    $scope.completedProcesses = data.statuses;
                }
            }).error(function(data, status){
            console.log("Error retrieving completed processes.");
        });
    };

    $scope.cancelJobProcess = function(processId) {
        $http.post(cancelUrl + "?id=" + processId).success(function(data){
            if (data) {
                alert(data.message);
            }
            $scope.getActiveProcesses();
        }).error(function(){});
    }
});