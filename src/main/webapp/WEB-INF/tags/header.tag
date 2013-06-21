<%@tag description="Sage Header Template" pageEncoding="UTF-8"%>
<%@taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<%@attribute name="ngController" fragment="true" required="false" %>
<%@attribute name="links" fragment="true" required="false" %>
<div style="width:100%" id="header" ng-controller="<jsp:invoke fragment="ngController"/>">
    <sage:logo></sage:logo>
    <ul class="top-method-header">
        <jsp:invoke fragment="links"/>
    </ul>
</div>