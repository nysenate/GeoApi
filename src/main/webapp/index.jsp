<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script type="text/javascript">
contextPath = "<%=request.getContextPath()%>";
</script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.min.js"></script>
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.color.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/app.js"></script>

<link href="<%=request.getContextPath()%>/style.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>SAGE (Senate Address GeoCoding Engine)</title>
</head>
<body onload="initialize();">
  <div class="response_body">
  </div>
  
  <div id="map_canvas">
  </div>

<div class="message_list"> 
    <p class="message_head"><cite>District Information</cite></p> 
    <div class="message_body"> 
        <form id="districtsForm" action="" method="post">
          <ol>
            <li>
              <label>Address</label>
              <input type="text" placeholder="street address..." name="addr2">
            </li>
            <li>
              <label>City</label>
              <input type="text" name="city">
            </li>
            <li>
              <label>State</label>
              <input type="text" placeholder="e.g. NY" name="state" maxlength="2">
            </li>
            <li>
              <label>Zip5</label>
              <input type="text" name="zip5" maxlength="5">
            </li>
            <li>
              <label>Zip4</label>
              <input type="text" name="zip4" maxlength="4">
            </li>
            <li>
              <label>Service</label>
              <select name="service">
                <option>Yahoo</option>
                <option>Mapquest</option>
                <option>OSM</option>
                <option>GeoCoder</option>
              </select>
            </li>
            <li id="send">
              <input type="submit" id="districts" name="submit">
            </li>
          </ol>
        </form>
    </div>

    <p class="message_head"><cite>Reverse GeoCode</cite></p> 
    <div class="message_body"> 
        <form id="revgeoForm" action="" method="post">
          <ol>
            <li>
              <label>Latitude</label>
              <input type="text" name="lat">
            </li>
            <li>
              <label>Longitude</label>
              <input type="text" name="lon">
            </li>
            <li>
              <label>Service</label>
              <select name="service">
                <option>Yahoo</option>
                <option>Google</option>
                <option>Bing</option>
              </select>
            </li>
            <li id="send">
              <input type="submit" id="revgeo" name="submit">
            </li>
          </ol>
        </form>
    </div> 

    <p class="message_head"><cite>Street Lookup</cite></p> 
    <div class="message_body"> 
      <form id="streetLookupForm" action="" method="post">
          <ol>
            <li>
              <label>Zip5</label>
              <input type="text" name="zip">
            </li>
            <li id="send">
              <input type="submit" id="streetLookup" name="submit">
            </li>
          </ol>
        </form>
    </div>

    <p class="message_head"><cite>City/State Lookup</cite></p> 
    <div class="message_body"> 
      <form id="citystateLookupForm" action="" method="post">
          <ol>
            <li>
              <label>Zip5</label>
              <input type="text" name="zip">
            </li>
            <li id="send">
              <input type="submit" id="citystateLookup" name="submit">
            </li>
          </ol>
        </form>
    </div>

    <p class="message_head"><cite>About</cite></p> 
    <div class="message_body">
      Geocoding for this service is provided primarily 
      by <a href="http://developer.yahoo.com/geo/placefinder/">Yahoo! PlaceFinder</a>, 
      but geocoding services from <a href="http://code.google.com/apis/maps/index.html">Google</a> 
      and <a href="http://www.microsoft.com/maps/developers/web.aspx">Bing</a> are also available 
      for testing purposes.
    </div>
    
</div> 

</body>
</html>
