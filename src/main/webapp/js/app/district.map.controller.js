var sage = angular.module('sage', ['sage-common']);

/**
 * Controller for handling the `District Maps` function.
 */
sage.controller("DistrictMapController", function($scope, $http, mapService, menuService, dataBus, uiBlocker){
    $scope.visible = false;
    $scope.id = 2;
    $scope.minimized = false;
    $scope.type = "";
    $scope.showMemberOption = false;
    $scope.showMemberList = false;
    $scope.sortedMemberList = [];
    $scope.districtList = [];
    $scope.selectedDistrict = "";

    $scope.$on(menuService.menuToggleEvent, function() {
        $scope.visible = menuService.isMethodActive($scope.id);
        if ($scope.visible) {
            mapService.toggleMap(true);
            $scope.minimized = false;
            $scope.showMemberList = false;
        }
    });

    /**
     * Performs request to district map API to retrieve meta data to populate districtList.
     */
    $scope.metaLookup = function() {
        $http.get(this.getDistrictMapUrl(this.type, null, true))
            .success(function(data) {
                $scope.showMemberOption = ($scope.type === 'senate' || $scope.type === 'congressional' || $scope.type === 'assembly');
                if ($scope.showMemberOption) {
                    // Filter out null members.
                    $scope.sortedMemberList = data.districts.filter(function(resp) { return resp.member != null });
                    $scope.sortedMemberList = $scope.sortedMemberList.sort(function(a, b){
                        return (a.type == "SENATE") ? a.member.shortName.localeCompare(b.member.shortName)
                            : a.member.name.localeCompare(b.member.name);
                    });
                    if ($scope.type == "senate") {
                        $.each($scope.sortedMemberList, function(i,v) {
                            v.member.name = v.member.lastName + ", " + v.member.name.replace(v.member.lastName, '');
                        });
                    }
                }
                $scope.showMemberList = false;
                /** Sort school, town, and county districts by name */
                if ($scope.type == "school" || $scope.type == "town" || $scope.type == "county") {
                    $scope.districtList = data.districts.sort(function(a,b) {
                        return a.name.localeCompare(b.name);
                    });
                }
                /** Otherwise keep the default sort order */
                else {
                    $scope.districtList = data.districts;
                }

                /** Resolve city/town ambiguities */
                if ($scope.type == "town") {
                    $.each($scope.districtList, function(i,v){
                        if (v.district.charAt(0) === '-') {
                            v.name += ' (City)';
                        }
                    });
                }
                $scope.districtList.unshift({district:null, name:'All districts'});
            })
            .error(function(data){});
    };

    /**
     * Performs request to district map API to retrieve map data and delegates to the `districtMap` handler.
     */
    $scope.lookup = function () {
        uiBlocker.block("Loading " + this.type + " maps..");
        $http.get(this.getDistrictMapUrl(this.type, this.selectedDistrict.district, false))
            .success(function(data) {
                dataBus.setBroadcast("districtMap", data);
            }).error(function(data) {
            uiBlocker.unBlock();
            alert("Failed to retrieve district maps.");
        });
    };

    /**
     * Returns the url for accessing the district map API.
     * @param meta If true then no polygon data will be retrieved (just meta data)
     * @returns {string}
     */
    $scope.getDistrictMapUrl = function(type, district, meta) {
        return contextPath + baseApi + "/map/" + type + "?showMembers=true"
            + ((meta === true) ? "&meta=true" :
                ((district) ? ("&district=" + district) : ""));
    }
});