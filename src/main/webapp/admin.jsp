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
        <script src="${pageContext.request.contextPath}/js/common/common.js" type="text/javascript"></script>
        <script src="${pageContext.request.contextPath}/js/admin/admin.js" type="text/javascript"></script>
    </jsp:attribute>
    <jsp:body>
        <div style="width:100%" id="header" ng-controller="MenuController">
            <sage:logo></sage:logo>
            <ul class="top-method-header">
                <li><a ng-click='toggleView(1)' class="active">Dashboard</a></li>
                <li><a ng-click='toggleView(2)'>Requests Log</a></li>
                <li><a ng-click='toggleView(3)'>User Console</a></li>
            </ul>
        </div>
        <div id="contentwrapper">
            <div id="contentcolumn" style="text-align:center;padding-top:20px;background-color:#f5f5f5">
                <div ng-controller='DashboardController' ng-show='visible'>
                    <h3>SAGE Dashboard</h3>
                    <div id="uptime-stats">
                        <ul class="highlight-section">
                            <li><label>Last Deployed | </label> {{${lastDeployed} | date:'medium'}}</li>
                            <li><label>Latest Uptime | </label>{{${latestUptime / 3600000} | number:3}} hours</li>
                            <li><label>Api Requests Since Deployment | </label>${latestRequestsSince}</li>
                        </ul>
                    </div>
                    <div id="api-usage-stats" class="highlight-section"></div>
                    <div id="geocoder-stats" class="highlight-section">
                        <h4 style='margin:5px 0px'>Geocoder usage distribution</h4>
                        <div id="geocoder-stats-pie">
                            <!-- Highcharts pie here -->
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </jsp:body>
</sage:wrapper>