/**-------------------------------------------------\
 * Base Configuration                               |
 * ------------------------------------------------*/
var baseApi = "/api/v2";
var map;

/**-------------------------------------------------\
 * Data Bus                                         |
 *--------------------------------------------------|
 * Allows controllers and views to communicate.     |
 * The ajax response is propagated to the specified |
 * 'view' by emitting an event from the $rootScope. |
 * ------------------------------------------------*/
sage.factory("responseService", function($rootScope) {
    var responseService = {};
    responseService.setResponse = function(view, response) {
        console.log(response);
        this.view = view;
        this.response = response;
        this.broadcastItem();
    }

    responseService.broadcastItem = function() {
        $rootScope.$broadcast(this.view);
    }
    return responseService;
});

/**------------------------------------------------\
 * Filters                                         |
 *------------------------------------------------*/

/** Removes a sequence from an input string */
sage.filter('remove', function(){
    return function(input, string) {
        if (input !== null && typeof input !== 'undefined') {
            return input.replace(string, "");
        }
    }
});

sage.filter('capitalize', function() {
    return function(input) {
        if (input !== null && typeof input !== 'undefined') {
            return input.substring(0,1).toUpperCase() + input.substring(1);
        }
    }
});

/** Formats an address properly */
sage.filter('addressFormat', function(){
    return function(address, line) {
        if (typeof address !== 'undefined') {
            var line1 = (notNullOrEmpty(address.addr1) ? address.addr1 : "") +
                        (notNullOrEmpty(address.addr2) ? " " + address.addr2 + "" : "");

            var line2 = (notNullOrEmpty(address.city) ? " " + address.city + "," : "") +
                        (notNullOrEmpty(address.state) ? " " + address.state : "") +
                        (notNullOrEmpty(address.zip5) ? " " + address.zip5 : "") +
                        (notNullOrEmpty(address.zip4) ? "-" + address.zip4 : "");
            return (((line1) ? line1 + "<br>" : "") + line2).trim();
        }

        function notNullOrEmpty(input) { return input != null && input != '' && input != 'null'; }
    }
});

/**------------------------------------------------\
 * Controllers                                     |
 *-------------------------------------------------|
 * Allows binding between the form and the         |
 * underlying model. Creates and sends requests.   |
 *------------------------------------------------*/
sage.controller('DistrictInfoController', function($scope, $http, responseService) {
    $scope.addr1 = "8450 169st";
    $scope.city = "Jamaica";
    $scope.state = "NY";
    $scope.zip5 = "11432";
    $scope.geoProvider = "default";
    $scope.provider = "default";

    $scope.changed = function() {
        console.log(this.addr1);
    }

    $scope.lookup = function() {
        $http.get(this.getDistUrl())
        .success(function(data, status, headers, config) {
            responseService.setResponse("districts", data);
        }).error(function(data, status, headers, config) {
                console.log(data);
        });

        $http.get(this.getValidateUrl())
        .success(function(data, status, headers, config) {
                responseService.setResponse("validate", data);
        }).error(function(data, status, headers, config) {
                console.log(data);
        });
    }

    $scope.getDistUrl = function () {
        var url = contextPath + baseApi + "/district/assign?addr1=" + this.addr1 + "&city=" + this.city
                              + "&state=" + this.state + "&zip5=" + this.zip5;
        url += (this.provider != "" && this.provider != "default") ? "&provider=" + this.provider : "";
        url += (this.geoProvider != "" && this.geoProvider != "default") ? "&geoProvider=" + this.geoProvider : "";
        url += "&showMembers=true&showMaps=true";
        url = url.replace(/#/g, "");
        return url;
    }

    $scope.getValidateUrl = function () {
        var url = contextPath + baseApi + "/address/validate?addr1=" + this.addr1 + "&city=" + this.city
                              + "&state=" + this.state + "&zip5=" + this.zip5;
        return url;
    }
});

/**------------------------------------------------\
 * Views                                           |
 *------------------------------------------------*/
sage.controller('ResultsViewController', function($scope, responseService) {
    $scope.paneVisible = false;

    // Expand the pane when the first result comes in
});

sage.controller('DistrictsViewController', function($scope, responseService) {
    $scope.addressValidated = false;
    $scope.hasFacebook = false;

    $scope.$on('districts', function() {
        if ($scope.addressValidated) {
            delete responseService.response.address;
        }
        $scope = angular.extend($scope, responseService.response);
    });
    $scope.$on('validate', function() {
        $scope.addressValidated = responseService.response.validated;
        if ($scope.addressValidated) {
            $scope = angular.extend($scope, responseService.response);
        }
    });

    $scope.showDistrict = function(district) {
        if ($scope.districts[district] != null && typeof $scope.districts[district] != "undefined"){
            responseService.setResponse("showDistrict", $scope.districts[district]);
        }
    }
});

sage.controller('CityStateController', function($scope, responseService) {
    $scope.$on('citystate', function() {
        $scope = angular.extend($scope, responseService.response);
    });
});

sage.controller('MapViewController', function($scope, responseService) {
    $scope.mapOptions = {
        center: new google.maps.LatLng(42.651445, -73.755254),
        zoom: 15,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        panControl: false,
        zoomControl: true,
        zoomControlOptions: {
            style: google.maps.ZoomControlStyle.LARGE,
            position: google.maps.ControlPosition.LEFT_TOP
        }
    };
    $scope.map = new google.maps.Map(document.getElementById("map_canvas"), $scope.mapOptions);
    $scope.polygons = [];
    $scope.polygon = null;
    $scope.poiMarker = null;
    $scope.polygonName = "";
    $scope.districtData = null;

    $scope.$on('districts', function() {
        var data = responseService.response;
        /** If the districts were assigned, initially set the map to display the senate boundary */
        if (data.districtAssigned) {
            $scope.setMapBoundary(data.districts.senate.map.geom);
            /** Keep a reference to the district data in local scope */
            $scope.districtData = data;
        }
        /** Update the marker location to point to the geocode */
        if (data.geocoded) {
            $scope.setMarker(data.geocode.lat, data.geocode.lon, data.address.addr1);
        }
    });

    $scope.$on('showDistrict', function() {
        var district = responseService.response;
        $scope.setMapBoundary(district.map.geom);
    });

    $scope.setMarker = function(lat, lon, markerTitle) {
        if (this.poiMarker != null) {
            this.poiMarker.setMap(null);
            this.poiMarker = null;
        }

        this.poiMarker = new google.maps.Marker({
            map: $scope.map,
            draggable:false,
            animation: google.maps.Animation.DROP,
            position: new google.maps.LatLng(lat, lon),
            title: markerTitle
        });

        this.map.setCenter(this.poiMarker.position);
    }

    $scope.setMapBoundary = function(geom, name) {
        if (geom != null) {
            for (var i in this.polygons) {
                this.polygons[i].setMap(null);
            }
            this.polygons = [];
        }

        var coords = [];
        for (var i in geom) {
            for (var j in geom[i]) {
                coords.push(new google.maps.LatLng(geom[i][j][0], geom[i][j][1]));
            }
            var polygon = new google.maps.Polygon({
                paths: coords,
                strokeColor: "teal",
                strokeOpacity: 1,
                strokeWeight: 2,
                fillColor: "teal",
                fillOpacity: 0.2
            });
            polygon.setMap(this.map);
            this.polygons.push(polygon);
            this.polygon = polygon;
            coords = [];
        }

        /** Set the zoom level to the district bounds and move a step closer */
        this.map.fitBounds(this.polygon.getBounds());
        this.map.setZoom(this.map.getZoom() + 1);

        /** Text to display on the map header */
        this.polygonName = name;
    }
});

$(document).ready(function(){
    initVerticalMenu();
});

/**
 * Google maps doesn't have a native get bounds method for polygons.
 * @return {google.maps.LatLngBounds}
 */
google.maps.Polygon.prototype.getBounds = function() {
    var bounds = new google.maps.LatLngBounds();
    var paths = this.getPaths();
    var path;
    for (var i = 0; i < paths.getLength(); i++) {
        path = paths.getAt(i);
        for (var ii = 0; ii < path.getLength(); ii++) {
            bounds.extend(path.getAt(ii));
        }
    }
    return bounds;
}