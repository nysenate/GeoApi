/**-------------------------------------------------\
 * Base Configuration                               |
 * ------------------------------------------------*/
var baseApi = "/api/v2";

var sage = angular.module('sage', []);
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
 * Controllers                                     |
 *-------------------------------------------------|
 * Allows binding between the form and the         |
 * underlying model. Creates and sends requests.   |
 *------------------------------------------------*/
sage.controller('DistrictInfoController', function($scope, $http, responseService) {
    $scope.addr1 = "100 Nyroy Dr";
    $scope.city = "Troy";
    $scope.state = "NY";
    $scope.zip5 = "12180";
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
    $scope.polygon = null;
    $scope.poiMarker = null;

    $scope.$on('districts', function() {
        if (responseService.response.districtAssigned) {
            $scope.setMapBoundary(responseService.response.districts.senate.map.geom);
        }
        if (responseService.response.geocoded) {
            $scope.setMarker(responseService.response.geocode.lat, responseService.response.geocode.lon);
        }
    });

    $scope.setMarker = function(lat, lon) {
        if (this.poiMarker != null) {
            this.poiMarker.setMap(null);
            this.poiMarker = null;
        }

        this.poiMarker = new google.maps.Marker({
            map: $scope.map,
            draggable:true,
            animation: google.maps.Animation.DROP,
            position: new google.maps.LatLng(lat, lon)
        });

        this.map.setCenter(this.poiMarker.position);
    }

    $scope.setMapBoundary = function(points) {
        var coords = [];
        for (var i in points) {
            coords.push(new google.maps.LatLng(points[i][0], points[i][1]));
        }

        if (this.polygon != null) {
            this.polygon.setMap(null);
            this.polygon = null;
        }

        this.polygon = new google.maps.Polygon({
            paths: coords,
            strokeColor: "teal",
            strokeOpacity: 1,
            strokeWeight: 2,
            fillColor: "teal",
            fillOpacity: 0.2
        });

        this.polygon.setMap(this.map);
    }
});

$(document).ready(function(){

  $("#districtsForm button").click(); // TEST HELPER

  $("p.method-header").click(function(event) {
    if (!$(this).hasClass("active")){
      $(".form-container.active").removeClass("active").slideUp(250);
      $("p.method-header.active").removeClass("active");
      $(this).addClass("active");
      $(this).next(".form-container").slideDown(250).addClass("active");
    }
  });
});

