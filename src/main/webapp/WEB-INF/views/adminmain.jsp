<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>

<sage:wrapper>
    <jsp:attribute name="ngApp">sage-admin</jsp:attribute>
    <jsp:attribute name="title">SAGE - Admin Console</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="http://cdnjs.cloudflare.com/ajax/libs/highcharts/3.0.2/highcharts.js" type="text/javascript"></script>
        <script src="${pageContext.request.contextPath}/js/common.js" type="text/javascript"></script>
        <script src="${pageContext.request.contextPath}/js/admin.js" type="text/javascript"></script>
    </jsp:attribute>
    <jsp:body>
        <div style="width:100%" id="header" ng-controller="MenuController">
            <sage:logo></sage:logo>
            <ul class="top-method-header">
                <li><a ng-click='toggleMethod(1)' class="active">Dashboard</a></li>
                <li><a ng-click='toggleMethod(2)'>Requests Log</a></li>
                <li><a ng-click='toggleMethod(3)'>User Console</a></li>
                <li><a ng-click='toggleMethod(4)'>Test Tool</a></li>
                <li><a ng-href="${pageContext.request.contextPath}/admin/logout">Logout</a></li>
            </ul>
        </div>
        <div id="contentwrapper">
            <div id="contentcolumn" style="text-align:center;padding-top:20px;background-color:#f5f5f5">
                <div ng-controller='DashboardController' ng-show='visible'>
                    <h3>SAGE Dashboard</h3>
                    <div id="uptime-stats" class="highlight-section">
                        <ul class="horizontal">
                            <li><label>Last Deployed | </label> {{lastDeployment.deployTime | date:'medium'}}</li>
                            <li><label>Latest Uptime | </label>{{latestUptime / 3600000 | number:3}} hours</li>
                            <li><label>Api Requests Since Deployment | </label>{{requestsSinceLatest}}</li>
                        </ul>
                    </div>
                    <div id="api-usage-stats" class="highlight-section"></div>
                </div>
                <div ng-controller="UserConsoleController" ng-show="visible">
                    <h3>User Console</h3>
                    <!-- Current Api Users -->
                    <div class="highlight-section">
                        <p class="title">Registered API Users</p>
                        <table class="admin-table" style="margin-top:10px;">
                            <tr>
                                <th>ID</th>
                                <th>Api Key</th>
                                <th>Name</th>
                                <th>Description</th>
                                <th>Monthly Limit</th>
                                <th>Actions</th>
                            </tr>
                            <tr ng-repeat="apiUser in currentApiUsers">
                                <td>{{apiUser.id}}</td>
                                <td>{{apiUser.apiKey}}</td>
                                <td>{{apiUser.name}}</td>
                                <td>{{apiUser.description}}</td>
                                <td>{{apiUser.monthlyRequestCap}}</td>
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
                                <label for="new_apiUserMonthlyCap">Monthly Limit</label>
                                <input ng-model="apiUserMonthlyCap" type="number" style="width:60px;" name="monthlyRequestCap" id="new_apiUserMonthlyCap" step="100"/>
                                <button style="width:80px;" class="submit">Create</button>
                            </form>
                        </div>
                    </div>
                    <!-- Current Batch Job Users -->
                    <div class="highlight-section">
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