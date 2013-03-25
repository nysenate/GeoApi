var sage = angular.module('sage', []);
var uploader;

sage.controller('JobAuthController', function($scope, $http) {
    $scope.email = "";
    $scope.password = "";
    $scope.error = false;
    $scope.errorMessage = "";
});

sage.controller('JobUploadController', function($scope, $http) {
    $scope.empty = true;
    $scope.processes = [];
    $scope.addProcess = function(process) {
        this.empty = false;
        this.processes.push(process);
    }
});

$(document).ready(function() {
    var canSubmit = false;

    if ($.trim($("#error").html())=="") {
        $("#error").hide();
    }

    uploader = new qq.FileUploader({
        action: contextPath + '/job/upload',
        element: document.getElementById('fileuploader'),
        allowedExtensions:['tsv','csv', 'txt'],
        multiple:false,
        template: '<ul class="qq-upload-list"></ul><div class="qq-uploader">' +
            '<div class="qq-upload-drop-area"><span>Drop file here to upload</span></div>' +
            '<div class="custom-button qq-upload-button"><span><span aria-hidden="true" style="color:teal;" data-icon="&#128228;"></span>Add a file</span></div>' +
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
});