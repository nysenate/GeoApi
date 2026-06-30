var sage = angular.module('sage');

/**
 * Controller for handling the `District Maps` function.
 */
sage.controller("DistrictMapController", function($scope, $http, $timeout, $filter, mapService, menuService, dataBus, uiBlocker){
    $scope.visible = false;
    $scope.id = 2;
    $scope.minimized = false;
    $scope.type = null;
    $scope.selectedDistrict = "";
    $scope.intersectType = "none";
    $scope.showMemberOption = false;
    $scope.showOptions = false;
    $scope.sortedMemberList = [];
    $scope.districtList = [];
    $scope.districtSearch = "";
    $scope.showDistrictDropdown = false;
    $scope.highlightIndex = 0;
    $scope.showIntersectMenu = false;
    $scope.districtTypes = [];

    /**
     * Loads the selectable district types (those backed by shapefiles) from the map API
     * so the Type and intersection menus stay in sync with the backend DistrictType enum.
     */
    $scope.loadDistrictTypes = function() {
        $http.get(contextPath + baseApi + "/map/options")
            .then(function(response) {
                $scope.districtTypes = response.data.map(function(t) {
                    return { value: t.enumName.toLowerCase(), label: t.displayName };
                });
            }, function(response) {});
    };
    $scope.loadDistrictTypes();


    $scope.$on(menuService.menuToggleEvent, function() {
        $scope.visible = menuService.isMethodActive($scope.id);
        if ($scope.visible) {
            mapService.toggleMap(true);
            $scope.minimized = false;
        }
        console.log("menu toggled for district map. visible? " + $scope.visible);
    });

    $scope.hideDropdown = function() {
        $timeout(function() { $scope.showDistrictDropdown = false; }, 200);
    };

    $scope.matchesSearch = function(d) {
        if (!$scope.districtSearch) return true;
        var search = $scope.districtSearch.toLowerCase();
        var name = d.name.toLowerCase();
        return name.indexOf(search) === 0 || name.indexOf(' ' + search) !== -1;
    };

    $scope.handleKeydown = function(event) {
        var matches = $scope.districtList.filter($scope.matchesSearch);
        if (event.keyCode === 40) {
            event.preventDefault();
            if ($scope.highlightIndex < matches.length - 1) $scope.highlightIndex++;
        } else if (event.keyCode === 38) {
            event.preventDefault();
            if ($scope.highlightIndex > 0) $scope.highlightIndex--;
        } else if (event.keyCode === 13) {
            if (matches.length && $scope.highlightIndex < matches.length) {
                $scope.selectDistrict(matches[$scope.highlightIndex]);
            }
        }
    };

    $scope.onMemberSelect = function() {
        if (!$scope.selectedDistrict) return;
        $scope.districtSearch = $scope.selectedDistrict.name;
        $scope.showDistrictDropdown = false;
        $scope.lookup();
    };

    $scope.selectDistrict = function(d) {
        $scope.selectedDistrict = d;
        $scope.districtSearch = d.name;
        $scope.showDistrictDropdown = false;
        $scope.lookup();
    };

    /**
     * Performs request to district map API to retrieve meta data to populate districtList.
     */
    $scope.metaLookup = function() {
        $scope.districtSearch = "";
        $scope.intersectType = "none";
        $http.get(this.getDistrictMapUrl(this.type, null, true))
            .then(function(response) {
                var data = response.data;
                $scope.sortedMemberList = data.districts.filter(function(resp) { return resp.member != null });
                $scope.showMemberOption = $scope.sortedMemberList.length !== 0;
                if ($scope.showMemberOption) {
                    $scope.sortedMemberList = $scope.sortedMemberList.sort(function(a, b){
                        return formatMemberName(a.member.info, true)
                            .localeCompare(formatMemberName(b.member.info, true));
                    });
                }
                /** Sort school, town, and county districts by name */
                $scope.districtList = data.districts;

                if ($scope.type !== "") {
                    $scope.districtList.unshift({district:null, name:'All districts'});
                }
            }, function(response){});
    };

    /**
     * Performs request to district map API to retrieve map data and delegates to the `districtMap` handler.
     */
    $scope.lookup = function () {
        uiBlocker.block("Loading " + this.type.replace("_", "/") + " maps...");
        $scope.showIntersectMenu = $scope.selectedDistrict.name !== "All districts";
        // If there is no intersection type specified, we can just retrieve the map
        if ($scope.intersectType === "none" || $scope.type === $scope.intersectType || $scope.selectedDistrict.district === null) {
            $http.get(this.getDistrictMapUrl(this.type, this.selectedDistrict.district, false))
                .then(function(response) {
                    mapService.clearAll();
                    dataBus.setBroadcast("districtMap", response.data);
                }, function(response) {
                mapService.clearAll();
                uiBlocker.unBlock();
                alert("Failed to retrieve district maps.");
            });
        }
        // otherwise perform an intersect request
        else {
            mapService.clearAll();
            $http.get(this.getIntersectUrl())
                .then(function(response) {
                    mapService.clearAll();
                    dataBus.setBroadcastAndView("districtInfo", response.data, "districtsView");
                }, function(response) {
                mapService.clearAll();
                uiBlocker.unBlock();
                alert("You must select the type and district / member first. Same source and Intersection type is not supported");
            });
        }
    };

    /**
     * Returns the url for accessing the district map API.
     * @param meta If true then no polygon data will be retrieved (just meta data)
     * @returns {string}
     */
    $scope.getDistrictMapUrl = function(type, district, meta) {
        return contextPath + baseApi + "/map/" + type + "?showMembers=true"
            + ((meta === true) ? "&meta=true" :
                (district ? ("&district=" + district) : ""));
    };

    /**
     * Returns the url for accessing the district intersection API.
     * @returns {string}
     */
    $scope.getIntersectUrl = function () {
        if($scope.selectedDistrict.district === null || $scope.selectedDistrict.district === "") {
            $scope.intersectType = "none";
            $scope.selectedDistrict.district = "";
            $scope.showIntersectMenu = false;
        }
        var url = contextPath + baseApi + "/district/intersect?sourceType=" + $scope.type + "&sourceId=" + $scope.selectedDistrict.district;
        url += "&intersectType=" + $scope.intersectType;
        url = url.replace(/#/g, ""); // Pound marks mess up the query string
        return url;
    };
});
