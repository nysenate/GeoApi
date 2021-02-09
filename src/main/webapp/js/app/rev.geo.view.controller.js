var sage = angular.module('sage');

sage.controller("RevGeoViewController", function($scope, $filter, dataBus, mapService) {
    $scope.visible = false;
    $scope.viewId = "revgeo";
    $scope.revGeocoded = false;

    $scope.$on("revgeo", function(){
        $scope = angular.extend($scope, dataBus.data);
        $scope.revGeocoded = ($scope.statusCode == 0);
        if ($scope.revGeocoded) {
            mapService
            var markerTitle = $filter('addressFormat')($scope.address, '');
            mapService.setMarker($scope.geocode.lat, $scope.geocode.lon, markerTitle , true, true, null);
        }
        dataBus.setBroadcast("expandResults", true);
        mapService.toggleMap(true);
    });

    $scope.$on(dataBus.viewHandleEvent, function(){
        $scope.visible = ($scope.viewId == dataBus.viewId);
    });
});