<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<sage:wrapper>
    <jsp:attribute name="title">SAGE - Batch Services Login</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link href="css/fileuploader.css" rel="stylesheet" type="text/css"/>
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="js/job.js" type="text/javascript"></script>
    </jsp:attribute>
    <jsp:body>
        <div class="top-header">
            <div class="icon-earth large-icon teal"></div>
            <div id="sage-logo-text">SAGE</div>
        </div>
        <h1 style="text-align: center; color:#222;">SAGE Batch Services</h1>
        <div style="margin:auto;width:720px;text-align: center;">
            <p>SAGE provides batch geocoding and district assignment services to registered users.</p>
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
                            <div class="icon-user"></div>
                            <span>Login</span>
                        </button>
                    </li>
                </ol>
            </form>
        </div>
    </jsp:body>
</sage:wrapper>