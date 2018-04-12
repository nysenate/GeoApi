<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>

<sage:wrapper>
    <jsp:attribute name="ngApp">sage-admin</jsp:attribute>
    <jsp:attribute name="title">SAGE - Admin Console</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.dataTables-1.9.4.css">
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="${pageContext.request.contextPath}/js/vendor/highcharts.js" type="text/javascript"></script>
        <script src="${pageContext.request.contextPath}/js/common.js" type="text/javascript"></script>
        <script src="${pageContext.request.contextPath}/js/admin.js" type="text/javascript"></script>
    </jsp:attribute>
    <jsp:body>
        <div style="width:100%" id="header" ng-controller="MenuController">
            <sage:logo></sage:logo>
            <ul class="top-method-header">
                <li><a ng-click='toggleMethod(1)' class="active">
                    <div ng-show="index == 1" class="icon-white-no-hover icon-house"></div>&nbsp;Dashboard</a></li>
                <li><a ng-click='toggleMethod(3)'>
                    <div ng-show="index == 3" class="icon-white-no-hover icon-user-add"></div>&nbsp;User Console</a></li>
                <li><a ng-href="${pageContext.request.contextPath}/admin/logout">Logout</a></li>
            </ul>
        </div>
        <div id="contentwrapper">
            <div id="contentcolumn" style="text-align:center;">
                <div ng-controller='DashboardController' ng-show='visible'>
                    <h3 class="slim">SAGE Dashboard</h3>
                    <div ng-controller="DeploymentStatsController" id="uptime-stats" class="highlight-section fixed">
                        <ul class="horizontal">
                            <li><label>Last Deployed | </label> {{lastDeployment.deployTime | date:'medium'}}</li>
                            <li><label>Latest Uptime | </label>{{latestUptime / 3600000 | number:3}} hours</li>
                            <li><label>API Requests Since Deployment | </label>{{requestsSinceLatest}}</li>
                        </ul>
                    </div>

                    <div class="highlight-section fixed">
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

                    <!-- Exception viewer -->
                    <div ng-controller="ExceptionViewController" ng-show="exceptions" class="highlight-section fixed">
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

                    <div ng-controller="ApiUsageController" class="highlight-section fixed">
                        <p class="blue-header">Api Hourly Usage</p>
                        <div id="api-usage-stats"></div>
                    </div>

                    <div ng-controller="JobStatusController" class="highlight-section fixed">
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

                    <div ng-controller="ApiUserStatsController" class="highlight-section fixed">
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

                    <div class="highlight-section fixed" ng-controller="GeocodeUsageController">
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


                    <div class="highlight-section fixed" ng-controller="GeocacheSubmitController">
                        <p class="blue-header">Geocache Address</p>
                        <hr/>
                        <p>The state is assumed to be NY</p>
                        <hr/>
                        <form>
                            <label for="geocache_addr1_input">Addr1: </label>
                            <input id="geocache_addr1_input" ng-model="geocache_addr1" type="text">

                            <label for="geocache_city_input">City: </label>
                            <input id="geocache_city_input" ng-model="geocache_city" type="text">

                            <label for="geocache_zip5_input">Zip5: </label>
                            <input id="geocache_zip5_input" ng-model="geocache_zip5" type="text">

                            <button type="submit" class="submit" style="width: auto;padding: 5px 10px;" ng-click="commenceGeocache()" ng-disabled="!isValidInfo()">Commence Geocache</button>
                        </form>
                        <hr ng-show="geocache_status"/>
                        <p ng-show="geocache_status">{{geocache_status_text}}</p>
                    </div>

                    <br/>
                    <br/>
                </div>

                <div ng-controller="UserConsoleController" ng-show="visible">
                    <h3 class="slim">User Console</h3>
                    <!-- Current Api Users -->
                    <div class="highlight-section fixed">
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
                    <!-- Current Batch Job Users -->
                    <div class="highlight-section fixed">
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
            </div>
        </div>
    </jsp:body>
</sage:wrapper>
