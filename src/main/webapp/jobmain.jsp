<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<% request.setAttribute("contextPath", request.getContextPath()); %>

<sage:wrapper>
    <jsp:attribute name="ngApp">sage-job</jsp:attribute>
    <jsp:attribute name="title">SAGE - Batch Services Main</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
        <link href="${pageContext.request.contextPath}/css/vendor/fileuploader.css" rel="stylesheet" type="text/css"/>
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="${pageContext.request.contextPath}/js/vendor/fileuploader.js" type="text/javascript"></script>
        <script src="${pageContext.request.contextPath}/js/job.js" type="text/javascript"></script>
        <script>
            downloadDir = "${downloadDir}";
        </script>
    </jsp:attribute>
    <jsp:body>
        <sage:header>
            <jsp:attribute name="ngController">MenuController</jsp:attribute>
            <jsp:attribute name="links">
                <li><a ng-click="toggleMethod(1)" class="active">New batch job</a></li>
                <li><a ng-click="toggleMethod(2)">Current jobs</a></li>
                <li><a ng-click="toggleMethod(3)">History</a></li>
                <li><a href="${pageContext.request.contextPath}/job/logout">Logout</a></li>
            </jsp:attribute>
        </sage:header>

        <div id="contentwrapper" ng-controller="JobController">
            <div id="contentcolumn" style="margin:0;padding-top:20px;background-color:#f5f5f5">
                <div id="upload-container" ng-show="visible"  ng-controller="JobUploadController">
                    <form id="uploadForm" method="post" action="${contextPath}/job/submit" style="width:95%;margin:auto;">
                        <ol>
                            <li>
                                <h3 style="color:#333">Upload files for processing</h3>
                            </li>
                            <li>
                                <div id="fileuploaded" style="padding:20px;">
                                    <div>
                                        <table class="job-table">
                                            <thead style="text-align:left;border-bottom: 1px solid #999">
                                            <tr>
                                                <th width="450px">File name</th>
                                                <th>Geocode</th>
                                                <th>District Assign</th>
                                                <th>Record Count</th>
                                            </tr>
                                            </thead>
                                            <tr ng-repeat="process in processes">
                                                <td>{{process.sourceFileName}}</td>
                                                <td>{{process.geocodeRequired | yesno}}</td>
                                                <td>{{process.districtRequired | yesno}}</td>
                                                <td>{{process.recordCount}}</td>
                                            </tr>
                                            <tr ng-show="empty">
                                                <td><p style="color:#444;">Upload queue is empty</p></td>
                                            </tr>
                                        </table>
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
                    <div style="text-align: center;margin-left:20px;">
                        <h3 ng-show="runningProcesses.length">Running jobs</h3>
                        <div class="running-process-view" ng-repeat="runningProcess in runningProcesses">
                            <table class="job-table">
                                <tr>
                                    <td><span style="color:teal;font-weight:bold;">Job {{runningProcess.processId}} - {{runningProcess.process.sourceFileName}}</span></td>
                                    <td><span></span></td>
                                    <td style="text-align: right;"><span>Started - {{runningProcess.startTime | date:'medium'}}</span></td>
                                </tr>
                                <tr>
                                    <td colspan="3">
                                        <div id="current-job" style="width:100%;margin:auto;">
                                            <div id="progress-bar" style="margin:auto;height:30px;border:1px solid #aaa;">
                                                <div ng-style="runningProcess.progressStyle" style="height:100%;background: #7BA838;">&nbsp;</div>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="3">
                                        <span>Records Completed: {{runningProcess.completedRecords}} / {{runningProcess.process.recordCount}}</span>
                                    </td>
                                </tr>
                            </table>
                        </div>

                        <div ng-show="showProcessQueue();">
                            <h3>Queued jobs</h3>
                            <div style="padding:20px;background:#fefefe;box-shadow: 0px 3px 2px #ddd">
                                <table class="job-table">
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
                                        <td style="color:teal">{{activeProcess.process.sourceFileName}}</td>
                                        <td>{{activeProcess.process.requestorEmail}}</td>
                                        <td>{{activeProcess.process.recordCount}}</td>
                                        <td>{{activeProcess.process.requestTime | date:'medium'}}</td>
                                        <td>{{activeProcess.condition}}</td>
                                    </tr>
                                </table>
                            </div>
                        </div>

                        <h3>Recently Completed Jobs</h3>
                        <div style="padding:20px;background:#fefefe;box-shadow: 0px 3px 2px #ddd">
                            <table class="job-table">
                                <thead style="text-align:left;border-bottom: 1px solid #999">
                                <tr>
                                    <th>Job Id</th>
                                    <th>File name</th>
                                    <th>Completed On</th>
                                    <th>Download Link</th>
                                </tr>
                                </thead>
                                <tr ng-repeat="completedProcess in completedProcesses">
                                    <td style="padding-top: 5px;">{{completedProcess.processId}}</td>
                                    <td style="color:teal">{{completedProcess.process.sourceFileName}}</td>
                                    <td>{{completedProcess.completeTime | date:'short'}}</td>
                                    <td>
                                        <a style="background:#477326;color:white;padding:2px;" ng-href="${downloadDir}{{completedProcess.process.fileName}}">Download</a>
                                    </td>
                                </tr>
                                <tr ng-hide="completedProcesses">
                                    <td></td>
                                    <td>Nothing recently completed.</td>
                                    <td></td>
                                    <td></td>
                                </tr>
                            </table>
                        </div>


                    </div>

                </div>
                <div id="history-container" ng-show="visible" ng-controller="JobHistoryController" style="width:100%;height:100%;">
                    <div style="text-align: center;margin-left:20px;">
                        <h3>Batch Job History</h3>
                        <div style="padding:20px;background:#fefefe;box-shadow: 0px 3px 2px #ddd">
                            <table class="job-table">
                                <thead style="text-align:left;border-bottom: 1px solid #999">
                                <tr>
                                    <th>Job Id</th>
                                    <th>File name</th>
                                    <th>Records</th>
                                    <th>Started</th>
                                    <th>Completed</th>
                                    <th>Status</th>
                                </tr>
                                </thead>
                                <tr ng-repeat="allProcess in allProcesses">
                                    <td>{{allProcess.processId}}</td>
                                    <td>{{allProcess.process.sourceFileName}}</td>
                                    <td>{{allProcess.process.recordCount}}</td>
                                    <td>{{allProcess.startTime | date:'medium'}}</td>
                                    <td>{{allProcess.completeTime | date:'medium'}}</td>
                                    <td>{{allProcess.condition}}</td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </jsp:body>
</sage:wrapper>