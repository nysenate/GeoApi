<html>
<body>
<div id="mapdiv" style="height:374px; width:482px;"></div>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.js"></script>
<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
<script><!--
	var lat;
	var lon;
	var zoom;
	var map;
	var selectFeat;
	var json;
	var layer;
	var markers;
	
	var district;
	var mfunc;

	$(document).ready(function(){
		
		mfunc = function(dnum) {
			if(map != null) {
				layer.removeAllFeatures();
				markers.clearMarkers();
			}
			else {
				map = new OpenLayers.Map("mapdiv");
				map.addLayer(new OpenLayers.Layer.OSM());
			}
			district = dnum;
			$.getJSON("json/sd" + district + ".json",function(data){
				json = data;
				lat = json.lat;
				lon = json.lon;
				zoom = json.zoom;
				
				var lonLat = new OpenLayers.LonLat(lon, lat).transform(
						new OpenLayers.Projection("EPSG:4326"),
						map.getProjectionObject());
				map.setCenter(lonLat, data.zoom-1);
				
				
				layer = new OpenLayers.Layer.Vector("KML", {
					strategies : [ new OpenLayers.Strategy.Fixed() ],
					protocol : new OpenLayers.Protocol.HTTP({
						url : "kml/sd" + district + ".kml",
						format : new OpenLayers.Format.KML})});
				
				layer.events.fallThrough = true;
				map.addLayer(layer);
				
				/*selectFeat = new OpenLayers.Control.SelectFeature(
		            layer, {
		            	toggle: true, 
		            	clickout:true, 
		            	onSelect:up,
		            	onUnselect : down});
				selectFeat.handlers['feature'].stopDown = false;
				selectFeat.handlers['feature'].stopUp = false;
				map.addControl(selectFeat);//instance of map
				selectFeat.activate();*/
				
				
				markers = new OpenLayers.Layer.Markers("Markers");
				map.addLayer(markers);
				markers.setZIndex(layer.getZIndex());
				for(var i in json.senate.senator.offices) {
					var office = json.senate.senator.offices[i];
					var tMarker = new OpenLayers.Marker(new OpenLayers.LonLat(office.lat, office.lon).transform(
							new OpenLayers.Projection("EPSG:4326"),
							map.getProjectionObject()));
					
					
					
					markers.addMarker(tMarker);
					
				}
				
			});
			
			doMap();
		};
	});
	
	function doMap() {
		
	}
	
	function onPopupClose(evt) {
		selectFeat.unselect(selectedFeature);
	}

	function up(feature) {
		selectedFeature = feature;
		text = '';
		popup = new OpenLayers.Popup.FramedCloud("featurePopup",
                feature.geometry.getBounds().getCenterLonLat(),
                new OpenLayers.Size(300,300),
                "<b>"+ feature.attributes.name + "</b>" +
				"<table cellspacing=\"10\">"
					+ "<tr>"
						+ "<td rowspan=3><img src=\"" + json.senate.senator.imageUrl + "\" alt=\"" + json.senate.senator.name + "\"></img></td>"
						+ "<td><a href=\"" + json.senate.districtUrl + "\">" + json.senate.district + "</a></td>"
					+ "</tr>"
					+ "<tr>"
						+ "<td><a href=\"" + json.senate.senator.url + "\">" + json.senate.senator.name + "</a></td>"
					+ "</tr>"
					+ "<tr>"
						+ "<td>" + json.senate.senator.contact + "</td>"
					+ "</tr>"
				+ "</table>",
                null, true, onPopupClose);
		feature.popup = popup;
		popup.setOpacity(0.95);
		map.addPopup(popup);
	}
	function down(feature) {
		map.removePopup(feature.popup);
		feature.popup.destroy();
		feature.popup = null;
	}

</script>

</body>
</html>