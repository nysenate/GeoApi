var sage = angular.module('sage');

sage.controller("MemberViewController", function($scope, dataBus, mapService) {
    $scope.visible = false;
    $scope.viewId = "member";
    $scope.member = {};

    $scope.$on(dataBus.viewHandleEvent, function() {
        $scope.visible = ($scope.viewId == dataBus.viewId);
    });

    $scope.showMember = function() {
        $scope.member = dataBus.data;
        dataBus.setBroadcast("expandResults", true);
    };

    /** Used for individual district view */
    $scope.$on("member", function() {
        if (dataBus.data) {
            $scope.showMember();
        }
    });

    $scope.setOfficeMarker = function(office) {
        if (office != null) {
            mapService.setMarker(office.latitude, office.longitude, office.name + ' - ' + office.street, true, true, null);
        }
    }
});