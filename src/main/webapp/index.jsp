<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="sage" tagdir="/WEB-INF/tags" %>

<sage:wrapper>
    <jsp:attribute name="ngApp">sage</jsp:attribute>
    <jsp:attribute name="title">SAGE</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/vendor/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/vendor/blockui.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/app/app.js"></script>
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
            <div id="streetView" ng-controller="StreetViewController">
                <div ng-show="visible" style="height:100%;">
                    <div class="top-header">
                        <div class="large-icon icon-database white"></div>
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
                            <th>Congress</th>
                            <th>Assembly</th>
                            <th>County</th>
                            <th>Town</th>
                            <th>Election</th>
                            </thead>
                        </table>
                    </div>
                </div>
            </div>
            <div id="districtInfoSearch" class="search-container" ng-controller="DistrictInfoController" ng-show="visible">
                <form id="districtsFormMini" action="" method="post">
                    <div class="icon-directions icon-teal"></div>
                    <label>Enter an address to district assign</label>
                    <div style='margin-top:2px'>
                        <input type="text" ng-model="addr" placeholder="e.g. 200 State St, Albany NY"/>
                        <button ng-click="lookup()" class="submit mini">
                            <div class="icon-search icon-white-no-hover"></div>
                            <span></span>
                        </button>
                    </div>
                    <a id="showDistrictInfoOptions" class="options-link" ng-hide="showOptions" ng-click="showOptions=true">Show options</a>
                    <a id="hideDistrictInfoOptions" class="options-link" ng-show="showOptions" ng-click="showOptions=false">Hide options</a>
                    <br/>
                    <div id="districtInfoOptions" ng-show="showOptions">
                        <table class="options-table">
                            <tr>
                                <td><label>District data source</label></td>
                                <td style="width:180px;">
                                    <select style="width: 100%;" ng-model="provider">
                                        <option value="default">Default (Recommended)</option>
                                        <option value="streetfile">Board of Elections</option>
                                        <option value="shapefile">Census TIGER/LINE</option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td><label>Geocoder</label></td>
                                <td>
                                    <select style="width: 100%;" ng-model="geoProvider">
                                        <option value="default">Default (Recommended)</option>
                                        <option value="yahoo">Yahoo</option>
                                        <option value="yahooboss">Yahoo Boss</option>
                                        <option value="tiger">Tiger</option>
                                        <option value="mapquest">MapQuest</option>
                                        <option value="ruby">Ruby</option>
                                        <option value="osm">OSM</option>
                                    </select>
                                </td>
                            </tr>
                        </table>
                    </div>
                </form>
            </div>
            <div id="districtMapViewSearch" class="search-container" ng-show="visible" ng-controller="DistrictMapController">
                <form id="districtMapFormMini" action="" method="post">
                    <div class="icon-list icon-teal"></div>
                    <label>Select which district(s) to display</label>
                    <div style="margin-right: 10px" class="icon-arrow-up2 icon-hover-teal small-right-icon"></div>
                    <br/>
                    <section style="border-top:1px solid #ddd;margin-top:4px;padding:5px;">
                        <div style="float:left">
                            <label style="display:block">Type</label>
                            <select style="display:block;width:170px;height:35px;" ng-model="type" ng-change="metaLookup();">
                                <option value="senate">Senate</option>
                                <option value="congressional">Congressional</option>
                                <option value="assembly">Assembly</option>
                                <option value="county">County</option>
                                <option value="town">Town</option>
                                <option value="school">School</option>
                            </select>
                        </div>
                        <div style="float:left">
                            <label style="display:block">District</label>
                            <select style="display:block;width:110px;height:35px;" ng-model="selectedDistrict" ng-options="d.district for d in senateDistricts"></select>
                        </div>
                        <div style="float:left">
                            <label style="display:block;">&nbsp;</label>
                            <button style="display:block;" class="submit mini" ng-click="lookup();">
                                <div class="icon-search icon-white-no-hover"></div>
                            </button>
                        </div>
                    </section>
                </form>
            </div>
        </div>
    </div>

    <div id="leftcolumn">
        <div class="innertube">
            <sage:header></sage:header>
            <div ng-controller="MenuController">
                <p class="method-header active teal" ng-click="toggleView(1)">District Information</p>
                <p class="method-header teal" ng-click="toggleView(2)">District Maps</p>
                <p class="method-header teal" ng-click="toggleView(3)">Street Finder</p>
                <div id="street-lookup-container" class="form-container">
                    <form id="street-lookup-form" action="" method="post" ng-controller="StreetLookupController">
                        <ol class="input-container">
                            <li>
                                <label>Zip5</label>
                                <input ng-model="zip5" type="text" name="zip" maxlength="5">
                            </li>
                            <li>
                                <button class="submit" ng-click="lookup();">
                                    <div class="icon-search"></div>
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
                                    <div class="icon-search"></div>
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
                                <div class="icon-search"></div>
                                <span>Find City/State</span>
                            </button>
                        </ol>
                    </form>
                </div>
                <a href="${contextPath}/job"><p class="method-header teal">Batch Jobs</p></a>
                <a href="https://sage-senate-address-geocoding-engine.readthedocs.org/en/latest/"><p class="method-header teal">API Reference</p></a>
            </div>
        </div>
    </div>

    <div ng-click="toggleResultPane(true);" id="showResultsTab" class="result-header success" ng-controller="ResultsViewController">
        <div style="float:left;">Show Results</div>
        <div class="icon-arrow-down2 icon-hover-white small-right-icon"></div>
    </div>

    <div id="rightcolumn" ng-controller="ResultsViewController">
        <div class="innertube">
            <div ng-click="toggleResultPane(false);" id="resultsTab" class="result-header success">
                <div style="float:left;">Results</div>
                <div class="icon-arrow-up2 icon-hover-white small-right-icon"></div>
            </div>
            <div id="district-results" ng-controller="DistrictsViewController">
                <div ng-show="visible">
                    <div id="success-district-results" ng-show="districtAssigned">
                        <div class="info-container clickable senator" title="Show Senate District Map" ng-click="showDistrict('senate');">
                            <div class="senator-pic-holder">
                                <a target="_blank" ng-href="{{districts.senate.senator.url}}">
                                    <img ng-src="{{districts.senate.senator.imageUrl | senatorPic}}" class="senator-pic">
                                </a>
                            </div>
                            <div style='margin-top:10px'>
                                <table style="width:230px;">
                                    <tr>
                                        <td>
                                            <p class="senator member-name">
                                                <a target="_blank" ng-href="{{districts.senate.senator.url}}">{{districts.senate.senator.name}}</a>
                                            </p>
                                            <p class="senate district">Senate District {{districts.senate.district}}</p>
                                        </td>
                                        <td class="right-icon-placeholder">
                                            <a title="Show Map" ng-show="districts.senate.map" ng-click="showDistrict('senate');">
                                                <div class="icon-map"></div>
                                            </a>
                                        </td>
                                    </tr>
                                </table>
                                <br/>
                            </div>
                        </div>

                        <div class="info-container slim">
                            <p class="member-email">
                                <div class="icon-mail icon-teal" style="margin-right: 5px;"></div>
                                <span style='font-size:15px'>{{districts.senate.senator.email}}</span>
                            </p>
                        </div>

                        <div class="info-container slim">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <a ng-hide="showOffices" ng-click="showOffices=true;">Senator Office Locations</a>
                                        <a ng-show="showOffices" ng-click="showOffices=false;">Senator Office Locations</a>
                                    </td>
                                    <td class="right-icon-placeholder">
                                        <a ng-hide="showOffices" ng-click="showOffices=true;"><div class="icon-arrow-down4 icon-hover-teal"></div></a>
                                        <a ng-show="showOffices" ng-click="showOffices=false;"><div class="icon-arrow-up4 icon-hover-teal"></div></a>
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
                                                    <div class="icon-location icon-hover-teal"></div>
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
                                        <a ng-hide="showNeighbors" ng-click="showNeighborDistricts('Senate', districts.senate.neighbors)">Neighbor Senate Districts</a>
                                        <a ng-show="showNeighbors" ng-click="hideNeighborDistricts()">Neighbor Senate Districts</a>
                                    </td>
                                    <td class="right-icon-placeholder">
                                        <a ng-hide="showNeighbors" ng-click="showNeighborDistricts('Senate', districts.senate.neighbors)"><div class="icon-arrow-down4 icon-hover-teal"></div></a>
                                        <a ng-show="showNeighbors" ng-click="hideNeighborDistricts()"><div class="icon-arrow-up4 icon-hover-teal"></div></a>
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
                                    <td><div class="icon-location icon-teal"></div></td>
                                    <td><p style="font-size: 16px;color:#111;" ng-bind-html-unsafe="address | addressFormat"></p></td>
                                </tr>
                                <tr>
                                    <td><div class="icon-target icon-teal"></div></td>
                                    <td><p style="font-size: 16px;color:teal;">({{geocode.lat | number:6}}, {{geocode.lon | number:6}}) <small style="float:right;">{{geocode.method | remove:'Dao'}}</small></p></td>
                                </tr>
                            </table>
                        </div>
                        <div class="info-container clickable congressional" ng-show="districts.congressional.district" title="Show Congressional District Map" ng-click="showDistrict('congressional');">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <p class="member-name"><a target="_blank" ng-href="{{districts.congressional.member.url}}">{{districts.congressional.member.name}}</a></p>
                                        <p class="district">Congressional District {{districts.congressional.district}}</p>
                                    </td>
                                    <td class="right-icon-placeholder">
                                        <a title="Show Map" ng-show="districts.congressional.map" ng-click="showDistrict('congressional');">
                                            <div class="icon-map"></div>
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div class="info-container clickable assembly" title="Show Assembly Map" ng-show="districts.assembly.district" ng-click="showDistrict('assembly');">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <p class="member-name"><a target="_blank" ng-href="{{districts.assembly.member.url}}">{{districts.assembly.member.name}}</a></p>
                                        <p class="district">Assembly District {{districts.assembly.district}}</p>
                                    </td>
                                    <td class="right-icon-placeholder">
                                        <a title="Show Map" ng-show="districts.assembly.map" ng-click="showDistrict('assembly');">
                                            <div class="icon-map"></div>
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div class="info-container clickable" title="Show County Map" ng-show="districts.county.district" ng-click="showDistrict('county');">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <p class="member-name">{{districts.county.name}}</p>
                                        <p class="district">County Code: {{districts.county.district}}</p>
                                    </td>
                                    <td class="right-icon-placeholder">
                                        <a title="Show Map" ng-show="districts.county.map" ng-click="showDistrict('county');">
                                            <div class="icon-map"></div>
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div class="info-container clickable" ng-show="districts.town.district" title="Show Town Map" ng-click="showDistrict('town');">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <p class="member-name" ng-show="districts.town.name">Town of {{districts.town.name}}</p>
                                        <p class="district">Town Code: {{districts.town.district}}</p></td>
                                    <td class="right-icon-placeholder">
                                        <a title="Show Map" ng-show="districts.town.map" ng-click="showDistrict('town');">
                                            <div class="icon-map"></div>
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div class="info-container clickable" ng-show="districts.school.district" title="Show School District Map" ng-click="showDistrict('school');">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <p class="member-name">{{districts.school.name}}</p>
                                        <p class="district">School District Code: {{districts.school.district}}</p>
                                    </td>
                                    <td class="right-icon-placeholder">
                                        <a title="Show Map" ng-show="districts.school.map" ng-click="showDistrict('school');">
                                            <div class="icon-map"></div>
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="info-container congressional" ng-show="districts.election.district">
                        <p class="district">Election District: {{districts.election.district}}</p>
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
                        <div class="info-container" style="height:70px;">
                            <div class="senator-pic-holder">
                                <img ng-src="{{member.imageUrl | senatorPic}}" class="senator-pic">
                            </div>
                            <div style="margin-top:10px">
                                <p class="senator member-name">
                                    <a target="_blank" ng-href="{{member.url}}">{{member.name}}</a>
                                </p>
                                <p class="senate district">Senate District {{member.district.number}}</p><br/>
                            </div>
                        </div>

                        <div class="info-container slim">
                            <p class="member-email">
                                <div class="icon-mail icon-teal" style="margin-right:10px;"></div>{{member.email}}
                            </p>
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
                                    <td><div class="icon-location icon-teal"></div></td>
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
