var sageJob = angular.module('sage-job');

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


sageJob.controller('JobUploadController', function($scope, $http, $window, menuService, dataBus) {
    $scope.id = 1;
    $scope.uploaderId = "fileUploaderBasic";
    $scope.uploaderInputId = "fileUploaderInput";
    $scope.visible = true;
    $scope.empty = true;
    $scope.processes = [];
    $scope.uploadProgress = 0;
    var allowedExtensions = ['tsv', 'txt', 'csv'];

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

    $scope.uploadFiles = function(files) {
        for (var i = 0; i < files.length; i++) {
            uploadFile(files[i]);
        }
    };

    function uploadFile(file) {
        if (!isAllowedFile(file.name)) {
            alert("Sorry, only " + allowedExtensions.join(", ") + " files are allowed for batch processing.");
            return;
        }

        var xhr = new XMLHttpRequest();
        xhr.open("POST", uploadUrl + "?qqfile=" + encodeURIComponent(file.name), true);
        xhr.setRequestHeader("Content-Type", "application/octet-stream");

        xhr.upload.onprogress = function(event) {
            if (event.lengthComputable) {
                $scope.$apply(function(){
                    $scope.uploadProgress = (event.loaded / event.total < 1) ? event.loaded / event.total : 0;
                });
            }
        };

        xhr.onload = function() {
            var responseJSON = parseUploadResponse(xhr.responseText);
            $scope.$apply(function(){
                $scope.uploadProgress = 0;
                if (xhr.status >= 200 && xhr.status < 300 && responseJSON && responseJSON.success) {
                    $scope.addProcess(responseJSON.jobProcess);
                }
                else if (responseJSON && responseJSON.error) {
                    alert(responseJSON.error);
                }
                else if (responseJSON && responseJSON.message) {
                    alert(responseJSON.message);
                }
                else {
                    alert("Server did not respond to upload request.");
                }
            });
        };

        xhr.onerror = function() {
            $scope.$apply(function(){
                $scope.uploadProgress = 0;
            });
            alert("Failed to upload file.");
        };

        xhr.send(file);
    }

    function isAllowedFile(fileName) {
        var extension = "";
        var extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex > -1) {
            extension = fileName.substring(extensionIndex + 1).toLowerCase();
        }
        for (var i = 0; i < allowedExtensions.length; i++) {
            if (allowedExtensions[i] == extension) {
                return true;
            }
        }
        return false;
    }

    function parseUploadResponse(responseText) {
        try {
            return JSON.parse(responseText);
        }
        catch (e) {
            return null;
        }
    }

    function initNativeUploader() {
        var input = document.getElementById($scope.uploaderInputId);
        if (input) {
            input.addEventListener('change', function() {
                $scope.uploadFiles(input.files);
                input.value = "";
            });
        }
    }

    if ($window.document.readyState == "complete") {
        initNativeUploader();
    }
    else {
        $window.addEventListener('load', initNativeUploader);
    }
});
