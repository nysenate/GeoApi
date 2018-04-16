<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<fmt:setLocale value = "es_ES"/>
<sage:wrapper>
    <jsp:attribute name="ngApp">sage-admin</jsp:attribute>
    <jsp:attribute name="title">SAGE - Admin Console</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/vendor/jquery.dataTables-1.9.4.css">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/tabs.css">
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="${pageContext.request.contextPath}/js/vendor/highcharts.js" type="text/javascript"></script>
        <script src="${pageContext.request.contextPath}/js/common.js" type="text/javascript"></script>
        <script src="${pageContext.request.contextPath}/js/admin.js" type="text/javascript"></script>
    </jsp:attribute>
    <jsp:body>

        <div style="width:100%" id="header">
            <sage:logo></sage:logo>
            <ul class="top-method-header">
                    <li class="tablinks" onclick="openTab(event,'exception-viewer')">
                        <a><div class=" icon-erase" id="defaultOpen"></div>&nbsp;Exceptions</a></li>

                    <li class="tablinks" onclick="openTab(event,'api-usage')">
                        <a><div class=" icon-graph"></div>&nbsp;Api Usage</a></li>

                    <li class="tablinks" onclick="openTab(event,'api-user-stats')">
                        <a><div class=" icon-users"></div>&nbsp;Api User Stats</a></li>

                    <li class="tablinks" onclick="openTab(event,'job-status')">
                        <a><div class=" icon-map"></div>&nbsp;Job Status</a></li>

                    <li class="tablinks" onclick="openTab(event,'geocode-usage')">
                        <a><div class=" icon-compass"></div>&nbsp;Geocode Usage</a></li>

                    <li class="tablinks" onclick="openTab(event,'geocaching')">
                        <a><div class=" icon-location"></div>&nbsp;Geocaching</a></li>

                    <li class="tablinks" onclick="openTab(event,'user-console')">
                        <a><div class=" icon-user-add"></div>&nbsp;User Console</a></li>

                    <li class="tablinks" onclick="openTab(event,'uptime-stats')">
                        <a><div class=" icon-statistics"></div>&nbsp;Uptime Stats</a></li>

                    <li><a ng-href="${pageContext.request.contextPath}/admin/logout">Logout</a></li>
            </ul>
        </div>



        <div id="contentwrapper">

            <div id="contentcolumn" style="text-align:center;">

                <div ng-controller='DashboardController' ng-show='visible'>
                    <h3 class="slim">SAGE Dashboard</h3>

                    <div ng-controller="DeploymentStatsController" id="uptime-stats" class="highlight-section fixed tabcontent">

                        <div >
                            <span>The time frame to view stats is between &nbsp;</span>
                            <input ng-model="fromMonth" style="width:35px;" min="1" max="12" maxlength="2" type="number"/>/
                            <input ng-model="fromDate" style="width:35px;" min="1" max="31" maxlength="2" type="number"/>/
                            <input ng-model="fromYear" style="width:70px;" min="2013" max="2020" maxlength="4" type="number"/>

                            <span>&nbsp; and &nbsp;</span>

                            <input ng-model="toMonth" style="width:35px;" min="1" max="12" maxlength="2" type="number"/>/
                            <input ng-model="toDate" style="width:35px;" min="1" max="31" maxlength="2" type="number"/>/
                            <input ng-model="toYear" style="width:70px;" min="2013" max="2020" maxlength="4" type="number"/>

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
                    <div ng-controller="ExceptionViewController" ng-show="exceptions" id="exception-viewer" class="highlight-section fixed tabcontent">
                        <p class="blue-header">Application Exceptions</p>
                        <hr/>
                        <div style="text-align: left;">
                            <div ng-repeat="(i, exception) in exceptions" style="padding: 5px;border-bottom: 1px solid #eee;">
                                <div>
                                    <span style="color:#CC333F">{{exception.exceptionType}}</span> - {{exception.catchTime | date:'medium'}}
                                    <span ng-show="exception.apiRequest"> | Api Request Id: {{exception.apiRequest.id}}</span>
                                    <div style="float:right;font-size:14px;">
                                        <a ng-click="toggleStackTrace = !toggleStackTrace" >Toggle stack trace</a>
                                        <span style="color:teal;margin:0 5px">|</span>
                                        <a ng-click="hideException(exception.id)">Remove</a>
                                    </div>
                                </div>
                                <pre ng-show="toggleStackTrace" style="color:teal;font-size:12px;text-align:left;" ng-bind-html-unsafe="exception.stackTrace | code">
                                </pre>
                            </div>
                        </div>
                    </div>

                    <div ng-controller="ApiUsageController" id="api-usage" class="highlight-section fixed tabcontent">
                        <p class="blue-header">Api Hourly Usage</p>
                        <div id="api-usage-stats"></div>
                    </div>

                    <div ng-controller="JobStatusController" id="job-status" class="highlight-section fixed tabcontent">
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

                    <div ng-controller="ApiUserStatsController" id="api-user-stats" class="highlight-section fixed tabcontent">
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
                            <table class="light-table" ng-repeat="(service,methodList) in apiUserStat.requestsByMethod">
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

                    <div ng-controller="GeocodeUsageController"  id="geocode-usage" class="highlight-section fixed tabcontent">
                        <p class="blue-header">Geocoder Usage</p>
                        <hr/>
                        <ul class="horizontal">
                            <li><label>Total Geocodes: </label>{{totalGeocodes}}</li>
                            <li><label>Cache Hits: </label>{{totalCacheHits}}</li>
                            <li><label>Cache Hit Rate: </label>{{(totalCacheHits / totalGeocodes) * 100 | number:2}}%</li>
                        </ul>
                        <hr/>
                        <table class="light-table" style="width:650px;margin:auto;text-align: left;">
                            <thead><tr><th>Geocoder</th><th>Requests</th><th colspan="2" style="width:300px">Percentage of requests</th></tr></thead>
                            <tbody>
                                <tr ng-repeat="(geocoder, requests) in geocoderUsage">
                                    <td>{{geocoder}}</td>
                                    <td>{{requests}}</td>
                                    <td style="width:50px;">{{(requests / totalRequests) * 100 | number:1}}%</td>
                                    <td style="width:250px;background:#f5f5f5;"><div style="background:#CC333F;" ng-style="getBarStyle(requests, totalRequests)">&nbsp;</div></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>


                    <div ng-controller="GeocacheSubmitController" id="geocaching" class="highlight-section fixed tabcontent" >
                        <p class="blue-header">Geocache Address</p>
                        <hr/>
                        <p>The state is assumed to be NY, in the seperated input form</p>
                        <hr/>
                        <button class="submit" style="width: auto;padding: 5px 10px;" ng-click="toggleInputSeperation()">Toggle Input Seperation</button>
                        <br>
                        <br>
                        <form ng-show="!seperatedInput">
                            <label for="geocache_addr_input">Address: </label>
                            <input id="geocache_addr_input" ng-model="geocache_addr" type="text">

                            <br>
                            <br>

                            <button type="submit" name="district_assign" class="submit" style="width: auto;padding: 5px 10px;"
                                    ng-click="admin_district_assign()" ng-disabled="!isValidInfo()">District Assign</button>

                            <button type="submit" name="update_geocache" class="submit" style="width: auto;padding: 5px 10px;"
                                    ng-click="updateGeocache()" ng-disabled="!isValidInfo()">Update Geocache</button>
                        </form>
                        <form ng-show="seperatedInput">
                            <label for="geocache_addr1_input">Addr1: </label>
                            <input id="geocache_addr1_input" ng-model="geocache_addr1" type="text">

                            <label for="geocache_city_input">City: </label>
                            <input id="geocache_city_input" ng-model="geocache_city" type="text">

                            <label for="geocache_zip5_input">Zip5: </label>
                            <input id="geocache_zip5_input" ng-model="geocache_zip5" type="text">

                            <br>
                            <br>

                            <button type="submit" name="district_assign" class="submit" style="width: auto;padding: 5px 10px;"
                                    ng-click="admin_district_assign()" ng-disabled="!isValidInfo()">District Assign</button>

                            <button type="submit" name="update_geocache" class="submit" style="width: auto;padding: 5px 10px;"
                                    ng-click="updateGeocache()" ng-disabled="!isValidInfo()">Update Geocache</button>
                        </form>
                        <hr ng-show="district_assign_status"/>
                        <div ng-show="district_assign_status">
                            <p ng-show="district_assign_status">{{district_assign_json.status}} </p>
                            <p ng-show="district_assign_status">{{district_assign_url}}</p>
                            <br>
                            <p ng-show="district_assign_geocode_status">Lat: {{district_assign_json.geocode.lat}}  Lon: {{district_assign_json.geocode.lon}}
                                <br>
                                Quality: {{district_assign_json.geocode.quality}}  Method:{{district_assign_json.geocode.method}}</p></p>
                            <br>
                            <p ng-show="district_assign_district_status">{{district_assign_json.districts}}</p>
                            <br>
                            <p ng-show="district_assign_status">{{district_assign_json}}</p>
                        </div>
                        <hr ng-show="geocache_status"/>
                        <div ng-show="geocache_status">
                            <p ng-show="geocache_status">{{geocache_json.status}}</p>
                            <p ng-show="geocache_status">{{contextPath}}{{geocache_url}}</p>
                            <br>
                            <p ng-show="geocode_status = true;">The address was inserted into the geocache as: <br>
                                Lat: {{geocache_json.geocode.lat}}  Lon: {{geocache_json.geocode.lon}} <br>
                                Quality: {{geocache_json.geocode.quality}}  Method:{{geocache_json.geocode.method}}</p>
                            <br>
                            <p ng-show="geocache_status">{{geocache_json}}</p>
                        </div>


                    </div>



                    <div ng-controller="UserConsoleController" id="user-console" class="highlight-section fixed tabcontent">
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
                                    <td><a style="color:#CC333F;font-size: 13px;" ng-click="deleteApiUser(apiUser.id);">Delete</a></td>
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
                                    <td><a style="color:#CC333F;font-size: 13px;" ng-click="deleteJobUser(jobUser.id);">Delete</a></td>
                                </tr>
                            </table>
                            <br/>
                            <p class="title" style="color:#639A00">Create new Job User</p>
                            <div class="create-entity">
                                <form ng-submit="createJobUser();">
                                    <label>First Name</label>
                                    <input ng-model="jobFirstName" style="width:120px" type="text" name="firstname"/>
                                    <label>Last Name</label>
                                    <input ng-model="jobLastName" style="width:120px" type="text" name="lastname"/>
                                    <label>Email</label>
                                    <input ng-model="jobEmail" type="text" name="email"/><br/>
                                    <br/>
                                    <label>Password</label>
                                    <input ng-model="jobPassword" style="width:126px" type="password" name="password"/>
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


        <script>
            function openTab(evt, tabName) {
                var i, tabcontent, tablinks;
                tabcontent = document.getElementsByClassName("tabcontent");
                for (i = 0; i < tabcontent.length; i++) {
                    tabcontent[i].style.display = "none";
                }
                tablinks = document.getElementsByClassName("tablinks");
                for (i = 0; i < tablinks.length; i++) {
                    tablinks[i].className = tablinks[i].className.replace(" active", "");
                }
                document.getElementById(tabName).style.display = "block";
                evt.currentTarget.className += " active";
            }

            // Get the element with id="defaultOpen" and click on it
            document.getElementById("defaultOpen").click();
        </script>
    </jsp:body>
</sage:wrapper>
