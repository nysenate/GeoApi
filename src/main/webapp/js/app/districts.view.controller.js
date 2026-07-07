var sage = angular.module('sage');

sage.controller('DistrictsViewController', function($scope, $http, $filter, dataBus, mapService, uiBlocker) {
    $scope.visible = false;
    $scope.viewId = "districtsView";
    $scope.showOffices = false;
    $scope.showNeighbors = false;
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
        $scope.address = undefined;
        $scope.geocoded = undefined;
        $scope.geocode = undefined;
        $scope.senateAssigned = undefined;
        $scope.districtAssigned = undefined;
        $scope.districts = undefined;
        $scope.intersectType = "none";
        $scope.overlaps = undefined;

        $scope = angular.extend($scope, dataBus.data);
        $scope.viewSuggestions = !$scope.districtAssigned && !$scope.overlaps;
        dataBus.setBroadcast("expandResults", true);
        mapService.toggleMap(true);

        if ($scope.overlaps) {
            $scope.drawIntersect();
        }
        else {
            /** Update the marker location to point to the geocode */
            if ($scope.districts?.senate?.map) {
                mapService.setOverlay($scope.districts.senate.map,
                    getMapName($scope.districts.senate), true, true, null);
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
            if (data?.districts) {
                mapService.clearPolygons();
                $.each(data.districts, function(i, v){
                    if (v.map) {
                        mapService.setOverlay(v.map, getMapName(v), false, false,
                            (v.member != null) ? function() {

                                /** Draw the office markers */
                                mapService.clearMarkers();
                                mapService.setOfficeMarkers(v.member.offices);
                                dataBus.setBroadcastAndView("member", v, "member");
                                $scope.$apply();
                            } : null
                        );
                    }
                });
                /** City council districts are NYC-only, so frame the boroughs instead of the whole state. */
                if (data.districts[0] && data.districts[0].type === "CITY_COUNCIL") {
                    mapService.map.fitBounds(mapService.getOverlayBounds());
                }
                else {
                    mapService.setCenter(42.440510, -76.495460); // Centers the map nicely over NY
                    mapService.setZoom(7);
                }
            }
            /** Show the individual district map */
            else if (data.map != null) {
                mapService.setOverlay(data.map, getMapName(data), true, true, null, null);
                if (data.member != null) {
                    dataBus.setBroadcastAndView("member", data, "member");

                    /** Draw the office markers */
                    mapService.clearMarkers();
                    mapService.setOfficeMarkers(data?.member?.offices);

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

    /**
     * Whether to show the generic "Code: {district}" line for a type. False for types that
     * already render their district number via a dedicated labeled line. Case-insensitive so
     * it works with both the uppercase intersectType and the lowercase districts map keys.
     */
    $scope.displayCode = function(type) {
        if (type == null) {
            return true;
        }
        var upperType = type.toUpperCase();
        return upperType !== 'SENATE' && upperType !== 'ASSEMBLY' && upperType !== 'CONGRESSIONAL' && upperType !== 'ZIP';
    };

    /** Show the specified district map */
    $scope.showDistrict = function(districtType) {
        if ($scope.districts[districtType] != null && typeof $scope.districts[districtType] != "undefined") {
            var district = $scope.districts[districtType];
            district.type = districtType; // Set the type for the formatDistrictName method
            mapService.resizeMap();
            mapService.setOverlay(district.map, getMapName(district), true, true, null);
        }
    };

    $scope.showFullMapForOverlap = function(index, overlap) {
        mapService.setOverlay(overlap.fullMap, overlap.name, false, true, null, this.colors[index % this.colors.length]);
    };

    $scope.setOfficeMarker = mapService.setOfficeMarker;

    $scope.requestDistrictInfo = function(addr) {
        dataBus.setBroadcast("requestDistrictInfo", addr);
    };

    $scope.getBgStyle = function(i) {
        return {"background-color" : this.colors[i % this.colors.length]};
    };

    $scope.getColorStyle = function(senateDistrict) {
        return {"color": this.senateColors[senateDistrict]};
    };

    $scope.drawIntersect = function() {
        mapService.clearPolygons();
        /** Draw the intersected senate maps */
        if ($scope.overlaps) {
            /** Assign a unique color to each district */
            $.each($scope.overlaps, function (i, overlap) {
                $scope.senateColors[overlap.district] = $scope.colors[i % $scope.colors.length];
                if (overlap.map != null) {
                    mapService.setOverlay(overlap.map, overlap.name + " Coverage", false, false, null,
                        $scope.senateColors[overlap.district], {fillOpacity: 0.5});
                }
            });
        }
    };
});
