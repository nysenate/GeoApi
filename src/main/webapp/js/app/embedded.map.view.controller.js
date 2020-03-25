var sage = angular.module('sage');

sage.controller("EmbeddedMapViewController", function($scope, dataBus, uiBlocker, mapService){
    $scope.showPrompt = false;
    $scope.viewInfo = false;

    $scope.$on("embeddedMap", function() {
        var data = dataBus.data;
        if (data.statusCode == 0) {
            mapService.clearMarkers();
            /** Show all the district map boundaries */
            if (data != null && data.districts != null) {
                mapService.clearPolygons();
                $.each(data.districts, function(i, v){
                    if (v.map != null) {
                        mapService.setOverlay(v.map.geom, formatDistrictName(v), false, false,
                            (v.type.toLowerCase() == "senate") ?
                                function() {
                                    dataBus.setBroadcast("showEmbedSenator", v);
                                }
                                : null, mapService.colors[0]);
                    }
                });
                mapService.setCenter(42.440510, -76.495460); // Centers the map nicely over NY
                mapService.setZoom(7);
            }
            /** Show the individual district map */
            else if (data.map != null) {
                $scope.senator = data.member;
                $scope.district = data.district;
                mapService.setOverlay(data.map.geom, formatDistrictName(data), true, true, null, mapService.colors[0]);
                if (data.type.toLowerCase() == "senate") {
                    $scope.setOfficeMarkers(data.member.offices);
                    $scope.showPrompt = false;
                    $scope.showInfo = false;
                }
            }
        }
        mapService.toggleMap(true);
        uiBlocker.unBlock();
    });

    $scope.$on("showEmbedSenator", function(){
        var data = dataBus.data;
        if (data) {
            $scope.$apply(function(){
                $scope.showPrompt = true;
                $scope.showInfo = true;
                $scope.senator = data.member;
                $scope.district = data.district;
                mapService.clearMarkers();
                $scope.setOfficeMarkers(data.member.offices);
            });
        }
    });


    //COUNTY SECTION
    $scope.$on("embeddedCountyMap", function() {
        var data = dataBus.data;
        if (data.statusCode == 0) {
            mapService.clearMarkers();
            /** Show all the district map boundaries */
            if (data != null && data.districts != null) {
                mapService.clearPolygons();
                $.each(data.districts, function(i, v){
                    if (v.map != null) {
                        mapService.setOverlay(v.map.geom, formatDistrictName(v), false, false,
                            (v.type.toLowerCase() == "county") ?
                                function() {
                                    dataBus.setBroadcast("showEmbedCounty", v);
                                }
                                : null, mapService.colors[0]);
                    }
                });
                mapService.setCenter(42.440510, -76.495460); // Centers the map nicely over NY
                mapService.setZoom(7);
            }
            /** Show the individual district map */
            else if (data.map != null) {
                $scope.link = data.link;
                $scope.district = data.district;
                mapService.setOverlay(data.map.geom, formatDistrictName(data), true, true, null, mapService.colors[0]);
                if (data.type.toLowerCase() == "county") {
                    $scope.setOfficeMarkers(data.member.offices);
                    $scope.showCountyPrompt = false;
                    $scope.showInfo = false;
                }
            }
        }
        mapService.toggleMap(true);
        uiBlocker.unBlock();
    });

    $scope.$on("showEmbedCounty", function(){
        var data = dataBus.data;
        if (data) {
            $scope.$apply(function(){
                $scope.showCountyPrompt = true;
                $scope.showInfo = true;
                $scope.link = data.link;
                $scope.district = data.district;
                $scope.distName = data.name;
                mapService.clearMarkers();
            });
        }
    });

    $scope.setOfficeMarkers = function(offices) {
        /** Clicking an office marker will open info pane with details */
        $.each(offices, function(i, office){
            if (office && office.name != null && office.name != "") {
                mapService.setMarker(office.latitude, office.longitude, office.name, false, false,
                    "<div style='width:160px;'>" +
                    "<p style='color:teal;font-size:18px;'>" + office.name + "</p>" +
                    "<p>" + office.street + "</p>" +
                    "<p>" + office.additional+ "</p>" +
                    "<p>" + office.city + ", " + office.province + " " + office.postalCode +"</p>" +
                    "<p>Phone " + office.phone + "</p>" +
                    "</div>");
            }
        });
    }
});