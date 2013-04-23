<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="sage" tagdir="/WEB-INF/tags" %>

<sage:wrapper>
    <jsp:attribute name="title">SAGE Map Viewer</jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script type="text/javascript" src="js/blockui.js"></script>
        <script type="text/javascript" src="app.js"></script>
    </jsp:attribute>
    <jsp:body>
        <div id="mapView" ng-controller="DistrictsViewController">
            <div class="top-header">
                <div class="icon" aria-hidden="true">&#59175;</div>
                <div class="text">Map | {{header}}</div>
            </div>
            <div id="map_canvas"></div>
        </div>
    </jsp:body>
</sage:wrapper>
