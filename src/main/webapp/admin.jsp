<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>

<sage:wrapper>
    <jsp:attribute name="title">SAGE - Admin Console</jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="http://cdnjs.cloudflare.com/ajax/libs/highcharts/3.0.2/highcharts.js" type="text/javascript"></script>
        <script src="${pageContext.request.contextPath}/js/admin.js" type="text/javascript"></script>
    </jsp:attribute>
    <jsp:body>
        <sage:header></sage:header>
        <div id="contentwrapper">
            <div id="contentcolumn" style="margin-left:250px;padding-top:20px;background-color:#f5f5f5">
                <div style="text-align: center">
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
        <div id="leftcolumn" style="width:250px;border-right:1px solid #ddd;">
            <div class="innertube">
                <p class="method-header teal active">Dashboard</p>
                <p class="method-header teal">Requests Log</p>
                <p class="method-header teal">User Console</p>
                <p class="method-header">Exit</p>
            </div>
        </div>
    </jsp:body>
</sage:wrapper>