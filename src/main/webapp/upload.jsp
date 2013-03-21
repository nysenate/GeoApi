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
    <jsp:body>
        <h1 style="text-align: center; color:#222;">Batch Job Upload</h1>
        <div style="width:500px;margin:auto;">
            <form id="uploadForm" method="post">
                <ol>
                    <li>
                        <label>email</label>
                        <input type="text" placeholder="example@email.com" id="email" name="email"><br>
                    </li>
                    <li>
                        <div id="fileuploader">
                            <noscript><p>Please enable Javascript to use the file uploader</p></noscript>
                        </div>
                        <div id="fileuploaded">

                        </div>
                    </li>
                    <li class="submit-li">
                        <label>&nbsp;</label>
                        <div class="custom-submit-button custom-button"><span id="form_submit">Submit</span></div>
                    </li>
                </ol>
                <input type="hidden" id="fileName" name="fileName">
                <input type="hidden" id="header" name="header">
                <input type="hidden" id="type" name="type">
            </form>
        </div>
    </jsp:body>
    <jsp:attribute name="jsIncludes">

    </jsp:attribute>
</sage:wrapper>