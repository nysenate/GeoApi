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


// sageJob.controller('JobController', function($scope, menuService) {
//
// });

$(document).ready(function() {
    initVerticalMenu();
});