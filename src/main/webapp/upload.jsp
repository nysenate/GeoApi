<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<%
    String email = (String)request.getParameter("email");
    String error = (String)request.getAttribute("error");
    String context = (String)request.getContextPath();
    email = email == null ? "":email;
%>
<sage:wrapper>
    <jsp:attribute name="title">SAGE - Batch Request</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link href="css/fileuploader.css" rel="stylesheet" type="text/css"/>
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="js/fileuploader.js" type="text/javascript"></script>
        <script src="js/job.js" type="text/javascript"></script>
    </jsp:attribute>
    <jsp:body>
        <h1 style="text-align: center; color:#222;">SAGE Batch Services</h1>
        <div style="margin:auto;width:720px;text-align: center;">
            <p>SAGE offers batch geocoding and district assignment services for registered users.</p>
        </div>
        <div style="width:500px;margin:auto;">
            <form id="uploadForm" method="post">
                <ol class="input-container">
                    <li>
                        <label>Email</label>
                        <input type="text" placeholder="example@email.com" id="email" name="email"><br>
                    </li>
                    <li>
                        <label>Password</label>
                        <input type="password" id="password" name="password"><br>
                    </li>
                    <li>
                    <li>
                        <button ng-click="lookup()" class="submit">
                            <span aria-hidden="true" data-icon="&#128269;" class="search-icon"></span>
                            <span>Login</span>
                        </button>
                    </li>

                        <div id="fileuploader">
                            <noscript><p>Please enable Javascript to use the file uploader</p></noscript>
                        </div>
                        <div id="fileuploaded">

                        </div>
                    </li>
                    <li class="submit-li">
                        <label>&nbsp;</label>
                        <button id="form_submit" class="submit">Submit</button>
                    </li>
                </ol>
                <input type="hidden" id="fileName" name="fileName">
                <input type="hidden" id="header" name="header">
                <input type="hidden" id="type" name="type">
            </form>
        </div>
    </jsp:body>
</sage:wrapper>