/** Shared functionality across SAGE */
var sageCommon = angular.module('sage-common', []);

function initVerticalMenu() {
    $(".top-method-header li a").click(function(event) {
        if (!$(this).hasClass("active")) {
            $(".top-method-header li a.active").removeClass("active");
            $(this).addClass("active");
        }
    });
}



