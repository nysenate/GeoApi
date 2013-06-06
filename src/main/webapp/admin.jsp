<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>

<sage:wrapper>
    <jsp:attribute name="title">SAGE - Admin Console</jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="${pageContext.request.contextPath}/js/admin.js" type="text/javascript"></script>
    </jsp:attribute>
    <jsp:body>
        <sage:header></sage:header>
        <div id="contentwrapper">
            <div id="contentcolumn" style="margin-left:250px;padding-top:20px;background-color:#f5f5f5">
                <div style="text-align: center">
                    <h3>SAGE Usage Stats</h3>
                    <div id="uptime-stats">
                        <ul>
                            <li>Last Deployed - 06/04/2013 12:34 PM</li>
                            <li>Uptime - 48234.3 hours</li>
                            <li>Api Requests - 1435</li>
                            <li>Bluebird Requests - 90</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <div id="leftcolumn" style="width:250px;border-right:1px solid #ddd;">
            <div class="innertube">
                <p class="method-header teal active">Quick Stats</p>
                <p class="method-header teal">Requests Log</p>
                <p class="method-header teal">User Console</p>
                <p class="method-header">Exit</p>
            </div>
        </div>
    </jsp:body>
</sage:wrapper>