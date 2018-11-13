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