<%@ page import="gov.nysenate.sage.util.Config" %>
<!DOCTYPE html>
<html>
<head>
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.js"></script>
    <script src="OpenLayers.js"></script>
    <script src="http://maps.google.com/maps/api/js?v=3.2&sensor=false"></script>
    <script>
    
    $(document).ready(function() {
    	epsg4326 = new OpenLayers.Projection('EPSG:4326');
    	epsg900913 = new OpenLayers.Projection('EPSG:900913');
    	
    	var navoff = <%=request.getParameter("nonav")%>;
    	var district = <%=request.getAttribute("district")%>;
    	var point_data = <%=request.getAttribute("point_data")%>;
    	var office_data = <%=request.getAttribute("office_data")%>;
        
        var points = new Array(point_data.length);
        for (var i=0; i<point_data.length; i++) {
            points[i] = new OpenLayers.Geometry.Point(point_data[i][1],point_data[i][0]).transform(epsg4326, epsg900913);
        }
        
    	var map = new OpenLayers.Map({div: 'map'});
        map.addLayer(new OpenLayers.Layer.Google("Google Streets"));
    	
        if (navoff != null) {
	        map.removeControl(map.getControl('OpenLayers.Control.Navigation_4'));
	        map.removeControl(map.getControl('OpenLayers.Control.ArgParser_6'));
	        map.removeControl(map.getControl('OpenLayers.Control.PanZoom_5'));
	        map.removeControl(map.getControl('OpenLayers.Control.Attribution_7'));
        }
        
    	if (district != 0) {
	        var vectorLayer = new OpenLayers.Layer.Vector("District");
	        var markers = new OpenLayers.Layer.Markers("Offices");
	        
	        // Add our district feature as an outline
	        var ring = new OpenLayers.Geometry.LinearRing(points);
	        var districtFeature = new OpenLayers.Feature.Vector(ring);
	        vectorLayer.addFeatures([districtFeature]);
	        vectorLayer.events.fallThrough = true;
	    
	        // Don't display markers with navoff. this is old behavior; Not sure why we do this.
            if (navoff == null) {
	            for(var i in office_data) {
	                var office = office_data[i];
	              
			        // Create a new OSM Marker on the map at the projected location!
			        var officeLocation = new OpenLayers.LonLat(office.lat, office.lon).transform(epsg4326,epsg900913);
			        var officeMarker = new OpenLayers.Marker(officeLocation);

			        var hasCloseBox = true;
	                var anchorObject = null;
	                var closeBoxCallback = function () { this.toggle(); };
	                
			        officeMarker.popup = new OpenLayersPopup.FramedCloud(
			        		"Office Data",
			        		officeLocation,
                            new OpenLayers.Size(200,00),
                            office.officeName + "<br>" + office.street + "<br>" + office.city + ", " + office.state + "<br>Phone: " +office.phone + "<br>Fax: " + office.fax,
                            anchorObject,
                            hasCloseBox,
                            closeBoxCallback
	        		);
			        
			        officeMarker.events.register('mousedown',officeMarker, function(event) {
			        	this.popup.toggle();
			        	OpenLayers.Event.stop();
		        	});

			        markers.addMarker(officeMarker);
			     }
             }
	        
		     map.addLayers([vectorLayer, markers]);
		     
		     // markers hide under the kml layer otherwise
		     markers.setZIndex(vectorLayer.getZIndex());
		     
		     // Zoom to the polygon shape
		     map.zoomToExtent(vectorLayer.getDataExtent());
		     
        } else {
            // Zoom to a blank map of NY if no district is requested
            map.zoomToExtent(new OpenLayers.Bounds(-80,40.38,-71.66,45.03).transform(
                new OpenLayers.Projection("EPSG:4326"),
                map.getProjectionObject()
            ));
        }
    });
    </script>
</head>
<body>
    <div style="width:<%=request.getAttribute("x")%>px; height:<%=request.getAttribute("y")%>px;" id="map"></div>
</body>
</html>
