<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head> 
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/> 
    <title>Google Maps API Example: GGeoXml KML Overlay</title> 
    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=ABQIAAAAzr2EBOXUKnm_jVnk0OJI7xSosDVG8KKPE1-m51RBrvYughuyMxQ-i1QfUnH94QxWIa6N4U6MouMmBA"
      type="text/javascript"></script> 
    <script type="text/javascript"> 
   
    var map;
    var geoXml; 
    var toggleState = 1;
 
    function initialize() {
      if (GBrowserIsCompatible()) {
        geoXml = new GGeoXml("http://geo.nysenate.gov/examples/asd1.kml");
        map = new GMap2(document.getElementById("map_canvas")); 
        map.setCenter(new GLatLng(41.875696,-87.624207), 11); 
        map.setUIToDefault();
        map.addOverlay(geoXml);
      }
    } 
 
    function toggleMyKml() {
      if (toggleState == 1) {
        map.removeOverlay(geoXml);
        toggleState = 0;
      } else {
        map.addOverlay(geoXml);
        toggleState = 1;
      }
    }
	</script> 
  </head> 
 
  <body onload="initialize()"> 
    <div id="map_canvas" style="width: 640px; height: 480px; float:left; border: 1px solid black;"></div> 
    </div> 
    <br clear="all"/> 
    <br/> 
    <input type="button" value="Toggle KML" onClick="toggleMyKml();"/> 
  </body> 
</html>