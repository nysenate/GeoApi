<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<fmt:setLocale value = "es_ES"/>
<sage:wrapper>
    <jsp:attribute name="ngApp">sage-job</jsp:attribute>
    <jsp:attribute name="title">SAGE - Batch Services Login</jsp:attribute>
    <jsp:attribute name="cssIncludes">
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="${pageContext.request.contextPath}/js/admin/admin.js" type="text/javascript"></script>
    </jsp:attribute>
    <jsp:body>
        <sage:header>
            <jsp:attribute name="ngController">MenuController</jsp:attribute>
            <jsp:attribute name="links">
                <li><a class="active">Login</a></li>
            </jsp:attribute>
        </sage:header>

        <h1 style="text-align: center; color:#222;font-weight: 400;">SAGE Admin Interface</h1>
        <div style="width:500px;margin:auto;" ng-controller="">
            <form id="uploadForm" method="post" ng-submit="login()" action="${pageContext.request.contextPath}/admin/login">
                <ol class="input-container" style="width:280px;margin:auto;padding:20px;">
                    <li>
                        <label>Username</label>
                        <input ng-model="username" type="text" id="username" name="username"><br>
                    </li>
                    <li>
                        <label>Password</label>
                        <input ng-model="password" type="password" id="password" name="password"><br>
                    </li>
                    <li>
                        <br/>
                    </li>
                    <li>
                        <button class="submit">
                            <div class="icon-user icon-white-no-hover"></div>
                            <span>Login</span>
                        </button>
                    </li>
                </ol>
            </form>
            <div id="login-error" ng-model="errorMessage">${errorMessage}</div>
        </div>
    </jsp:body>
</sage:wrapper>