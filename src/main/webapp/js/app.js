var sage = angular.module('sage', ['sage-common']);

/**-------------------------------------------------\
 * Base Configuration                               |
 * ------------------------------------------------*/
var baseApi = "/api/v2";
var map;

/**--------------------------------------------
 * Map Service
 ---------------------------------------------*/
sage.factory("mapService", function($rootScope, uiBlocker, dataBus) {

    google.maps.visualRefresh = true;

    /** Load up the NY State bounds */
    var nyBounds = [[40.488737, -74.264832],[40.955011, -71.762695],[41.294317, -71.932983],[40.955011, -73.641357],[41.100052, -73.721008],[41.215854, -73.487549],[41.298444, -73.550720],[42.085994, -73.504028],[42.747012, -73.267822],[43.612217, -73.289795],[45.003651, -73.300781],[45.011419, -74.959717], [43.612217, -77.189941],[43.269206, -79.112549],[42.843751, -78.936768],[42.536892, -79.782715],[42.000325, -79.749756],[41.983994, -75.366211],[41.327326, -74.783936],[40.996484, -73.907776],[40.653555, -74.058838],[40.640009, -74.200287]];
    var nyLatLngBounds = new google.maps.LatLngBounds();
    $.each(nyBounds, function(i,v){
        nyLatLngBounds.extend(new google.maps.LatLng(v[0], v[1]));
    });

    /** Styles */
    var styles = [{
        featureType: "transit",
        stylers: [{ "visibility": "off"}, ]
    }];

    if (window.customMapStyle) {
        styles.push(window.customMapStyle);
    }

    //                  teal      orangered    green      red        yellow     cyan       pink      purple     darkblue
    var polyColors =  ["#008080", "#ff4500", "#639A00", "#CC333F", "#EDC951", "#09AA91", "#F56991", "#524656", "#547980"];

    if (window.customPolyStyle) {
        polyColors.unshift(window.customPolyStyle.hue);
    }

    /** Initialization */
    var mapService = {};
    mapService.el = $("#mapView");
    mapService.tooltipEl = $("#mapTooltip");
    mapService.mapOptions = {
        center: new google.maps.LatLng(42.440510, -76.495460), // Centers the map nicely over NY
        zoom: 7,
        mapTypeControl: false,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        panControl: false,
        zoomControl: true,
        streetViewControl: false,
        zoomControlOptions: {
            style: google.maps.ZoomControlStyle.DEFAULT,
            position: google.maps.ControlPosition.LEFT_TOP
        },
        styles: styles
    };
    mapService.map = new google.maps.Map(document.getElementById("map_canvas"), mapService.mapOptions);
    mapService.autoComplete = new google.maps.places.AutocompleteService();
    mapService.bounds = null;
    mapService.polygons = [];
    mapService.lines = [];
    mapService.polygon = null;
    mapService.markers = [];
    mapService.activeMarker = null;
    mapService.districtData = null;
    mapService.mouseEventName = null;
    mapService.colors = polyColors;

    /**
     * Resize when window size changes
     */
    google.maps.event.addDomListener(window, 'resize', function() {
        mapService.resizeMap();
    });

    /**
     * Triggers a map resize
     */
    mapService.resizeMap = function() {
        google.maps.event.trigger(mapService.map, 'resize');
    };

    google.maps.event.addListener(mapService.map, 'bounds_changed', function() {
        mapService.bounds =  mapService.map.getBounds();
    });

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
    };

    /** Set the zoom level of the map */
    mapService.setZoom = function(level) {
        this.map.setZoom(level);
    };

    mapService.setCenter = function(lat, lon) {
        this.map.setCenter(new google.maps.LatLng(lat, lon));
    };

    mapService.makeAutocomplete = function(inputId) {
        new google.maps.places.Autocomplete(
            document.getElementById(inputId), {
                bounds: nyLatLngBounds,
                types: ['geocode'],
                componentRestrictions: {country:'us'}
            });
    };

    mapService.getPlacePredictions = function(input, handle) {
        this.autoComplete.getPlacePredictions({componentRestrictions: {country: 'us'}, input: input, bounds: nyLatLngBounds, types: ['geocode']}, function(a,b) {
            dataBus.setBroadcast(handle, {'status': b, 'data' :a});
        });
    };

    /**
     * Place a marker on the map.
     * lat - float
     * lon - float
     * title - tooltip text to display on hover
     * clear - remove all other markers
     * clickContent - display content upon marker click
     * center - set true to center map on marker
     */
    mapService.setMarker = function(lat, lon, title, clear, center, clickContent) {
        if (clear) {
            this.clearMarkers();
        }

        var marker = new google.maps.Marker({
            map: mapService.map,
            draggable:false,
            position: new google.maps.LatLng(lat, lon)
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
        if (center) {
            this.map.setCenter(this.activeMarker.position);
        }
        if (title) {
            google.maps.event.addListener(marker, "mouseover", function(){
                mapService.tooltipEl.show();
                mapService.tooltipEl.text(title);
            });
            google.maps.event.addListener(marker, "mouseout", function(){
                mapService.tooltipEl.hide();
            });
        }
    };

    /**
     * Sets a polygon overlay on the map with hover and click functionality
     * @param geom          Nested array of point arrays, e.g [[43.1,-73],[43.2,-73],[43.2,-73]]
     * @param name          The name of the polygon to display on the info header bar
     * @param fitBounds     If true then map will resize to fit polygon's bounds
     * @param clear         If true then map will be cleared of all overlays
     * @param clickHandler  If a callback is supplied it will be called when the polygon is clicked
     * @param color         Color of the polygon (default is teal)
     * @param style         Override style properties for the polygon e.g. {'fillOpacity': 0.5}
     */
    mapService.setOverlay = function(geom, name, fitBounds, clear, clickHandler, color, style) {
        if (geom != null) {
            if (style === null || typeof style === 'undefined') {
                style = {};
            }
            style = $.extend({
                strokeColor: (color) ? color : "teal",
                strokeOpacity: 1,
                strokeWeight: 1.5,
                fillColor: (color) ? color : "teal",
                fillOpacity: 0.3
            }, style);

            if (clear == true) {
                this.clearPolygons();
            }
            var coords = [];
            for (var i in geom) {
                for (var j in geom[i]) {
                    coords.push(new google.maps.LatLng(geom[i][j][0], geom[i][j][1]));
                }

                var overlayProps = $.extend({}, style, {
                    paths: coords
                });

                var polygon = new google.maps.Polygon(overlayProps);

                /** On mouseover reveal the tooltip and decrease opacity */
                google.maps.event.addListener(polygon,"mouseover",function() {
                    this.setOptions({fillOpacity: style.fillOpacity - 0.2});
                    mapService.tooltipEl.show();
                });

                google.maps.event.addListener(polygon, "mousemove", function(mousemove) {
                    /** Have to find the correct property name that contains the client x,y data */
                    if (mapService.mouseEventName == null) {
                        for (var prop in mousemove) {
                            if (mousemove.hasOwnProperty(prop) && typeof mousemove[prop] == 'object') {
                                if (mousemove[prop] != null && 'clientY' in mousemove[prop]) {
                                    mapService.mouseEventName = prop;
                                    break;
                                }
                            }
                        }
                    }
                    if (mapService.mouseEventName != null) {
                        mapService.tooltipEl.offset({top: mousemove[mapService.mouseEventName].clientY + 20, left: mousemove[mapService.mouseEventName].clientX});
                        mapService.tooltipEl.text(name);
                    }
                });

                /** On mouseout restore the opacity and hide the tooltip */
                google.maps.event.addListener(polygon,"mouseout",function(){
                    this.setOptions({fillOpacity: style.fillOpacity});
                    mapService.tooltipEl.hide();
                });

                /** Set up event handling for mouse click on polygon */
                if(clickHandler) {
                    google.maps.event.addListener(polygon,"click", function() {
                        if (mapService.polygon) {
                            mapService.polygon.setOptions({fillColor: style.fillColor});
                            mapService.polygon.setOptions({fillOpacity: style.fillOpacity});
                        }
                        this.setOptions({fillColor: "#ffcc00"});
                        this.setOptions({fillOpacity: 0.6});
                        mapService.polygon = this;
                        clickHandler();
                    });
                }

                polygon.setMap(this.map);
                this.polygons.push(polygon);
                this.polygon = polygon;
                coords = [];
            }

            /** Set the zoom level to the polygon bounds */
            if (fitBounds) {
                this.map.fitBounds(google.maps.getBoundsForPolygons(this.polygons));
            }

            /** Text to display on the map header */
            return this.polygon;
        }
        else {
            this.clearPolygons();
        }
        return null;
    };

    mapService.setLines = function(geom, fitBounds, clear, style, lineSymbolStyle) {
        var coords = [];
        if (clear) {
            this.clearPolyLines();
        }
        var latLngBounds = new google.maps.LatLngBounds();

        var lineSymbol = $.extend({
            path: 'M 0,-0.5 0,0.5',
            strokeWeight: 3,
            strokeOpacity: 1,
            scale: 1,
            zIndex: 1000
        }, lineSymbolStyle);

        if (style === null || typeof style === 'undefined') {
            style = {};
        }
        style = $.extend({
            strokeColor: "#333",
            strokeOpacity: 0,
            icons: [{
                icon: lineSymbol,
                offset: '100%',
                repeat: '8px'
            }]
        }, style);

        for (var i in geom) {
            for (var j in geom[i]) {
                var latLng = new google.maps.LatLng(geom[i][j][0], geom[i][j][1]);
                latLngBounds.extend(latLng);
                coords.push(latLng);
            }

            var line = new google.maps.Polyline($.extend({}, style, {path: coords}));
            line.setMap(this.map);
            this.lines.push(line);

            /** Set the zoom level to the district bounds for the first polyline */
            coords = [];
        }

        if (fitBounds) {
            this.map.fitBounds(latLngBounds);
            this.map.setZoom(this.map.getZoom());
        }
    };

    mapService.clearPolygon = function(polygon) {
        if (polygon) {
            try { polygon.setMap(null);}
            catch (ex) {}
        }
    };

    /**
     * Removes all polylines
     */
    mapService.clearPolyLines = function() {
        $.each(this.lines, function (i, v){
            v.setMap(null);
        });
        this.lines = [];
    };

    /**
     * Removes all polygon overlays
     */
    mapService.clearPolygons = function() {
        $.each(this.polygons, function (i, v){
            v.setMap(null);
        });
        this.polygons = [];
    };

    /**
     * Removes all markers
     */
    mapService.clearMarkers = function() {
        $.each(this.markers, function(i, v){
            v.setMap(null);
        });
        this.markers = [];
    };

    /**
     * Clears markers and overlays
     */
    mapService.clearAll = function() {
        this.clearMarkers();
        this.clearPolygons();
        this.clearPolyLines();
    };

    /**--------------------------------------------
     * Client Geocoder
     ---------------------------------------------*/
    mapService.geocode = function(address, callback) {
        var googleGeocoder = new google.maps.Geocoder();
        googleGeocoder.geocode( { 'address': address}, function(results, status) {
            if (status == google.maps.GeocoderStatus.OK) {
                return callback(results[0].geometry.location);
            }
            return null;
        });
    };

    mapService.formatDistrictName = function(dist) {
        return ((dist.name) ? dist.name + " " : capitalize(dist.type) + " District ")  + dist.district +
            ((dist.member) ? " - " + dist.member.name : "") +
            ((dist.senator) ? " - " + dist.senator.name : "");
    };

    return mapService;
});

sage.factory("uiBlocker", function($rootScope) {
    var uiBlocker = {};
    uiBlocker.block = function(msg) {
        $.blockUI({css:{border:'1px solid #ddd'}, overlayCSS: { backgroundColor: '#eee', opacity:0.5 }, message: msg});
    };
    uiBlocker.unBlock = function() {
        $.unblockUI();
    };
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

sage.filter("addressLevel", function(){
    return function(address, level) {
        switch (level) {
            case "POINT" :
            case "HOUSE" :
            case "STREET" :
                return address;
                break;
            case "CITY" :
                return {city: address.city, state: address.state};
                break;
            case "ZIP5" :
                return {state: address.state, zip5: address.zip5};
                break;
            default:
                return address;
        }
    }
});


sage.filter("parityFilter", function(){
    return function(parity) {
        switch (parity) {
            case "EVENS" : return "E";
            case "ODDS" : return "O";
            case "ALL" : return "EO";
            default : return "-";
        }
    };
});

/** Formats an address properly */
sage.filter('addressFormat', function(){
    return function(address, delim) {
        if (address != null && typeof address !== 'undefined') {
            var line1 = (notNullOrEmpty(address.addr1) ? address.addr1 : "") +
                (notNullOrEmpty(address.addr2) ? " " + address.addr2 + "" : "");

            var line2 = (notNullOrEmpty(address.city) ? " " + address.city + "," : "") +
                (notNullOrEmpty(address.state) ? " " + address.state : "") +
                (notNullOrEmpty(address.zip5) ? " " + address.zip5 : "") +
                (notNullOrEmpty(address.zip4) ? "-" + address.zip4 : "");
            return ((line1) ? line1 + ((delim != null && typeof delim != 'undefined') ? delim : "<br>") : "") + line2;
        }
    }
});

function notNullOrEmpty(input) { return input != null && input != '' && input != 'null'; }

function capitalize(input) {
    if (input !== null && typeof input !== 'undefined') {
        return input.substring(0,1).toUpperCase() + input.substring(1).toLowerCase();
    }
    else {
        return input;
    }
}

/**
 * Returns a proper district title depending on the district type.
 * @param district
 * @param type
 * @returns {string}
 */
function formatDistrictName(district, type) {
    var distType = district.type || type;
    if (distType !== null && typeof distType !== "undefined") {
        distType = distType.toLowerCase();
    }
    var districtName = (district.name) ? district.name + " " : "";
    if (distType == "school" || distType == "town" || distType == "county") {
        districtName += " - (" + district.district + ")";
    }
    else {
        districtName += (district.senator) ? " - " + district.senator.name : "";
        districtName += (district.member) ? " - " + district.member.name : "";
    }
    return districtName;
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
                "bPaginate": true,
                "bLengthChange": true,
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
                    $scope.sortedMemberList = data.districts.slice(0);
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

sage.controller("UspsLookupController", function($scope, $http, dataBus, mapService, menuService, uiBlocker) {
    $scope.id = 6;
    $scope.visible = false;

    $scope.$on(menuService.menuToggleEvent, function() {
        $scope.visible = menuService.isMethodActive($scope.id);
        if ($scope.visible) {
            dataBus.setBroadcast("hideResultTab", false);
            mapService.toggleMap(true);
        }
    });
});


sage.controller("StreetLookupController", function($scope, $http, dataBus, mapService, menuService, uiBlocker) {
    $scope.id = 3;
    $scope.visible = false;
    $scope.showFilter = false;
    $scope.zip5 = "";

    $scope.$on(menuService.menuToggleEvent, function() {
        $scope.visible = menuService.isMethodActive($scope.id);
        if ($scope.visible) {
            mapService.toggleMap(true);
            dataBus.setBroadcast("hideResultTab", false);
        }
    });

    $scope.lookup = function () {
        uiBlocker.block("Loading street information.");
        $http.get(this.getStreetLookupUrl())
            .success(function(data) {
                dataBus.setBroadcastAndView("street", data, "street");
                $scope.showFilter = true;
            }).error(function(data) {
                uiBlocker.unBlock();
            });
    };

    $scope.getStreetLookupUrl = function () {
        return contextPath + baseApi + "/street/lookup?zip5=" + this.zip5;
    };
});

sage.controller("RevGeoController", function($scope, $http, mapService, menuService, dataBus) {
    $scope.id = 4;
    $scope.visible = false;
    $scope.lat = "";
    $scope.lon = "";

    $scope.$on(menuService.menuToggleEvent, function() {
        $scope.visible = menuService.isMethodActive($scope.id);
        if ($scope.visible) {
            mapService.toggleMap(true);
        }
    });

    $scope.lookup = function() {
        $http.get(this.getRevGeoUrl())
            .success(function(data, status, headers, config) {
                dataBus.setBroadcastAndView("revgeo", data, "revgeo");
            });
    };

    $scope.getRevGeoUrl = function() {
        return contextPath + baseApi + "/geo/revgeocode?lat=" + this.lat + "&lon=" + this.lon;
    };
});

sage.controller("EmbeddedMapController", function($scope, $http, $window, dataBus, uiBlocker) {
    $scope.districtType = $window.districtType;
    $scope.districtCode = $window.districtCode;

    $scope.lookup = function() {
        if (this.districtType) {
            uiBlocker.block("Loading maps..");
            $http.get(this.getDistrictMapUrl())
                .success(function(data) {
                    dataBus.setBroadcast("embeddedMap", data);
                })
                .error(function(data){});
        }
    };

    $scope.getDistrictMapUrl = function () {
        return contextPath + baseApi + "/map/" + this.districtType + "?showMembers=true" + ((this.districtCode) ? ("&district=" + this.districtCode) : "");
    };

    $scope.lookup();
});

/**------------------------------------------------\
 * Views                                           |
 *------------------------------------------------*/
sage.controller('ResultsViewController', function($scope, dataBus, mapService) {
    $scope.paneVisible = false;
    $scope.showResultsTab = $("#showResultsTab");
    $scope.centercolumn = $('#contentcolumn');
    $scope.rightcolumn = $("#rightcolumn");
    $scope.toggleClass = "sidebar";

    $scope.$on('expandResults', function() {
        $scope.paneVisible = $scope.toggleResultPane(dataBus.data);
    });

    $scope.$on('hideResultTab', function(){
        $scope.paneVisible = false;
        $scope.closeResults();
        $scope.showResultsTab.hide();
    });

    $scope.toggleResultPane = function(show) {
        if (show != null) {
            if (show) {
                $scope.openResults();
                mapService.resizeMap();
                $scope.showResultsTab.hide();
                return true;
            }
            else {
                $scope.closeResults();
                mapService.resizeMap();
                $scope.showResultsTab.show();
            }
        }
        return false;
    };

    $scope.closeResults = function() {
        $scope.centercolumn.removeClass($scope.toggleClass);
        $scope.rightcolumn.hide();
    };

    $scope.openResults = function() {
        $scope.centercolumn.addClass($scope.toggleClass);
        $scope.rightcolumn.show();
    }
});

sage.controller('DistrictsViewController', function($scope, $http, $filter, dataBus, mapService, uiBlocker) {
    $scope.visible = false;
    $scope.viewId = "districtsView";
    $scope.showOffices = false;
    $scope.showNeighbors = false;
    $scope.neighborPolygons = [];
    $scope.colors = mapService.colors;
    $scope.neighborColors = ["#FF4500", "#639A00"];
    $scope.senateColors = {};
    $scope.placeSuggestions = {};
    $scope.viewSuggestions = false;

    $scope.$on(dataBus.viewHandleEvent, function(){
        $scope.visible = ($scope.viewId == dataBus.viewId);
    });

    /** Handle results of district info query */
    $scope.$on("districtInfo", function() {
        $scope = angular.extend($scope, dataBus.data);
        $scope.viewSuggestions = (!$scope.districtAssigned && !$scope.multiMatch);
        dataBus.setBroadcast("expandResults", true);
        mapService.toggleMap(true);

        if ($scope.multiMatch) {
            var fillOpacity = 0.5;
            /** Draw the intersected senate maps */
            if ($scope.overlaps.senate) {
                /** Sort the senate maps by greatest percentage first */
                $scope.overlaps.senate = $scope.overlaps.senate.sort(function(a, b) {
                    return b.areaPercentage - a.areaPercentage;
                });
                /** Assign a unique color to each senate district */
                $.each($scope.overlaps.senate, function(i,v){
                    $scope.senateColors[v.district] = $scope.colors[i];
                    if (v.map != null) {
                        mapService.setOverlay(v.map.geom, "NY Senate District " + v.district + " Coverage", false, false, null, $scope.colors[i], {fillOpacity:fillOpacity});
                    }
                });
            }
            /** Display senate street lines if available */
            if ($scope.matchLevel == "STREET") {
                fillOpacity = 0.2;
                if ($scope.streetLine && $scope.streetLine.geom) {
                    mapService.setLines($scope.streetLine.geom, true, false, "#555", {
                        path: 'M 0 0 L 0 1 L 1 1 L 1 0 z', strokeWeight: 1, strokeOpacity: 0,
                        scale: 9, fillOpacity: 0.5, fillColor: "yellow"
                    });
                }
                else {
                    mapService.setMarker($scope.geocode.lat, $scope.geocode.lon, '', true, true);
                }
            }
            else {
                /** Set region (city / zip) dashed line boundary for multi-matches */
                mapService.setLines($scope.referenceMap.geom, true, false, {});
            }
        }
        else {
            /** Update the marker location to point to the geocode */
            if ($scope.districtAssigned) {
                if ($scope.districts.senate.map) {
                    mapService.setOverlay($scope.districts.senate.map.geom,
                        formatDistrictName($scope.districts.senate, "Senate"), true, true, null);
                }
            }
            if ($scope.geocoded) {
                mapService.setMarker($scope.geocode.lat, $scope.geocode.lon,
                    $filter('addressFormat')($scope.address, ''), true, true, null);
                mapService.setZoom(15);
            }
        }

        /** Hide neighbors initially */
        $scope.showNeighbors = false;
        uiBlocker.unBlock();
        mapService.resizeMap();
    });

    /** Handle results of district maps query */
    $scope.$on("districtMap", function() {
        var data = dataBus.data;
        if (data.statusCode == 0) {
            /** Hide results tab and markers */
            dataBus.setBroadcast("hideResultTab", false);
            mapService.clearMarkers();

            /** Show all the district map boundaries */
            if (data != null && data.districts != null) {
                mapService.clearPolygons();
                $.each(data.districts, function(i, v){
                    if (v.map != null) {
                        mapService.setOverlay(v.map.geom, formatDistrictName(v), false, false,
                            (v.type == "SENATE") ? function() {

                                /** Draw the office markers */
                                mapService.clearMarkers();
                                $.each(v.member.offices, function(i, office){
                                    if (office && office.name != null && office.name != "") {
                                        mapService.setMarker(office.latitude, office.longitude, office.name + ' - ' + office.street, false, false);
                                    }
                                });
                                dataBus.setBroadcastAndView("member", v.member, "member");
                                $scope.$apply();
                            } : null
                        );
                    }
                });
                mapService.setCenter(42.440510, -76.495460); // Centers the map nicely over NY
                mapService.setZoom(7);
            }
            /** Show the individual district map */
            else if (data.map != null) {
                mapService.setOverlay(data.map.geom, formatDistrictName(data), true, true, null, null);
                if (data.type == "SENATE") {
                    dataBus.setBroadcastAndView("member", data.member, "member");

                    /** Draw the office markers */
                    mapService.clearMarkers();
                    $.each(data.member.offices, function(i, office){
                        if (office && office.name != null && office.name != "") {
                            mapService.setMarker(office.latitude, office.longitude, office.name + ' - ' + office.street, false, false);
                        }
                    });
                }
                else {
                    dataBus.setBroadcast("hideResultTab");
                }
            }
        }
        mapService.toggleMap(true);
        uiBlocker.unBlock();
    });

    $scope.$on("placeSuggestions", function(){
        var sugg = dataBus.data;
        if (sugg != null && sugg.status == 'OK') {
            $scope.placeSuggestions = sugg.data;
        }
        else {
            $scope.placeSuggestions = {};
        }
    });

    /** Show the specified district map */
    $scope.showDistrict = function(districtType) {
        if ($scope.districts[districtType] != null && typeof $scope.districts[districtType] != "undefined") {
            var district = $scope.districts[districtType];
            district.type = districtType; // Set the type for the formatDistrictName method
            mapService.resizeMap();
            mapService.setOverlay(district.map.geom, formatDistrictName(district), true, true, null);
        }
    };

    $scope.showFullMapForOverlap = function(index, overlap, matchLevel) {
        var geom = (matchLevel == "STREET") ? overlap.map.geom : overlap.fullMap.geom;
        mapService.setOverlay(geom, overlap.name, false, true, null, this.colors[index]);
    };

    $scope.showNeighborDistricts = function(type, neighbors) {
        this.showNeighbors = true;
        $.each(neighbors, function(i, neighbor){
            neighbor.style = {'color' : $scope.neighborColors[i % 2] };
            $scope.neighborPolygons.push(mapService.setOverlay(neighbor.map.geom, formatDistrictName(neighbor, "Senate"),
                                                               false, false, null, neighbor.style['color']));
        });
    };

    $scope.hideNeighborDistricts = function()  {
        this.showNeighbors = false;
        $.each($scope.neighborPolygons, function(i, neighborPolygon){
            mapService.clearPolygon(neighborPolygon);
        });
    };

    $scope.setOfficeMarker = function(office) {
        if (office != null) {
            mapService.setMarker(office.latitude, office.longitude, office.name + ' - ' + office.street, true, true, null);
        }
    };

    $scope.requestDistrictInfo = function(addr) {
        dataBus.setBroadcast("requestDistrictInfo", addr);
    };

    $scope.getBgStyle = function(i) {
        return {"background-color" : this.colors[i]};
    };

    $scope.getColorStyle = function(senateDistrict) {
        return {"color": this.senateColors[senateDistrict]};
    };
});

sage.controller("StreetViewController", function($scope, dataBus, uiBlocker, mapService) {
   $scope.visible = false;
   $scope.viewId = "street";
   $scope.streets = [];

   $scope.columnDefs = [
        { "mDataProp": "bldgLoNum", "aTargets":[0]},
        { "mDataProp": "bldgHiNum", "aTargets":[1]},
        { "mDataProp": "street", "aTargets":[2]},
        { "mDataProp": "parity", "aTargets":[3]},
        { "mDataProp": "location", "aTargets":[4]},
        { "mDataProp": "zip5", "aTargets":[5]},
        { "mDataProp": "senate", "aTargets":[6]},
        { "mDataProp": "congressional", "aTargets":[7]},
        { "mDataProp": "assembly", "aTargets":[8]},
        { "mDataProp": "county", "aTargets":[9]},
        { "mDataProp": "town", "aTargets":[10]},
        { "mDataProp": "election", "aTargets":[11]}
    ];

    $scope.overrideOptions = {
        "bStateSave": false,
        "iCookieDuration": 2419200, /* 1 month */
        "bJQueryUI": false,
        "bPaginate": true,
        "bLengthChange": true,
        "bFilter": true,
        "bInfo": true,
        "bDestroy": true,
        "iDisplayLength": 20,
        "sPaginationType" : "full_numbers"
    };

    // Sort by street ( column index 2 )
    $scope.sortDefault = [[2, "asc"]];

    $scope.$on("street", function(){
       $scope.streets = (dataBus.data.streets);
       uiBlocker.unBlock();
   });

   $scope.$on(dataBus.viewHandleEvent, function(){
        $scope.visible = ($scope.viewId == dataBus.viewId);
   });
});

sage.controller("MemberViewController", function($scope, dataBus, mapService) {
    $scope.visible = false;
    $scope.viewId = "member";
    $scope.member = {};

    $scope.$on(dataBus.viewHandleEvent, function() {
        $scope.visible = ($scope.viewId == dataBus.viewId);
    });

    $scope.showMember = function() {
        $scope.member = dataBus.data;
        dataBus.setBroadcast("expandResults", true);
    };

    /** Used for individual district view */
    $scope.$on("member", function() {
        if (dataBus.data) {
            $scope.showMember();
        }
    });

    $scope.setOfficeMarker = function(office) {
        if (office != null) {
            mapService.setMarker(office.latitude, office.longitude, office.name + ' - ' + office.street, true, true, null);
        }
    }
});

sage.controller("RevGeoViewController", function($scope, $filter, dataBus, mapService) {
    $scope.visible = false;
    $scope.viewId = "revgeo";
    $scope.revGeocoded = false;

    $scope.$on("revgeo", function(){
        $scope = angular.extend($scope, dataBus.data);
        $scope.revGeocoded = ($scope.statusCode == 0);
        if ($scope.revGeocoded) {
            var markerTitle = $filter('addressFormat')($scope.address, '');
            mapService.setMarker($scope.geocode.lat, $scope.geocode.lon, markerTitle , false, true, null);
        }
        dataBus.setBroadcast("expandResults", true);
        mapService.toggleMap(true);
    });

    $scope.$on(dataBus.viewHandleEvent, function(){
        $scope.visible = ($scope.viewId == dataBus.viewId);
    });
});

sage.controller("EmbeddedMapViewController", function($scope, dataBus, uiBlocker, mapService){
    $scope.showPrompt = false;
    $scope.viewInfo = false;

    $scope.$on("embeddedMap", function() {
        var data = dataBus.data;
        if (data.statusCode == 0) {
            mapService.clearMarkers();
            /** Show all the district map boundaries */
            if (data != null && data.districts != null) {
                mapService.clearPolygons();
                $.each(data.districts, function(i, v){
                    if (v.map != null) {
                        mapService.setOverlay(v.map.geom, formatDistrictName(v), false, false,
                            (v.type.toLowerCase() == "senate") ?
                                function() {
                                    dataBus.setBroadcast("showEmbedSenator", v);
                                }
                            : null, mapService.colors[0]);
                    }
                });
                mapService.setCenter(42.440510, -76.495460); // Centers the map nicely over NY
                mapService.setZoom(7);
            }
            /** Show the individual district map */
            else if (data.map != null) {
                $scope.senator = data.member;
                $scope.district = data.district;
                mapService.setOverlay(data.map.geom, formatDistrictName(data), true, true, null, mapService.colors[0]);
                if (data.type.toLowerCase() == "senate") {
                    $scope.setOfficeMarkers(data.member.offices);
                    $scope.showPrompt = false;
                    $scope.showInfo = false;
                }
            }
        }
        mapService.toggleMap(true);
        uiBlocker.unBlock();
    });

    $scope.$on("showEmbedSenator", function(){
        var data = dataBus.data;
        if (data) {
            $scope.$apply(function(){
                $scope.showPrompt = true;
                $scope.showInfo = true;
                $scope.senator = data.member;
                $scope.district = data.district;
                mapService.clearMarkers();
                $scope.setOfficeMarkers(data.member.offices);
            });
        }
    });

    $scope.setOfficeMarkers = function(offices) {
        /** Clicking an office marker will open info pane with details */
        $.each(offices, function(i, office){
            if (office && office.name != null && office.name != "") {
                mapService.setMarker(office.latitude, office.longitude, office.name, false, false,
                    "<div style='width:160px;'>" +
                        "<p style='color:teal;font-size:18px;'>" + office.name + "</p>" +
                        "<p>" + office.street + "</p>" +
                        "<p>" + office.additional+ "</p>" +
                        "<p>" + office.city + ", " + office.province + " " + office.postalCode +"</p>" +
                        "<p>Phone " + office.phone + "</p>" +
                    "</div>");
            }
        });
    }
});

$(document).ready(function(){
    initVerticalMenu();
    resizeContentHeights();

    $("#contentcolumn").show();

    /** Expand/Collapse behavior for search containers
     * TODO: Move to directive */
    var activeSearchContainer = ".search-container:visible";
    var activeSearchContent = ".search-container:visible .search-container-content";
    $(".collapse-search").on("click", function(){
        $(activeSearchContent).hide();
        $(activeSearchContainer).animate({
            width: '150px'
        }, 100, function(){});
    });

    $(".expand-search").on("click", function() {
        var width = $(activeSearchContainer).hasClass("small") ? "240px" : "350px";
        $(activeSearchContainer).animate({
            width: width
        }, 100, function(){
            $(activeSearchContent).fadeIn();
        });
    });

    /**
     * Debounced Resize() jQuery Plugin
     */
    (function($,sr){
        // debouncing function from John Hann
        // http://unscriptable.com/index.php/2009/03/20/debouncing-javascript-methods/
        var debounce = function (func, threshold, execAsap) {
            var timeout;

            return function debounced () {
                var obj = this, args = arguments;
                function delayed () {
                    if (!execAsap)
                        func.apply(obj, args);
                    timeout = null;
                }

                if (timeout)
                    clearTimeout(timeout);
                else if (execAsap)
                    func.apply(obj, args);

                timeout = setTimeout(delayed, threshold || 100);
            };
        };
        // smartresize
        jQuery.fn[sr] = function(fn){ return fn ? this.bind('resize', debounce(fn)) : this.trigger(sr); };

    })(jQuery,'smartresize');

    $(window).smartresize(function(e){
        resizeContentHeights();
    });

    /**
     * Resize the page dynamically to avoid scrollbars but only do so if the window isn't fixed size
     * via query params.
     */
    function resizeContentHeights() {
        if (typeof width == 'undefined' || typeof height == 'undefined' || width == null || width <= 0 || height == null || height <= 0) {
            var windowHeight = $(window).height();
            var $contentColumn = $('#contentcolumn');
            var $mapContentColumn = $('#mapcontentcolumn');
            var $scrollableContent = $('.scrollable-content');
            var $embeddedFrames = $('#uspsIframe, #streetViewFrame');

            $contentColumn.height(windowHeight - 42);
            $mapContentColumn.height(windowHeight - 2);
            $scrollableContent.height(windowHeight - 84);
            $embeddedFrames.height($contentColumn.height() - 4);
        }
    }

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
    };

    google.maps.Polyline.prototype.getBounds = function() {
        var bounds = new google.maps.LatLngBounds();
        this.getPath().forEach(function(e) {
            bounds.extend(e);
        });
        return bounds;
    };

    google.maps.getBoundsForPolygons = function(polygons) {
        var bounds = new google.maps.LatLngBounds();
        $.each(polygons, function(index, polygon){
            var paths = polygon.getPaths();
            var path;
            for (var i = 0; i < paths.getLength(); i++) {
                path = paths.getAt(i);
                for (var ii = 0; ii < path.getLength(); ii++) {
                    bounds.extend(path.getAt(ii));
                }
            }
        });
        return bounds;
    };

    function getBoundsForMultiPolyLine(lines) {
        var bounds = new google.maps.LatLngBounds();
        $.each(lines, function(i,v) {
            v.getPath().forEach(function(e) {
                bounds.extend(e);
            });
        });
        return bounds;
    }
});