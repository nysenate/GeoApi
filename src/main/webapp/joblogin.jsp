<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<%
    request.setAttribute("email", "MUWHAHAHA");
    String email = (String)request.getParameter("email");
    String error = (String)request.getAttribute("error");
    String context = (String)request.getContextPath();
    email = email == null ? "" : email;
%>
<sage:wrapper>
    <jsp:attribute name="title">SAGE - Batch Services Login</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link href="css/fileuploader.css" rel="stylesheet" type="text/css"/>
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="js/job.js" type="text/javascript"></script>
    </jsp:attribute>
    <jsp:body>
        <h1 style="text-align: center; color:#222;">SAGE Batch Services</h1>
        <div style="margin:auto;width:720px;text-align: center;">
            <p>SAGE offers batch geocoding and district assignment services for registered users.</p>
        </div>
        <div style="width:500px;margin:auto;" ng-controller="JobAuthController">
            <form id="uploadForm" method="post" ng-submit="login()" action="job/login">
                <ol class="input-container" style="width:280px;margin:auto;padding:20px;">
                    <li>
                        <label>Email</label>
                        <input ng-model="email" type="email" required="true" placeholder="example@email.com" id="email" name="email"><br>
                    </li>
                    <li>
                        <label>Password</label>
                        <input ng-model="password" type="password" required="true" id="password" name="password"><br>
                    </li>
                    <li>
                        <br/>
                    </li>
                    <li>
                        <button class="submit">
                            <span aria-hidden="true" data-icon="&#128100;"></span>
                            <span>Login</span>
                        </button>
                    </li>
                </ol>
            </form>

            <div class="error-container" ng-show="error">
                <p><span style="position:relative; top:-1px;" aria-hidden="true" data-icon="&#8854;"></span>
                    {{errorMessage}}</p>
            </div>
        </div>
    </jsp:body>
</sage:wrapper>