var sage = angular.module('sage');

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

    //                  teal      orangered    green      red        yellow     cyan       pink      purple     darkblue
    var polyColors =  ["#008080", "#ff4500", "#639A00", "#CC333F", "#EDC951", "#09AA91", "#F56991", "#524656", "#547980"];

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
    mapService.overlayFeatures = [];
    mapService.selectedFeature = null;
    mapService.overlayHandlers = new WeakMap();
    mapService.boundaryLines = [];
    mapService.markers = [];
    mapService.activeMarker = null;
    mapService.districtData = null;
    mapService.colors = polyColors;
    mapService.showToolTip = true;

    /**
     * District overlays are drawn with the Data layer: the API returns GeoJSON
     * geometry that we hand straight to map.data.addGeoJson. Each overlay's base
     * appearance lives on its feature's "style" property; hover and click-selection
     * are applied as temporary style overrides on top of it.
     */
    mapService.map.data.setStyle(function(feature) {
        return feature.getProperty("style") || {};
    });

    var applySelectedStyle = function(feature) {
        mapService.map.data.overrideStyle(feature, {fillColor: "#ffcc00", fillOpacity: 0.6});
    };

    /** On mouseover reveal the tooltip and decrease opacity */
    mapService.map.data.addListener("mouseover", function(event) {
        var style = event.feature.getProperty("style");
        mapService.map.data.overrideStyle(event.feature, {fillOpacity: style.fillOpacity - 0.2});
        if (mapService.showToolTip) {
            mapService.tooltipEl.show();
        }
    });

    /** Follow the cursor with the district name */
    mapService.map.data.addListener("mousemove", function(event) {
        var name = event.feature.getProperty("name");
        /** County DoH embeds label districts by county name only */
        if (window.doh === true && name) {
            name = name.split("-")[0].trim();
        }
        if (event.domEvent) {
            mapService.tooltipEl.offset({top: event.domEvent.clientY + 20, left: event.domEvent.clientX});
            mapService.tooltipEl.text(name);
        }
    });

    /** On mouseout drop the hover override (re-applying selection if needed) and hide the tooltip */
    mapService.map.data.addListener("mouseout", function(event) {
        mapService.map.data.revertStyle(event.feature);
        if (event.feature === mapService.selectedFeature) {
            applySelectedStyle(event.feature);
        }
        mapService.tooltipEl.hide();
    });

    /** Only overlays registered with a click handler are selectable */
    mapService.map.data.addListener("click", function(event) {
        var handler = mapService.overlayHandlers.get(event.feature);
        if (!handler) {
            return;
        }
        if (mapService.selectedFeature) {
            mapService.map.data.revertStyle(mapService.selectedFeature);
        }
        applySelectedStyle(event.feature);
        mapService.selectedFeature = event.feature;
        handler();
    });

    /**
     * Resize when window size changes
     */
    window.addEventListener('resize', function() {
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
        console.log(title);

        var marker = new google.maps.Marker({
            map: mapService.map,
            position: new google.maps.LatLng(lat, lon),
            draggable:false,
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

        google.maps.event.addListener(marker, 'mouseover', function() {
            mapService.showToolTip = false;
            mapService.tooltipEl.hide();
        });

        google.maps.event.addListener(marker, 'mouseout', function() {
            mapService.showToolTip = true;
            mapService.tooltipEl.show();

        });

        this.activeMarker = marker;
        this.markers.push(marker);
        if (center) {
            this.map.setCenter(this.activeMarker.position);
        }
    };

    /**
     * Draw a marker for a senator/DoH office.
     * It shows the office name and street on hover and the
     * full office contact details in a popup when clicked. A single office is
     * treated as a "locate" action: other markers are cleared and the map centers
     * on it.
     */
    const setOfficeMarkerHelper = function (office, focus) {
        if (!office?.name || !office.point) {
            return;
        }
        mapService.setMarker(office.point.lat, office.point.lon,
            office.name + ' - ' + office.address.addr1, focus, focus,
            focus ? null :
                "<div style='width:160px;'>" +
                "<p style='color:teal;font-size:18px;'>" + office.name + "</p>" +
                "<p>" + office.address.addr1 + "</p>" +
                "<p>" + office.address.city + ", NY " + office.address.zip5 + "</p>" +
                "<p>Phone " + office.phone + "</p>" +
                "</div>");
    };

    mapService.setOfficeMarker = function(office) {
        setOfficeMarkerHelper(office, true);
    };

    mapService.setOfficeMarkers = function(offices) {
        if (offices) {
            $.each(offices, function(i, office) {setOfficeMarkerHelper(office, false);})
        }
    };

    /**
     * Draws a district overlay from a GeoJSON geometry using the Data layer.
     * @param geom          GeoJSON geometry object, e.g. {type: "MultiPolygon", coordinates: [...]}
     * @param name          The name of the district to display in the tooltip
     * @param fitBounds     If true then map will resize to fit the overlay's bounds
     * @param clear         If true then map will be cleared of all overlays first
     * @param clickHandler  If a callback is supplied it will be called when the overlay is clicked
     * @param color         Color of the overlay (default is teal)
     * @param style         Override style properties for the overlay e.g. {'fillOpacity': 0.5}
     */
    mapService.setOverlay = function(geom, name, fitBounds, clear, clickHandler, color, style) {
        if (geom == null) {
            this.clearPolygons();
            return null;
        }
        if (clear === true) {
            this.clearPolygons();
        }

        var overlayStyle = $.extend({
            strokeColor: color || "teal",
            strokeOpacity: 1,
            strokeWeight: 1.5,
            fillColor: color || "teal",
            fillOpacity: 0.3
        }, style);

        var added = this.map.data.addGeoJson({
            type: "Feature",
            geometry: geom,
            properties: {name: name, style: overlayStyle}
        });

        var self = this;
        added.forEach(function(feature) {
            if (clickHandler) {
                self.overlayHandlers.set(feature, clickHandler);
            }
            self.overlayFeatures.push(feature);
        });

        /** Set the zoom level to the overlay bounds */
        if (fitBounds) {
            this.map.fitBounds(this.getOverlayBounds());
        }

        return added.length ? added[added.length - 1] : null;
    };

    /**
     * Computes a bounding box that contains every drawn overlay feature.
     */
    mapService.getOverlayBounds = function() {
        var bounds = new google.maps.LatLngBounds();
        this.overlayFeatures.forEach(function(feature) {
            feature.getGeometry().forEachLatLng(function(latLng) {
                bounds.extend(latLng);
            });
        });
        return bounds;
    };

    /**
     * Draws a dashed outline of a GeoJSON geometry.
     * @param geom      GeoJSON Polygon or MultiPolygon geometry
     * @param fitBounds If true, the map is framed to the boundary
     * @returns {boolean} true if a boundary was drawn
     */
    mapService.setBoundary = function(geom, fitBounds) {
        this.clearBoundary();
        if (geom == null || geom.coordinates == null) {
            return false;
        }
        var dashSymbol = {path: 'M 0,-0.5 0,0.5', strokeWeight: 3, strokeOpacity: 1, scale: 1};
        var lineStyle = {
            strokeColor: "#333",
            strokeOpacity: 0,
            zIndex: 1000,
            icons: [{icon: dashSymbol, offset: '100%', repeat: '8px'}]
        };
        /** MultiPolygon nests as [polygon][ring][point]; Polygon as [ring][point]. */
        var polygons = (geom.type === "MultiPolygon") ? geom.coordinates : [geom.coordinates];
        var bounds = new google.maps.LatLngBounds();
        var self = this;
        polygons.forEach(function(rings) {
            rings.forEach(function(ring) {
                /** GeoJSON positions are [lon, lat]; LatLng takes (lat, lon). */
                var path = ring.map(function(pt) {
                    var latLng = new google.maps.LatLng(pt[1], pt[0]);
                    bounds.extend(latLng);
                    return latLng;
                });
                var line = new google.maps.Polyline($.extend({}, lineStyle, {path: path}));
                line.setMap(self.map);
                self.boundaryLines.push(line);
            });
        });
        if (fitBounds && !bounds.isEmpty()) {
            this.map.fitBounds(bounds);
        }
        return this.boundaryLines.length > 0;
    };

    /**
     * Removes the dashed boundary outline
     */
    mapService.clearBoundary = function() {
        this.boundaryLines.forEach(function(line) {
            line.setMap(null);
        });
        this.boundaryLines = [];
    };

    /**
     * Removes all district overlays
     */
    mapService.clearPolygons = function() {
        var self = this;
        this.overlayFeatures.forEach(function(feature) {
            self.map.data.remove(feature);
        });
        this.overlayFeatures = [];
        this.selectedFeature = null;
        this.overlayHandlers = new WeakMap();
        this.clearBoundary();
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
    };

    return mapService;
});
