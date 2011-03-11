<html>
	<body>
		<% if(request.getParameter("y") != null) { %>
			<div id="mapdiv" style="height:<%=request.getParameter("y") %>px;width:<%=request.getParameter("x")%>px;"></div>
		<% }
		else { %>
					<div id="mapdiv" ></div><!-- style="height:600px;width:860px;" -->
		<% } %>
		
		<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.js"></script>
		<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
		<script>
			var districtCache = new Array();
			var map;
			var selectControl;
			var vector = new Array();
			$(document).ready(function(){
				map = new OpenLayers.Map("mapdiv");
				map.addLayer(new OpenLayers.Layer.OSM());
				map.setCenter(new OpenLayers.LonLat(-8426374.7674546, 5327280.0418454),7);
				
				for(var i = 1; i <= 62; i++) {
					var layer = new OpenLayers.Layer.Vector("KML", {
						strategies : [ new OpenLayers.Strategy.Fixed() ],
						protocol : new OpenLayers.Protocol.HTTP({
							url : "kml/sd" + i + ".kml",
							format : new OpenLayers.Format.KML})});
					
					//allows cursor to click and navigate over kml layer
					layer.sd = i;
					layer.events.fallThrough = true;
					vector.push(layer);
				}
				
				map.addLayers(vector);
				map.addControl(new OpenLayers.Control.LayerSwitcher());
				selectControl = new OpenLayers.Control.SelectFeature(
					vector,
					{
						clickout: true, toggle: false,
						multiple: false, hover: false,
						onSelect: featureSelected, onUnselect:featureUnselected
					}
				);
				selectControl.events.fallThrough = true;
				
				map.addControl(selectControl);
				selectControl.activate();
				
				function onPopupClose(feature) {
					//map.removePopup(feature.object);
					selectControl.unselectAll();
				}
				
				function featureSelected(feature) {
					var sd = feature.layer.sd;
					getSenateDistrict(feature, writePopup, sd);
				};
				
				function writePopup(feature, callback, sd, json) {
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
				
				function featureUnselected(feature) {
					map.removePopup(feature.popup);
					feature.popup.destroy();
					feature.popup = null;
				};
				
				function getSenateDistrict(feature, callback, sd) {
					if(districtCache[sd]) {
						callback(feature, callback, sd, districtCache[sd]);
					}
					else {
						$.getJSON("json/sd" + sd + ".json", function(data) {
							districtCache[sd] = data;
							callback(feature, callback, sd, data);
						});
					}
					
				}
			});
		</script>
	</body>
</html>