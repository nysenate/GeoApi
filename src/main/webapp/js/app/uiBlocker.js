var sage = angular.module('sage');

sage.factory("uiBlocker", function($rootScope) {
    var uiBlocker = {};
    uiBlocker.block = function(msg) {
        $.blockUI({css:{border:'1px solid #ddd'}, overlayCSS: { backgroundColor: '#eee', opacity:0.5 }, message: msg});
    };
    uiBlocker.unBlock = function() {
        $.unblockUI();
    };
    return uiBlocker;
});