var sageAdmin = angular.module('sage-admin', ['sage-common']);
var baseApi = contextPath + "/api/v2/";

sageAdmin.controller('GeocacheSubmitController', function($scope, $http, dataBus){

    $scope.init = function() {
        $scope.selected_provider = "";
        $scope.geocache_status = false;
        $scope.district_assign_status = false;
        $scope.separatedInput = false;
        $scope.geo_comparison_status = false;
        $scope.geo_provider_to_use = "";
        $scope.activeComparisonTab = "";

    };
    //These 4 vars can be shared by district assignment and geocaching
    $scope.input_addr = "";
    $scope.input_addr1 = "";
    $scope.input_city = "";
    $scope.input_zip5 = "";
    $scope.uspsValidate = true;
    $scope.separatedInput = false;
    $scope.selected_provider = "";
    $scope.geo_comparison_status = false;
    $scope.activeComparisonTab = "";
    $scope.markers = [];
    $scope.activeMarker;
    $scope.map;
    $scope.el = $("#geocache_map");

    //These vars are specific to geocaching only
    $scope.geocache_result_url = "";
    $scope.geocache_result_json = "";
    $scope.geocache_result_status = false;
    $scope.geocode_result_status = false;
    $scope.geocache_result_show_json = false;

    $scope.geocache_url = "";
    $scope.geocache_json = "";
    $scope.geocache_status = false;
    $scope.geocode_status = false;
    $scope.geocache_show_json = false;

    $scope.geo_google_url = "";
    $scope.geo_google_json = "";
    $scope.geo_google_status = false;
    $scope.geo_google_geocode_status = false;
    $scope.geo_google_show_json = false;

    $scope.geo_tiger_url = "";
    $scope.geo_tiger_json = "";
    $scope.geo_tiger_status = false;
    $scope.geo_tiger_geocode_status = false;
    $scope.geo_tiger_show_json = false;

    $scope.geo_nys_url = "";
    $scope.geo_nys_json = "";
    $scope.geo_nys_status = false;
    $scope.geo_nys_geocode_status = false;
    $scope.geo_nys_show_json = false;

    //These vars are specific to district assignment only
    $scope.district_assign_shape_url = "";
    $scope.district_assign_shape_json = "";
    $scope.district_assign_shape_status = false;
    $scope.district_assign_shape_geocode_status = false;
    $scope.district_assign_shape_district_status = false;
    $scope.district_assign_shape_show_json = false;

    $scope.district_assign_street_url = "";
    $scope.district_assign_street_json = "";
    $scope.district_assign_street_status = false;
    $scope.district_assign_street_geocode_status = false;
    $scope.district_assign_street_district_status = false;
    $scope.district_assign_street_show_json = false;

    $scope.toggleGeocacheResultJson = function() {
        $scope.geocache_result_show_json = !$scope.geocache_result_show_json;
    };

    $scope.toggleGeocacheJson = function() {
        $scope.geocache_show_json = !$scope.geocache_show_json;
    };

    $scope.toggleGoogleJson = function() {
        $scope.geo_google_show_json = !$scope.geo_google_show_json;
    };

    $scope.toggleTigerJson = function() {
        $scope.geo_tiger_show_json = !$scope.geo_tiger_show_json;
    };
    $scope.toggleNYSJson = function() {
        $scope.geo_nys_show_json = !$scope.geo_nys_show_json;
    };
    $scope.toggleStreetDistAssignJson = function() {
        $scope.district_assign_street_show_json = !$scope.district_assign_street_show_json;
    };

    $scope.toggleShapeDistAssignJson = function() {
        $scope.district_assign_shape_show_json = !$scope.district_assign_shape_show_json;
    };

    $scope.changeCompTab = function(inputTab) {
        $scope.activeComparisonTab = inputTab;
    };

    $scope.determineActiveCompTab = function(input) {
        return input === $scope.activeComparisonTab;
    };


    $scope.admin_district_assign_shape = function() {
        if (!$scope.separatedInput) {
            $scope.district_assign_shape_url = baseApi + "district/assign?addr=" + $scope.input_addr + "&provider=shapefile";
        }
        else {
            $scope.district_assign_shape_url = baseApi + "district/assign?addr1=" + $scope.input_addr1 +
                "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&provider=shapefile&uspsValidate=" + $scope.uspsValidate;
        }
        $http.get($scope.district_assign_shape_url)
            .success(function(data){
                if (data) {
                    $scope.district_assign_shape_json = data;
                    $scope.district_assign_shape_status = true;

                    if ($scope.district_assign_shape_json.geocode != null) {
                        $scope.district_assign_shape_geocode_status = true;
                    }
                    if ($scope.district_assign_shape_json.districts != null) {
                        $scope.district_assign_shape_district_status = true;
                    }
                }
            })
            .error(function(data){
                console.log("Failed to district assign submitted address - check Input / Google Geocodes / Server Status ");
            });
    };

    $scope.admin_district_assign_street = function() {
        if (!$scope.separatedInput) {
            $scope.district_assign_street_url = baseApi + "district/assign?addr=" + $scope.input_addr + "&provider=streetfile";
        }
        else {
            $scope.district_assign_street_url = baseApi + "district/assign?addr1=" + $scope.input_addr1 +
                "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&provider=streetfile&uspsValidate=" + $scope.uspsValidate;
        }
        $http.get($scope.district_assign_street_url)
            .success(function(data){
                if (data) {
                    $scope.district_assign_street_json = data;
                    $scope.district_assign_street_status = true;

                    if ($scope.district_assign_street_json.geocode != null) {
                        $scope.district_assign_street_geocode_status = true;
                    }
                    if ($scope.district_assign_street_json.districts != null) {
                        $scope.district_assign_street_district_status = true;
                    }
                }
            })
            .error(function(data){
                console.log("Failed to district assign submitted address - check Input / Google Geocodes / Server Status ");
            });
    };

    $scope.callGoogle = function() {

        if (!$scope.separatedInput) {
            $scope.geo_google_url = baseApi + "geo/geocode?addr=" + $scope.input_addr + "&bypassCache=true&provider=google&useFallback=false&doNotCache=true&uspsValidate=" + $scope.uspsValidate;
        }
        else {
            $scope.geo_google_url = baseApi + "geo/geocode?addr1=" + $scope.input_addr1 +
                "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&bypassCache=true&provider=google&useFallback=false&doNotCache=true&uspsValidate=" + $scope.uspsValidate;
        }
        $http.get($scope.geo_google_url)
            .success(function(data){
                if (data) {
                    $scope.geo_google_json = data;

                    console.log($scope.geo_google_json.geocode);

                    if (($scope.geo_google_json.geocode !== null) && ($scope.geo_google_json.status !== "NO_GEOCODE_RESULT")){
                        $scope.geo_google_status = true;
                        $scope.geo_google_geocode_status = true;
                        $scope.setMarker($scope.geo_google_json.geocode.lat,$scope.geo_google_json.geocode.lon,"Google",false,true);
                    }
                }
            })
            .error(function(data){
                console.log("Failed to geocache submitted address - check Input / Google Geocodes / Server Status ");
            });

    };

    $scope.callTiger = function() {

        if (!$scope.separatedInput) {
            $scope.geo_tiger_url = baseApi + "geo/geocode?addr=" + $scope.input_addr + "&bypassCache=true&provider=tiger&useFallback=false&doNotCache=true&uspsValidate=" + $scope.uspsValidate;
        }
        else {
            $scope.geo_tiger_url = baseApi + "geo/geocode?addr1=" + $scope.input_addr1 +
                "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&bypassCache=true&provider=tiger&useFallback=false&doNotCache=true&uspsValidate=" + $scope.uspsValidate;
        }
        $http.get($scope.geo_tiger_url)
            .success(function(data){
                if (data) {
                    $scope.geo_tiger_json = data;

                    console.log($scope.geo_tiger_json.geocode);
                    console.log($scope.geo_tiger_json.status);
                    //NO_GEOCODE_RESULT

                    if (($scope.geo_tiger_json.geocode !== null) && ($scope.geo_tiger_json.status !== "NO_GEOCODE_RESULT")) {
                        $scope.geo_tiger_status = true;
                        $scope.geo_tiger_geocode_status = true;
                        $scope.setMarker($scope.geo_tiger_json.geocode.lat,$scope.geo_tiger_json.geocode.lon,"Tiger",false,true);
                    }
                }
            })
            .error(function(data){
                console.log("Failed to geocache submitted address - check Input / Tiger DB / Server Status ");
            });

    };

    $scope.callNYS = function() {

        if (!$scope.separatedInput) {
            $scope.geo_nys_url = baseApi + "geo/geocode?addr=" + $scope.input_addr + "&bypassCache=true&provider=nysgeo&useFallback=false&doNotCache=true&uspsValidate=" + $scope.uspsValidate;
        }
        else {
            $scope.geo_nys_url = baseApi + "geo/geocode?addr1=" + $scope.input_addr1 +
                "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&bypassCache=true&provider=nysgeo&useFallback=false&doNotCache=true&uspsValidate=" + $scope.uspsValidate;
        }
        $http.get($scope.geo_nys_url)
            .success(function(data){
                if (data) {
                    $scope.geo_nys_json = data;

                    console.log($scope.geo_nys_json.geocode);
                    console.log($scope.geo_nys_json.status);
                    //NO_GEOCODE_RESULT

                    if (($scope.geo_nys_json.geocode !== null) && ($scope.geo_nys_json.status !== "NO_GEOCODE_RESULT")) {
                        $scope.geo_nys_status = true;
                        $scope.geo_nys_geocode_status = true;
                        $scope.setMarker($scope.geo_nys_json.geocode.lat,$scope.geo_nys_json.geocode.lon,"NYSGeo",false,true);
                    }
                }
            })
            .error(function(data){
                console.log("Failed to geocache submitted address - check Input / NYS Geocoder / Server Status ");
            });

    };

    $scope.callGeocache = function() {

        if (!$scope.separatedInput) {
            $scope.geocache_url = baseApi + "geo/geocode?addr=" + $scope.input_addr + "&provider=geocache&useFallback=false&uspsValidate=" + $scope.uspsValidate;
        }
        else {
            $scope.geocache_url = baseApi + "geo/geocode?addr1=" + $scope.input_addr1 +
                "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&provider=geocache&useFallback=false&uspsValidate=" + $scope.uspsValidate;
        }
        $http.get($scope.geocache_url)
            .success(function(data){
                if (data) {
                    $scope.geocache_json = data;

                    console.log($scope.geocache_json.geocode);

                    if (($scope.geocache_json.geocode !== null) && ($scope.geocache_json.status !== "NO_GEOCODE_RESULT")){
                        $scope.geocache_status = true;
                        $scope.geocode_status = true;
                        $scope.setMarker($scope.geocache_json.geocode.lat,$scope.geocache_json.geocode.lon,"Geocache",false,true);
                    }
                    console.log($scope.geocache_status);
                }
            })
            .error(function(data){
                console.log("Failed to geocache submitted address - check Input / Google Geocodes / Server Status ");
            });

    };

    $scope.updateGeocache = function() {

        if (!$scope.separatedInput) {
            $scope.geocache_result_url = baseApi + "geo/geocode?addr=" + $scope.input_addr + "&bypassCache=true&doNotCache=false&provider=" + $scope.selected_provider + "&uspsValidate=" + $scope.uspsValidate;
        }
        else {
            $scope.geocache_result_url = baseApi + "geo/geocode?addr1=" + $scope.input_addr1 +
                "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&bypassCache=true&doNotCache=false&provider=" + $scope.selected_provider + "&uspsValidate=" + $scope.uspsValidate;
        }
        $http.get($scope.geocache_result_url)
            .success(function(data){
                if (data) {
                    $scope.geocache_result_json = data;
                    $scope.geocache_result_status = true;

                    console.log($scope.geocache_result_json.geocode);

                    if (($scope.geocache_result_json.geocode != null) && ($scope.geocache_json.status !== "NO_GEOCODE_RESULT")){
                        $scope.geocode_result_status = true;
                    }
                }
            })
            .error(function(data){
                console.log("Failed to geocache submitted address - check Input / Google Geocodes / Server Status ");
            });
    };

    $scope.look_up = function() {
        $scope.admin_district_assign_shape();
        $scope.admin_district_assign_street();
        $scope.callGeocache();
        $scope.callGoogle();
        $scope.callNYS();
        $scope.callTiger();
        $scope.geo_comparison_status = true;
        $scope.activeComparisonTab = "geocache"
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.isValidInfo = function() {
        if (!$scope.separatedInput) {
            return $scope.input_addr !== "";
        }
        else {
            return $scope.input_addr1 !== "" && $scope.input_city !== "" && $scope.input_zip5.length === 5;
        }

    };

    $scope.isProviderSelected = function() {
        return $scope.selected_provider !== "";
    };

    $scope.toggleInputSeparation = function() {
        $scope.separatedInput = !$scope.separatedInput;
        console.log($scope.separatedInput);
        $scope.resetOnChange();
    };

    $scope.resetOnChange = function() {
        $scope.district_assign_status = false;
        $scope.geocache_status = false;
        $scope.geo_tiger_status = false;
        $scope.geo_nys_status = false;
        $scope.geo_google_status = false;
        $scope.geo_comparison_status = false;
        $scope.geocache_result_status = false;
        $scope.selected_provider = "";
        $scope.district_assign_street_status = false;
        $scope.district_assign_shape_district_status = false;
        $scope.activeComparisonTab = "";
        $scope.handleMapReset();
    };

    $scope.init();

    $scope.resizeMap = function() {
        google.maps.event.trigger(document.getElementById('geocache_map'), 'resize');
    };


    $scope.displayMap = function() {
        var mapOptions = {
            center: new google.maps.LatLng(42.440510, -76.495460), // Centers the map nicely over NY
            zoom: 7,
            mapTypeControl: false,
            mapTypeId: google.maps.MapTypeId.ROADMAP,
            streetViewControl: false
        };
        $scope.map = new google.maps.Map(document.getElementById('geocache_map'), mapOptions);

    };

    $scope.setMarker = function(lat, lon, title, clear, center) {
        if (clear) {
            $scope.clearMarkers();
        }
        // console.log(title);

        var marker = new google.maps.Marker({
            map: $scope.map,
            position: new google.maps.LatLng(lat, lon),
            draggable:false,
            label: title
        });
        $scope.map.setZoom(15);

        $scope.activeMarker = marker;

        if (center) {
            $scope.map.setCenter($scope.activeMarker.position);
        }

        $scope.compareMarkerLocation(marker);
        $scope.markers.push(marker);
    };

    $scope.compareMarkerLocation = function(marker) {
        // console.log(marker.getPosition());
        // console.log(marker.getPosition().lat());
        // console.log(marker.getPosition().lng());

        if ($scope.markers.length > 1) {
            var i;
            for (i = 0; i < $scope.markers.length; i++) {
                if ( ( $scope.markers[i].getPosition().lat() ===  marker.getPosition().lat() ) &&
                    ( $scope.markers[i].getPosition().lng() === marker.getPosition().lng() )
                    && ( $scope.markers[i].label !== marker.label )
                ) {
                    var combinedLabel =  $scope.markers[i].label + " & " + marker.label;
                    $scope.markers[i].label = combinedLabel;
                    console.log($scope.markers[i].label);
                    marker.label = combinedLabel;
                    console.log(marker.label);

                    $scope.markers[i].setMap(null);
                    $scope.markers[i].setMap($scope.map);
                }
            }
        }
    };

    $scope.clearMarkers = function() {
        $.each($scope.markers, function(i, v){
            v.setMap(null);
        });
        $scope.markers = [];
    };

    $scope.setCenter = function(lat, lon) {
        $scope.map.setCenter(new google.maps.LatLng(lat, lon));
    };

    $scope.handleMapReset = function() {
        $scope.clearMarkers();
        $scope.setCenter(42.440510, -76.495460);
        $scope.map.setZoom(7);
    };

    window.onload = function() {
        $scope.displayMap();
        $scope.resizeMap();
    };
});