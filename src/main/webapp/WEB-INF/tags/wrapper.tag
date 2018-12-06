<%@tag description="Sage Base Template" pageEncoding="UTF-8"%>
<%@attribute name="title" fragment="true" required="true" %>
<%@attribute name="ngApp" fragment="true" required="true" %>
<%@attribute name="jsIncludes" fragment="true" required="false" %>
<%@attribute name="cssIncludes" fragment="true" required="false"%>

<% request.setAttribute("contextPath", request.getContextPath());  %>

<!doctype html>
<html xmlns:ng="http://angularjs.org" ng-app='<jsp:invoke fragment="ngApp"/>' id="ng-app">
    <head>
        <base href="${pageContext.request.contextPath}"/>
        <title><jsp:invoke fragment="title"/></title>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/normalize.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/main.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/icons.css" />
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/static/img/icons/favicon.ico" />
        <jsp:invoke fragment="cssIncludes"/>
        <script>contextPath = "<%=request.getContextPath()%>";</script>
    <!--[if lte IE 8]>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/json2.js"></script>
    <![endif]-->
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/vendor/jquery-1.12.2.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/vendor/angular.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/common/common.js"></script>
        <jsp:invoke fragment="jsIncludes"/>
    </head>
    <body>
        <div id="maincontainer">
            <jsp:doBody />
        </div>
    </body>
</html>
