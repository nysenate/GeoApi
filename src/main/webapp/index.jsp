<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="sage" tagdir="/WEB-INF/tags" %>

<sage:wrapper>
    <jsp:attribute name="title">SAGE</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script type="text/javascript" charset="utf8" src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="js/blockui.js"></script>
        <script type="text/javascript" src="app.js"></script>
    </jsp:attribute>
    <jsp:body>
    <div id="contentwrapper">
        <div id="contentcolumn">
            <div id="mapView" ng-controller="EmbeddedMapViewController">
                <div class="top-header">
                    <div class="large-icon icon-map white"></div>
                    <div class="text">Map | {{header}}</div>
                </div>
                <div id="map_canvas"></div>
            </div>
            <div ng-controller="StreetViewController">
                <div ng-show="visible" style="height:100%;">
                    <div class="top-header">
                        <div class="icon" aria-hidden="true">&#128248;</div>
                        <div class="text">Street File Results</div>
                    </div>
                    <div style="padding:10px;">
                        <div class="street-search-filter">
                            <label>Filter by street name: </label>
                            <input id="street-search" type="text" />
                        </div>
                        <table id="street-view-table" my-table="overrideOptions" aa-data="streets"
                               ao-column-defs="columnDefs" aa-sorting="sortDefault">
                            <thead>
                            <th>From Bldg</th>
                            <th>To Bldg</th>
                            <th style="width:300px;">Street</th>
                            <th>Location</th>
                            <th>Zip</th>
                            <th>Senate</th>
                            <th>Assembly</th>
                            <th>Congress</th>
                            <th>County</th>
                            <th>Town</th>
                            <th>Election</th>
                            </thead>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="leftcolumn">
        <div class="innertube">
            <div class="top-header">
                <div class="icon-earth large-icon teal"></div>
                <div id="sage-logo-text">SAGE</div>
            </div>
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
                            <select style="width: 98px; color:#444;" ng-model="provider">
                                <option value="default">Default</option>
                                <option value="streetfile">Streetfile</option>
                                <option value="shapefile">Shapefile</option>
                                <option value="geoserver">Geoserver</option>
                            </select>
                            <select style="width: 98px; color:#444;" ng-model="geoProvider">
                                <option value="default">Default</option>
                                <option value="tiger">Tiger</option>
                                <option value="yahoo">Yahoo</option>
                                <option value="mapquest">MapQuest</option>
                                <option value="osm">OSM</option>
                            </select>
                        </li>
                        <li>
                            <button ng-click="lookup()" class="submit">
                                <div class="icon-search"></div>
                                <span>Find Districts</span>
                            </button>
                        </li>
                    </ol>

                </form>
            </div>
            <p class="method-header teal">District Maps</p>
            <div id="district-mapview-container" class="form-container">
                <form id="mapForm" action="" method="post" ng-controller="DistrictMapController" autocomplete="false">
                    <ol class="input-container">
                        <li>
                            <label>Type</label>
                            <select ng-model="type">
                                <option value="senate">Senate</option>
                                <option value="congressional">Congressional</option>
                                <option value="assembly">Assembly</option>
                                <option value="county">County</option>
                                <option value="town">Town</option>
                                <option value="school">School</option>
                            </select>
                        </li>
                        <li>
                            <label style="width:190px;">District (leave blank to view all)</label>
                            <input style="width:52px;" ng-model="district" type="text" id="districtCodeInput" />
                        </li>
                        <li>
                            <button class="submit" ng-click="lookup();">
                                <div class="icon-search"></div>
                                <span>Show Map</span>
                            </button>
                        </li>
                    </ol>
                </form>
            </div>
            <p class="method-header teal">Street Finder</p>
            <div id="street-lookup-container" class="form-container">
                <form id="street-lookup-form" action="" method="post" ng-controller="StreetLookupController">
                    <ol class="input-container">
                        <li>
                            <label>Zip5</label>
                            <input ng-model="zip5" type="text" name="zip">
                        </li>
                        <li>
                            <button class="submit" ng-click="lookup();">
                                <span aria-hidden="true" data-icon="&#128269;" class="search-icon"></span>
                                <span>Find Streets</span>
                            </button>
                        </li>
                    </ol>
                </form>
            </div>
            <p class="method-header teal">Reverse Geocode</p>
            <div id="reverse-geocode-container" class="form-container" ng-controller="RevGeoController">
                <form id="revgeo-form" action="" method="post">
                    <ol class="input-container">
                        <li>
                            <label>Latitude</label>
                            <input type="text" ng-model="lat" name="lat">
                        </li>
                        <li>
                            <label>Longitude</label>
                            <input type="text" ng-model="lon" name="lon">
                        </li>
                        <li>
                            <button class="submit" ng-click="lookup();">
                                <span aria-hidden="true" data-icon="&#128269;" class="search-icon"></span>
                                <span>Find Address</span>
                            </button>
                        </li>
                    </ol>
                </form>
            </div>
            <p class="method-header teal">City/State Lookup</p>
            <div id="citystate-lookup-container" ng-controller="CityStateController" class="form-container">
                <form id="citystate-lookup-form" action="" method="post">
                    <ol class="input-container">
                        <li>
                            <label>Zip5</label>
                            <input ng-model="zip5" maxlength="5" type="text" name="zip">
                        </li>
                        <button ng-click="lookup()" class="submit">
                            <span aria-hidden="true" data-icon="&#128269;" class="search-icon"></span>
                            <span>Find City/State</span>
                        </button>
                    </ol>
                </form>
            </div>
            <a href="${contextPath}/job"><p class="method-header teal">Batch Jobs</p></a>
        </div>
    </div>

    <div id="rightcolumn" ng-controller="ResultsViewController">
        <div class="innertube">
            <div class="result-header success" style="cursor:auto;height:20px;">
                <div style="float:left;">Results</div>
                <div ng-click="toggleResultPane(false);" class="icon-cross small-right-icon"></div>
            </div>
            <div id="district-results" ng-controller="DistrictsViewController">
                <div ng-show="visible">
                    <div id="success-district-results" ng-show="districtAssigned">
                        <div class="info-container senator">
                            <div class="senator-pic-holder">
                                <img ng-src="{{districts.senate.senator.imageUrl}}" class="senator-pic">
                            </div>
                            <div>
                                <p class="senator member-name">
                                    <a target="_blank" ng-href="{{districts.senate.senator.url}}">{{districts.senate.senator.name}}</a>
                                </p>
                                <table style="width:225px;">
                                    <tr>
                                        <td><p class="senate district">Senate District {{districts.senate.district}}</p></td>
                                        <td class="right-icon-placeholder">
                                            <a title="Show Map" ng-click="showDistrict('senate');">
                                                <div class="icon-map"></div>
                                            </a>
                                        </td>
                                    </tr>
                                </table>
                                <br/>
                                <p class="member-email"><div class="icon-mail" style="margin-right: 5px;position: relative;top: 3px;"></div>{{districts.senate.senator.email}}</p>
                            </div>
                        </div>
                        <div class="info-container" style="padding:5px 10px;">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <a ng-hide="showOffices" ng-click="showOffices=true;">View Senator Office Locations</a>
                                        <a ng-show="showOffices" ng-click="showOffices=false;">Hide Senator Office Locations</a>
                                    </td>
                                    <td class="right-icon-placeholder">
                                        <a ng-hide="showOffices" ng-click="showOffices=true;"><div class="icon-arrow-down"></div></a>
                                        <a ng-show="showOffices" ng-click="showOffices=false;"><div class="icon-arrow-up"></div></a>
                                    </td>
                                </tr>
                            </table>
                            <div ng-show="showOffices" ng-repeat="office in districts.senate.senator.offices">
                                <div style="padding:5px;border-top:1px solid #ddd;" ng-show="office.name">
                                    <table style="width:100%">
                                        <tr>
                                            <td><p style="font-size:18px;color:teal;">{{office.name}}</p></td>
                                            <td class="right-icon-placeholder">
                                                <a title="Locate office" ng-click="setOfficeMarker(office);">
                                                    <div class="icon-location"></div>
                                                </a>
                                            </td>
                                        </tr>
                                    </table>

                                    <p>{{office.street}}</p>
                                    <p>{{office.additional}}</p>
                                    <p>{{office.city}}, {{office.province}} {{office.postalCode}}</p>
                                    <p>Phone {{office.phone}}</p>
                                </div>
                            </div>
                        </div>
                        <div class="info-container" ng-show="districts.senate.nearBorder" style="padding:5px 10px;">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <a ng-hide="showNeighbors" ng-click="showNeighborDistricts('Senate', districts.senate.neighbors)">View Neighbor Senate District</a>
                                        <a ng-show="showNeighbors" ng-click="hideNeighborDistricts()">Hide Neighbor Senate District</a>
                                    </td>
                                    <td class="right-icon-placeholder">
                                        <a ng-hide="showNeighbors" ng-click="showNeighborDistricts('Senate', districts.senate.neighbors)"><div class="icon-arrow-down"></div></a>
                                        <a ng-show="showNeighbors" ng-click="hideNeighborDistricts()"><div class="icon-arrow-up"></div></a>
                                    </td>
                                </tr>
                            </table>
                            <div ng-show="showNeighbors">
                                <div style="padding:5px;border-top:1px solid #ddd;" ng-repeat="neighbor in districts.senate.neighbors">
                                    <div class="senator">
                                        <div class="senator-pic-holder" style="width:50px;height:50px;">
                                            <img ng-src="{{neighbor.member.imageUrl | senatorPic}}" class="senator-pic">
                                        </div>
                                        <div>
                                            <p class="senator member-name">
                                                <a target="_blank" ng-href="{{neighbor.member.url}}">{{neighbor.member.name}}</a>
                                            </p>
                                            <p class="senate district" ng-style="neighbor.style">Senate District {{neighbor.district}}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                        </div>
                        <div class="info-container">
                            <table style="width:100%">
                                <tr>
                                    <td><div class="icon-location"></div></td>
                                    <td><p style="font-size: 16px;color:#111;" ng-bind-html-unsafe="address | addressFormat"></p>
                                    </td>
                                </tr>
                                <tr>
                                    <td><div class="icon-target"></div></td>
                                    <td><p style="font-size: 16px;color:teal;">({{geocode.lat | number:6}}, {{geocode.lon | number:6}}) <small style="float:right;">{{geocode.method | remove:'Dao'}}</small></p></td>
                                </tr>
                            </table>
                        </div>
                        <div class="info-container congressional">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <p class="member-name"><a target="_blank" ng-href="{{districts.congressional.member.url}}">{{districts.congressional.member.name}}</a></p>
                                        <p class="district">Congressional District {{districts.congressional.district}}</p>
                                    </td>
                                    <td class="right-icon-placeholder">
                                        <a title="Show Map" ng-click="showDistrict('congressional');">
                                            <div class="icon-map"></div>
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div class="info-container assembly">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <p class="member-name"><a target="_blank" ng-href="{{districts.assembly.member.url}}">{{districts.assembly.member.name}}</a></p>
                                        <p class="district">Assembly District {{districts.assembly.district}}</p>
                                    </td>
                                    <td class="right-icon-placeholder">
                                        <a title="Show Map" ng-click="showDistrict('assembly');">
                                            <div class="icon-map"></div>
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div class="info-container">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <p class="member-name">{{districts.county.name}}</p>
                                        <p class="district">County Code: {{districts.county.district}}</p>
                                    </td>
                                    <td class="right-icon-placeholder">
                                        <a title="Show Map" ng-click="showDistrict('county');">
                                            <div class="icon-map"></div>
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div class="info-container" ng-show="districts.town.district">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <p class="member-name">Town of {{districts.town.name}}</p>
                                        <p class="district">Town Code: {{districts.town.district}}</p></td>
                                    <td class="right-icon-placeholder">
                                        <a title="Show Map" ng-click="showDistrict('town');">
                                            <div class="icon-map"></div>
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div class="info-container" ng-show="districts.school.district">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <p class="member-name">{{districts.school.name}}</p>
                                        <p class="district">School District Code: {{districts.school.district}}</p>
                                    </td>
                                    <td class="right-icon-placeholder">
                                        <a title="Show Map" ng-click="showDistrict('school');">
                                            <div class="icon-map"></div>
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div id="failed-district-result" ng-hide="districtAssigned">
                        <div class="info-container">
                            <p class="member-name" style="color:orangered;">No District Results</p>
                            <span>{{description}}</span>
                        </div>
                    </div>
                </div>
            </div>
            <div id="citystate-results" ng-controller="CityStateView">
                <div ng-show="visible">
                    <div class="info-container">
                        <p class="member-name">{{city}}, {{state}} {{zip5}}</p>
                    </div>
                </div>
            </div>
            <div id="map-member-results" ng-controller="MemberViewController">
                <div ng-show="visible">
                    <div>
                        <div class="info-container senator">
                            <div class="senator-pic-holder">
                                <img ng-src="{{member.imageUrl}}" class="senator-pic">
                            </div>
                            <div>
                                <p class="senator member-name">
                                    <a target="_blank" ng-href="{{member.url}}">{{member.name}}</a>
                                </p>
                                <p class="senate district">Senate District {{member.district.number}}</p><br/>
                                <p class="member-email">
                                    <div class="icon-email"></div>{{member.email}}
                                </p>
                            </div>
                        </div>

                        <div class="info-container" ng-repeat="office in member.offices">
                            <div ng-show="office.name">
                                <table style="width:100%">
                                    <tr>
                                        <td><p style="font-size:18px;color:teal;">{{office.name}}</p></td>
                                        <td class="right-icon-placeholder">
                                            <a title="Locate office" ng-click="setOfficeMarker(office);">
                                                <div class="icon-location"></div>
                                            </a>
                                        </td>
                                    </tr>
                                </table>

                                <p>{{office.street}}</p>
                                <p>{{office.additional}}</p>
                                <p>{{office.city}}, {{office.province}} {{office.postalCode}}</p>
                                <p>Phone {{office.phone}}</p>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
            <div id="rev-geo-results" ng-controller="RevGeoViewController">
                <div ng-show="visible">
                    <div ng-show="revGeocoded">
                        <div class="info-container">
                            <p style="color:teal;">Reverse Geocoded Address</p>
                        </div>
                        <div class="info-container" >
                            <table style="width:100%">
                                <tr>
                                    <td><div class="icon-location"></div></td>
                                    <td><p style="font-size: 16px;color:#111;" ng-bind-html-unsafe="address | addressFormat"></p>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div id="failed-geocode-result" ng-hide="revGeocoded">
                        <div class="info-container">
                            <p class="member-name" style="color:orangered;">No Reverse Geocode Result</p>
                            <span>{{description}}</span>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>
    </jsp:body>
</sage:wrapper>
