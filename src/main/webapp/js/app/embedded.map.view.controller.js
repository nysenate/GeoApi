var sage = angular.module('sage');

sage.controller("EmbeddedMapViewController", function($scope, $window, dataBus, uiBlocker, mapService){
    $scope.showPrompt = false;
    $scope.viewInfo = false;
    /** County DoH embeds show DoH info on county clicks instead of senator info */
    $scope.isDoh = $window.doh === true;
    $scope.infoTitle = $scope.isDoh ? "DoH Information" : "Senator Information";

    var clickableType = $scope.isDoh ? "county" : "senate";

    $scope.$on("embeddedMap", function() {
        var data = dataBus.data;
        if (data.statusCode == 0) {
            mapService.clearMarkers();
            /** Show all the district map boundaries */
            if (data?.districts) {
                mapService.clearPolygons();
                $.each(data.districts, function(i, v){
                    if (v.map != null) {
                        mapService.setOverlay(v.map, getMapName(v), false, false,
                            (v.type.toLowerCase() == clickableType) ?
                                function() {
                                    dataBus.setBroadcast("showEmbedDistrict", v);
                                }
                                : null, mapService.colors[0]);
                    }
                });
                mapService.setCenter(42.440510, -76.495460); // Centers the map nicely over NY
                mapService.setZoom(7);
            }
            /** Show the individual district map */
            else if (data.map != null) {
                $scope.setDistrictInfo(data);
                mapService.setOverlay(data.map, getMapName(data), true, true, null, mapService.colors[0]);
                if (data.type.toLowerCase() == clickableType) {
                    mapService.setOfficeMarkers(data?.member?.offices);
                    $scope.showPrompt = false;
                    $scope.showInfo = false;
                }
            }
        }
        mapService.toggleMap(true);
        uiBlocker.unBlock();
    });

    $scope.$on("showEmbedDistrict", function(){
        var data = dataBus.data;
        if (data) {
            $scope.$apply(function(){
                $scope.showPrompt = true;
                $scope.showInfo = true;
                $scope.setDistrictInfo(data);
                mapService.clearMarkers();
                mapService.setOfficeMarkers(data?.member?.offices);
            });
        }
    });

    $scope.setDistrictInfo = function(data) {
        $scope.senator = data.member;
        $scope.district = data.district;
        $scope.link = data.link;
        $scope.distName = data.name;
    };
});
