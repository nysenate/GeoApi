var map = angular.module('map', ['sage-common']);

map.factory("mapService", function($rootScope, uiBlocker, dataBus) {

    google.maps.visualRefresh = true;

    /** Load up the NY State bounds */
    var nyBounds = [[40.488737, -74.264832],[40.955011, -71.762695],[41.294317, -71.932983],[40.955011, -73.641357],[41.100052, -73.721008],[41.215854, -73.487549],[41.298444, -73.550720],[42.085994, -73.504028],[42.747012, -73.267822],[43.612217, -73.289795],[45.003651, -73.300781],[45.011419, -74.959717], [43.612217, -77.189941],[43.269206, -79.112549],[42.843751, -78.936768],[42.536892, -79.782715],[42.000325, -79.749756],[41.983994, -75.366211],[41.327326, -74.783936],[40.996484, -73.907776],[40.653555, -74.058838],[40.640009, -74.200287]];
    var nyLatLngBounds = new google.maps.LatLngBounds();
    $.each(nyBounds, function(i,v){
        nyLatLngBounds.extend(new google.maps.LatLng(v[0], v[1]));
    });

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
        styles: [
            {featureType: "transit",
                stylers: [{ "visibility": "off"}]}
        ]
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
    //                    teal      orangered    green      red        yellow     cyan    pink         purple     darkblue
    mapService.colors = ["#008080", "#ff4500", "#639A00", "#CC333F", "#EDC951", "#09AA91", "#F56991", "#524656", "#547980"];

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
            position: new google.maps.LatLng(lat, lon),
            zIndex: google.maps.Marker.MAX_ZINDEX + 1
        });
        marker.setZIndex(google.maps.Marker.MAX_ZINDEX + 1);

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
        console.log("Titles comp: " + title !== '');
        if (title !== '') {
            marker.addListener('mouseover', function() {
                console.log("Got inside mouseover event");
                mapService.tooltipEl.text(title);
                mapService.tooltipEl.show();
            });
            marker.addListener('click', function() {
                mapService.tooltipEl.text(title);
                mapService.tooltipEl.show();
            });
            marker.addListener('mouseout', function() {
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