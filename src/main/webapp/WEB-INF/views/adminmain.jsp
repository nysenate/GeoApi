<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<fmt:setLocale value="es_ES"/>
<sage:wrapper>
    <jsp:attribute name="ngApp">sage-admin</jsp:attribute>
    <jsp:attribute name="title">SAGE - Admin Console</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/css/vendor/jquery.dataTables-1.9.4.css">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/admin.css">
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="${pageContext.request.contextPath}/js/vendor/highcharts.js" type="text/javascript"></script>
        <script src="${pageContext.request.contextPath}/js/common.js" type="text/javascript"></script>
        <script src="${pageContext.request.contextPath}/js/admin.js" type="text/javascript"></script>
    </jsp:attribute>
    <jsp:body>

        <div style="width:100%" id="header" ng-controller="AdminPageController">
            <sage:logo></sage:logo>
            <ul class="top-method-header">
                <li>
                    <a ng-class="{'active': activeTab=='exceptions'}" id="defaultOpen"
                       ng-click="changeTab('exceptions')">
                        <div class=" icon-new"></div>&nbsp;Exceptions</a></li>

                <li>
                    <a ng-class="{'active': activeTab=='api-usage'}" ng-click="changeTab('api-usage')">
                        <div class=" icon-graph"></div>&nbsp;Api Usage</a></li>

                <li>
                    <a ng-class="{'active': activeTab=='api-user-stats'}" ng-click="changeTab('api-user-stats')">
                        <div class=" icon-users"></div>&nbsp;Api User Stats</a></li>

                <li>
                    <a ng-class="{'active': activeTab=='job-status'}" ng-click="changeTab('job-status')">
                        <div class=" icon-map"></div>&nbsp;Job Status</a></li>

                <li>
                    <a ng-class="{'active': activeTab=='geocode-usage'}" ng-click="changeTab('geocode-usage')">
                        <div class=" icon-compass"></div>&nbsp;Geocode Usage</a></li>

                <li>
                    <a ng-class="{'active': activeTab=='geocaching'}" ng-click="changeTab('geocaching')">
                        <div class=" icon-location"></div>&nbsp;Geocaching</a></li>

                <li>
                    <a ng-class="{'active': activeTab=='user-console'}" ng-click="changeTab('user-console')">
                        <div class=" icon-user-add"></div>&nbsp;User Console</a></li>

                <li>
                    <a ng-class="{'active': activeTab=='uptime-stats'}" ng-click="changeTab('uptime-stats')">
                        <div class=" icon-statistics"></div>&nbsp;Uptime Stats</a></li>

                <li><a ng-href="${pageContext.request.contextPath}/admin/logout">Logout</a></li>
            </ul>


            <div id="contentwrapper">

                <div id="contentcolumn" style="text-align:center;">

                    <div ng-controller='DashboardController' ng-show='visible'>

                        <div ng-controller="DeploymentStatsController" id="uptime-stats" class="highlight-section fixed"
                             ng-show="determineActiveTab('uptime-stats')">

                            <div>
                                <span>The time frame to view stats is between &nbsp;</span>
                                <input ng-model="fromMonth" style="width:35px;" min="1" max="12" maxlength="2"
                                       type="number"/>/
                                <input ng-model="fromDate" style="width:35px;" min="1" max="31" maxlength="2"
                                       type="number"/>/
                                <input ng-model="fromYear" style="width:70px;" min="2013" max="2020" maxlength="4"
                                       type="number"/>

                                <span>&nbsp; and &nbsp;</span>

                                <input ng-model="toMonth" style="width:35px;" min="1" max="12" maxlength="2"
                                       type="number"/>/
                                <input ng-model="toDate" style="width:35px;" min="1" max="31" maxlength="2"
                                       type="number"/>/
                                <input ng-model="toYear" style="width:70px;" min="2013" max="2020" maxlength="4"
                                       type="number"/>

                                <button ng-click="update()" class="submit" style="width:auto; padding:5px 10px;">
                                    <span>Update</span>
                                </button>
                            </div>

                            <hr/>

                            <ul class="horizontal">
                                <li><label>Last Deployed | </label> {{lastDeployment.deployTime | date:'medium'}}</li>
                                <li><label>Latest Uptime | </label>{{latestUptime / 3600000 | number:3}} hours</li>
                                <li><label>API Requests Since Deployment | </label>{{requestsSinceLatest}}</li>
                            </ul>
                        </div>


                        <!-- Exception viewer -->
                        <div ng-controller="ExceptionViewController" id="exceptions" class="highlight-section fixed"
                             ng-show="determineActiveTab('exceptions')">
                            <p class="blue-header">Application Exceptions</p>
                            <hr/>
                            <div style="text-align: left;">
                                <div ng-repeat="(i, exception) in exceptions"
                                     style="padding: 5px;border-bottom: 1px solid #eee;">
                                    <div>
                                        <span style="color:#CC333F">{{exception.exceptionType}}</span> -
                                        {{exception.catchTime | date:'medium'}}
                                        <span ng-show="exception.apiRequest"> | Api Request Id: {{exception.apiRequest.id}}</span>
                                        <div style="float:right;font-size:14px;">
                                            <a ng-click="toggleStackTrace = !toggleStackTrace">Toggle stack trace</a>
                                            <span style="color:teal;margin:0 5px">|</span>
                                            <a ng-click="hideException(exception.id)">Remove</a>
                                        </div>
                                    </div>
                                    <pre ng-show="toggleStackTrace" style="color:teal;font-size:12px;text-align:left;"
                                         ng-bind-html-unsafe="exception.stackTrace | code">
                                </pre>
                                </div>
                            </div>
                        </div>

                        <div ng-controller="ApiUsageController" id="api-usage" class="highlight-section fixed"
                             ng-show="determineActiveTab('api-usage')">
                            <p class="blue-header">Api Hourly Usage</p>
                            <div id="api-usage-stats"></div>
                        </div>

                        <div ng-controller="JobStatusController" id="job-status" class="highlight-section fixed"
                             ng-show="determineActiveTab('job-status')">
                            <p class="blue-header">Batch Job Usage</p>
                            <hr/>
                            <div>
                                <table class="light-table">
                                    <tr>
                                        <th>Job Id</th>
                                        <th>Requestor</th>
                                        <th>File Name</th>
                                        <th>Record Count</th>
                                        <th>Start Time</th>
                                        <th>End Time</th>
                                        <th>Condition</th>
                                    </tr>
                                    <tr ng-repeat="jobStatus in jobStatuses">
                                        <td>{{jobStatus.processId}}</td>
                                        <td>{{jobStatus.process.requestorEmail}}</td>
                                        <td>{{jobStatus.process.sourceFileName}}</td>
                                        <td>{{jobStatus.process.recordCount}}</td>
                                        <td>{{jobStatus.startTime | date:'medium'}}</td>
                                        <td>{{jobStatus.completeTime | date:'medium'}}</td>
                                        <td>{{jobStatus.condition}}</td>
                                    </tr>
                                </table>
                            </div>
                        </div>

                        <div ng-controller="ApiUserStatsController" id="api-user-stats" class="highlight-section fixed"
                             ng-show="determineActiveTab('api-user-stats')">
                            <p class="blue-header">Api User Request Stats</p>
                            <hr/>
                            <table class="light-table">
                                <tr>
                                    <th>Api User Id</th>
                                    <th>Api User Name</th>
                                    <th>Api Requests</th>
                                    <th>Geocode Requests</th>
                                    <th>District Assign Requests</th>
                                </tr>
                                <tr ng-repeat="(id, apiUserStat) in apiUserStats">
                                    <td>{{id}}</td>
                                    <td>{{apiUserStat.apiUser.name}}</td>
                                    <td>{{apiUserStat.apiRequests}}</td>
                                    <td>{{apiUserStat.geoRequests}}</td>
                                    <td>{{apiUserStat.distRequests}}</td>
                                </tr>
                            </table>

                            <br/>
                            <p class="blue-header">Requests per method</p>
                            <hr/>
                            <table class="light-table">
                                <tr>
                                    <th style="width:100px;">Api User Id</th>
                                    <th style="width:300px;">Api User Name</th>
                                    <th style="width:175px;">Service</th>
                                    <th style="width:175px;">Method</th>
                                    <th style="width:100px;">Requests</th>
                                </tr>
                            </table>
                            <div ng-repeat="(id, apiUserStat) in apiUserStats">
                                <table class="light-table"
                                       ng-repeat="(service,methodList) in apiUserStat.requestsByMethod">
                                    <tr ng-repeat="(method, requests) in methodList">
                                        <td style="width:100px;">{{id}}</td>
                                        <td style="width:300px;">{{apiUserStat.apiUser.name}}</td>
                                        <td style="width:175px;">{{service}}</td>
                                        <td style="width:175px;">{{method}}</td>
                                        <td style="width:100px;">{{requests}}</td>
                                    </tr>
                                </table>
                            </div>
                        </div>

                        <div ng-controller="GeocodeUsageController" id="geocode-usage" class="highlight-section fixed "
                             ng-show="determineActiveTab('geocode-usage')">
                            <p class="blue-header">Geocoder Usage</p>
                            <hr/>
                            <ul class="horizontal">
                                <li><label>Total Geocodes: </label>{{totalGeocodes}}</li>
                                <li><label>Cache Hits: </label>{{totalCacheHits}}</li>
                                <li><label>Cache Hit Rate: </label>{{(totalCacheHits / totalGeocodes) * 100 |
                                    number:2}}%
                                </li>
                            </ul>
                            <hr/>
                            <table class="light-table" style="width:650px;margin:auto;text-align: left;">
                                <thead>
                                <tr>
                                    <th>Geocoder</th>
                                    <th>Requests</th>
                                    <th colspan="2" style="width:300px">Percentage of requests</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr ng-repeat="(geocoder, requests) in geocoderUsage">
                                    <td>{{geocoder}}</td>
                                    <td>{{requests}}</td>
                                    <td style="width:50px;">{{(requests / totalRequests) * 100 | number:1}}%</td>
                                    <td style="width:250px;background:#f5f5f5;">
                                        <div style="background:#CC333F;"
                                             ng-style="getBarStyle(requests, totalRequests)">&nbsp;
                                        </div>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>


                        <div ng-controller="GeocacheSubmitController" id="geocaching" class="highlight-section fixed"
                             ng-show="determineActiveTab('geocaching')">
                            <p class="blue-header">Geocache Address</p>
                            <hr/>
                            <p ng-show="!seperatedInput">Be sure to separate input with commas, (i.e. 200 State Street,
                                Albany, NY, 12210)</p>
                            <p ng-show="seperatedInput">The state is assumed to be NY</p>
                            <hr/>
                            <button class="toggle" style="width: auto;padding: 5px 10px;"
                                    ng-click="toggleInputSeperation()">Toggle Input Seperation
                            </button>
                            <br>
                            <br>

                            <form>

                                <div ng-show="!seperatedInput">
                                    <label for="input_addr_input">Address: </label>
                                    <input id="input_addr_input" ng-model="input_addr" type="text" size="50"
                                           ng-change="resetOnChange()">


                                </div>

                                <div ng-show="seperatedInput">
                                    <label for="input_addr1_input">Addr1: </label>
                                    <input id="input_addr1_input" ng-model="input_addr1" type="text" size="28"
                                           ng-change="resetOnChange()">

                                    <label for="input_city_input">City: </label>
                                    <input id="input_city_input" ng-model="input_city" type="text"
                                           ng-change="resetOnChange()">

                                    <label for="input_zip5_input">Zip5: </label>
                                    <input id="input_zip5_input" ng-model="input_zip5" type="text"
                                           ng-change="resetOnChange()">
                                </div>

                                <br>

                                <button type="submit" name="lookup" class="submit"
                                        style="width: auto;padding: 5px 10px;"
                                        ng-click="look_up()" ng-disabled="!isValidInfo()">Look Up
                                </button>
                            </form>


                            <div>
                                <hr ng-show="geo_comparison_status"/>
                                <form ng-show="geo_comparison_status">

                                    <p ng-show="geocache_status">Geocache: <br>Lat: {{geocache_json.geocode.lat || ""}}
                                        Lon: {{geocache_json.geocode.lon || ""}} <br>
                                        Quality: {{geocache_json.geocode.quality || ""}}
                                        Method:{{geocache_json.geocode.method || ""}} </p>

                                    <label for="google_coords">Google:
                                        <p ng-show="geo_google_status">Lat: {{geo_google_json.geocode.lat || ""}} Lon:
                                            {{geo_google_json.geocode.lon || ""}} <br>
                                            Quality: {{geo_google_json.geocode.quality || ""}}
                                            Method:{{geo_google_json.geocode.method || ""}} </p>
                                    </label>
                                    <input ng-show="geo_google_status" type="radio" id="google_coords"
                                           ng-model="geo_provider_to_use" value="Google">
                                    <br>
                                    <label for="tiger_coords">Tiger:
                                        <p ng-show="geo_tiger_status">Lat: {{geo_tiger_json.geocode.lat || ""}} Lon:
                                            {{geo_tiger_json.geocode.lon || ""}} <br>
                                            Quality: {{geo_tiger_json.geocode.quality || ""}}
                                            Method:{{geo_tiger_json.geocode.method || ""}} </p>
                                    </label>
                                    <input ng-show="geo_tiger_status" type="radio" ng-model="geo_provider_to_use"
                                           id="tiger_coords" value="Tiger"> <br><br>
                                    <button type="submit" name="update_geocache" class="geocache"
                                            style="width: auto;padding: 5px 10px;"
                                            ng-click="updateGeocache()" ng-disabled="!isProviderSelected()">Update
                                        Geocache
                                    </button>
                                </form>
                            </div>

                                <%--Geocache result status--%>

                            <div ng-show="geocache_result_status">
                                <hr ng-show="geocache_result_status"/>
                                <p>Updated Geocache</p>
                                <p>{{geocache_result_json.status}}</p>
                                <p>{{geocache_result_url}}</p>
                                <br>
                                <p ng-show="geocode_result_status">The address was inserted into the geocache as: <br>
                                    Lat: {{geocache_result_json.geocode.lat}} Lon: {{geocache_result_json.geocode.lon}}
                                    <br>
                                    Quality: {{geocache_result_json.geocode.quality}}
                                    Method:{{geocache_result_json.geocode.method}}</p>
                                <br>
                                <button class="accordion" onclick="doAccordian()">Toggle Json</button>
                                <p class="panel" style="word-wrap: break-word">{{geocache_result_json}}</p>
                            </div>

                            <hr ng-show="geo_comparison_status"/>
                            <div class="tab" ng-show="geo_comparison_status">
                                <button class="datatabs" onclick="openDataTab(event, 'geocache_status')">Geocache
                                </button>
                                <button class="datatabs" onclick="openDataTab(event, 'google')">Google</button>
                                <button class="datatabs" onclick="openDataTab(event, 'tiger')">Tiger</button>
                                <button class="datatabs" id="sda"
                                        onclick="openDataTab(event, 'street_district_assign')">Street District Assign
                                </button>
                            </div>

                                <%--Geocache status--%>

                            <div ng-show="geocache_status" id="geocache_status" class="datacontent">
                                <hr ng-show="geocache_status"/>
                                <p>Currently Geocached</p>
                                <p>{{geocache_json.status}}</p>
                                <p>{{geocache_url}}</p>
                                <br>
                                <p ng-show="geocache_json"><br>
                                    Lat: {{geocache_json.geocode.lat}} Lon: {{geocache_json.geocode.lon}} <br>
                                    Quality: {{geocache_json.geocode.quality}} Method:{{geocache_json.geocode.method}}
                                </p>
                                <br>
                                <button class="accordion" onclick="doAccordian()">Toggle Json</button>
                                <p class="panel" style="word-wrap: break-word">{{geocache_json}}</p>
                            </div>

                                <%--Google status--%>

                            <div ng-show="geo_google_status" id="google" class="datacontent">
                                <hr ng-show="geo_google_status"/>
                                <p>Google Coordinates</p>
                                <p>{{geo_google_json.status}}</p>
                                <p>{{geo_google_url}}</p>
                                <br>
                                <p ng-show="geo_google_geocode_status"><br>
                                    Lat: {{geo_google_json.geocode.lat}} Lon: {{geo_google_json.geocode.lon}} <br>
                                    Quality: {{geo_google_json.geocode.quality}}
                                    Method:{{geo_google_json.geocode.method}}</p>
                                <br>
                                <button class="accordion" onclick="doAccordian()">Toggle Json</button>
                                <p class="panel" style="word-wrap: break-word">{{geo_google_json}}</p>
                            </div>

                                <%--Tiger result status--%>

                            <div ng-show="geo_tiger_status" id="tiger" class="datacontent">
                                <hr ng-show="geo_tiger_status"/>
                                <p>Tiger Coordinates</p>
                                <p>{{geo_tiger_json.status}}</p>
                                <p>{{geo_tiger_url}}</p>
                                <br>
                                <p ng-show="geo_tiger_geocode_status">
                                    Lat: {{geo_tiger_json.geocode.lat}} Lon: {{geo_tiger_json.geocode.lon}} <br>
                                    Quality: {{geo_tiger_json.geocode.quality}} Method:{{geo_tiger_json.geocode.method}}
                                </p>
                                <br>
                                <button class="accordion" onclick="doAccordian()">Toggle Json</button>
                                <p class="panel" style="word-wrap: break-word">{{geo_tiger_json}}</p>
                            </div>

                                <%--STREEET DISTRICT ASSIGNMENT--%>

                            <div ng-show="district_assign_street_status" id="street_district_assign"
                                 class="datacontent">
                                <hr ng-show="district_assign_street_status"/>
                                <p>Street District Assignment</p>
                                <p>{{district_assign_street_json.status}} </p>
                                <p>{{district_assign_street_url}}</p>
                                <br>
                                <p ng-show="district_assign_street_geocode_status">Lat:
                                    {{district_assign_street_json.geocode.lat}} Lon:
                                    {{district_assign_street_json.geocode.lon}}
                                    <br>
                                    Quality: {{district_assign_street_json.geocode.quality}}
                                    Method:{{district_assign_street_json.geocode.method}}</p></p>
                                <br>
                                <p ng-show="district_assign_street_district_status">Senate District:
                                    {{(district_assign_street_json.districts.senate.district || "" )}} <br>
                                    Congressional District:
                                    {{(district_assign_street_json.districts.congressional.district || "" )}} <br>
                                    Assembly District: {{(district_assign_street_json.districts.assembly.district || ""
                                    )}} <br>
                                    County District: {{(district_assign_street_json.districts.county.district || "" )}}
                                    <br>
                                    Election District: {{(district_assign_street_json.districts.election.district || ""
                                    )}} <br>
                                    School District: {{(district_assign_street_json.districts.school.district || "" )}}
                                    <br>
                                    Town District: {{(district_assign_street_json.districts.town.district || "" )}} <br>
                                    Zip District: {{(district_assign_street_json.districts.zip.district || "" )}} <br>
                                    Cleg: {{(district_assign_street_json.districts.cleg.district || "" )}} <br>
                                    Ward: {{(district_assign_street_json.districts.ward.district || "" )}} <br>
                                    Village: {{(district_assign_street_json.districts.village.district || "" )}} <br>

                                </p>
                                <br>
                                <button class="accordion" onclick="doAccordian()">Toggle Json</button>
                                <p class="panel" style="word-wrap: break-word">{{district_assign_street_json}}</p>
                            </div>

                                <%--SHAPE DISTRICT ASSIGNMENT--%>

                            <div ng-show="district_assign_shape_district_status" id="shape_district_assign">
                                <hr ng-show="district_assign_shape_district_status"/>
                                <p>Shape District Assignment</p>
                                <p>{{district_assign_shape_json.status}} </p>
                                <p>{{district_assign_shape_url}}</p>
                                <br>
                                <p ng-show="district_assign_shape_geocode_status">Lat:
                                    {{district_assign_shape_json.geocode.lat}} Lon:
                                    {{district_assign_shape_json.geocode.lon}}
                                    <br>
                                    Quality: {{district_assign_shape_json.geocode.quality}}
                                    Method:{{district_assign_shape_json.geocode.method}}</p></p>
                                <br>
                                <p ng-show="district_assign_shape_district_status">Senate District:
                                    {{(district_assign_shape_json.districts.senate.district || "" )}} <br>
                                    Congressional District:
                                    {{(district_assign_shape_json.districts.congressional.district || "" )}} <br>
                                    Assembly District: {{(district_assign_shape_json.districts.assembly.district || ""
                                    )}} <br>
                                    County District: {{(district_assign_shape_json.districts.county.district || "" )}}
                                    <br>
                                    Election District: {{(district_assign_shape_json.districts.election.district || ""
                                    )}} <br>
                                    School District: {{(district_assign_shape_json.districts.school.district || "" )}}
                                    <br>
                                    Town District: {{(district_assign_shape_json.districts.town.district || "" )}} <br>
                                    Zip District: {{(district_assign_shape_json.districts.zip.district || "" )}} <br>
                                    Cleg: {{(district_assign_shape_json.districts.cleg.district || "" )}} <br>
                                    Ward: {{(district_assign_shape_json.districts.ward.district || "" )}} <br>
                                    Village: {{(district_assign_shape_json.districts.village.district || "" )}} <br>

                                </p>
                                <br>
                                <button class="accordion" onclick="doAccordian()">Toggle Json</button>
                                <p class="panel" style="word-wrap: break-word">{{district_assign_shape_json}}</p>
                            </div>

                        </div>


                        <div ng-controller="UserConsoleController" id="user-console" class="highlight-section fixed"
                             ng-show="determineActiveTab('user-console')">
                            <h3 class="slim">User Console</h3>
                            <!-- Current Api Users -->
                            <div>
                                <p class="title">Registered API Users</p>
                                <table class="admin-table" style="margin-top:10px;">
                                    <tr>
                                        <th>ID</th>
                                        <th>Api Key</th>
                                        <th>Name</th>
                                        <th>Description</th>
                                        <th>Actions</th>
                                    </tr>
                                    <tr ng-repeat="apiUser in currentApiUsers">
                                        <td>{{apiUser.id}}</td>
                                        <td>{{apiUser.apiKey}}</td>
                                        <td>{{apiUser.name}}</td>
                                        <td>{{apiUser.description}}</td>
                                        <td><a style="color:#CC333F;font-size: 13px;"
                                               ng-click="deleteApiUser(apiUser.id);">Delete</a></td>
                                    </tr>
                                </table>
                                <br/>
                                <p class="title" style="color:#639A00">Create new API User</p>
                                <div class="create-entity">
                                    <form ng-submit="createApiUser();">
                                        <label for="new_apiUserName">Name</label>
                                        <input ng-model="apiUserName" type="text" name="name" id="new_apiUserName"/>
                                        <label for="new_apiUserDesc">Description</label>
                                        <input ng-model="apiUserDesc" type="text" name="desc" id="new_apiUserDesc"/>
                                        <button style="width:80px;" class="submit">Create</button>
                                    </form>
                                </div>
                            </div>
                            <hr/>
                            <!-- Current Batch Job Users -->
                            <div>
                                <p class="title">Registered Job Users</p>
                                <table class="admin-table" style="margin-top:10px;">
                                    <tr>
                                        <th>ID</th>
                                        <th>First Name</th>
                                        <th>Last Name</th>
                                        <th>Email</th>
                                        <th>Active</th>
                                        <th>Admin</th>
                                        <th>Actions</th>
                                    </tr>
                                    <tr ng-repeat="jobUser in currentJobUsers">
                                        <td>{{jobUser.id}}</td>
                                        <td>{{jobUser.firstname}}</td>
                                        <td>{{jobUser.lastname}}</td>
                                        <td>{{jobUser.email}}</td>
                                        <td>{{jobUser.active}}</td>
                                        <td>{{jobUser.admin}}</td>
                                        <td><a style="color:#CC333F;font-size: 13px;"
                                               ng-click="deleteJobUser(jobUser.id);">Delete</a></td>
                                    </tr>
                                </table>
                                <br/>
                                <p class="title" style="color:#639A00">Create new Job User</p>
                                <div class="create-entity">
                                    <form ng-submit="createJobUser();">
                                        <label>First Name</label>
                                        <input ng-model="jobFirstName" style="width:120px" type="text"
                                               name="firstname"/>
                                        <label>Last Name</label>
                                        <input ng-model="jobLastName" style="width:120px" type="text" name="lastname"/>
                                        <label>Email</label>
                                        <input ng-model="jobEmail" type="text" name="email"/><br/>
                                        <br/>
                                        <label>Password</label>
                                        <input ng-model="jobPassword" style="width:126px" type="password"
                                               name="password"/>
                                        <label>Admin</label>
                                        <input ng-model="jobAdmin" type="checkbox"/>
                                        <button style="width:80px;" class="submit">Create</button>
                                    </form>
                                </div>
                            </div>
                        </div>

                        <br/>
                        <br/>
                    </div>

                </div>
            </div>
        </div>


        <script>
            function doAccordian() {
                var acc = document.getElementsByClassName("accordion");
                var i;

                for (i = 0; i < acc.length; i++) {
                    acc[i].addEventListener("click", function () {
                        this.classList.toggle("active");
                        var panel = this.nextElementSibling;
                        if (panel.style.display === "block") {
                            panel.style.display = "none";
                        } else {
                            panel.style.display = "block";
                        }
                    });
                }
            }

            function openDataTab(evt, id) {
                var i, tabcontent, tablinks;
                tabcontent = document.getElementsByClassName("datacontent");
                for (i = 0; i < tabcontent.length; i++) {
                    tabcontent[i].style.display = "none";
                }
                tablinks = document.getElementsByClassName("datatabs");
                for (i = 0; i < tablinks.length; i++) {
                    tablinks[i].className = tablinks[i].className.replace(" active", "");
                }
                document.getElementById(id).style.display = "block";
                evt.currentTarget.className += " active";
            }

            // Get the element with id="defaultOpen" and click on it
            document.getElementById("defaultOpen").click();
        </script>
    </jsp:body>
</sage:wrapper>
