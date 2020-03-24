var sage = angular.module('sage');

sage.controller("EmbeddedMapController", function($scope, $http, $window, dataBus, uiBlocker) {
    $scope.districtType = $window.districtType;
    $scope.districtCode = $window.districtCode;

    $scope.lookup = function() {
        if (this.districtType) {
            uiBlocker.block("Loading maps..");
            $http.get(this.getDistrictMapUrl())
                .success(function(data) {
                    if (this.districtType === "county") {
                        console.log("District type is county");
                        dataBus.setBroadcast("embeddedCountyMap", data);
                    }
                    else {
                        dataBus.setBroadcast("embeddedMap", data);
                    }
                })
                .error(function(data){});
        }
    };

    $scope.getDistrictMapUrl = function () {
        console.log(contextPath + baseApi + "/map/" + this.districtType + "?showMembers=true" + ((this.districtCode) ? ("&district=" + this.districtCode) : ""));
        return contextPath + baseApi + "/map/" + this.districtType + "?showMembers=true" + ((this.districtCode) ? ("&district=" + this.districtCode) : "");
    };

    $scope.lookup();
});