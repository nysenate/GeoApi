<html>
<body>
<div id="mapdiv" style="height:265px; width:265px;"></div>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.js"></script>
<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
<script><!--
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
			
			/*var lonLat = new OpenLayers.LonLat(lon, lat).transform(
					new OpenLayers.Projection("EPSG:4326"),
					map.getProjectionObject()
			);*/
			
			var lonLat = null;
			if(json.offsetLon && json.offsetLat) {
				var lonLat = new OpenLayers.LonLat(json.offsetLon, json.offsetLat);
			}
			else {
				var lonLat = new OpenLayers.LonLat(lon, lat).transform(
						new OpenLayers.Projection("EPSG:4326"),
						map.getProjectionObject());
			}
			map.setCenter(lonLat, zoom);

			map.setCenter(lonLat, data.zoom-2);
			
			//$('.olControlAttribution').hide();
			
			map.removeControl(map.getControl('OpenLayers.Control.Navigation_4'));
			map.removeControl(map.getControl('OpenLayers.Control.ArgParser_6'));
			map.removeControl(map.getControl('OpenLayers.Control.Attribution_7'));
			map.removeControl(map.getControl('OpenLayers.Control.PanZoom_5'));
			
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