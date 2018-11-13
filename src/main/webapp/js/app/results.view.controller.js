var sage = angular.module('sage');

/**------------------------------------------------\
 * Views                                           |
 *------------------------------------------------*/
sage.controller('ResultsViewController', function($scope, dataBus, mapService) {
    $scope.paneVisible = false;
    $scope.showResultsTab = $("#showResultsTab");
    $scope.centercolumn = $('#contentcolumn');
    $scope.rightcolumn = $("#rightcolumn");
    $scope.toggleClass = "sidebar";

    $scope.$on('expandResults', function() {
        $scope.paneVisible = $scope.toggleResultPane(dataBus.data);
    });

    $scope.$on('hideResultTab', function(){
        $scope.paneVisible = false;
        $scope.closeResults();
        $scope.showResultsTab.hide();
    });

    $scope.toggleResultPane = function(show) {
        if (show != null) {
            if (show) {
                $scope.openResults();
                mapService.resizeMap();
                $scope.showResultsTab.hide();
                return true;
            }
            else {
                $scope.closeResults();
                mapService.resizeMap();
                $scope.showResultsTab.show();
            }
        }
        return false;
    };

    $scope.closeResults = function() {
        $scope.centercolumn.removeClass($scope.toggleClass);
        $scope.rightcolumn.hide();
    };

    $scope.openResults = function() {
        $scope.centercolumn.addClass($scope.toggleClass);
        $scope.rightcolumn.show();
    }
});