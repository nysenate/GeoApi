var sage = angular.module('sage');

sage.controller("EmbeddedMapController", function($scope, $http, $window, dataBus, uiBlocker) {
    $scope.districtType = $window.districtType;
    $scope.districtId = $window.districtId;

    $scope.lookup = function() {
        if (this.districtType) {
            uiBlocker.block("Loading maps..");
            $http.get(this.getDistrictMapUrl())
                .then(function(response) {
                    dataBus.setBroadcast("embeddedMap", response.data);
                }, function(response){});
        }
    };

    $scope.getDistrictMapUrl = function () {
        return contextPath + baseApi + "/map/" + this.districtType + "?showMembers=true" + ((this.districtId) ? ("&district=" + this.districtId) : "");
    };

    $scope.lookup();
});