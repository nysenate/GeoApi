<%@tag description="Sage Base Template" pageEncoding="UTF-8"%>
<%@attribute name="title" fragment="true" required="true" %>
<%@attribute name="ngApp" fragment="true" required="true" %>
<%@attribute name="jsIncludes" fragment="true" required="false" %>
<%@attribute name="cssIncludes" fragment="true" required="false"%>

<% request.setAttribute("contextPath", request.getContextPath());  %>

<!doctype html>
<html ng-app='<jsp:invoke fragment="ngApp"/>' id="ng-app">
    <head>
        <base href="${pageContext.request.contextPath}"/>
        <title><jsp:invoke fragment="title"/></title>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
        <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:400,600" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/normalize.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/main.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/icons.css" />
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/static/img/icons/favicon.ico" />
        <jsp:invoke fragment="cssIncludes"/>
        <script>contextPath = "<%=request.getContextPath()%>";</script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/vendor/jquery.min.js"></script>
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
