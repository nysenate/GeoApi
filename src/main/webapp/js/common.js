/** Shared functionality across SAGE */

/** Defines the global sage module */
var sage = angular.module('sage', []);
var dataDebug = false;

function initVerticalMenu() {
    $("p.method-header").click(function(event) {
        if (!$(this).hasClass("active")){
            $(".form-container.active").removeClass("active").slideUp(250);
            $("p.method-header.active").removeClass("active");
            $(this).addClass("active");
            $(this).next(".form-container").slideDown(250).addClass("active");
        }
    });
}


