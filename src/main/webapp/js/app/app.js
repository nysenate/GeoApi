var sage = angular.module('sage', ['sage-common']);

/**-------------------------------------------------\
 * Base Configuration                               |
 * ------------------------------------------------*/
var baseApi = "/api/v2";
var map;



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
        return input;
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

            $contentColumn.height(windowHeight - 62);
            $mapContentColumn.height(windowHeight - 2);
            $scrollableContent.height(windowHeight - 124);
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