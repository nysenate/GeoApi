var sage = angular.module('sage');

sage.controller("MemberViewController", function($scope, dataBus, mapService) {
    $scope.visible = false;
    $scope.viewId = "member";
    $scope.member = {};

    $scope.$on(dataBus.viewHandleEvent, function() {
        $scope.visible = ($scope.viewId == dataBus.viewId);
    });

    $scope.showMember = function() {
        $scope.member = dataBus.data.member;
        $scope.district = dataBus.data.district;
        $scope.name = dataBus.data.name;
        dataBus.setBroadcast("expandResults", true);
    };

    /** Used for individual district view */
    $scope.$on("member", function() {
        if (dataBus.data) {
            $scope.showMember();
        }
    });

    $scope.setOfficeMarker = mapService.setOfficeMarker;
});
