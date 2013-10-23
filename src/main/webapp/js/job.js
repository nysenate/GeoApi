var sageJob = angular.module('sage-job', ['sage-common']);
var submitUrl = contextPath + "/job/submit";
var uploadUrl = contextPath + "/job/upload";
var removeUrl = contextPath + "/job/remove";
var cancelUrl = contextPath + "/job/cancel";
var statusUrl = contextPath + "/job/status";
var uploader;

sageJob.filter('yesno', function(){
    return function(input) {
        return (input) ? 'Yes' : 'No';
    }
});

sageJob.filter('conditionFilter', function(){
    return function(input) {
        switch (input) {
            case 'WAITING_FOR_CRON' : return 'Waiting';
            case 'RUNNING' : return 'Processing';
            case 'COMPLETED' : return 'Completed';
            case 'COMPLETED_WITH_ERRORS' : return 'Completed with some errors';
            case 'SKIPPED' : return 'Skipped';
            case 'FAILED' : return 'Failed';
            case 'CANCELLED' : return 'Cancelled';
            case 'INACTIVE' : return "Currently inactive";
            default : return input;
        }
    }
});

sageJob.filter('conditionSuccess', function(){
    return function(input) {
        return (input == "COMPLETED" || input == "COMPLETED_WITH_ERRORS");
    }
});

sageJob.controller('JobAuthController', function($scope, $http) {
    $scope.visible = true;
    $scope.email = "";
    $scope.password = "";
    $scope.error = false;
    $scope.errorMessage = "";
});

sageJob.controller('JobController', function($scope, menuService) {

});

sageJob.controller('JobUploadController', function($scope, $http, $window, menuService, dataBus) {
    $scope.id = 1;
    $scope.uploaderId = "fileUploaderBasic";
    $scope.visible = true;
    $scope.empty = true;
    $scope.processes = [];
    $scope.uploadProgress = 0;

    $scope.addProcess = function(process) {
        this.empty = false;
        this.processes.push(process);
    };

    $scope.$on(menuService.menuToggleEvent, function() {
        $scope.visible = menuService.isMethodActive($scope.id);
    });

    $scope.getProgressStyle = function() {
        return {'visibility' : ($scope.uploadProgress > 0) ? 'visible' : 'collapsed',
                'width' : ($scope.uploadProgress * 100) + '%'};
    };

    $scope.submitJobRequest = function() {
        $http.post(submitUrl).success(function(data){
            if (data != null && data.success == true) {
                alert("Your request has been submitted");
                $scope.processes = [];
                menuService.toggleMethod(2);
            }
            else {
                alert(data.message);
            }
        }).error(function(){
            alert("Failed to submit batch job request!");
        });
    };

    $scope.removeFile = function(fileName) {
        $http.post(removeUrl + "?fileName=" + fileName).success(function(data){
            if (data.success) {
                for (var i = 0; i < $scope.processes.length; i++) {
                    if ($scope.processes[i].fileName == fileName) {
                        $scope.processes.splice(i, 1);
                        break;
                    }
                }
            }
            alert(data.message);
        }).error(function(){ alert("Failed to remove file from request."); });
    };

    $window.onload = function() {
        var uploader = new qq.FileUploaderBasic({
            button: document.getElementById($scope.uploaderId),
            action: uploadUrl,
            debug: true,
            allowedExtensions: ['tsv', 'txt', 'csv'],
            hideShowDropArea: true,

            onSubmit: function(id, fileName){
                //console.log('Submit: ' + id + " " + fileName);
            },
            onProgress: function(id, fileName, loaded, total){
                //console.log("Progress: " + fileName + " " + loaded + "/" + total);
                var scope = angular.element("#upload-container").scope();
                scope.$apply(function(){
                    scope.uploadProgress = (loaded / total < 1) ? loaded / total : 0;
                });
            },
            onComplete: function(id, fileName, responseJSON){
                //console.log("Complete: " + fileName + " " + responseJSON);
                if (responseJSON.success) {
                    var scope = angular.element($("#upload-container")).scope();
                    scope.$apply(function(){
                        scope.addProcess(responseJSON.jobProcess);
                    });
                }
                else if (responseJSON.message) {
                    alert(responseJSON.message);
                }
                else {
                    alert("Server did not respond to upload request.");
                }
            },
            onCancel: function(id, fileName){
                //console.log("Cancel: " + fileName);
            },
            onUpload: function(id, fileName, xhr){
                //console.log("Upload: " + fileName + " " + xhr);
            },
            onError: function(id, fileName, xhr) {
                //console.log("Error: " + fileName + " " + xhr);
            },
            messages: {
                typeError: "Sorry, only {extensions} files are allowed for batch processing."
            }
        });
    }
});

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

$(document).ready(function() {
    initVerticalMenu();
});