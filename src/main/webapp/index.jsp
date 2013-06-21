<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="sage" tagdir="/WEB-INF/tags" %>

<sage:wrapper>
    <jsp:attribute name="ngApp">sage</jsp:attribute>
    <jsp:attribute name="title">SAGE</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyC-vIdRb4DI5jzKI92UNTnjHiwU7P0GqxI&sensor=false"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/vendor/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/vendor/blockui.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/app/app.js"></script>
    </jsp:attribute>
    <jsp:body>
    <div id="contentwrapper">
        <div style="width:100%" id="header" ng-controller="MenuController">
            <sage:logo></sage:logo>
            <ul class="top-method-header">
                <li><a class="active" ng-click="toggleMethod(1)">District Assign</a></li>
                <li><a ng-click="toggleMethod(2)">District Maps</a></li>
                <li><a ng-click="toggleMethod(3)">Street Finder</a></li>
                <li ng-click="toggleMethod(4)"><a>Reverse Geocode</a></li>
                <li ng-click="toggleMethod(5)"><a>City/State</a></li>
                <li><a href="${contextPath}/job">Batch</a></li>
                <li><a href="https://sage-senate-address-geocoding-engine.readthedocs.org/en/latest/">Developer API</a></li>
            </ul>
        </div>

        <div id="contentcolumn">
            <div id="mapView" ng-controller="EmbeddedMapViewController">
                <div class="top-header autohide">
                    <div class="large-icon icon-map white"></div>
                    <div class="text">{{mapTitle}}</div>
                </div>
                <div id="map_canvas"></div>
            </div>
            <div id="streetView" ng-show="visible" ng-controller="StreetLookupController">
                <div style="height:100%;">
                    <div class="top-header autohide">
                        <div class="large-icon icon-database white"></div>
                        <div class="text">Board of Elections Street Lookup</div>
                    </div>
                    <div id="streetLookupSearch" class="search-container small">
                        <form id="streetLookupForm" action="" method="post">
                            <div class="icon-location icon-teal"></div>
                            <label>Enter a zipcode to find streets</label>
                            <div style='margin-top:2px'>
                                <input type="text" ng-model="zip5" style="width:175px" maxlength="5" placeholder="e.g. 12210"/>
                                <button ng-click="lookup()" class="submit mini">
                                    <div class="icon-search icon-white-no-hover"></div>
                                    <span></span>
                                </button>
                            </div>
                        </form>
                        <div id="streetSearchFilter" ng-show="showFilter">
                            <div class="icon-list icon-teal"></div>
                            <label for="street-search">Filter by street</label>
                            <div style='margin-top:2px'>
                                <input id="street-search" type="text" style="width:175px"/>
                            </div>
                        </div>
                        <div style="clear:both"></div>
                    </div>
                    <div style="padding:10px;" ng-controller="StreetViewController">
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
                    <label for="addressInput" ng-hide="minimized">Enter an address to district assign</label>
                    <label ng-show="minimized" ng-click="minimized=false;" class="expand-search">Show search</label>
                    <div ng-click="minimized=true;" ng-hide="minimized" class="collapse-search icon-arrow-up4 icon-hover-teal small-right-icon"></div>
                    <div ng-click="minimized=false;" ng-show="minimized" class="expand-search icon-arrow-down4 icon-hover-teal small-right-icon"></div>
                    <br/>
                    <div class="search-container-content">
                        <div style='margin-top:5px'>
                            <input id="addressInput" type="text" ng-model="addr" placeholder="e.g. 200 State St, Albany NY"/>
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
                                    <td><label for="dataSourceMenu">District data source</label></td>
                                    <td style="width:180px;">
                                        <select id="dataSourceMenu" style="width: 100%;" ng-model="provider">
                                            <option value="default">Default (Recommended)</option>
                                            <option value="streetfile">Board of Elections</option>
                                            <option value="shapefile">Census TIGER/LINE</option>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <td><label for="geocoderMenu">Geocoder</label></td>
                                    <td>
                                        <select id="geocoderMenu" style="width: 100%;" ng-model="geoProvider">
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
                    </div>
                </form>
            </div>
            <div id="districtMapViewSearch" class="search-container" ng-show="visible" ng-controller="DistrictMapController">
                <form id="districtMapFormMini" action="" method="post">
                    <div class="icon-list icon-teal"></div>
                    <label ng-hide="minimized">Select which district(s) to display</label>
                    <label ng-show="minimized" ng-click="minimized=false;" class="expand-search">Show search</label>
                    <div ng-click="minimized=true;" ng-hide="minimized" class="collapse-search icon-arrow-up4 icon-hover-teal small-right-icon"></div>
                    <div ng-click="minimized=false;" ng-show="minimized" class="expand-search icon-arrow-down4 icon-hover-teal small-right-icon"></div>
                    <br/>
                    <div class="search-container-content">
                        <div class="section">
                            <div style="float:left">
                                <label for="districtTypeMenu" class="menu-overhead">Type</label>
                                <select id="districtTypeMenu" class="menu" style="width:85px;" ng-model="type" ng-change="metaLookup();">
                                    <option value="senate">Senate</option>
                                    <option value="congressional">Congressional</option>
                                    <option value="assembly">Assembly</option>
                                    <option value="county">County</option>
                                    <option value="town">Town</option>
                                    <option value="school">School</option>
                                </select>
                            </div>
                            <div style="float:left">
                                <label for="districtCodeMenu" class="menu-overhead">District</label>
                                <select id="districtCodeMenu" class="menu" style="width:190px;" ng-model="selectedDistrict" ng-options="d.name for d in districtList"></select>
                            </div>
                            <div style="float:left">
                                <label class="menu-overhead">&nbsp;</label>
                                <button class="submit mini compact" ng-click="lookup();">
                                    <div class="icon-search icon-white-no-hover"></div>
                                </button>
                            </div>
                        </div>
                        <a ng-show="showMemberOption" ng-click="showMemberList=true;showMemberOption=false;" class="options-link" ng-click="">Show Senator/Member List</a>
                        <a ng-show="showMemberList" ng-click="showMemberList=false;showMemberOption=true;" class="options-link" ng-click="">Hide Senator/Member List</a>
                        <div style="border-top:1px solid #ddd;margin-top:4px;padding:5px;" ng-show="showMemberList">
                            <div style="float:left">
                                <label for="districtMemberMenu" class="menu-overhead">Member</label>
                                <select id="districtMemberMenu" class="menu" style="width:280px;" ng-model="selectedDistrict" ng-options="d.member.name for d in districtList"></select>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div id="cityStateSearch" class="search-container small" ng-show="visible" ng-controller="CityStateController">
                <form id="cityStateForm" action="" method="post">
                    <div class="icon-location icon-teal"></div>
                    <label ng-hide="minimized" for="cityStateInput">Enter a zipcode</label>
                    <label ng-show="minimized" ng-click="minimized=false;" class="expand-search">Show search</label>
                    <div ng-click="minimized=true;" ng-hide="minimized" class="collapse-search icon-arrow-up4 icon-hover-teal small-right-icon"></div>
                    <div ng-click="minimized=false;" ng-show="minimized" class="expand-search icon-arrow-down4 icon-hover-teal small-right-icon"></div>
                    <div style='margin-top:2px' class="search-container-content">
                        <input id="cityStateInput" type="text" ng-model="zip5" style="width:175px" maxlength="5" placeholder="e.g. 12210"/>
                        <button ng-click="lookup()" class="submit mini">
                            <div class="icon-search icon-white-no-hover"></div>
                            <span></span>
                        </button>
                    </div>
                </form>
            </div>
            <div id="reverseGeocodeSearch" ng-show="visible" class="search-container small" ng-controller="RevGeoController">
                <form id="revGeoForm" action="" method="post">
                    <div class="icon-target icon-teal"></div>
                    <label ng-hide="minimized">Enter geo-coordinate</label>
                    <label ng-show="minimized" ng-click="minimized=false;" class="expand-search">Show search</label>
                    <div ng-click="minimized=true;" ng-hide="minimized" class="collapse-search icon-arrow-up4 icon-hover-teal small-right-icon"></div>
                    <div ng-click="minimized=false;" ng-show="minimized" class="expand-search icon-arrow-down4 icon-hover-teal small-right-icon"></div>
                    <br/>
                    <div class="section search-container-content">
                        <div style="float:left">
                            <label for="revGeoLatInput" class="menu-overhead">Latitude</label>
                            <input id="revGeoLatInput" type="text" style="width:80px;margin-right:5px;" ng-model="lat" name="lat">
                        </div>
                        <div style="float:left">
                            <label for="revGeoLonInput" class="menu-overhead">Longitude</label>
                            <input id="revGeoLonInput" type="text" style="width:80px;margin-right:5px;" ng-model="lon" name="lon">
                        </div>
                        <div style="float:left">
                            <label class="menu-overhead">&nbsp;</label>
                            <button class="submit mini" ng-click="lookup();">
                                <div class="icon-search icon-white-no-hover"></div>
                            </button>
                        </div>
                    </div>
                </form>
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
                        <div class="info-container" ng-show="geocoded">
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
                    </div>
                </div>
            </div>
            <div id="citystate-results" ng-controller="CityStateView">
                <div ng-show="visible">
                    <div class="info-container">
                        <p ng-hide="error" class="member-name">{{city}}, {{state}} {{zip5}}</p>
                        <p ng-show="error" style="color:orangered;">No city/state result!</p>
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
