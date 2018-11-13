var sageAdmin = angular.module('sage-admin', ['sage-common']);
var baseAdminApi = contextPath + "/admin/api";

sageAdmin.filter("code", function(){
    return function(input) {
        return (input) ? input.replace(/\\n/g, '<br/>').replace(/\\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;') : '';
    }
});

$(document).ready(function() {
    initVerticalMenu();
});