var sageJob = angular.module('sage-job', ['sage-common']);
var baseStatusApi = "/job/status";
var uploader;

sageJob.filter('yesno', function(){
    return function(input) {
        return (input) ? 'Yes' : 'No';
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
    (function(){menuService.toggleMethod(2);}());
});

sageJob.controller('JobUploadController', function($scope, $http, $window, menuService, dataBus) {
    $scope.id = 1;
    $scope.uploaderId = "fileUploaderBasic";
    $scope.visible = true;
    $scope.empty = true;
    $scope.processes = [];

    $scope.addProcess = function(process) {
        this.empty = false;
        this.processes.push(process);
    };

    $scope.$on(menuService.menuToggleEvent, function(){
        $scope.visible = menuService.isMethodActive($scope.id);
    });

    $window.onload = function() {
        var uploader = new qq.FileUploaderBasic({
            button: document.getElementById($scope.uploaderId),
            action: contextPath + "/jobtest/upload",
            debug: true,
            allowedExtensions: ['tsv', 'txt', 'csv'],
            hideShowDropArea: true,

            onSubmit: function(id, fileName){
                console.log('Submit: ' + id + " " + fileName);
            },
            onProgress: function(id, fileName, loaded, total){
                console.log("Progress: " + fileName + " " + loaded + "/" + total);
            },
            onComplete: function(id, fileName, responseJSON){
                console.log("Complete: " + fileName + " " + responseJSON);
                if (responseJSON.success) {
                    var scope = angular.element($("#upload-container")).scope();
                    scope.$apply(function(){
                        scope.addProcess(responseJSON.jobProcess);
                    });
                }
            },
            onCancel: function(id, fileName){
                console.log("Cancel: " + fileName);
            },
            onUpload: function(id, fileName, xhr){
                console.log("Upload: " + fileName + " " + xhr);
            },
            onError: function(id, fileName, xhr) {
                console.log("Error: " + fileName + " " + xhr);
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

            $scope.cpInterval = setInterval(function() {$scope.getActiveProcesses()}, 6000);
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
        $http.get(contextPath + baseStatusApi + "/running")
            .success(function(data, status, headers, config) {
                if (data && data.success) {
                    $scope.runningProcesses = data.statuses;
                    $scope.computeProgress();
                }
            }).error(function(data) {
                console.log("Failed to retrieve running job processes " + data);
            });
    }

    $scope.computeProgress = function() {
        $.each(this.runningProcesses, function(){
            var total = this.process.recordCount;
            var current = this.completedRecords;
            if (total > 0) {
                this.progressStyle = {'width' : ((current/total) * 100) + "%"};
            }
        });
    }

    $scope.getActiveProcesses = function() {
        $http.get(contextPath + baseStatusApi + "/active")
            .success(function(data, status, headers, config) {
                if (data && data.success) {
                    $scope.activeProcesses = data.statuses;
                }
                else {
                    console.log("Active processes: " + data);
                }
            }).error(function(data) {
                console.log("Error retrieving active processes. " + data);
            });
    };

    $scope.getCompletedProcesses = function() {
        $http.get(contextPath + baseStatusApi + "/completed")
            .success(function(data, status, headers, config) {
                if (data && data.success) {
                    $scope.completedProcesses = data.statuses;
                }
            }).error(function(data, status){
                console.log("Error retrieving completed processes.");
            });
    };
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
        $http.get(contextPath + baseStatusApi + "/all")
             .success(function(data, status, headers, config){
                 if (data && data.success) {
                     $scope.allProcesses = data.statuses;
                 }
             });
    }

});
    /*
function initUploader() {
    if (uploader === null || typeof uploader === 'undefined') {
        uploader = new qq.FileUploader({
            action: contextPath + '/job/upload',
            element: document.getElementById('fileuploader'),
            allowedExtensions:['tsv','csv', 'txt'],
            multiple:false,
            template: '<ul class="qq-upload-list"></ul><div class="qq-uploader">' +
                '<div class="qq-upload-drop-area"><span>Drop file here to upload</span></div>' +
                '<div class="custom-button qq-upload-button"><span><div class="icon-upload teal" style="margin-right:5px;"></div>Add a file</span></div>' +
                '</div>',
            onSubmit: doSubmit,
            onComplete: doComplete
        });

        function doSubmit(id, fileName) {
            return true;
        }

        function doComplete(id, fileName, uploadResponse) {
            var html="";
            if(uploadResponse.success == true && uploadResponse.process != null) {

                var scope = angular.element($("#upload-container")).scope();
                scope.$apply(function(){
                    scope.addProcess(uploadResponse.process);
                });
            }
            else {
                //take care of bad headers or blank files here
                alert(uploadResponse.message);
            }
        }

        $('.custom-submit-button').click(function() {
            var message = validate();
            if(message == "" && canSubmit) {
                $('#form_submit').html("Saving...");

                canSubmit = false;

                $('#uploadForm').submit();
                return true;
            }
            else {
                if(!canSubmit) {
                    message += "<br>Select a valid file";
                }
                $("#error").html(message);
                if(!$("#error").is(":visible")) {
                    $("#error").slideToggle(500);
                }
                return false;
            }
        });
    }
}     */

$(document).ready(function() {
    initVerticalMenu();
    if ($.trim($("#error").html())=="") {
        $("#error").hide();
    }
});