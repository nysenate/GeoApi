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
            <div ng-controller="MapViewController" id="mapView">
                <div class="result-header" style="background-color: #333;color:white;">
                    <span aria-hidden="true" data-icon="&#59175;"></span>{{polygonName}}
                </div>
                <div id="map_canvas"></div>
            </div>
            <div ng-controller="StreetViewController">
                <div ng-show="visible" style="height:100%;">
                    <div class="icon-header" style="background-color: #333;color:white;">
                        <div class="icon" aria-hidden="true">&#57349;</div>
                        <div class="text">Street File Results</div>
                    </div>
                    <div style="padding:10px;">
                        <div class="info-container street-search-filter">
                            <label>Filter by street name: </label>
                            <input id="street-search" type="text" />
                        </div>
                        <table id="street-view-table" my-table="overrideOptions" aa-data="streets"
                               ao-column-defs="columnDefs" aa-sorting="sortDefault">
                            <thead>
                            <th>From Bldg</th>
                            <th>To Bldg</th>
                            <th>Street</th>
                            <th>Location</th>
                            <th>Zip</th>
                            <th>Senate</th>
                            <th>Assembly</th>
                            <th>Congressional</th>
                            <th>Town</th>
                            <th>County</th>
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
            <div class="icon-header" style="background:#333;color:white;">
                <span class="icon">&#9776;</span>
                <div class="text">Services</div>
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
                                <span aria-hidden="true" data-icon="&#128269;" class="search-icon"></span>
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
                            <label>District</label>
                            <input ng-model="district" type="text" placeholder="(leave blank to view all)" id="districtCodeInput" />
                        </li>
                        <li>
                            <button class="submit" ng-click="lookup();">
                                <span aria-hidden="true" data-icon="&#128269;" class="search-icon"></span>
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
        </div>
    </div>

    <div id="rightcolumn" ng-controller="ResultsViewController">
        <div class="innertube">
            <p class="result-header success" style="cursor:auto">Results <a ng-click="toggleResultPane(false);" class="result-close">&#10006;</a></p>
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
                                <p class="senate district">Senate District {{districts.senate.district}}</p><br/>
                                <p class="member-email"><span aria-hidden="true" data-icon="&#9993;" style="color:teal;"></span>{{districts.senate.senator.email}}</p>
                            </div>
                        </div>
                        <div class="info-container">
                            <table style="width:100%">
                                <tr>
                                    <td><span aria-hidden="true" data-icon="&#59172;" style="color:teal;"></span></td>
                                    <td><p style="font-size: 16px;color:#111;" ng-bind-html-unsafe="address | addressFormat"></p>
                                    </td>
                                </tr>
                                <tr>
                                    <td><span aria-hidden="true" data-icon="&#127919;" style="color:teal;"></span></td>
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
                                    <td style="width:100px;font-size: 13px;color: #555;text-align: right;"><a ng-click="showDistrict('congressional');">Map</a></td>
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
                                    <td style="width:100px;font-size: 13px;color: #555;text-align: right;"><a ng-click="showDistrict('assembly');">Map</a></td>
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
                                    <td style="width:100px;font-size: 13px;color: #555;text-align: right;"><a ng-click="showDistrict('county');">Map</a></td>
                                </tr>
                            </table>
                        </div>
                        <div class="info-container">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <p class="member-name">Town of {{districts.town.name}}</p>
                                        <p class="district">Town Code: {{districts.town.district}}</p></td>
                                    <td style="width:100px;font-size: 13px;color: #555;text-align: right;"><a ng-click="showDistrict('town');">Map</a></td>
                                </tr>
                            </table>
                        </div>
                        <div class="info-container">
                            <table style="width:100%">
                                <tr>
                                    <td>
                                        <p class="member-name">{{districts.school.name}}</p>
                                        <p class="district">School District Code: {{districts.school.district}}</p>
                                    </td>
                                    <td style="width:100px;font-size: 13px;color: #555;text-align: right;"><a ng-click="showDistrict('school');">Map</a></td>
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
                                    <span aria-hidden="true" data-icon="&#9993;" style="color:teal;"></span>{{member.email}}
                                </p>
                            </div>
                        </div>

                        <div id="district-offices" ng-repeat="office in member.offices">
                            <div class="info-container" ng-show="office.name">
                                <p style="font-size:18px;color:teal;">{{office.name}}</p>
                                <p>{{office.street}}</p>
                                <p>{{office.additional}}</p>
                                <p>{{office.city}}, {{office.province}} {{office.postalCode}}</p>
                                <p>Phone {{office.phone}}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    </jsp:body>
</sage:wrapper>
