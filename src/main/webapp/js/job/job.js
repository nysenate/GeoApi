var sageJob = angular.module('sage-job', ['sage-common']);
var submitUrl = contextPath + "/job/submit";
var uploadUrl = contextPath + "/job/upload";
var removeUrl = contextPath + "/job/remove";
var cancelUrl = contextPath + "/job/cancel";
var statusUrl = contextPath + "/job/status";
var uploader;

sageJob.controller('JobController', function($scope, menuService) {

});

$(document).ready(function() {
    initVerticalMenu();
});