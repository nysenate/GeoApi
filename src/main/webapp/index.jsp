<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="gov.nysenate.sage.config.ApplicationFactory" %>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<fmt:setLocale value = "es_ES"/>
<% request.setAttribute("amsUrl", ApplicationFactory.getConfig().getValue("usps.ams.ui.url")); %>
<% request.setAttribute("activeGeocoders", ApplicationFactory.getActiveGeoProviders()); %>

<sage:wrapper>
    <jsp:attribute name="ngApp">sage</jsp:attribute>
    <jsp:attribute name="title">SAGE</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/vendor/jquery.dataTables-1.9.4.css">
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?v=3&key=AIzaSyC-vIdRb4DI5jzKI92UNTnjHiwU7P0GqxI&libraries=places"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/vendor/jquery.dataTables-1.9.4.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/vendor/blockui.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/app.js"></script>
    </jsp:attribute>
    <jsp:body>
    <div id="contentwrapper">
        <div id="header" ng-controller="MenuController">
            <sage:logo></sage:logo>
            <ul class="top-method-header">
                <li>
                    <a class="active" ng-click="toggleMethod(1)">
                        <div ng-show="index == 1" class="icon-white-no-hover icon-search"></div>&nbsp;&nbsp;District Lookup
                    </a>
                </li>
                <li>
                    <a ng-click="toggleMethod(2)">
                        <div ng-show="index == 2" class="icon-white-no-hover icon-map"></div>&nbsp;&nbsp;District Maps
                    </a>
                </li>
                <li>
                    <a ng-click="toggleMethod(6)">
                        <div ng-show="index == 6" class="icon-white-no-hover icon-mail"></div>&nbsp;&nbsp;USPS Lookup
                    </a>
                </li>
                <li>
                    <a ng-click="toggleMethod(3)">
                        <div ng-show="index == 3" class="icon-white-no-hover icon-directions"></div>&nbsp;&nbsp;Street Finder
                    </a>
                </li>
                <li ng-click="toggleMethod(4)">
                    <a>
                        <div ng-show="index == 4" class="icon-white-no-hover icon-target"></div>&nbsp;&nbsp;Reverse Geocode
                    </a>
                </li>
                <li>
                    <a href="${contextPath}/job">Batch
                    </a>
                </li>
                <li>
                    <a href="${contextPath}/docs/html/index.html">Developer API
                    </a>
                </li>
            </ul>
        </div>

        <div id="contentcolumn" style="display:none;">
            <div id="mapView" ng-controller="EmbeddedMapViewController">
                <div id="map_canvas"></div>
            </div>
            <div id="streetView" ng-show="visible" ng-controller="StreetLookupController">
                <div id="streetViewFrame">
                    <div id="streetLookupSearch">
                        <h3 class='section-title street-finder'>Board of Elections Street Finder</h3>
                        <hr class='section-title-hr'/>
                        <div style='padding:10px;'>
                            <p>SAGE provides a database of NYS street ranges that are associated with legislative district codes.</p>
                            <p>You can begin your search by entering the Zip 5 code to obtain a listing of street records.</p><br/>
                            <form id="streetLookupForm" action="" method="post">
                                <label>Zip 5</label>
                                <div style='margin-top:2px'>
                                    <input type="text" ng-model="zip5" style="width:175px" maxlength="5" placeholder="e.g. 12210"/>
                                    <button ng-click="lookup()" class="submit mini">
                                        <div class="icon-search icon-white-no-hover"></div>
                                        <span></span>
                                    </button>
                                </div>
                            </form>
                            <div id="streetSearchFilter" ng-show="showFilter">
                                <label for="street-search">Filter by street</label>
                                <div style='margin-top:2px'>
                                    <input id="street-search" type="text" style="width:175px"/>
                                </div>
                            </div>
                            <div style='clear:both;'></div>
                            <div id="streetViewTableContainer" ng-controller="StreetViewController">
                                <table id="street-view-table" my-table="overrideOptions" aa-data="streets"
                                       ao-column-defs="columnDefs" aa-sorting="sortDefault">
                                    <thead>
                                    <th>From Bldg</th>
                                    <th>To Bldg</th>
                                    <th style="width:170px">Street</th>
                                    <th>E/O</th>
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
                </div>
            </div>
            <div id="districtInfoSearch" class="search-container" ng-controller="DistrictInfoController" ng-show="visible">
                <form id="districtsFormMini" method="post" ng-submit="lookup()">
                    <div class="icon-directions icon-teal"></div>
                    <label for="addressInput" ng-hide="minimized">Enter an address for district lookup</label>
                    <label ng-show="minimized" ng-click="minimized=false;" class="expand-search">Show search</label>
                    <div ng-click="minimized=true;" ng-hide="minimized" class="collapse-search icon-arrow-up4 icon-hover-teal small-right-icon"></div>
                    <div ng-click="minimized=false;" ng-show="minimized" class="expand-search icon-arrow-down4 icon-hover-teal small-right-icon"></div>
                    <br/>
                    <div class="search-container-content">
                        <div style='margin-top:5px'>
                            <input id="addressInput" type="text" ng-model="addr" placeholder="e.g. 200 State St, Albany NY 12210"/>
                            <button class="submit mini">
                                <div class="icon-search icon-white-no-hover"></div>
                                <span></span>
                            </button>
                        </div>

                        <div class="options-link-container" ng-hide="showOptions" ng-click="showOptions=true" style="padding-left:5px;border-right:1px solid #ddd">
                            <a class="options-link">Options</a>
                        </div>
                        <div class="options-link-container" ng-show="showOptions" ng-click="showOptions=false" style="padding-left:5px;border-right:1px solid #ddd">
                            <a class="options-link">Hide options</a>
                        </div>

                        <div class="options-link-container" ng-hide="showHelp" ng-click="showHelp=true" style="padding-left:10px;">
                            <a class="options-link">Help</a>
                        </div>
                        <div class="options-link-container" ng-show="showHelp" ng-click="showHelp=false" style="padding-left:10px;">
                            <a class="options-link">Hide help</a>
                        </div>

                        <br/>
                        <div id="districtInfoOptions" style="margin-top:10px;" ng-show="showOptions">
                            <table class="options-table">
                                <tr>
                                    <td><label for="dataSourceMenu">District data source</label></td>
                                    <td style="width:180px;">
                                        <select id="dataSourceMenu" style="width: 100%;" ng-model="provider">
                                            <option value="default">Default</option>
                                            <option value="streetfile">Board of Elections</option>
                                            <option value="shapefile">Census TIGER/LINE</option>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <td><label for="geocoderMenu">Geocoder</label></td>
                                    <td>
                                        <select id="geocoderMenu" style="width: 100%;" ng-model="geoProvider">
                                            <option value="default">Default</option>
                                            <c:forEach var="geocoder" items="${activeGeocoders}">
                                                <option value="${geocoder.key}">${geocoder.value.simpleName}</option>
                                            </c:forEach>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <td><label for="uspsValidateMenu">USPS Validate</label></td>
                                    <td>
                                        <select id="uspsValidateMenu" style="width: 100%;" ng-model="uspsValidate">
                                            <option value="false">No</option>
                                            <option value="true">Yes</option>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div id="districtInfoHelp" ng-show="showHelp">
                            <p>District lookup can be used to either retrieve district information for a specific street
                            address or a summary of district overlaps for a street, city, or zip code.
                            <br/><br/>
                            <p>A <strong>building match</strong> displays districts for a specific address and is likely with an input of the following style: </p>
                            <a ng-click="addr='200 State St, Albany NY 12203'">200 State St, Albany NY 12203</a>
                            <br/>
                            <a ng-click="addr='1222 East 96th St, Brooklyn, NY 11236'">1222 East 96th St, Brooklyn, NY 11236</a>
                            <br/><br/>


                            <p>A <strong>street match</strong> will highlight the street and display senate district ranges within either
                            the city or zipcode.</p>
                            <a ng-click="addr='State St, Albany NY 12210'">State St, Albany NY 12210</a><br/>
                            <a ng-click="addr='Nyroy Dr, 12180'">Nyroy Dr, 12180</a><br/>
                            <br/>

                            <p>A <strong>city or zip match</strong> will display the percentage of each senate district's overlap.</p>
                            <a ng-click="addr='Troy, NY'">Troy, NY</a><br/>
                            <a ng-click="addr='12210'">12210</a>
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
                                <select id="districtTypeMenu" class="menu" style="width:100px;" ng-model="type" ng-change="metaLookup();">
                                    <option value="">Choose</option>
                                    <option value="senate">Senate</option>
                                    <option value="congressional">Congressional</option>
                                    <option value="assembly">Assembly</option>
                                    <option value="county">County</option>
                                    <option value="town">Town</option>
                                    <option value="school">School</option>
                                    <option value="zip">Zip</option>
                                </select>
                            </div>
                            <div>
                                <label for="districtCodeMenu" class="menu-overhead">District</label>
                                <select id="districtCodeMenu" class="menu" ng-change="lookup()" style="width:220px;" ng-model="selectedDistrict" ng-options="d.name for d in districtList"></select>
                            </div>
                        </div>
                        <div style="margin-top:4px;padding:5px;" ng-show="showMemberOption">
                            <div style="float:left">
                                <label for="districtMemberMenu" class="menu-overhead">Member</label>
                                <select id="districtMemberMenu" class="menu" style="width:325px;" ng-change="lookup()" ng-model="selectedDistrict" ng-options="d.member.name for d in sortedMemberList">
                                </select>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div id="uspsLookupView" ng-show="visible" ng-controller="UspsLookupController">
                <iframe id="uspsIframe" src='${amsUrl}'></iframe>
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
            <div id="district-results" ng-show="visible" ng-controller="DistrictsViewController" class="scrollable-content">
                <!-- District lookup error message -->
                <div id="failed-district-result" ng-hide="districtAssigned || multiMatch" >
                    <div class="info-container">
                        <p class="member-name" style="color:orangered;">No District Lookup Result</p>
                        <hr/>
                        <span class="message">{{description}}</span>
                    </div>
                </div>
                <!-- Geocoded location information -->
                <div class="info-container title" ng-switch="matchLevel" ng-show="geocoded && (districtAssigned || multiMatch)">
                    <p ng-switch-when="HOUSE">Showing matching results for address</p>
                    <p ng-switch-when="STREET">Showing matching results for street</p>
                    <p ng-switch-when="CITY">Showing matching results for city</p>
                    <p ng-switch-when="ZIP5">Showing matching results for zip code</p>
                </div>
                <div style="border-bottom:1px solid #ddd" class="info-container connected" ng-show="geocoded">
                    <table style="width:100%">
                        <tr>
                            <td><div class="icon-location icon-teal"></div></td>
                            <td><p style="font-size: 16px;color:#111;" ng-bind-html-unsafe="address | addressLevel:matchLevel | addressFormat"></p></td>
                            <td style="text-align:right;"><small ng-show="uspsValidated" style="color:teal;">USPS</small></td>
                        </tr>
                        <tr>
                            <td><div class="icon-target icon-teal"></div></td>
                            <td><p style="font-size: 16px;color:teal;">({{geocode.lat | number:6}}, {{geocode.lon | number:6}})</p></td>
                            <td style="text-align:right;">
                                <small style="color:teal;">{{geocode.method | remove:'Dao'}}</small>
                                <p ng-show="geocode.cached" class="icon-database icon-teal" style="color:teal;"></p>
                            </td>
                        </tr>
                    </table>
                </div>
                <div style="color:#444;font-size:13px;border-bottom:1px solid #ddd" ng-switch="matchLevel" ng-show="multiMatch" class="info-container connected slim">
                    <span style="color:orangered">Note: </span>
                    <span ng-switch-when="STREET">Street boundary may be incomplete.</span>
                    <span ng-switch-when="ZIP5">Zip code boundary is approximated.</span>
                    <span ng-switch-when="CITY">City boundary is approximated.</span>
                </div>
                <div class="info-container connected-top slim">
                    <a style="font-size:13px;" ng-hide="viewSuggestions" ng-click="viewSuggestions=true">Did you mean something else?</a>
                    <div ng-show="viewSuggestions">
                        <span style="color:#333;font-size:13px;">If the returned location is not what you intended, try entering more information
                        such as the city and zip code. <span ng-show="placeSuggestions.length">You can also try some suggestions listed below: </span></span>
                        <ul style="padding: 5px;margin: 5px auto;" ng-repeat="ps in placeSuggestions">
                            <li style="font-size:14px;"><a ng-click="requestDistrictInfo(ps.description);">{{ps.description}}</a></li>
                        </ul>
                    </div>
                </div>

                <div class="info-container title" ng-show="senateAssigned">
                    <p class="member-name success-color">New York State Senator</p>
                </div>
                <div ng-show="senateAssigned">
                    <div class="info-container clickable connected senator" title="Show Senate District Map" ng-click="showDistrict('senate');">
                        <div class="senator-pic-holder">
                            <a target="_blank" ng-href="{{districts.senate.senator.url}}">
                                <img ng-src="{{districts.senate.senator.imageUrl}}" class="senator-pic">
                            </a>
                        </div>
                        <div style='margin-top:10px'>
                            <table class="senator-info-district-result">
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

                    <div class="info-container connected slim" style="border-bottom:1px solid #ddd;">
                        <p class="member-email">
                        <div class="icon-mail icon-teal" style="margin-right: 5px;"></div>
                        <span style='font-size:15px'>{{districts.senate.senator.email}}</span>
                        </p>
                    </div>

                    <div class="info-container connected slim">
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
                            <div style="padding:5px;border-top:1px solid #ddd;font-size:14px;" ng-show="office.name">
                                <table style="width:100%">
                                    <tr>
                                        <td><p style="font-size:16px;color:teal;">{{office.name}}</p></td>
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

                    <div class="info-container slim connected" ng-show="districts.senate.nearBorder">
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
                </div>
                <div id="multi-senate-results" ng-show="multiMatch && overlaps.senate.length > 1">
                    <div class="info-container title connected-bottom">
                        <p class="member-name">{{overlaps.senate.length}} Senate District Matches</p>
                    </div>
                    <div class="info-container title connected">
                        <span class="message" ng-switch="matchLevel">
                            <span ng-switch-when="ZIP5">This zipcode area contains multiple Senate Districts.</span>
                            <span ng-switch-when="CITY">This approximated city area contains multiple Senate Districts.</span>
                            <span ng-switch-when="STREET">The indicated street contains multiple Senate Districts.</span>
                        </span>
                    </div>
                    <div class="info-container connected clickable slim2" title="Show full district map" ng-repeat="(i, d) in overlaps.senate" ng-click="showFullMapForOverlap(i, d, matchLevel);">
                        <table style="width:100%">
                            <tr>
                                <td>
                                    <div ng-show="matchLevel != 'STREET'" style="line-height:42px;height:42px;margin-right:0;" class="small-box" ng-style="getBgStyle(i)">{{(d.areaPercentage*100).toFixed(0) || '<1'}}%</div>
                                    <div class="senator" style="height:56px;">
                                        <div class="senator-pic-holder" style="width:50px;height:50px;">
                                            <a target="_blank" ng-href="{{d.member.url}}"><img ng-src="{{d.member.imageUrl | senatorPic}}" class="senator-pic"></a>
                                        </div>
                                        <div style="line-height: 25px;">
                                            <p class="senator member-name" style="font-size:16px;">
                                                <a target="_blank" ng-href="{{d.member.url}}">{{d.member.name}}</a>
                                            </p>
                                            <p style="font-size:16px;" class="senate district" ng-style="getColorStyle(d.district)">Senate District {{d.district}}</p>
                                        </div>
                                    </div>
                                </td>
                                <td class="right-icon-placeholder">
                                    <a title="Show Map">
                                        <div class="icon-map"></div>
                                    </a>
                                </td>
                            </tr>
                        </table>
                    </div>

                </div>

                <div class="info-container title" ng-show="multiMatch && streets">
                    <p class="member-name">Street ranges for <span style="text-transform: capitalize">{{streets[0].street | lowercase}}</span></p>
                </div>
                <div id="multi-street-results" class="info-container connected-top" ng-show="multiMatch && streets">
                    <table class="light-table">
                        <tr>
                            <th>Bldg From</th>
                            <th>Bldg To</th>
                            <th>E/O</th>
                            <th>Zip5</th>
                            <th>Senate District</th>
                        </tr>
                        <tr ng-repeat="(i,v) in streets">
                            <td>{{v.bldgLoNum}}</td>
                            <td>{{v.bldgHiNum}}</td>
                            <td>{{v.parity | parityFilter}}</td>
                            <td>{{v.zip5}}</td>
                            <td ng-style="getColorStyle(v.senate)">{{v.senate}}</td>
                        </tr>
                    </table>
                    <hr/>
                    <span style="font-size: 13px;color:#333;">If you are looking for more detailed street range information, try the Street Finder option located on the
                    top menu.</span>
                </div>
                <div id="success-district-results" ng-show="districtAssigned ">
                    <div class="info-container title connected-bottom">
                        <p class="member-name success-color">Matched New York State Districts</p>
                    </div>
                    <div class="info-container connected slim" ng-show="multiMatch">
                        <span class="message">Any districts listed below are confirmed since there are no other overlapping districts within the outlined
                            geographic area.</span>
                    </div>
                    <div class="info-container clickable connected congressional" ng-show="districts.congressional.district" title="Show Congressional District Map" ng-click="showDistrict('congressional');">
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
                    <div class="info-container clickable connected assembly" title="Show Assembly Map" ng-show="districts.assembly.district" ng-click="showDistrict('assembly');">
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
                    <div class="info-container clickable connected" title="Show County Map" ng-show="districts.county.district" ng-click="showDistrict('county');">
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
                    <div class="info-container clickable connected" ng-show="districts.town.district" title="Show Town Map" ng-click="showDistrict('town');">
                        <table style="width:100%">
                            <tr>
                                <td>
                                    <p class="member-name" ng-show="districts.town.name">City/Town: {{districts.town.name}}</p>
                                    <p class="district">Town Code: {{districts.town.district}}</p></td>
                                <td class="right-icon-placeholder">
                                    <a title="Show Map" ng-show="districts.town.map" ng-click="showDistrict('town');">
                                        <div class="icon-map"></div>
                                    </a>
                                </td>
                            </tr>
                        </table>
                    </div>
                    <div class="info-container clickable connected" ng-show="districts.school.district" title="Show School District Map" ng-click="showDistrict('school');">
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
                    <div class="info-container clickable connected" title="Show Zip Map" ng-show="districts.zip.district" ng-click="showDistrict('zip');">
                        <table style="width:100%">
                            <tr>
                                <td>
                                    <p class="district">Zip Code {{districts.zip.district}}</p>
                                </td>
                                <td class="right-icon-placeholder">
                                    <a title="Show Map" ng-show="districts.zip.map" ng-click="showDistrict('zip');">
                                        <div class="icon-map"></div>
                                    </a>
                                </td>
                            </tr>
                        </table>
                    </div>
                    <div class="info-container connected" ng-show="districts.election.district">
                        <p class="district">Election District: {{districts.election.district}}</p>
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

                        <div class="info-container" style="font-size:14px;" ng-repeat="office in member.offices">
                            <div ng-show="office.name">
                                <table style="width:100%">
                                    <tr>
                                        <td><p style="font-size:16px;color:teal;">{{office.name}}</p></td>
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

    <!-- Tooltip -->
    <div id="mapTooltip"></div>

    </jsp:body>
</sage:wrapper>
