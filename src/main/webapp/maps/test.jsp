<html>
	<body>
		<div id="mapdiv"></div>
		
		<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.js"></script>
		<script src="OpenLayers.js"></script>
		<script src="http://maps.google.com/maps/api/js?v=3.2&sensor=false"></script>
		<script>
			var map;
			var layer;
			var layer2;
			var markers;
			var mfunc;
			var popup;			 
			var list={list:[{lat:41.794135,lon:-73.599301,addr:"273 OLD RTE, WASSAIC, NY 12592"},{lat:41.794135,lon:-73.599301,addr:"273 OLD RTE, WASSAIC, NY 12592"},{lat:41.794135,lon:-73.599301,addr:"393 OLD RTE, WASSAIC, NY 12592"},{lat:41.753605,lon:-73.587358,addr:"90 TINKER TOWN RD, DOVER PLAINS, NY 12522"},{lat:41.639564,lon:-73.655564,addr:"18 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.639453,lon:-73.65531,addr:"21 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.639453,lon:-73.65531,addr:"21 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638998,lon:-73.654148,addr:"39 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638849,lon:-73.653888,addr:"50 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638849,lon:-73.653888,addr:"50 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638957,lon:-73.654077,addr:"57 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638957,lon:-73.654077,addr:"57 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638934,lon:-73.654037,addr:"64 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638851,lon:-73.653891,addr:"96 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638851,lon:-73.653891,addr:"96 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.639791,lon:-73.662008,addr:"3 GAIL LN, POUGHQUAG, NY 12570"},{lat:41.636826,lon:-73.753297,addr:"75 BEAVER RD, LAGRANGEVILLE, NY 12540"},{lat:41.556095,lon:-73.795851,addr:"3 LAKEVIEW TER, HOPEWELL JUNCTION, NY 12533"},{lat:41.562987,lon:-73.714081,addr:"325 S GREENHAVEN RD, STORMVILLE, NY 12582"},{lat:41.562987,lon:-73.714081,addr:"325 S GREENHAVEN RD, STORMVILLE, NY 12582"},{lat:41.562973,lon:-73.714091,addr:"341 S GREENHAVEN RD, STORMVILLE, NY 12582"},{lat:41.651897,lon:-73.697946,addr:"5 DUNCAN RD, LAGRANGEVILLE, NY 12540"},{lat:41.750154,lon:-73.583787,addr:"5 CEDAR BERRY LN, DOVER PLAINS, NY 12522"},{lat:41.75058,lon:-73.584972,addr:"28 CEDAR BERRY LN, DOVER PLAINS, NY 12522"},{lat:41.747504,lon:-73.603241,addr:"153 HALLS CORNERS RD, DOVER PLAINS, NY 12522"},{lat:41.712896,lon:-73.636416,addr:"53 HIGH MDWS, DOVER PLAINS, NY 12522"},{lat:41.712896,lon:-73.636416,addr:"53 HIGH MDWS, DOVER PLAINS, NY 12522"},{lat:41.995389,lon:-73.863865,addr:"941 ROUTE 199, RED HOOK, NY 12571"},{lat:41.999819,lon:-73.904097,addr:"1340 ROUTE 199, RED HOOK, NY 12571"},{lat:42.050642,lon:-73.50781,addr:"5 OLD POST RD, MILLERTON, NY 12546"},{lat:42.050654,lon:-73.507784,addr:"9 OLD POST RD, MILLERTON, NY 12546"},{lat:42.050654,lon:-73.507784,addr:"9 OLD POST RD, MILLERTON, NY 12546"},{lat:42.001114,lon:-73.573785,addr:"338 WINCHELL MOUNTAIN RD, MILLERTON, NY 12546"},{lat:41.510049,lon:-73.715337,addr:"164 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.510049,lon:-73.715337,addr:"164 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.510049,lon:-73.715337,addr:"164 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.510049,lon:-73.715337,addr:"171 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.510049,lon:-73.715337,addr:"171 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.510049,lon:-73.715337,addr:"171 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.510049,lon:-73.715337,addr:"184 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.510049,lon:-73.715337,addr:"188 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.510154,lon:-73.715358,addr:"224 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.496055,lon:-73.869196,addr:"431 E MOUNTAIN RD N, COLD SPRING, NY 10516"},{lat:40.959659,lon:-73.687015,addr:"21 VALLEY DR W, SHENOROCK, NY 10587"},{lat:40.959576,lon:-73.687038,addr:"8 ROGERS LN, SHENOROCK, NY 10587"},{lat:41.533526,lon:-73.732643,addr:"35 BAKERS LAKE RD, STORMVILLE, NY 12582"},{lat:41.480122,lon:-73.974734,addr:"7 CLEARWATER CT, BEACON, NY 12508"},{lat:41.576886,lon:-73.839899,addr:"33 FOX RD, HOPEWELL JUNCTION, NY 12533"},{lat:41.533753,lon:-73.88092,addr:"1010 JEFFERSON BLVD, FISHKILL, NY 12524"},{lat:41.533971,lon:-73.880684,addr:"1101 JEFFERSON BLVD, FISHKILL, NY 12524"},{lat:41.517955,lon:-73.738903,addr:"370 LEETOWN RD, STORMVILLE, NY 12582"},{lat:41.645076,lon:-73.847207,addr:"229 MALONEY RD, WAPPINGERS FALLS, NY 12590"},{lat:41.619929,lon:-73.796746,addr:"838 N HILLSIDE RD, WAPPINGERS FALLS, NY 12590"},{lat:41.538861,lon:-73.721897,addr:"279 OVERHILL RD, STORMVILLE, NY 12582"},{lat:41.540147,lon:-73.723847,addr:"PO BOX 207, STORMVILLE, NY 12582"},{lat:41.540147,lon:-73.723847,addr:"PO BOX 207, STORMVILLE, NY 12582"},{lat:41.540147,lon:-73.723847,addr:"PO BOX 207, STORMVILLE, NY 12582"},{lat:41.876674,lon:-73.621566,addr:"PO BOX 270, AMENIA, NY 12501"},{lat:41.876674,lon:-73.621566,addr:"PO BOX 461, AMENIA, NY 12501"},{lat:41.540147,lon:-73.723847,addr:"PO BOX 534, STORMVILLE, NY 12582"},{lat:41.876674,lon:-73.621566,addr:"PO BOX 651, AMENIA, NY 12501"},{lat:41.559711,lon:-73.738806,addr:"19 PRIMROSE LN, STORMVILLE, NY 12582"},{lat:41.895616,lon:-73.7876,addr:"869 PUMPKIN LN, CLINTON CORNERS, NY 12514"},{lat:41.510588,lon:-73.946107,addr:"50 ROUNDTREE CT, BEACON, NY 12508"},{lat:41.523071,lon:-73.974953,addr:"2 S STEUBEN CT, BEACON, NY 12508"},{lat:41.536468,lon:-73.708726,addr:"410 STORMVILLE MOUNTAIN RD, STORMVILLE, NY 12582"},{lat:41.638864,lon:-73.653913,addr:"92 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638864,lon:-73.653913,addr:"92 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.647298,lon:-73.738499,addr:"8 RUGAR RD, LAGRANGEVILLE, NY 12540"},{lat:41.562964,lon:-73.714096,addr:"351 S GREENHAVEN RD, STORMVILLE, NY 12582"},{lat:41.674788,lon:-73.710259,addr:"22 POTTERS LN, LAGRANGEVILLE, NY 12540"},{lat:41.747504,lon:-73.603241,addr:"285 HALLS CORNERS RD, DOVER PLAINS, NY 12522"},{lat:41.756152,lon:-73.606157,addr:"418 HALLS CORNERS RD, DOVER PLAINS, NY 12522"},{lat:41.732286,lon:-73.576996,addr:"145 HIGH MDWS, DOVER PLAINS, NY 12522"},{lat:41.995389,lon:-73.863865,addr:"811 ROUTE 199, RED HOOK, NY 12571"},{lat:41.995389,lon:-73.863865,addr:"1593 ROUTE 199, RED HOOK, NY 12571"},{lat:41.999819,lon:-73.904097,addr:"1060 ROUTE 199, RED HOOK, NY 12571"},{lat:41.988992,lon:-73.600412,addr:"584 PROSPECT HILL RD, PINE PLAINS, NY 12567"},{lat:42.000297,lon:-73.622934,addr:"0 SCHULTZ HILL RD, PINE PLAINS, NY 12567"},{lat:41.999517,lon:-73.625386,addr:"394 SCHULTZ HILL RD, PINE PLAINS, NY 12567"},{lat:41.501332,lon:-73.822522,addr:"4 LONG MOUNTAIN CT, HOPEWELL JUNCTION, NY 12533"},{lat:41.50059,lon:-73.727451,addr:"370 WHITE POND RD, STORMVILLE, NY 12582"},{lat:41.173866,lon:-73.719613,addr:"92 WESTWAY, MT KISCO, NY 10549"},{lat:40.959589,lon:-73.687035,addr:"10 OLD MILL RD, SHENOROCK, NY 10587"},{lat:41.765109,lon:-73.698471,addr:"626 OAK SUMMIT RD, MILLBROOK, NY 12545"},{lat:41.51646,lon:-73.690459,addr:"4080 ROUTE 52, HOLMES, NY 12531"},{lat:41.523082,lon:-73.979396,addr:"108 STERLING ST, BEACON, NY 12508"}]};
			var list2={list:[{lat:41.787574,lon:-73.554066,addr:"273 OLD RTE, WASSAIC, NY 12592"},{lat:41.787574,lon:-73.554066,addr:"273 OLD RTE, WASSAIC, NY 12592"},{lat:41.787574,lon:-73.554066,addr:"393 OLD RTE, WASSAIC, NY 12592"},{lat:41.757602,lon:-73.582692,addr:"90 TINKER TOWN RD, DOVER PLAINS, NY 12522"},{lat:41.639356,lon:-73.655074,addr:"18 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.63927,lon:-73.654811,addr:"21 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.63927,lon:-73.654811,addr:"21 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638767,lon:-73.653594,addr:"39 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638443,lon:-73.652866,addr:"50 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638443,lon:-73.652866,addr:"50 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638264,lon:-73.652376,addr:"57 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638264,lon:-73.652376,addr:"57 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.638154,lon:-73.651852,addr:"64 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.63935,lon:-73.650203,addr:"96 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.63935,lon:-73.650203,addr:"96 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.639964,lon:-73.661997,addr:"3 GAIL LN, POUGHQUAG, NY 12570"},{lat:41.638439,lon:-73.753544,addr:"75 BEAVER RD, LAGRANGEVILLE, NY 12540"},{lat:41.61866,lon:-73.752437,addr:"3 LAKEVIEW TER, HOPEWELL JUNCTION, NY 12533"},{lat:41.567312,lon:-73.709293,addr:"325 S GREENHAVEN RD, STORMVILLE, NY 12582"},{lat:41.567312,lon:-73.709293,addr:"325 S GREENHAVEN RD, STORMVILLE, NY 12582"},{lat:41.567027,lon:-73.71034,addr:"341 S GREENHAVEN RD, STORMVILLE, NY 12582"},{lat:41.637729,lon:-73.704946,addr:"5 DUNCAN RD, LAGRANGEVILLE, NY 12540"},{lat:41.745971,lon:-73.603164,addr:"5 CEDAR BERRY LN, DOVER PLAINS, NY 12522"},{lat:41.74551,lon:-73.602809,addr:"28 CEDAR BERRY LN, DOVER PLAINS, NY 12522"},{lat:41.735334,lon:-73.614301,addr:"153 HALLS CORNERS RD, DOVER PLAINS, NY 12522"},{lat:41.714897,lon:-73.637576,addr:"53 HIGH MDWS, DOVER PLAINS, NY 12522"},{lat:41.714897,lon:-73.637576,addr:"53 HIGH MDWS, DOVER PLAINS, NY 12522"},{lat:41.968061,lon:-73.80427,addr:"941 ROUTE 199, RED HOOK, NY 12571"},{lat:41.953574,lon:-73.771534,addr:"1340 ROUTE 199, RED HOOK, NY 12571"},{lat:41.995793,lon:-73.518621,addr:"5 OLD POST RD, MILLERTON, NY 12546"},{lat:41.995937,lon:-73.518507,addr:"9 OLD POST RD, MILLERTON, NY 12546"},{lat:41.995937,lon:-73.518507,addr:"9 OLD POST RD, MILLERTON, NY 12546"},{lat:41.966148,lon:-73.527825,addr:"338 WINCHELL MOUNTAIN RD, MILLERTON, NY 12546"},{lat:41.504352,lon:-73.713986,addr:"164 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.504352,lon:-73.713986,addr:"164 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.504352,lon:-73.713986,addr:"164 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.504718,lon:-73.714009,addr:"171 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.504718,lon:-73.714009,addr:"171 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.504718,lon:-73.714009,addr:"171 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.50538,lon:-73.714399,addr:"184 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.505579,lon:-73.714521,addr:"188 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.507514,lon:-73.714777,addr:"224 RESSIQUE ST, STORMVILLE, NY 12582"},{lat:41.493752,lon:-73.865028,addr:"431 E MOUNTAIN RD N, COLD SPRING, NY 10516"},{lat:41.321748,lon:-73.737753,addr:"21 VALLEY DR W, SHENOROCK, NY 10587"},{lat:41.321748,lon:-73.737753,addr:"8 ROGERS LN, SHENOROCK, NY 10587"},{lat:41.5339,lon:-73.732435,addr:"35 BAKERS LAKE RD, STORMVILLE, NY 12582"},{lat:41.480226,lon:-73.974788,addr:"7 CLEARWATER CT, BEACON, NY 12508"},{lat:41.576768,lon:-73.839972,addr:"33 FOX RD, HOPEWELL JUNCTION, NY 12533"},{lat:41.534803,lon:-73.879803,addr:"1010 JEFFERSON BLVD, FISHKILL, NY 12524"},{lat:41.534787,lon:-73.879264,addr:"1101 JEFFERSON BLVD, FISHKILL, NY 12524"},{lat:41.516788,lon:-73.739396,addr:"370 LEETOWN RD, STORMVILLE, NY 12582"},{lat:41.645495,lon:-73.846306,addr:"229 MALONEY RD, WAPPINGERS FALLS, NY 12590"},{lat:41.619951,lon:-73.795815,addr:"838 N HILLSIDE RD, WAPPINGERS FALLS, NY 12590"},{lat:41.538898,lon:-73.721685,addr:"279 OVERHILL RD, STORMVILLE, NY 12582"},{lat:41.5474,lon:-73.728409,addr:"PO BOX 207, STORMVILLE, NY 12582"},{lat:41.5474,lon:-73.728409,addr:"PO BOX 207, STORMVILLE, NY 12582"},{lat:41.5474,lon:-73.728409,addr:"PO BOX 207, STORMVILLE, NY 12582"},{lat:41.8573,lon:-73.553648,addr:"PO BOX 270, AMENIA, NY 12501"},{lat:41.8573,lon:-73.553648,addr:"PO BOX 461, AMENIA, NY 12501"},{lat:41.5474,lon:-73.728409,addr:"PO BOX 534, STORMVILLE, NY 12582"},{lat:41.8573,lon:-73.553648,addr:"PO BOX 651, AMENIA, NY 12501"},{lat:41.559768,lon:-73.73908,addr:"19 PRIMROSE LN, STORMVILLE, NY 12582"},{lat:41.895539,lon:-73.787573,addr:"869 PUMPKIN LN, CLINTON CORNERS, NY 12514"},{lat:41.510073,lon:-73.947337,addr:"50 ROUNDTREE CT, BEACON, NY 12508"},{lat:41.52275,lon:-73.97536,addr:"2 S STEUBEN CT, BEACON, NY 12508"},{lat:41.536972,lon:-73.709131,addr:"410 STORMVILLE MOUNTAIN RD, STORMVILLE, NY 12582"},{lat:41.639126,lon:-73.650298,addr:"92 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.639126,lon:-73.650298,addr:"92 SUSAN DR, POUGHQUAG, NY 12570"},{lat:41.647115,lon:-73.738162,addr:"8 RUGAR RD, LAGRANGEVILLE, NY 12540"},{lat:41.566731,lon:-73.710915,addr:"351 S GREENHAVEN RD, STORMVILLE, NY 12582"},{lat:41.638475,lon:-73.726358,addr:"22 POTTERS LN, LAGRANGEVILLE, NY 12540"},{lat:41.739862,lon:-73.609125,addr:"285 HALLS CORNERS RD, DOVER PLAINS, NY 12522"},{lat:41.74732,lon:-73.603267,addr:"418 HALLS CORNERS RD, DOVER PLAINS, NY 12522"},{lat:41.715058,lon:-73.642595,addr:"145 HIGH MDWS, DOVER PLAINS, NY 12522"},{lat:41.96981,lon:-73.816281,addr:"811 ROUTE 199, RED HOOK, NY 12571"},{lat:41.957197,lon:-73.748089,addr:"1593 ROUTE 199, RED HOOK, NY 12571"},{lat:41.963857,lon:-73.792006,addr:"1060 ROUTE 199, RED HOOK, NY 12571"},{lat:41.98233,lon:-73.612701,addr:"584 PROSPECT HILL RD, PINE PLAINS, NY 12567"},{lat:41.95544,lon:-73.608014,addr:"0 SCHULTZ HILL RD, PINE PLAINS, NY 12567"},{lat:41.972051,lon:-73.613567,addr:"394 SCHULTZ HILL RD, PINE PLAINS, NY 12567"},{lat:41.497999,lon:-73.820729,addr:"4 LONG MOUNTAIN CT, HOPEWELL JUNCTION, NY 12533"},{lat:41.51042,lon:-73.718173,addr:"370 WHITE POND RD, STORMVILLE, NY 12582"},{lat:41.198048,lon:-73.730889,addr:"92 WESTWAY, MT KISCO, NY 10549"},{lat:41.321748,lon:-73.737753,addr:"10 OLD MILL RD, SHENOROCK, NY 10587"},{lat:41.765044,lon:-73.69808,addr:"626 OAK SUMMIT RD, MILLBROOK, NY 12545"},{lat:41.517358,lon:-73.691558,addr:"4080 ROUTE 52, HOLMES, NY 12531"},{lat:41.522905,lon:-73.976768,addr:"108 STERLING ST, BEACON, NY 12508"}]};

			$(document).ready(function(){
				mfunc = function(dnum) {
					if(map != null) {
						layer.removeAllFeatures();
						markers.clearMarkers();
					}
					else {
						map = new OpenLayers.Map("mapdiv");

					}
					map.addLayer(new OpenLayers.Layer.OSM());
						map.setCenter(new OpenLayers.LonLat(-8426374.7674546, 5327280.0418454),7);
						
						layer = new OpenLayers.Layer.Vector("KML", {
							strategies : [ new OpenLayers.Strategy.Fixed() ],
							protocol : new OpenLayers.Protocol.HTTP({
								url : "kml/sd40.kml",
								format : new OpenLayers.Format.KML})});
						
						//allows cursor to click and navigate over kml layer
						layer.events.fallThrough = true;
						map.addLayer(layer);

						layer2 = new OpenLayers.Layer.Vector("KML", {
							strategies : [ new OpenLayers.Strategy.Fixed() ],
							protocol : new OpenLayers.Protocol.HTTP({
								url : "kml/sd41.kml",
								format : new OpenLayers.Format.KML})});
						
						//allows cursor to click and navigate over kml layer
						layer2.events.fallThrough = true;
						map.addLayer(layer2);


						lineLayer = new OpenLayers.Layer.Vector("Line Layer");
					      style = { strokeColor: '#0000ff',
					         strokeOpacity: 1,
					         strokeWidth: 1
					      };

					      map.addLayer(lineLayer);
						
							markers = new OpenLayers.Layer.Markers("Markers");
							map.addLayer(markers);
							//markers hide under the kml layer otherwise
							markers.setZIndex(layer.getZIndex() + layer2.getZIndex());
							for(var i in list.list) {
								var point = list.list[i];
								var tMarker = new OpenLayers.Marker(new OpenLayers.LonLat(point.lon, point.lat).transform(
										new OpenLayers.Projection("EPSG:4326"),
										map.getProjectionObject()));
								//assign the marker it's office information, used for
								//rendering the popups
								tMarker.point = point;
								tMarker.origin = "geocoder";
								
								markers.addMarker(tMarker);				
								tMarker.events.register('mousedown',tMarker, mousedown);

								var point2 = list2.list[i];
								var tMarker = new OpenLayers.Marker(new OpenLayers.LonLat(point2.lon, point2.lat).transform(
										new OpenLayers.Projection("EPSG:4326"),
										map.getProjectionObject()));
								//assign the marker it's office information, used for
								//rendering the popups
								tMarker.point = point2;
								tMarker.origin = "yahoo";
								
								markers.addMarker(tMarker);				
								tMarker.events.register('mousedown',tMarker, mousedown);

								 var points = new Array();

							      points[0] =new OpenLayers.LonLat(point.lon,point.lat ).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());;
							      points[0] = new OpenLayers.Geometry.Point(points[0].lon,points[0].lat);

							      points[1] = new OpenLayers.LonLat(point2.lon,point2.lat ).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());;
							      points[1] = new OpenLayers.Geometry.Point(points[1].lon,points[1].lat);

							      var linear_ring = new OpenLayers.Geometry.LinearRing(points);
							      polygonFeature = new OpenLayers.Feature.Vector(
							         new OpenLayers.Geometry.Polygon([linear_ring]), null, style);
							         lineLayer.addFeatures([polygonFeature]);
							}
						
						//the attribution by default hangs out in an awkward part of the map
						$('.olControlAttribution').css('bottom','0px');
				}();
			});
			
			function mousedown(evt) {
				destroyLocalPopup();
				
				var o = evt.object.point;
				console.log(o);
				popup = new OpenLayers.Popup.FramedCloud("data", 
						new OpenLayers.LonLat(o.lon, o.lat).transform(
								new OpenLayers.Projection("EPSG:4326"),
								map.getProjectionObject()),
								new OpenLayers.Size(200,200), 
								evt.object.origin + ": " + o.addr, 
								null, true, function() {popup.toggle();});
				markers.map.addPopup(popup);
				
				OpenLayers.Event.stop(evt);
			}
			
			function destroyLocalPopup() {
				if(popup != null) {
					popup.destroy();
		            popup = null;
		        }
			}
		</script>
	</body>
</html>