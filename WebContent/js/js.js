var api = "/GeoApi/api/json/";
var districts = "districts/extended?";
var geocode = "geocode/extended?";
var revgeo = "revgeo/latlon/";
var validate = "validate/extended?";
var cityState = "/citystatelookup/extended?";

var geocoder;
var map;

function initialize() {
    geocoder = new google.maps.Geocoder();
    var latlng = new google.maps.LatLng(42.651445, -73.755254);
    var myOptions = {
      zoom: 12,
      center: latlng,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
}

function addAddressToMap(address) {
	initialize();
	geocoder.geocode( { 'address': address}, function(results, status) {
	if (status == google.maps.GeocoderStatus.OK) {
		map.setCenter(results[0].geometry.location);
		updateMap(results[0].geometry.location, address);
	}});
}

function addLatLonToMap(latlon, address) {
	initialize();
	map.setCenter(latlon);
	updateMap(latlon,address);
}

function updateMap(latlon, address) {
	var marker = new google.maps.Marker({
		map: map, 
		position: latlon,
		title: address});
	var infowindow = new google.maps.InfoWindow({
		content: address,
		size: new google.maps.Size(50,50)});
	google.maps.event.addListener(marker, 'click', function() {
		infowindow.open(map,marker);});	
}

function buildUrl(name, val) {
	var ret = null;
	if(name == "service") {
		if(val != "(Optional)") {
			ret = name + '=' + val + '&';
		}
	}
	else {
		if(name != "submit") {
			ret = name + '=' + val + '&';
		}
	}
	if(ret == null) {
		return "";
	}
	return ret;
}

$(document).ready(function(){
	
	var menu_pos = 0;
	
	//initialize page
	$(".message_list .message_body:gt(0)").hide(500);
	$(".message_list .message_head:lt(1)").css("backgroundColor","#9D9D9D");
	$(".message_list .message_head:gt(0)").animate({opacity: 0.6},500);
	$(".message_list .message_head").append("&nbsp;&nbsp;<img src='img/down.gif'></img>");
	
	//toggle message_body
	$(".message_head").click(function(){
		$(".message_list .message_body").hide(500);
		$(".message_list .message_head").css("backgroundColor","#eee");
		$(".message_list .message_head").css("opacity",0.6);

		$(this).next(".message_body").slideToggle(500);
		$(this).css("background","#9D9D9D");
		$(this).css("opacity",1.0);
		
		menu_pos = $(this).index();
		return false;
	});
	
	$(".message_head").hover(
		function(){
			if($(this).index() != menu_pos) {
				$(this).stop().animate({
					opacity: 1.0,
					backgroundColor: "#9D9D9D"
				},100);
			}
		},
		function() {
			if($(this).index() != menu_pos) {
				$(this).stop().animate({
					opacity: 0.6,
					backgroundColor: "#eee"
				},100);
			}
	});

	$("#districts").click(function() {
		$(".response_body").html("<img src='img/r.gif'>");
		var url = api + districts;
		
		var validateUrl = api+validate;
		
		var $inputs = $("#districtsForm :input");
		$inputs.each(function() {
			url = url + buildUrl(this.name, $(this).val());
			validateUrl = validateUrl + buildUrl(this.name, $(this).val());
		});
		
		var validatedAddr = null;
		$.getJSON(validateUrl,function(data){
			validatedAddr = data;
		});
		
		$.getJSON(url, function(data) {
			$(".response_body").hide();
			if(data.message != null) {
				$(".response_body").html("There was an issue processing your request because necessary " +
					"information is missing or the address you entered is invalid.");
			}
			else {
				var nearby = null;
				if(data.senate.nearbyDistricts[0] != null) {
					var nBLen = data.senate.nearbyDistricts.length;
					var nBCount = 1;
					for(district in data.senate.nearbyDistricts) {
						if(nearby != null) {
							if(nBCount == nBLen) {
								nearby += " and ";
							}
							else {
								nearby += ", ";
							}
						}
						else {
							nearby = "";
						}
						nearby += "<a href=\"" + data.senate.nearbyDistricts[district].districtUrl 
						+ "\">" + data.senate.nearbyDistricts[district].district + "</a>";
						nBCount++;

					}
				}
				$(".response_body").html(
					"<div class = \"response_data\">"
						+ "<ol>"
							+ "<li>Latitude: " + data.lat + "</li>"
							
							+ "<li>Longitude: " + data.lon + "</li>"
							
							+ (validatedAddr.message == null ? 
									"<li>Address: " + validatedAddr.address2
									+", " + validatedAddr.city
									+", " + validatedAddr.state
									+" " + validatedAddr.zip5 + "-" +validatedAddr.zip4
									:"<li>Address: " + data.address + "</li>")
							
							+ "<li><table cellspacing=\"10\">"
								+ "<tr>"
									+ "<td rowspan=3><img src=\"" + data.senate.senator.imageUrl + "\" alt=\"" + data.senate.senator.name + "\"></img></td>"
									+ "<td><a href=\"" + data.senate.districtUrl + "\">" + data.senate.district + "</a></td>"
								+ "</tr>"
							+ "<tr>"
								+ "<td><a href=\"" + data.senate.senator.url + "\">" + data.senate.senator.name + "</a></td>"
							+ "</tr>"
							+ "<tr>"
								+ "<td>" + data.senate.senator.contact + "</td>"
							+ "</tr>"
							+ "</table>"
							+ (nearby != null ?
									"<div style=\"text-align:center;font-size: 75%;border-width:1px;border-style:dotted;\" class=\"nearby_districts\">You're also very close to "
									+ nearby
									:"")
							+ "</li>"
							
							+ "<li>" + data.assembly.district + " - <a href=\"" + data.assembly.member.url + "\">" + data.assembly.member.name + "</a></li>"
							
							+ "<li>" + data.congressional.district + " - <a href=\"" + data.congressional.member.url + "\">" + data.congressional.member.name + "</a></li>"
							
							+ "<li>" + data.county.countyName + "</li>"
							+ "<li>" + data.election.district + "</li>"
							+ "<li><label>Census FIPS: </label>" + data.census.fips + "</li>"
						+ "</ol>"
					+ "</div>");

				var latlon = new google.maps.LatLng(data.lat, data.lon);

				addLatLonToMap(latlon, data.address);
				
			}
			$(".response_body").show(400);
		});
		
		return false;
	});
	
	$("#revgeo").click(function() {
		$(".response_body").html("<img src='img/r.gif'>");
		var url = api + revgeo;
		
		var $inputs = $("#revgeoForm :input");
		
		var rg_lat;
		var rg_lon;
		$inputs.each(function() {
			if(this.name == "lat") {
				rg_lat = $(this).val();
			}
			if(this.name == "lon") {
				rg_lon = $(this).val();
			}
		});
		
		url = url + rg_lat + "," + rg_lon;

		$.getJSON(url, function(data) {
			$(".response_body").hide();
			if(data.message != null) {
				$(".response_body").html("There was an issue processing your request because necessary " +
					"information is missing or the address you entered is invalid.");
			}
			else {
				
				$(".response_body").html(
						"<div class = \"response_data\">"
						+ "<ol>"
							+ "<li>Latitude: " + data[0].lat + "</li>"
							+ "<li>Longitude: " + data[0].lon + "</li>"
							+ "<li>Address: " + data[0].address + "</li>"
						+ "</ol>"
					+ "</div>");
				
				var latlon = new google.maps.LatLng(data[0].lat, data[0].lon);

				addLatLonToMap(latlon, data[0].address);
			}
			$(".response_body").show(400);
		});
		
		return false;
	});
});