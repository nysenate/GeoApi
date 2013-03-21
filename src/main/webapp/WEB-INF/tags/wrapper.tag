<%@tag description="Sage Base Template" pageEncoding="UTF-8"%>
<%@attribute name="title" fragment="true" required="true" %>
<%@attribute name="jsIncludes" fragment="true" required="false" %>
<%@attribute name="cssIncludes" fragment="true" required="false"%>

<html ng-app="sage">
    <head>
        <base href="<%= request.getContextPath()%>/" />
        <title><jsp:invoke fragment="title"/></title>
        <link rel="stylesheet" type="text/css" href="css/main.css" />
        <link rel="stylesheet" type="text/css" href="css/normalize.css" />
        <jsp:invoke fragment="cssIncludes"/>
        <script>contextPath = "<%=request.getContextPath()%>";</script>
    </head>
    <body>
        <div id="maincontainer">
            <div id="topsection">
                <div class="innertube">
                    <h1 class="logo">SAGE</h1>
                    <ul class="nav">
                        <li><a>About</a></li>
                        <li><a>API Reference</a></li>
                        <li><a>Batch Request</a></li>
                    </ul>
                </div>
            </div>
            <jsp:doBody />
        </div>
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
        <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyC-vIdRb4DI5jzKI92UNTnjHiwU7P0GqxI&sensor=false"></script>
        <script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.0.4/angular.min.js"></script>
        <jsp:invoke fragment="jsIncludes"/>
    </body>
</html>