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
        <div id="contentwrapper">
            <div id="contentcolumn" style="margin:0px 100px 0px 300px;padding-top:20px;">
                <div id="upload-container" ng-show="visible"  ng-controller="JobUploadController" style="width:100%;height:100%;">
                    <form id="uploadForm" method="post" action="${contextPath}/job/submit" style="width:650px;margin:auto;">
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
                                        <p ng-show="empty" style="color:#444;">Upload queue is empty</p>
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
                    </form>
                </div>
                <div id="status-container" ng-show="visible" ng-controller="JobStatusController" style="width:100%;height:100%;">
                    <div style="text-align: center;">
                        <h3 style="color:#333">Current batch jobs</h3>
                        <div id="progress-bar" style="margin:auto;width:600px;height:30px;border:1px solid #ddd;">
                            <div style="height:100%;width:300px;background: #7BA838;">&nbsp;</div>
                        </div>
                        <h4>Queued jobs</h4>
                        <table style="width:100%">
                            <thead style="text-align:left;border-bottom: 1px solid #999">
                            <tr>
                                <th width="450px">File name</th><th width="100px">Type</th><th>Records</th><th>Status</th><th>Status</th>
                            </tr>
                            </thead>
                            <tr ng-repeat="process in processes">
                                <td>{{process.fileName}}</td><td>{{process.fileType}}</td><td>{{process.recordCount}}</td>
                            </tr>
                        </table>
                        <p ng-show="empty" style="color:#444;">Upload queue is empty</p>
                    </div>

                </div>
            </div>
        </div>

        <div id="leftcolumn" ng-controller="MenuController">
            <div class="innertube">
                <p ng-click="toggleView(1)" class="method-header active teal">Start new batch jobs</p>
                <p ng-click="toggleView(2)" class="method-header maroon">View running batch jobs</p>
                <p ng-click="toggleView(3)" class="method-header purple">View batch job history</p>
            </div>
        </div>

    </jsp:body>
</sage:wrapper>