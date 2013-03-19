<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html ng-app="sage">
<head>
    <title>SAGE - Senate Address Geocoding Engine</title>
    <link rel="stylesheet" type="text/css" href="main.css" />
    <link rel="stylesheet" type="text/css" href="css/normalize.css" />
    <script type="text/javascript"> contextPath = "<%=request.getContextPath()%>"; </script>
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
    <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyC-vIdRb4DI5jzKI92UNTnjHiwU7P0GqxI&sensor=false"></script>
    <script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.0.4/angular.min.js"></script>
    <script type="text/javascript" src="app.js"></script>
</head>
<body>
<div id="maincontainer">
    <div id="topsection">
        <div class="innertube">
            <h1 class="logo">SAGE</h1>
            <ul class="nav">
                <li><a>API Reference</a></li>
                <li><a>Batch Request</a></li>
            </ul>
        </div>
    </div>

    <div id="contentwrapper">
        <div id="contentcolumn">
            <div style="display:none" class="result-header success">District Maps </div>
            <div id="map_canvas" ng-controller="MapViewController"></div>
        </div>
    </div>

    <div id="leftcolumn">
        <div class="innertube">
            <p class="method-header active teal">District Information</p>
            <div id="district-lookup-container" class="form-container active">
                <form id="districtsForm" action="" method="post" ng-controller="DistrictInfoController" autocomplete="false">
                    <ol class="input-container">
                        <li>
                            <label>Address</label>
                            <input type="text" placeholder="street address..." name="addr1" ng-model="addr1" autocomplete="false">
                        </li>
                        <li>
                            <label>City</label>
                            <input type="text" name="city" ng-model="city">
                        </li>
                        <li>
                            <label>State</label>
                            <input type="text" placeholder="e.g. NY" name="state" maxlength="2" ng-model="state" autocomplete="false">
                        </li>
                        <li>
                            <label>Zip5</label>
                            <input type="text" name="zip5" maxlength="5" ng-model="zip5" autocomplete="false">
                        </li>
                        <li>
                            <label style="color:#444;">Zip4</label>
                            <input type="text" name="zip4" maxlength="4" ng-model="zip4" autocomplete="false">
                        </li>
                        <li>
                            <label style="color:#444">Method</label>
                            <select style="width: 98px; color:#444;" ng-model="geoProvider">
                                <option value="default">Default</option>
                                <option value="tiger">Tiger</option>
                                <option value="yahoo">Yahoo</option>
                                <option value="mapquest">MapQuest</option>
                                <option value="osm">OSM</option>
                            </select>
                            <select style="width: 98px; color:#444;" ng-model="provider">
                                <option value="default">Default</option>
                                <option value="streetfile">Streetfile</option>
                                <option value="shapefile">Shapefile</option>
                                <option value="geoserver">Geoserver</option>
                            </select>
                        </li>
                        <li>
                            <button ng-click="lookup()" class="submit">
                                <span aria-hidden="true" data-icon="&#128269;" class="search-icon"></span>
                                <span>Find Districts</span>
                            </button>
                        </li>
                    </ol>

                </form>
            </div>
            <p class="method-header maroon">Reverse Geocode</p>
            <div id="reverse-geocode-container" class="form-container">
                <form id="revgeo-form" action="" method="post">
                    <ol class="input-container">
                        <li>
                            <label>Latitude</label>
                            <input type="text" name="lat">
                        </li>
                        <li>
                            <label>Longitude</label>
                            <input type="text" name="lon">
                        </li>
                        <li>
                            <label>Method</label>
                            <select name="service">
                                <option>Yahoo</option>
                                <option>YahooBoss</option>
                                <option>Google</option>
                                <option>Bing</option>
                            </select>
                        </li>
                        <li>
                            <button class="submit">
                                <span aria-hidden="true" data-icon="&#128269;" class="search-icon"></span>
                                <span>Find Address</span>
                            </button>
                        </li>
                    </ol>
                </form>
            </div>
            <p class="method-header green">Street Lookup</p>
            <div id="street-lookup-container" class="form-container">
                <form id="street-lookup-form" action="" method="post">
                    <ol class="input-container">
                        <li>
                            <label>Zip5</label>
                            <input type="text" name="zip">
                        </li>
                        <li>
                            <button class="submit">
                                <span aria-hidden="true" data-icon="&#128269;" class="search-icon"></span>
                                <span>Find Streets</span>
                            </button>
                        </li>
                    </ol>
                </form>
            </div>
            <p class="method-header purple">City/State Lookup</p>
            <div id="citystate-lookup-container" class="form-container">
                <form id="citystate-lookup-form" action="" method="post">
                    <ol class="input-container">
                        <li>
                            <label>Zip5</label>
                            <input type="text" name="zip">
                        </li>
                        <button class="submit">
                            <span aria-hidden="true" data-icon="&#128269;" class="search-icon"></span>
                            <span>Find City/State</span>
                        </button>
                    </ol>
                </form>
            </div>
            <p class="method-header brown">About</p>
        </div>
    </div>

    <div id="rightcolumn" >
        <div class="innertube">
            <div style="display:none">
                The Senate Address Geocoding Engine is an open source web service that provides geocoding and district
                mapping solutions for the NY State Senate. Geocoding services are provided by third party vendors as
                well as from Census TIGER line sources.

                https://github.com/nysenate/GeoApi
            </div>

            <p class="result-header success">Results</p>
            <div id="district-results" ng-controller="DistrictsViewController" ng-show="districtAssigned">
                <div class="info-container senator">
                    <div style="float:left;margin-right:10px;margin-bottom:5px;">
                        <img ng-src={{districts.senate.senator.imageUrl}} width="90px" height="120px" class="senator-pic">
                    </div>
                    <div style="padding:10px;">
                        <p class="senator-name">
                            <a target="_blank" ng-href="{{districts.senate.senator.url}}">{{districts.senate.senator.name}}</a>
                        </p>
                        <p class="senate-district">Senate District {{districts.senate.district}}</p><br/>
                        <p class="senator-email"><span aria-hidden="true" data-icon="&#9993;" style="color:teal;"></span>{{districts.senate.senator.email}}</p>
                        <div class="senator-social">
                            <p class="senator-email" >
                                <a target="_blank" ng-href="{{districts.senate.senator.social.facebook}}">
                                    <span aria-hidden="true" data-socialicon="&#62222;" class="social" style="color:teal;"></span>Facebook Page
                                </a>
                            </p>
                        </div>
                    </div>
                    <hr style="clear:left;"/>
                </div>
                <div class="info-container">
                    <p>{{geocode.lat}}, {{geocode.lon}}</p>
                    <p>{{address.addr1}}, {{address.city}}, {{address.state}} {{address.zip5}}</p>
                </div>
                <div class="info-container congressional">
                    <p>Congressional District {{districts.congressional.district}}</p>
                    <p>
                        <a target="_blank" ng-href="{{districts.congressional.member.url}}">{{districts.congressional.member.name}}</a>
                    </p>
                </div>
                <div class="info-container assembly">
                    <p>Assembly District {{districts.assembly.district}}</p>
                    <p>
                        <a target="_blank" ng-href="{{districts.assembly.member.url}}">{{districts.assembly.member.name}}</a>
                    </p>
                </div>
                <div class="info-container">
                    <p>{{districts.county.name}}</p>
                    <p>County Code: {{districts.county.district}}</p>
                </div>
                <div class="info-container">
                    <p>{{districts.school.name}}</p>
                    <p>School District Code: {{districts.school.district}}</p>
                </div>
                <div class="info-container">
                    <p>Election District Code: {{districts.election.district}}</p>
                </div>
            </div>
        </div>
    </div>
</div>
</div>
</body>
</html>