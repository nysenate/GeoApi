var sage = angular.module('sage', ['sage-common']);

/**------------------------------------------------\
 * Methods                                         |
 *------------------------------------------------*/
/**
 * Controller for handling the `District Information` function.
 */
sage.controller('DistrictInfoController', function($scope, $http, mapService, menuService, dataBus, uiBlocker) {
    $scope.visible = true;
    $scope.id = 1;
    $scope.inputId = "addressInput";
    $scope.addr = "";
    $scope.showOptions = false;
    $scope.geoProvider = "default";
    $scope.provider = "default";
    $scope.uspsValidate = "true";
    $scope.showMaps = "true";

    mapService.makeAutocomplete($scope.inputId);

    $scope.$on(menuService.menuToggleEvent, function() {
        $scope.visible = menuService.isMethodActive($scope.id);
        if ($scope.visible) {
            mapService.toggleMap(true);
        }
    });

    /** Listens for requests to perform lookup */
    $scope.$on("requestDistrictInfo", function() {
        $scope.addr = dataBus.data;
        $scope.lookup(true);
    });

    /**
     * Performs request to District Assign API and delegates to the `districtInfo` handler.
     */
    $scope.lookup = function(skipValidate) {
        if (skipValidate || this.validateSearch()) {
            mapService.getPlacePredictions(this.addr, "placeSuggestions");
            uiBlocker.block("Looking up districts for " + this.addr);
            mapService.clearAll();
            $http.get(this.getDistUrl())
                .success(function(data) {
                    dataBus.setBroadcastAndView("districtInfo", data, "districtsView");
                }).error(function(data, status, headers, config) {
                uiBlocker.unBlock();
                alert("Failed to lookup districts. The application did not return a response.");
            });
            return true;
        }
        else {
            alert("Your address search should be at least 5 characters long. Please try to be as specific " +
                "as possible.");
            return false;
        }
    };

    /** Validates the address input. Also updates the model entry by fetching value manually since
     it's tricky to auto-bind properly with autocomplete widgets */
    $scope.validateSearch = function() {
        this.addr = $("#" + this.inputId).val();
        return (this.addr != '' && this.addr.length >= 5);
    };

    /**
     * Returns the url for accessing the district assignment API.
     * @returns {string}
     */
    $scope.getDistUrl = function () {
        var url = contextPath + baseApi + "/district/assign?addr=" + this.addr;
        url += (this.provider != "" && this.provider != "default") ? "&provider=" + this.provider : "";
        url += (this.geoProvider != "" && this.geoProvider != "default") ? "&geoProvider=" + this.geoProvider : "";
        url += (this.uspsValidate != "false" && this.uspsValidate != "") ? "&uspsValidate=true" : "";
        url += (this.showMaps != "false" && this.showMaps != "") ? "&showMaps=true" : "";
        url += "&showMembers=true&showMultiMatch=true";
        url = url.replace(/#/g, ""); // Pound marks mess up the query string
        return url;
    };
});