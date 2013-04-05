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
            <div id="contentcolumn" style="margin:0px 50px 0px 300px;padding-top:20px;">
                <div id="upload-container" ng-show="visible"  ng-controller="JobUploadController" style="width:100%;height:100%;">
                    <form id="uploadForm" method="post" action="${contextPath}/job/submit" style="width:95%;margin:auto;">
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
                                                <th width="450px">File name</th>
                                                <th>Validate Address</th>
                                                <th>Geocode</th>
                                                <th>District Assign</th>
                                                <th>Record Count</th>
                                            </tr>
                                            </thead>
                                            <tr ng-repeat="process in processes">
                                                <td>{{process.fileName}}</td>
                                                <td>{{process.validationRequired | yesno}}</td>
                                                <td>{{process.geocodeRequired | yesno}}</td>
                                                <td>{{process.districtRequired | yesno}}</td>
                                                <td>{{process.recordCount}}</td>
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
                        <h3>Current batch jobs</h3>
                        <div id="current-job" style="width:700px;margin:auto;padding:10px;">
                            <div id="progress-bar" style="margin:auto;height:30px;border:1px solid #aaa;">
                                <div style="height:100%;width:5%;background: #7BA838;">&nbsp;</div>
                            </div>
                        </div>

                        <h3>Queued jobs</h3>
                        <table style="width:100%">
                            <thead style="text-align:left;border-bottom: 1px solid #999">
                            <tr>
                                <th>Job Id</th>
                                <th>File name</th>
                                <th>Submitter</th>
                                <th>Records</th>
                                <th>Submitted On</th>
                                <th>Status</th>
                            </tr>
                            </thead>
                            <tr ng-repeat="activeProcess in activeProcesses">
                                <td>{{activeProcess.processId}}</td>
                                <td>{{activeProcess.process.sourceFileName}}</td>
                                <td>{{activeProcess.process.requestorEmail}}</td>
                                <td>{{activeProcess.process.recordCount}}</td>
                                <td>{{activeProcess.process.requestTime}}</td>
                                <td>{{activeProcess.condition}}</td>
                            </tr>
                        </table>
                    </div>

                </div>
            </div>
        </div>

        <div id="leftcolumn" ng-controller="MenuController">
            <div class="innertube">
                <p ng-click="toggleView(1)" class="method-header  teal">Start new batch jobs</p>
                <p ng-click="toggleView(2)" class="method-header active maroon">View active batch jobs</p>
                <p ng-click="toggleView(3)" class="method-header purple">View batch job history</p>
            </div>
        </div>

    </jsp:body>
</sage:wrapper>