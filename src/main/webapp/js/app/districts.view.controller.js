var sage = angular.module('sage');

sage.controller('DistrictsViewController', function($scope, $http, $filter, dataBus, mapService, uiBlocker) {
    $scope.visible = false;
    $scope.viewId = "districtsView";
    $scope.showOffices = false;
    $scope.showNeighbors = false;
    $scope.neighborPolygons = [];
    $scope.colors = mapService.colors;
    $scope.neighborColors = ["#FF4500", "#639A00"];
    $scope.senateColors = {};
    $scope.placeSuggestions = {};
    $scope.viewSuggestions = false;

    $scope.$on(dataBus.viewHandleEvent, function(){
        $scope.visible = ($scope.viewId == dataBus.viewId);
    });

    /** Handle results of district info query */
    $scope.$on("districtInfo", function() {
        $scope = angular.extend($scope, dataBus.data);
        $scope.viewSuggestions = (!$scope.districtAssigned && !$scope.multiMatch);
        dataBus.setBroadcast("expandResults", true);
        mapService.toggleMap(true);

        if ($scope.multiMatch) {
            var fillOpacity = 0.5;
            /** Draw the intersected senate maps */
            if ($scope.overlaps[$scope.intersectType]) {
                /** Sort the senate maps by greatest percentage first */
                $scope.overlaps[$scope.intersectType] = $scope.overlaps[$scope.intersectType].sort(function(a, b) {
                    return b.areaPercentage - a.areaPercentage;
                });
                /** Assign a unique color to each senate district */
                $.each($scope.overlaps[$scope.intersectType], function(i,v){
                    $scope.senateColors[v.district] = $scope.colors[i % $scope.colors.length];
                    if (v.map != null) {
                        var name =  "NY " + $scope.intersectType.charAt(0).toUpperCase() + $scope.intersectType.slice(1) + " District ";
                        mapService.setOverlay(v.map.geom, name + v.district + " Coverage", false, false, null,
                            $scope.colors[i % $scope.colors.length], {fillOpacity:fillOpacity});
                    }
                });
            }
            /** Display senate street lines if available */
            if ($scope.matchLevel == "STREET") {
                fillOpacity = 0.2;
                if ($scope.streetLine && $scope.streetLine.geom) {
                    mapService.setLines($scope.streetLine.geom, true, false, "#555", {
                        path: 'M 0 0 L 0 1 L 1 1 L 1 0 z', strokeWeight: 1, strokeOpacity: 0,
                        scale: 9, fillOpacity: 0.5, fillColor: "yellow"
                    });
                }
                else {
                    mapService.setMarker($scope.geocode.lat, $scope.geocode.lon, '', true, true);
                }
            }
            else {
                /** Set region (city / zip) dashed line boundary for multi-matches */
                mapService.setLines($scope.referenceMap.geom, true, false, {});
            }
        }
        else {
            /** Update the marker location to point to the geocode */
            if ($scope.districtAssigned) {
                if ($scope.districts.senate.map) {
                    mapService.setOverlay($scope.districts.senate.map.geom,
                        formatDistrictName($scope.districts.senate, "Senate"), true, true, null);
                }
            }
            if ($scope.geocoded) {
                mapService.setMarker($scope.geocode.lat, $scope.geocode.lon,
                    $filter('addressFormat')($scope.address, ''), true, true, null);
                mapService.setZoom(15);
            }
        }

        /** Hide neighbors initially */
        $scope.showNeighbors = false;
        uiBlocker.unBlock();
        mapService.resizeMap();
    });

    /** Handle results of district maps query */
    $scope.$on("districtMap", function() {
        var data = dataBus.data;
        if (data.statusCode == 0) {
            /** Hide results tab and markers */
            dataBus.setBroadcast("hideResultTab", false);
            mapService.clearMarkers();

            /** Show all the district map boundaries */
            if (data != null && data.districts != null) {
                mapService.clearPolygons();
                $.each(data.districts, function(i, v){
                    if (v.map != null) {
                        mapService.setOverlay(v.map.geom, formatDistrictName(v), false, false,
                            (v.type == "SENATE") ? function() {

                                /** Draw the office markers */
                                mapService.clearMarkers();
                                $.each(v.member.offices, function(i, office){
                                    console.log(office);

                                    if (office && office.name != null && office.name != "") {
                                        mapService.setMarker(office.latitude, office.longitude, office.name + ' - ' + office.street, false, false);
                                    }
                                });
                                dataBus.setBroadcastAndView("member", v.member, "member");
                                $scope.$apply();
                            } : null
                        );
                    }
                });
                mapService.setCenter(42.440510, -76.495460); // Centers the map nicely over NY
                mapService.setZoom(7);
            }
            /** Show the individual district map */
            else if (data.map != null) {
                mapService.setOverlay(data.map.geom, formatDistrictName(data), true, true, null, null);
                if (data.type == "SENATE") {
                    dataBus.setBroadcastAndView("member", data.member, "member");

                    /** Draw the office markers */
                    mapService.clearMarkers();
                    if (data.member && data.member.offices) {
                        $.each(data.member.offices, function(i, office){
                            if (office && office.name != null && office.name != "") {
                                mapService.setMarker(office.latitude, office.longitude, office.name + ' - ' + office.street, false, false);
                            }
                        });
                    }
                }
                else {
                    dataBus.setBroadcast("hideResultTab");
                }
            }
        }
        mapService.toggleMap(true);
        uiBlocker.unBlock();
    });

    $scope.$on("placeSuggestions", function(){
        var sugg = dataBus.data;
        if (sugg != null && sugg.status == 'OK') {
            $scope.placeSuggestions = sugg.data;
        }
        else {
            $scope.placeSuggestions = {};
        }
    });

    /** Show the specified district map */
    $scope.showDistrict = function(districtType) {
        if ($scope.districts[districtType] != null && typeof $scope.districts[districtType] != "undefined") {
            var district = $scope.districts[districtType];
            district.type = districtType; // Set the type for the formatDistrictName method
            mapService.resizeMap();
            mapService.setOverlay(district.map.geom, formatDistrictName(district), true, true, null);
        }
    };

    $scope.showFullMapForOverlap = function(index, overlap, matchLevel) {
        var geom = (matchLevel == "STREET") ? overlap.map.geom : overlap.fullMap.geom;
        mapService.setOverlay(geom, overlap.name, false, true, null, this.colors[index % this.colors.length]);
    };

    $scope.showNeighborDistricts = function(type, neighbors) {
        this.showNeighbors = true;
        $.each(neighbors, function(i, neighbor){
            neighbor.style = {'color' : $scope.neighborColors[i % 2] };
            $scope.neighborPolygons.push(mapService.setOverlay(neighbor.map.geom, formatDistrictName(neighbor, "Senate"),
                false, false, null, neighbor.style['color']));
        });
    };

    $scope.hideNeighborDistricts = function()  {
        this.showNeighbors = false;
        $.each($scope.neighborPolygons, function(i, neighborPolygon){
            mapService.clearPolygon(neighborPolygon);
        });
    };

    $scope.setOfficeMarker = function(office) {
        if (office != null) {
            mapService.setMarker(office.latitude, office.longitude, office.name + ' - ' + office.street, true, true, null);
        }
    };

    $scope.requestDistrictInfo = function(addr) {
        dataBus.setBroadcast("requestDistrictInfo", addr);
    };

    $scope.getBgStyle = function(i) {
        return {"background-color" : this.colors[i % this.colors.length]};
    };

    $scope.getColorStyle = function(senateDistrict) {
        return {"color": this.senateColors[senateDistrict]};
    };
});