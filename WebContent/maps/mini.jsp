
<html>
<body>
<div id="mapdiv" style="height:206px; width:265px;"></div>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.js"></script>
<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
<script>
	var lat;
	var lon;
	var zoom;
	var map;
	var selectFeat;
	var json;

	$(document).ready(function(){
		doMap();
		$.getJSON("json/sd" + <%= request.getParameter("sd") %> + ".json",function(data){
			json = data;
			lat = json.lat;
			lon = json.lon;
			zoom = json.zoom;
			
			var lonLat = new OpenLayers.LonLat(lon, lat).transform(
					new OpenLayers.Projection("EPSG:4326"),
					map.getProjectionObject()
			);

			map.setCenter(lonLat, data.zoom-2);
			
		});
	});
	
	function doMap() {
		map = new OpenLayers.Map("mapdiv");
		map.addLayer(new OpenLayers.Layer.OSM());
		
		var vectors = new Array();

		var layer = new OpenLayers.Layer.Vector("KML", {
			strategies : [ new OpenLayers.Strategy.Fixed() ],
			protocol : new OpenLayers.Protocol.HTTP({
				url : "kml/sd" + <%= request.getParameter("sd") %> + ".kml",
				format : new OpenLayers.Format.KML
			})
		});
		
		layer.events.fallThrough = true;
		
		vectors.push(layer);

		map.addLayers(vectors);
		
		selectFeat = new OpenLayers.Control.SelectFeature(
            vectors, {
            	toggle: true, 
            	clickout:true
            });
		selectFeat.handlers['feature'].stopDown = false;
		selectFeat.handlers['feature'].stopUp = false;
		map.addControl(selectFeat);//instance of map
		selectFeat.activate();

	}

</script>

</body>
</html>