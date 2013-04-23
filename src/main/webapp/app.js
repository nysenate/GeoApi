var sage = angular.module('sage', []);

/**-------------------------------------------------\
 * Base Configuration                               |
 * ------------------------------------------------*/
var baseApi = "/api/v2";
var map;

/**-------------------------------------------------\
 * Response Service                                 |
 *-------------------------------------------------*/
sage.factory("responseService", function($rootScope) {
    var responseService = {};
    responseService.setResponse = function(handle, response, view) {
        this.handle = handle;
        this.response = response;
        this.view = view;
        this.broadcastToViews();
        this.broadcastItem();
    }

    responseService.broadcastItem = function() {
        $rootScope.$broadcast(this.handle);
    }

    responseService.broadcastToViews = function() {
        if (this.view != null && typeof this.view != "undefined") {
            $rootScope.$broadcast('view');
        }
    }
    return responseService;
});

/**--------------------------------------------
 * Result View Service
 ---------------------------------------------*/


/**--------------------------------------------
 * Map Service
 ---------------------------------------------*/
sage.factory("mapService", function($rootScope, uiBlocker) {
    
    /** Initialization */
    var mapService = {};
    mapService.el = $("#mapView");
    mapService.mapOptions = {
        center: new google.maps.LatLng(42.651445, -73.755254),
        zoom: 15,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        panControl: false,
        zoomControl: true,
        zoomControlOptions: {
            style: google.maps.ZoomControlStyle.LARGE,
            position: google.maps.ControlPosition.LEFT_TOP
        },
        styles: [
            {featureType: "transit",
            stylers: [{ "visibility": "off"}]}
        ]
    };
    mapService.map = new google.maps.Map(document.getElementById("map_canvas"), mapService.mapOptions);
    mapService.polygons = [];
    mapService.polygon = null;
    mapService.markers = [];
    mapService.activeMarker = null;
    mapService.header = "Map";
    mapService.districtData = null;

    /**
     * Triggers a map resize
     */
    mapService.resizeMap = function() {
        google.maps.event.trigger(mapService.map, 'resize');
    }

    /**
     * Toggle the visibility of the map
     * @param show - if true, then show and resize map
     */
    mapService.toggleMap = function(show) {
        if (show) {
            $(this.el).fadeIn('fast', function(){
                mapService.resizeMap();
            });
        } else {
            $(this.el).hide();
        }
    }

    /**
     * Place a marker on the map.
     * lat - float
     * lon - float
     * title - tooltip text to display on hover
     * clear - remove all other markers
     * clickContent - display content upon marker click
     */
    mapService.setMarker = function(lat, lon, title, clear, clickContent) {
        if (clear) {
            this.clearMarkers();
        }

        var marker = new google.maps.Marker({
            map: mapService.map,
            draggable:false,
            animation: google.maps.Animation.DROP,
            position: new google.maps.LatLng(lat, lon),
            title: title
        });

        if (clickContent) {
            var infowindow = new google.maps.InfoWindow({
                content: clickContent
            });
            google.maps.event.addListener(marker, 'click', function() {
                infowindow.open(mapService.map, marker);
            });
        }

        this.activeMarker = marker;
        this.markers.push(marker);
        this.map.setCenter(this.activeMarker.position);
    }

    /**
     * Sets a polygon overlay on the map with hover and click functionality
     * @param geom          Nested array of point arrays, e.g [[43.1,-73],[43.2,-73],[43.2,-73]]
     * @param name          The name of the polygon to display on the info header bar
     * @param fitBounds     If true then map will resize to fit polygon's bounds
     * @param clear         If true then map will be cleared of all overlays
     * @param clickHandler  If a callback is supplied it will be called when the polygon is clicked
     * @param color         Color of the polygon (default is teal)
     */
    mapService.setOverlay = function(geom, name, fitBounds, clear, clickHandler, color) {
        if (geom != null) {
            if (clear == true) {
                this.clearPolygons();
            }
            var coords = [];
            for (var i in geom) {
                for (var j in geom[i]) {
                    coords.push(new google.maps.LatLng(geom[i][j][0], geom[i][j][1]));
                }
                var polygon = new google.maps.Polygon({
                    paths: coords,
                    strokeColor: (color) ? color : "teal",
                    strokeOpacity: 1,
                    strokeWeight: 2,
                    fillColor: (color) ? color : "teal",
                    fillOpacity: 0.1
                });

                /** On mouseover update the header title */
                google.maps.event.addListener(polygon,"mouseover",function() {
                    this.setOptions({fillOpacity: 0.4});
                    var scope = angular.element(mapService.el).scope();
                    scope.$apply(function() {
                        scope.header = name;
                    });
                });

                /** On mouseout restore the opacity */
                google.maps.event.addListener(polygon,"mouseout",function(){
                    this.setOptions({fillOpacity: 0.2});
                });

                /** Set up event handling for mouse click on polygon */
                if(clickHandler) {
                    google.maps.event.addListener(polygon,"click", function() {
                        if (mapService.polygon) {
                            mapService.polygon.setOptions({fillColor: "teal"});
                            mapService.polygon.setOptions({fillOpacity: 0.2});
                        }
                        this.setOptions({fillColor: "orange"});
                        this.setOptions({fillOpacity: 0.5});
                        mapService.polygon = this;
                        clickHandler();
                    });
                }

                polygon.setMap(this.map);
                this.polygons.push(polygon);
                this.polygon = polygon;
                coords = [];
            }

            /** Set the zoom level to the district bounds and move a step closer */
            if (fitBounds) {
                this.map.fitBounds(this.polygon.getBounds());
                this.map.setZoom(this.map.getZoom() + 1);
            }

            /** Text to display on the map header */
            this.header = name;
            return this.polygon;
        }
        else {
            this.clearPolygons();
        }
        return null;
    }

    mapService.clearPolygon = function(polygon) {
        if (polygon) {
            try { polygon.setMap(null);}
            catch (ex) {}
        }
    }

    /**
     * Removes all polygon overlays
     */
    mapService.clearPolygons = function() {
        $.each(this.polygons, function (i, v){
            v.setMap(null);
        });
        this.polygons = [];
    }

    /**
     * Removes all markers
     */
    mapService.clearMarkers = function() {
        $.each(this.markers, function(i, v){
            v.setMap(null);
        });
        this.markers = [];
    }

    /* map.setCenter(results[0].geometry.location);
     var marker = new google.maps.Marker({
     map: map,
     position: results[0].geometry.location
     }); */
    /**--------------------------------------------
     * Client Geocoder
     ---------------------------------------------*/
    mapService.geocode = function(address) {
        googleGeocoder.geocode( { 'address': address}, function(results, status) {
            if (status == google.maps.GeocoderStatus.OK) {
                return results[0].geometry.location;
            }
            return null;
        });
    }

    mapService.formatDistrictName = function(dist) {
        console.log(dist);
        return ((dist.name) ? dist.name + " " : capitalize(dist.type) + " District ")  + dist.district +
            ((dist.member) ? " - " + dist.member.name : "") +
            ((dist.senator) ? " - " + dist.senator.name : "");
    }

    return mapService;
});

sage.factory("uiBlocker", function($rootScope) {
    var uiBlocker = {};
    uiBlocker.block = function(msg) {
        $.blockUI({css:{border:'1px solid #ddd'}, overlayCSS: { backgroundColor: '#eee', opacity:0.5 }, message: msg});
    }
    uiBlocker.unBlock = function() {
        $.unblockUI();
    }
    return uiBlocker;
});

/**------------------------------------------------\
 * Filters                                         |
 *------------------------------------------------*/
/** Removes a sequence from an input string */
sage.filter("remove", function() {
    return function(input, string) {
        if (input !== null && typeof input !== 'undefined') {
            return input.replace(string, "");
        }
    }
});

sage.filter("capitalize", function() {
    return function(input) {
        if (input !== null && typeof input !== 'undefined') {
            return capitalize(input);
        }
    }
});

sage.filter("districtName", function() {
    return function(input) {
        return formatDistrictName(input, type);
    }
});

sage.filter("senatorPic", function() {
    return function(input) {
        if (input) {
            return "http://www.nysenate.gov/files/imagecache/senator_teaser/" + input.substring(30) ;
        }
    }
});

/** Formats an address properly */
sage.filter('addressFormat', function(){
    return function(address) {
        if (address != null && typeof address !== 'undefined') {
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

function capitalize(input) {
    if (input !== null && typeof input !== 'undefined') {
        return input.substring(0,1).toUpperCase() + input.substring(1).toLowerCase();
    }
}

/**
 * Return the district name header as |
 * @param district
 * @param type
 * @returns {string}
 */
function formatDistrictName(district, type) {
    return ((district.name) ? district.name + " " : ((type) ? type : capitalize(district.type)) + " District ")  +
            ((district.type != "SENATE" && type != "Senate") ? district.district : "") +
            ((district.member) ? " - " + district.member.name : "") +
            ((district.senator) ? " - " + district.senator.name : "");
}

/**------------------------------------------------\
 * Directives                                      |
 *-------------------------------------------------|*/
sage.directive('myTable', function() {
    return function(scope, element, attrs) {

        // apply DataTable options, use defaults if none specified by user
        var options = {};
        if (attrs.myTable.length > 0) {
            options = scope.$eval(attrs.myTable);
        } else {
            options = {
                "bStateSave": true,
                "iCookieDuration": 2419200, /* 1 month */
                "bJQueryUI": true,
                "bPaginate": false,
                "bLengthChange": false,
                "bFilter": false,
                "bInfo": false,
                "bDestroy": true
            };
        }

        // Tell the dataTables plugin what columns to use
        // We can either derive them from the dom, or use setup from the controller
        var explicitColumns = [];
        element.find('th').each(function(index, elem) {
            explicitColumns.push($(elem).text());
        });
        if (explicitColumns.length > 0) {
            options["aoColumns"] = explicitColumns;
        } else if (attrs.aoColumns) {
            options["aoColumns"] = scope.$eval(attrs.aoColumns);
        }

        // aoColumnDefs is dataTables way of providing fine control over column config
        if (attrs.aoColumnDefs) {
            options["aoColumnDefs"] = scope.$eval(attrs.aoColumnDefs);
        }

        // aaSorting defines which column to sort on by default
        if (attrs.aaSorting) {
            options["aaSorting"] = scope.$eval(attrs.aaSorting);
        }

        if (attrs.fnRowCallback) {
            options["fnRowCallback"] = scope.$eval(attrs.fnRowCallback);
        }

        // apply the plugin
        var dataTable = element.dataTable(options);


        $("#street-search").keyup( function () {
            /* Filter on the street column */
            dataTable.fnFilter( this.value, 2 );
        });

        // watch for any changes to our data, rebuild the DataTable
        scope.$watch(attrs.aaData, function(value) {
            var val = value || null;
            if (val) {
                dataTable.fnClearTable();
                dataTable.fnAddData(scope.$eval(attrs.aaData));
            }
        });
    };
});

/**------------------------------------------------\
 * Controllers                                     |
 *-------------------------------------------------|
 * Allows binding between the form and the         |
 * underlying model. Creates and sends requests.   |
 *------------------------------------------------*/
sage.controller('DistrictInfoController', function($scope, $http, responseService, uiBlocker) {
    $scope.addr1 = "8450 169st";
    $scope.city = "Jamaica";
    $scope.state = "NY";
    $scope.zip5 = "11432";
    $scope.geoProvider = "default";
    $scope.provider = "default";

    $scope.lookup = function() {
        uiBlocker.block("Looking up districts");
        $http.get(this.getDistUrl())
        .success(function(data) {
            responseService.setResponse("districtInfo", data, "districtView");
        }).error(function(data, status, headers, config) {
            uiBlocker.unBlock();
            alert("Failed to lookup districts. The application did not return a response.");
        });
    }

    $scope.getDistUrl = function () {
        var url = contextPath + baseApi + "/district/assign?addr1=" + this.addr1 + "&city=" + this.city
                              + "&state=" + ((this.state) ? this.state : "NY") + "&zip5=" + this.zip5;
        url += (this.provider != "" && this.provider != "default") ? "&provider=" + this.provider : "";
        url += (this.geoProvider != "" && this.geoProvider != "default") ? "&geoProvider=" + this.geoProvider : "";
        url += "&showMembers=true&showMaps=true";
        url = url.replace(/#/g, ""); // Pound marks mess up the query string
        return url;
    }
});

sage.controller("DistrictMapController", function($scope, $http, responseService, uiBlocker){
    $scope.type = "senate";
    $scope.district = "";

    $scope.lookup = function () {
        uiBlocker.block("Loading " + this.type + " maps..");
        $http.get(this.getDistrictMapUrl())
            .success(function(data) {
                responseService.setResponse("districtMap", data);
            }).error(function(data) {
                uiBlocker.unBlock();
                alert("Failed to retrieve district maps.");
            });
    }

    $scope.getDistrictMapUrl = function () {
        return contextPath + baseApi + "/map/" + this.type + "?showMembers=true" + ((this.district) ? ("&district=" + this.district) : "");
    }

});

sage.controller('CityStateController', function($scope, $http, responseService) {
    $scope.zip5 = "";

    $scope.lookup = function() {
        $http.get(this.getCityStateUrl())
             .success(function(data, status, headers, config) {
                responseService.setResponse("citystate", data, "citystate");
            });
    }

    $scope.getCityStateUrl = function() {
        return contextPath + baseApi + "/address/citystate?provider=mapquest&zip5=" + this.zip5;
    }
});

sage.controller("StreetLookupController", function($scope, $http, responseService){
    $scope.zip5 = "";

    $scope.lookup = function () {
        $http.get(this.getStreetLookupUrl())
            .success(function(data){
                responseService.setResponse("street", data, "street");
            });
    }

    $scope.getStreetLookupUrl = function () {
        return contextPath + baseApi + "/street/lookup?zip5=" + this.zip5;
    }
});

sage.controller("RevGeoController", function($scope, $http, responseService) {
    $scope.lat = "";
    $scope.lon = "";

    $scope.lookup = function() {
        $http.get(this.getRevGeoUrl())
            .success(function(data, status, headers, config) {
                responseService.setResponse("revgeo", data, "revgeo");
            });
    }

    $scope.getRevGeoUrl = function() {
        return contextPath + baseApi + "/geo/revgeocode?lat=" + this.lat + "&lon=" + this.lon;
    }
});
/**------------------------------------------------\
 * Views                                           |
 *------------------------------------------------*/
sage.controller('ResultsViewController', function($scope, responseService, mapService) {
    $scope.paneVisible = false;
    $scope.centercolumn = $('#contentcolumn');
    $scope.rightcolumn = $("#rightcolumn");
    $scope.width = '360px';

    $scope.$on('expandResults', function() {
        $scope.paneVisible = $scope.toggleResultPane(responseService.response);
    });

    $scope.toggleResultPane = function(expand) {
        if (expand != null) {
            if (expand) {
                $scope.centercolumn.css("marginRight", $scope.width); $scope.rightcolumn.show();
                mapService.resizeMap();
                return true;
            } else {
                $scope.centercolumn.css("marginRight", 0); $scope.rightcolumn.hide();
                mapService.resizeMap();
            }
        }
        return false;
    }
});

sage.controller('DistrictsViewController', function($scope, $http, responseService, mapService, uiBlocker) {
    $scope.visible = false;
    $scope.viewId = "districtView";
    $scope.showOffices = false;
    $scope.showNeighbors = false;
    $scope.neighborPolygon = null;
    $scope.neighborSenator = null;

    $scope.$on("view", function(){
        $scope.visible = ($scope.viewId == responseService.view);
    });

    /** Handle results of district info query */
    $scope.$on("districtInfo", function() {
        $scope = angular.extend($scope, responseService.response);
        responseService.setResponse("expandResults", true);
        mapService.toggleMap(true);

        /** If the districts were assigned, initially set the map to display the senate boundary */
        if ($scope.districtAssigned) {
            mapService.setOverlay($scope.districts.senate.map.geom,
                                  formatDistrictName($scope.districts.senate, "Senate"), true, true, null);
        }
        /** Update the marker location to point to the geocode */
        if ($scope.geocoded) {
            mapService.setMarker($scope.geocode.lat, $scope.geocode.lon,
                                 ($scope.address.addr1 != null) ? $scope.address.addr1 : "", true, null);
        }
        uiBlocker.unBlock();
        mapService.resizeMap();
    });

    /** Handle results of district maps query */
    $scope.$on("districtMap", function() {
        var data = responseService.response;
        if (data.statusCode == 0) {
            mapService.clearMarkers();
            /** Show all the district map boundaries */
            if (data != null && data.districts != null) {
                mapService.clearPolygons();
                $.each(data.districts, function(i, v){
                    if (v.map != null) {
                        mapService.setOverlay(v.map.geom, formatDistrictName(v), false, false,
                            (v.type == "SENATE") ? function() {responseService.setResponse("member", v.member, "member");}
                                : null
                        );
                    }
                });
            }
            /** Show the individual district map */
            else if (data.map != null) {
                mapService.setOverlay(data.map.geom, formatDistrictName(data), true, true,
                    (data.type == "SENATE") ? function() {responseService.setResponse("member", data.member, "member");}
                    : null
                );
            }
        }
        mapService.toggleMap(true);
        uiBlocker.unBlock();
    });

    /** Show the specified district map */
    $scope.showDistrict = function(districtType) {
        if ($scope.districts[districtType] != null && typeof $scope.districts[districtType] != "undefined") {
            var district = $scope.districts[districtType];
            district.type = districtType; // Set the type for the formatDistrictName method
            mapService.resizeMap();
            mapService.setOverlay(district.map.geom, formatDistrictName(district), true, true, null);
        }
    }

    $scope.showNeighborDistrict = function(neighbor) {
        this.showNeighbors = true;
        this.neighborPolygon = mapService.setOverlay(neighbor.map.geom, formatDistrictName(neighbor, "Senate"), false, false, null, "#FF4500");
    }

    $scope.hideNeighborDistrict = function()  {
        this.showNeighbors = false;
        mapService.clearPolygon(this.neighborPolygon);
    }

    $scope.setOfficeMarker = function(office) {
        if (office != null) {
            mapService.setMarker(office.latitude, office.longitude, office.name, false, null);
        }
    }

    $scope.getDistrictMapUrl = function () {
        return
    }
});

sage.controller("CityStateView", function($scope, responseService, mapService) {
    $scope.visible = false;
    $scope.viewId = "citystate";

    $scope.$on("view", function(){
        $scope.visible = ($scope.viewId == responseService.view);
    });

    $scope.$on("citystate", function() {
        $scope = angular.extend($scope, responseService.response);
        responseService.setResponse("expandResults", true);
    });
});

sage.controller("StreetViewController", function($scope, responseService, mapService) {
   $scope.visible = false;
   $scope.viewId = "street";
   $scope.streets = [];

   $scope.columnDefs = [
        { "mDataProp": "bldgLoNum", "aTargets":[0]},
        { "mDataProp": "bldgHiNum", "aTargets":[1]},
        { "mDataProp": "street", "aTargets":[2]},
        { "mDataProp": "location", "aTargets":[3]},
        { "mDataProp": "zip5", "aTargets":[4]},
        { "mDataProp": "senate", "aTargets":[5]},
        { "mDataProp": "congressional", "aTargets":[6]},
        { "mDataProp": "assembly", "aTargets":[7]},
        { "mDataProp": "county", "aTargets":[8]},
        { "mDataProp": "town", "aTargets":[9]},
        { "mDataProp": "election", "aTargets":[10]}
    ];

    $scope.overrideOptions = {
        "bStateSave": false,
        "iCookieDuration": 2419200, /* 1 month */
        "bJQueryUI": false,
        "bPaginate": true,
        "bLengthChange": false,
        "bFilter": true,
        "bInfo": true,
        "bDestroy": true,
        "iDisplayLength": 20
    };

    // Sort by street ( column index 2 )
    $scope.sortDefault = [[2, "asc"]];

    $scope.$on("street", function(){
       $scope.streets = (responseService.response.streets);
       responseService.setResponse("expandResults", false);
       mapService.toggleMap(false);
   });

   $scope.$on("view", function(){
        $scope.visible = ($scope.viewId == responseService.view);
   });
});

sage.controller("MemberViewController", function($scope, responseService, mapService) {
    $scope.visible = false;
    $scope.viewId = "member"
    $scope.member;

    $scope.$on("view", function(){
        $scope.visible = ($scope.viewId == responseService.view);
    });

    $scope.$on("member", function() {
        $scope.member = responseService.response;
        responseService.setResponse("expandResults", true);     
    });

    $scope.setOfficeMarker = function(office) {
        if (office != null) {
            mapService.setMarker(office.latitude, office.longitude, office.name, false, null);
        }
    }
});

sage.controller("RevGeoViewController", function($scope, responseService, mapService) {
    $scope.visible = false;
    $scope.viewId = "revgeo";
    $scope.revGeocoded = false;

    $scope.$on("revgeo", function(){
        $scope = angular.extend($scope, responseService.response);
        $scope.revGeocoded = ($scope.statusCode == 0);
        if ($scope.revGeocoded) {
            mapService.setMarker($scope.geocode.lat, $scope.geocode.lon);
        }
        responseService.setResponse("expandResults", true);
        mapService.toggleMap(true);
    });

    $scope.$on("view", function(){
        $scope.visible = ($scope.viewId == responseService.view);
    });
});

$(document).ready(function(){
    initVerticalMenu();

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
});