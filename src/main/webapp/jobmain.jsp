<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<% request.setAttribute("contextPath", request.getContextPath()); %>

<sage:wrapper>
    <jsp:attribute name="title">SAGE - Batch Services Main</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link href="css/fileuploader.css" rel="stylesheet" type="text/css"/>
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="js/fileuploader.js" type="text/javascript"></script>
        <script src="js/job.js" type="text/javascript"></script>
    </jsp:attribute>
    <jsp:body>
        <h1 style="text-align: center; color:#333;">SAGE Batch Services</h1>
        <div style="width:650px;margin:auto;text-align: center;" id="upload-container" ng-controller="JobUploadController">
            <form id="uploadForm" method="post" action="${contextPath}/job/submit">
                <ol>
                    <li>
                        <h3 style="color:#333">Upload files for processing</h3>
                    </li>
                    <li>
                        <div id="fileuploaded" style="padding:20px;">
                            <div>
                                <table style="width:100%">
                                    <thead style="text-align:left;border-bottom: 1px solid #999">
                                    <tr>
                                        <th width="450px">File name</th><th width="100px">Type</th><th>Records</th>
                                    </tr>
                                    </thead>
                                    <tr ng-repeat="process in processes">
                                        <td>{{process.fileName}}</td><td>{{process.fileType}}</td><td>{{process.recordCount}}</td>
                                    </tr>
                                </table>
                                <p ng-show="empty" style="color:#444;">No files have been uploaded yet</p>
                            </div>
                        </div>
                        <div id="fileuploader">
                            <noscript><p>Please enable Javascript to use the file uploader</p></noscript>
                        </div>
                    </li>
                    <li class="submit-li">
                        <label>&nbsp;</label>
                        <button class="submit" ng-disabled="empty"><span id="form_submit">Submit Batch Request</span></button>
                    </li>
                </ol>
                <input type="hidden" id="fileName" name="fileName">
                <input type="hidden" id="header" name="header">
                <input type="hidden" id="type" name="type">
            </form>
        </div>
    </jsp:body>
</sage:wrapper>