<%@ page import="gov.nysenate.sage.config.Environment" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="gov.nysenate.sage.service.geo.SageGeocodeServiceProvider" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:setLocale value="es_ES"/>
<%
    ApplicationContext ac = RequestContextUtils.findWebApplicationContext(request);
    Environment env = (Environment) ac.getBean("environment");
    SageGeocodeServiceProvider geocodeServiceProvider = (SageGeocodeServiceProvider) ac.getBean("sageGeocodeServiceProvider");
    request.setAttribute("amsUrl", env.getUspsAmsUiUrl());
    request.setAttribute("geocoders", geocodeServiceProvider.geocoders());
    String googleMapsUrl = env.getGoogleMapsUrl();
    String googleMapsKey = env.getGoogleMapsKey();
    if (googleMapsKey != null && !googleMapsKey.isEmpty()) {
        googleMapsUrl = googleMapsUrl + "&key=" + googleMapsKey;
    }
    request.setAttribute("googleMapsUrl", googleMapsUrl);
%>
<sage:wrapper>
    <jsp:attribute name="ngApp">sage-admin</jsp:attribute>
    <jsp:attribute name="title">SAGE - Admin Console</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/css/vendor/jquery.dataTables-1.9.4.css">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/admin.css">
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script async defer type="text/javascript" src="${googleMapsUrl}"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/js/vendor/jquery.dataTables-1.9.4.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/vendor/highcharts.js" type="text/javascript"></script>
        <sage:common></sage:common>
        <sage:admin></sage:admin>
    </jsp:attribute>
    <jsp:body>

        <div style="width:100%" id="header" ng-controller="AdminPageController">
            <sage:logo></sage:logo>
            <ul class="top-method-header">
                <li>
                    <a ng-class="{'active': activeTab=='api-usage'}" ng-click="changeTab('api-usage')">
                        <div class=" icon-graph"></div>&nbsp;Api Usage</a></li>

                <li>
                    <a ng-class="{'active': activeTab=='api-user-stats'}" ng-click="changeTab('api-user-stats')">
                        <div class=" icon-users"></div>&nbsp;Api User Stats</a></li>

                <li>
                    <a ng-class="{'active': activeTab=='geocode-usage'}" ng-click="changeTab('geocode-usage')">
                        <div class=" icon-compass"></div>&nbsp;Geocode Usage</a></li>

                <li>
                    <a ng-class="{'active': activeTab=='user-console'}" ng-click="changeTab('user-console')">
                        <div class=" icon-user-add"></div>&nbsp;User Console</a></li>

                <li>
                    <a ng-class="{'active': activeTab=='uptime-stats'}" ng-click="changeTab('uptime-stats')">
                        <div class=" icon-statistics"></div>&nbsp;Uptime Stats</a></li>

                <li><a ng-href="${pageContext.request.contextPath}/admindocs/html/index.html" target="_blank">Admin Docs</a></li>

                <li><a ng-href="${pageContext.request.contextPath}/admin/logout">Logout</a></li>
            </ul>


            <div id="contentwrapper">

                <div id="contentcolumn" style="text-align:center;">

                    <div ng-controller='DashboardController' ng-show='visible'>

                        <div ng-controller="DeploymentStatsController" id="uptime-stats" class="highlight-section fixed"
                             ng-show="determineActiveTab('uptime-stats')">

                            <sage:datepicker></sage:datepicker>

                            <hr/>

                            <ul class="horizontal">
                                <li><label>Last Deployed | </label> {{lastDeployment.deployTime | date:'medium'}}</li>
                                <li><label>Latest Uptime | </label>{{latestUptime / 3600000 | number:3}} hours</li>
                                <li><label>API Requests Since Deployment | </label>{{requestsSinceLatest}}</li>
                            </ul>
                        </div>

                        <div ng-controller="ApiUsageController" id="api-usage" class="highlight-section fixed"
                             ng-show="determineActiveTab('api-usage')">
                            <p class="blue-header">Api Hourly Usage</p>
                            <sage:datepicker></sage:datepicker>
                            <hr/>
                            <div id="api-usage-stats"></div>
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
                                        <th>Is Admin</th>
                                        <th>Actions</th>
                                    </tr>
                                    <tr ng-repeat="apiUser in currentApiUsers">
                                        <td>{{apiUser.id}}</td>
                                        <td>{{apiUser.apiKey}}</td>
                                        <td>{{apiUser.name}}</td>
                                        <td>{{apiUser.description}}</td>
                                        <td>{{apiUser.admin}}</td>
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
                                        <label for="new_apiUserAdmin">Is admin</label>
                                        <input ng-model="apiUserAdmin" name="admin" id="new_apiUserAdmin" type="checkbox">
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
    </jsp:body>
</sage:wrapper>
