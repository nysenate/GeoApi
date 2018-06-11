var sageAdmin = angular.module('sage-admin', ['sage-common']);
var baseAdminApi = contextPath + "/admin/api";
var baseApi = contextPath + "/api/v2/";

sageAdmin.filter("code", function(){
    return function(input) {
        return (input) ? input.replace(/\\n/g, '<br/>').replace(/\\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;') : '';
    }
});

sageAdmin.controller('DashboardController', function($scope, $http, menuService, dataBus) {
    $scope.id = 1;
    $scope.visible = true;
    $scope.now = new Date();
    $scope.lastWeek = new Date(new Date().setDate(new Date().getDate() - 7));

    $scope.from = $scope.lastWeek;
    $scope.to = $scope.now;

    $scope.fromMonth = $scope.lastWeek.getMonth() + 1;
    $scope.fromDate = $scope.lastWeek.getDate();
    $scope.fromYear = $scope.lastWeek.getFullYear();

    $scope.toMonth = $scope.now.getMonth() + 1;
    $scope.toDate = $scope.now.getDate();
    $scope.toYear = $scope.now.getFullYear();

    $scope.$on(menuService.menuToggleEvent, function(){
        $scope.visible = ($scope.id == dataBus.data);
    });

    $scope.update = function() {
        dataBus.setBroadcast("update", true);
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init = function() {
        $scope.from.setMonth($scope.fromMonth - 1);
        $scope.from.setDate($scope.fromDate);
        $scope.from.setFullYear($scope.fromYear);
        $scope.from.setHours(0);
        $scope.from.setMinutes(0);
        $scope.to.setMonth($scope.toMonth - 1);
        $scope.to.setDate($scope.toDate);
        $scope.to.setFullYear($scope.toYear);
        $scope.to.setHours(23);
        $scope.to.setMinutes(59);
    };
});

sageAdmin.controller('AdminPageController', function($scope, $http, dataBus) {
    $scope.activeTab = "exceptions";

    $scope.exceptionTab = true;

    $scope.changeTab = function(newTab) {
        $scope.activeTab = newTab;
    };

    $scope.determineActiveTab = function (inputTab) {
        // console.log(inputTab);
        return $scope.activeTab === inputTab;
    };
});

sageAdmin.controller('DeploymentStatsController', function($scope, $http, dataBus) {

    $scope.init = function() {
        this.getDeploymentStats();
    };

    $scope.getDeploymentStats = function() {
        $http.get(baseAdminApi + "/deployment")
            .success(function(data){
                $scope = angular.extend($scope, data);
            })
            .error(function(data){
                console.log("Error retrieving deployment stats! " + data);
            });
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});

sageAdmin.controller('ApiUsageController', function($scope, $http, dataBus){

    $scope.init = function() {
        this.getUsageStats((+this.from), (+this.to));
    };

    $scope.getUsageStats = function(startTime, endTime) {
        $http.get(baseAdminApi + "/usage?interval=HOUR&from=" + startTime + "&to=" + endTime)
            .success(function(data){
                $scope = angular.extend($scope, data);
                getSeriesData($scope.intervalFrom, $scope.intervalTo, $scope.intervalSizeInMinutes, $scope.intervalUsageCounts);
            })
            .error(function(data){
                console.log("Error retrieving deployment stats! " + data);
            });
    };

    var getSeriesData = function(startDate, endDate, interval, data) {
        var seriesData = [];
        var intervalMilli = interval * 60000;
        var next = startDate;
        $.each(data, function(i, v) {
            while (next < v.time && next < endDate) {
                seriesData.push(0);
                next += intervalMilli;
            }
            seriesData.push(v.count);
            next += intervalMilli;
        });
        makeApiUsageChart(startDate, seriesData);
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});

sageAdmin.controller('ExceptionViewController', function($scope, $http, dataBus){
    $scope.exceptions = [];

    $scope.init = function() {
        this.getExceptions();
    };

    $scope.getExceptions = function() {
        $http.get(baseAdminApi + "/exception")
            .success(function(data){
                $scope.exceptions = data;
            })
            .error(function(){});
    };

    $scope.hideException = function(id) {
        $http.post(baseAdminApi + "/hideException?id=" + id)
            .success(function(data){
                if (data) {
                    if (data.success) {
                        $scope.getExceptions();
                    }
                    else {
                        alert(data.message);
                    }
                }
            })
            .error(function(){});
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});

sageAdmin.controller('ApiUserStatsController', function($scope, $http, dataBus) {
    $scope.apiUserStats = [];

    $scope.init = function() {
        this.getApiUserStats();
    };

    $scope.getApiUserStats = function() {
        $http.get(baseAdminApi + "/apiUserUsage?from=" + (+$scope.from) + "&to=" + (+$scope.to))
            .success(function(data){
                $scope.apiUserStats = data;
            })
            .error(function(){});
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});

sageAdmin.controller('JobStatusController', function($scope, $http, dataBus){
    $scope.jobStatuses = [];

    $scope.init = function() {
        this.getJobStatuses();
    };

    $scope.getJobStatuses = function() {
        $http.get(baseAdminApi + "/jobStatuses?from=" + (+$scope.from) + "&to=" + (+$scope.to))
            .success(function(data){
                if (data) {
                    $scope.jobStatuses = data;
                }
            })
            .error(function(){
                console.log("Failed to retrieve job statuses!")
            });
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});

sageAdmin.controller("GeocodeUsageController", function($scope, $http, dataBus){

    $scope.init = function() {
        this.getGeocodeStats();
    };

    $scope.getGeocodeStats = function() {
        $http.get(baseAdminApi + "/geocodeUsage?from=" + (+$scope.from) + "&to=" + (+$scope.to))
            .success(function(data){
                if (data) {
                    $scope = angular.extend($scope, data);
                }
            })
            .error(function(data){
                console.log("Failed to retrieve geocode usage response!");
            });
    };

    $scope.getBarStyle = function(hits, total) {
        return {width: (hits/total) * 100 + "%"}
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});

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
            "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&provider=shapefile&uspsValidate=true";
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
                "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&provider=streetfile&uspsValidate=true";
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
            $scope.geo_google_url = baseApi + "geo/geocode?addr=" + $scope.input_addr + "&bypassCache=true&provider=google&useFallback=false&doNotCache=true";
        }
        else {
            $scope.geo_google_url = baseApi + "geo/geocode?addr1=" + $scope.input_addr1 +
                "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&bypassCache=true&provider=google&useFallback=false&doNotCache=true";
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
            $scope.geo_tiger_url = baseApi + "geo/geocode?addr=" + $scope.input_addr + "&bypassCache=true&provider=tiger&useFallback=false&doNotCache=true";
        }
        else {
            $scope.geo_tiger_url = baseApi + "geo/geocode?addr1=" + $scope.input_addr1 +
                "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&bypassCache=true&provider=tiger&useFallback=false&doNotCache=true";
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
                console.log("Failed to geocache submitted address - check Input / Google Geocodes / Server Status ");
            });

    };

    $scope.callGeocache = function() {

        if (!$scope.separatedInput) {
            $scope.geocache_url = baseApi + "geo/geocode?addr=" + $scope.input_addr + "&provider=geocache&useFallback=false";
        }
        else {
            $scope.geocache_url = baseApi + "geo/geocode?addr1=" + $scope.input_addr1 +
                "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&provider=geocache&useFallback=false";
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
            $scope.geocache_result_url = baseApi + "geo/geocode?addr=" + $scope.input_addr + "&bypassCache=true&doNotCache=false&provider=" + $scope.selected_provider;
        }
        else {
           $scope.geocache_result_url = baseApi + "geo/geocode?addr1=" + $scope.input_addr1 +
               "&city=" + $scope.input_city + "&state=NY&zip5=" + $scope.input_zip5 + "&bypassCache=true&doNotCache=false&provider=" + $scope.selected_provider;
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

sageAdmin.controller('UserConsoleController', function($scope, $http, menuService, dataBus) {
    $scope.id = 3;
    $scope.visible = false;
    $scope.currentApiUsers = null;
    $scope.currentJobUsers = null;

    $scope.$on(menuService.menuToggleEvent, function(){
        $scope.visible = ($scope.id == dataBus.data);
    });

    $scope.getCurrentApiUsers = function() {
        $http.get(baseAdminApi + "/currentApiUsers").success(function(data){
            $scope.currentApiUsers = data;
        }).error(function(data){
            console.log("Failed to retrieve list of current Api users!");
        });
    };

    $scope.getCurrentJobUsers = function() {
        $http.get(baseAdminApi + "/currentJobUsers").success(function(data){
            $scope.currentJobUsers = data;
        }).error(function(data){
            console.log("Failed to retrieve list of current Job users!")
        });
    };

    $scope.createApiUser = function() {
        if (this.apiUserName == null || this.apiUserName == '') {
            alert("A name is required!");
        }
        else {
            $http.post(baseAdminApi + "/createApiUser?name=" + this.apiUserName + "&desc=" + this.apiUserDesc)
                .success(function(data){
                    if (data) {
                        alert(data.message);
                        if (data.success) $scope.getCurrentApiUsers();
                    }
                    else {
                        alert("Failed to add Api User!")
                    }
                }).error(function(data){
                    console.log("Failed to add Api User, invalid response from Admin Api.");
                });
        }
    };

    $scope.deleteApiUser = function(id) {
        if (id != null && confirm("Are you sure you want to delete this user?")) {
            $http.post(baseAdminApi + "/deleteApiUser?id=" + id)
                .success(function(data){
                    if (data) {
                        alert(data.message);
                        if (data.success) $scope.getCurrentApiUsers();
                    }
                }).error(function(data){
                    console.log("Failed to delete Api User, invalid response from Admin Api");
                });
        }
    };

    $scope.createJobUser = function() {
        if (this.jobEmail == null || this.jobEmail == '' || this.jobPassword == null || this.jobPassword == '') {
            alert("Email and password must be specified!");
        }
        else {
            $http.post(baseAdminApi + "/createJobUser?email=" + this.jobEmail + "&password=" + this.jobPassword
                                                  + "&firstname=" + this.jobFirstName + "&lastname=" + this.jobLastName + "&admin=" + (this.jobAdmin ? "true" : "false"))
                .success(function(data){
                    if (data) {
                        alert(data.message);
                        if (data.success) {
                            $scope.getCurrentJobUsers();
                        }
                    }
                }).error(function(data){
                    console.log("Failed to create Job User, invalid response from Admin Api");
                });
        }
    };

    $scope.deleteJobUser = function(id) {
        if (id != null && confirm("Are you sure you want to delete this user?")) {
            $http.post(baseAdminApi + "/deleteJobUser?id=" + id).success(function(data){
                if (data) {
                    alert(data.message);
                    if (data.success) $scope.getCurrentJobUsers();
                }
            }).error(function(data){
                console.log("Failed to delete Job User, invalid response from Admin Api");
            });
        }
    };

    (function init() {
        $scope.getCurrentApiUsers();
        $scope.getCurrentJobUsers();
    })();

});

$(document).ready(function() {
    initVerticalMenu();
});

function makeApiUsageChart(startDate, seriesData) {
    $('#api-usage-stats').highcharts({
        chart: {
            zoomType: 'x',
            spacingRight: 20,
            height: 300
        },
        credits: {
            enabled: false
        },
        title: {
            text: null
        },
        subtitle: {
            text: null
        },
        xAxis: {
            type: 'datetime',
            maxZoom: 3600000, // 1 hour
            title: {
                text: null
            },
            tickColor: 'teal',
            tickWidth: 3
        },
        yAxis: {
            title: {
                text: 'Requests per hour'
            },
            min: 0
        },
        tooltip: {
            shared: true
        },
        legend: {
            enabled: false
        },
        plotOptions: {
            areaspline : {
                fillColor: '#CC333F',
                lineWidth: 1,
                lineColor: '#CC333F',
                marker: {
                    enabled: false
                },
                shadow: false,
                states: {
                    hover: {
                        lineWidth: 1
                    }
                },
                threshold: null
            }
        },
        series: [{
            type: 'areaspline',
            name: 'Requests',
            pointInterval: 3600 * 1000,
            pointStart: startDate - (4 * 3600000), /* EST Time zone correction ~.~ */
            data: seriesData
        }]
    });
}