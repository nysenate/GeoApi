var api = "/GeoApi/api/json/";
var districts = "districts/extended?";
var geocode = "geocode/extended?";
var revgeo = "revgeo/";
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
	$(".message_list .message_head").append("&nbsp;&nbsp;<img src='down.gif'></img>");
	
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
	
	$("#validate").click(function() {
		$(".response_body").html("<img src='r.gif'>");
		var url = api + validate;
		var $inputs = $("#validateForm :input");
		$inputs.each(function() {
			url = url + buildUrl(this.name, $(this).val());
		});
		$.getJSON(url, function(data) {
			if(data.message != null) {
				$(".response_body").html("There was an issue processing your request because necessary " +
					"information is missing or the address you entered is invalid.");
			}
			else {
				if(data.address1 != null) {
					$(".response_body").html("<label>Address1:</label> " + data.address1 + "<br />");
					$(".response_body").append("<label>Address2:</label> " + data.address2 + "<br />");
				}
				else {
					$(".response_body").html("<label>Address:</label> " + data.address2 + "<br />");
				}
				$(".response_body").append("<label>City:</label> " + data.city + "<br />");
				$(".response_body").append("<label>State:</label> " + data.state + "<br />");
				$(".response_body").append("<label>Zip5:</label> " + data.zip5 + "<br />");
				$(".response_body").append("<label>Zip4:</label> " + data.zip4 + "<br />");
				var address = data.address2 + ", " + data.city + ", " + data.state + " " + data.zip5;
				addAddressToMap(address);
			}
		});
		return false;
	});

	$("#districts").click(function() {
		$(".response_body").html("<img src='r.gif'>");
		var url = api + districts;
		
		var $inputs = $("#districtsForm :input");
		$inputs.each(function() {
			url = url + buildUrl(this.name, $(this).val());
		});
		
		$.getJSON(url, function(data) {
			if(data.message != null) {
				$(".response_body").html("There was an issue processing your request because necessary " +
					"information is missing or the address you entered is invalid.");
			}
			else {
				$(".response_body").html("<label>Latitude:</label> " + data.lat + "<br />");
				$(".response_body").append("<label>Longitude:</label> " + data.lon + "<br />");
				$(".response_body").append("<label>Address:</label> " + data.address + "<br />");
				$(".response_body").append(data.assembly.district + "<br />");
				$(".response_body").append(data.congressional.district + "<br />");
				$(".response_body").append(data.county.countyName + "<br />");
				$(".response_body").append(data.election.district + "<br />");
				$(".response_body").append(data.senate.district + "<br />");
				$(".response_body").append("FIPS Code: "+data.census.fips + "<br />");

				var latlon = new google.maps.LatLng(data.lat, data.lon);

				addLatLonToMap(latlon, data.address);
			}
		});
		
		return false;
	});
	
	$("#revgeo").click(function() {
		$(".response_body").html("<img src='r.gif'>");
		var url = api + revgeo;
		
		var $inputs = $("#revgeoForm :input");
		$inputs.each(function() {
			if(this.name == "lat") {
				url = url + $(this).val() + ",";
			}
			if(this.name == "lon") {
				url = url + $(this).val();
			}
		});

		$.getJSON(url, function(data) {
			if(data.message != null) {
				$(".response_body").html("There was an issue processing your request because necessary " +
					"information is missing or the address you entered is invalid.");
			}
			else {
				$(".response_body").html("<label>Latitude:</label> " + data[0].lat + "<br />");
				$(".response_body").append("<label>Longitude:</label> " + data[0].lon + "<br />");
				$(".response_body").append("<label>Address:</label> " + data[0].address + "<br />");
				
				var latlon = new google.maps.LatLng(data[0].lat, data[0].lon);

				addLatLonToMap(latlon, data[0].address);
			}
		});
		
		return false;
	});
	
	$("#zip").click(function() {
		$(".response_body").html("<img src='r.gif'>");
		var url = api + cityState;
		
		var $inputs = $("#zipForm :input");
		$inputs.each(function() {
			url = url + buildUrl(this.name, $(this).val());
		});
		
		$.getJSON(url, function(data) {
			if(data.message != null) {
				$(".response_body").html("There was an issue processing your request because necessary " +
					"information is missing or the address you entered is invalid.");
			}
			else {
				$(".response_body").html("<label>City:</label> " + data.city + "<br />");
				$(".response_body").append("<label>State:</label> " + data.state + "<br />");
				$(".response_body").append("<label>Zip5:</label> " + data.zip5 + "<br />");
				
				var address = data.city + " " + data.state + " " + data.zip5;
				addAddressToMap(address);
			}
		});
		
		return false;
	});
});