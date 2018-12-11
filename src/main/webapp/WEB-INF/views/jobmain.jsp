<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sage" tagdir="/WEB-INF/tags" %>
<% request.setAttribute("contextPath", request.getContextPath()); %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<fmt:setLocale value = "es_ES"/>

<sage:wrapper>
    <jsp:attribute name="ngApp">sage-job</jsp:attribute>
    <jsp:attribute name="title">SAGE - Batch Services Main</jsp:attribute>
    <jsp:attribute name="cssIncludes">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/vendor/jquery.dataTables-1.9.4.css">
        <link href="${pageContext.request.contextPath}/css/vendor/fileuploader.css" rel="stylesheet" type="text/css"/>
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <sage:common></sage:common>
        <sage:job></sage:job>
        <script src="${pageContext.request.contextPath}/js/vendor/fileuploader.js" type="text/javascript"></script>
        <script src="${pageContext.request.contextPath}/js/vendor/jquery.dataTables-1.9.4.min.js" type="text/javascript"></script>
        <script>
            downloadBaseUrl = "${downloadBaseUrl}";
        </script>
    </jsp:attribute>
    <jsp:body>
        <sage:header>
            <jsp:attribute name="ngController">MenuController</jsp:attribute>
            <jsp:attribute name="links">
                <li><a ng-click="toggleMethod(1)" ng-class="getMethodClass(1)">New batch job</a></li>
                <li><a ng-click="toggleMethod(2)" ng-class="getMethodClass(2)">Current jobs</a></li>
                <li><a ng-click="toggleMethod(3)" ng-class="getMethodClass(3)">History</a></li>
                <li><a href="${pageContext.request.contextPath}/job/logout">Logout</a></li>
            </jsp:attribute>
        </sage:header>

        <div id="contentwrapper" style="height:auto;padding-bottom:20px;" ng-controller="JobController">
            <div id="contentcolumn" style="margin:0;padding-top:20px;background-color:#f5f5f5">
                <div id="upload-container" ng-show="visible"  ng-controller="JobUploadController" style="width:100%;height:100%;">
                    <form id="uploadForm" ng-submit="submitJobRequest()" style="width:95%;margin:auto;">
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
                                                <th>USPS Validate</th>
                                                <th>Geocode</th>
                                                <th>District Assign</th>
                                                <th>Record Count</th>
                                                <th>Actions</th>
                                            </tr>
                                            </thead>
                                            <tr ng-repeat="process in processes">
                                                <td>{{process.sourceFileName}}</td>
                                                <td>{{process.validationRequired | yesno}}</td>
                                                <td>{{process.geocodeRequired | yesno}}</td>
                                                <td>{{process.districtRequired | yesno}}</td>
                                                <td>{{process.recordCount}}</td>
                                                <td>
                                                    <div class="cancel" ng-click="removeFile(process.fileName);">Remove</div>
                                                </td>
                                            </tr>
                                            <tr ng-show="empty">
                                                <td colspan="4"><p style="color:#444;">The upload queue is empty. Use the Upload File button below to add files.</p></td>
                                            </tr>
                                        </table>
                                    </div>
                                    <div style="margin-top:10px;">
                                        <div id="fileUploaderBasic" class="qq-upload-button">
                                            <div class="icon-upload icon-teal"></div> Upload a file
                                        </div>
                                        <div id="fileUploadProgress" ng-style="getProgressStyle()">&nbsp;</div>
                                    </div>
                                </div>
                                <noscript>
                                    Please enable JavaScript in your browser.
                                </noscript>
                            </li>
                            <li class="submit-li">
                                <label>&nbsp;</label>
                                <button class="submit" ng-disabled="empty"><span id="form_submit">Submit Batch Request</span></button>
                            </li>
                        </ol>
                    </form>
                    <div id="jobInstructions" style="font-size:14px;">
                        <h3>Guidelines</h3>
                        <p>The job processor performs bulk geocoding and district assignment given a list of addresses.</p>
                        <p>The file that is uploaded must be formatted in order to be processed successfully.</p>
                        <br/>
                        <p><strong>Supported formats</strong> - .tsv | .csv | .txt</p>
                        <p><strong>Supported delimiters</strong> - tab | comma | semi-colon</p>
                        <br/>
                        <p>The following tables list the columns that are processed. The columns are read from the first line of the file.</p>
                        <p>The aliases are the names that can be used to represent the column. The aliases are shown camelCased but </p>
                        <p>they may also be underscored e.g streetAddress = street_address</p>
                        <br/>
                        <hr style="width:500px;outline:0;border:0;border-top:1px solid #ddd;"/>
                        <p>Each row must contain an address. If the address is not parsed it can be defined by just the Street column.</p>
                        <br/>
                        <p><strong style="color:teal">Address columns</strong></p>
                        <table class="columnAliasTable">
                            <tr>
                                <th style="width:110px;">Column</th>
                                <th>Aliases</th>
                            </tr>
                            <tr>
                                <td>Street</td>
                                <td><code>street | streetAddress</code></td>
                            </tr>
                            <tr>
                                <td>City</td>
                                <td><code>city</code></td>
                            </tr>
                            <tr>
                                <td>State</td>
                                <td><code>state | stateProvinceId</code></td>
                            </tr>
                            <tr>
                                <td>Zip5</td>
                                <td><code>zip5 | zip | postal | postalCode</code></td>
                            </tr>
                            <tr>
                                <td>Zip4</td>
                                <td><code>zip4 | postalSuffix | postalCodeSuffix</code></td>
                            </tr>
                        </table>
                        <br/>
                        <p>If USPS address columns are specified, the processor will perform address correction.</p>
                        <p>The Address columns above are used as input and the USPS Address columns will contain the corrected address.</p>
                        <br/>
                        <p><strong style="color:teal">USPS Address columns</strong></p>
                        <table class="columnAliasTable">
                            <tr>
                                <th style="width:110px;">Column</th>
                                <th>Aliases</th>
                            </tr>
                            <tr>
                                <td>USPS Street</td>
                                <td><code>uspsStreetAddress | uspsStreet</code></td>
                            </tr>
                            <tr>
                                <td>USPS City</td>
                                <td><code>uspsCity</code></td>
                            </tr>
                            <tr>
                                <td>USPS State</td>
                                <td><code>uspsState</code></td>
                            </tr>
                            <tr>
                                <td>USPS Zip5</td>
                                <td><code>uspsZip5</code></td>
                            </tr>
                            <tr>
                                <td>USPS Zip4</td>
                                <td><code>uspsZip4</code></td>
                            </tr>
                        </table>
                        <br/>
                        <p>If geocode columns are specified, the processor will perform geocoding.</p>
                        <br/>
                        <p><strong style="color:teal">Geocode columns</strong></p>
                        <table class="columnAliasTable">
                            <tr>
                                <th style="width:110px;">Column</th>
                                <th>Aliases</th>
                            </tr>
                            <tr>
                                <td>Latitude</td>
                                <td><code>latitude | lat | geoCode1</code></td>
                            </tr>
                            <tr>
                                <td>Longitude</td>
                                <td><code>longitude | lon | lng | geoCode2</code></td>
                            </tr>
                            <tr>
                                <td>Geocode Method</td>
                                <td><code>geoMethod | geoSource</code></td>
                            </tr>
                            <tr>
                                <td>Geocode Quality</td>
                                <td><code>geoQuality | accuracy</code></td>
                            </tr>
                        </table>
                        <br/>
                        <p>If any district assignment columns are specified, the processor will perform district assignment.</p>
                        <br/>
                        <p><strong style="color:teal">District code columns</strong></p>
                        <table class="columnAliasTable">
                            <tr>
                                <th style="width:110px;">Column</th>
                                <th>Aliases</th>
                            </tr>
                            <tr>
                                <td>Senate</td>
                                <td><code>senate | nySenateDistrict47 | sd | senateDistrict</code></td>
                            </tr>
                            <tr>
                                <td>Assembly</td>
                                <td><code>assembly | nyAssemblyDistrict48 |  ad | assemblyDistrict</code></td>
                            </tr>
                            <tr>
                                <td>Congressional</td>
                                <td><code>congressional | congressionalDistrict46 | cd | congressionalDistrict</code></td>
                            </tr>
                            <tr>
                                <td>Town</td>
                                <td><code>town | town52 | townCode</code></td>
                            </tr>
                            <tr>
                                <td>School</td>
                                <td><code>school | schoolDistrict54 | schoolDistrict</code></td>
                            </tr>
                            <tr>
                                <td>Ward</td>
                                <td><code>ward | ward53 | wardCode</code></td>
                            </tr>
                            <tr>
                                <td>Election</td>
                                <td><code>election | electionDistrict49 | electionDistrict | ed</code></td>
                            </tr>
                        </table>
                        <br/>
                    </div>
                </div>
                <div id="status-container" ng-show="visible" ng-controller="JobStatusController" style="width:100%;">
                    <div style="text-align: center;padding:20px;width:95%;margin:auto;">
                        <span style="color:#CC333F;font-weight:bold;" ng-hide="processorRunning">Job processor is not running.</span>
                        <span style="color:#639A00;font-weight:bold;" ng-show="processorRunning">Job processor is running.</span>
                        <h3 ng-show="runningProcesses.length" style="color:#333;">Running Job</h3>
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

                        <div style="padding:20px;width:95%;margin:auto;" ng-show="showProcessQueue();">
                            <h3 style="color:#333;">Queued jobs</h3>
                            <div>
                                <table class="job-table">
                                    <thead style="text-align:left;border-bottom: 1px solid #999">
                                    <tr>
                                        <th>Job Id</th>
                                        <th style="width:240px;">File name</th>
                                        <th>Submitter</th>
                                        <th>Records</th>
                                        <th>Submitted On</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                    </thead>
                                    <tr ng-repeat="activeProcess in activeProcesses">
                                        <td>{{activeProcess.processId}}</td>
                                        <td style="color:teal">{{activeProcess.process.sourceFileName}}</td>
                                        <td>{{activeProcess.process.requestorEmail}}</td>
                                        <td>{{activeProcess.process.recordCount}}</td>
                                        <td>{{activeProcess.process.requestTime | date:'medium'}}</td>
                                        <td>{{activeProcess.condition | conditionFilter}}</td>
                                        <td>
                                            <button  class="cancel" ng-click="cancelJobProcess(activeProcess.processId)">Cancel</button>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>

                        <div style="padding:20px;width:95%;margin:auto;">
                            <h3 style="color:#333;">Recently Completed Jobs</h3>
                            <div>
                                <table class="job-table">
                                    <thead style="text-align:left;border-bottom: 1px solid #999">
                                    <tr>
                                        <th>Job Id</th>
                                        <th>File name</th>
                                        <th>Submitted By</th>
                                        <th>Started On</th>
                                        <th>Completed On</th>
                                        <th>Download Link</th>
                                    </tr>
                                    </thead>
                                    <tr ng-repeat="completedProcess in completedProcesses">
                                        <td style="padding-top: 5px;">{{completedProcess.processId}}</td>
                                        <td style="color:teal">{{completedProcess.process.sourceFileName}}</td>
                                        <td>{{completedProcess.process.requestorEmail}}</td>
                                        <td>{{completedProcess.startTime | date:'short'}}</td>
                                        <td>{{completedProcess.completeTime | date:'short'}}</td>
                                        <td>
                                            <a style="background:#477326;color:white;padding:2px;" ng-href="${downloadBaseUrl}download/{{completedProcess.process.fileName}}">Download</a>
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
                </div>
                <div id="history-container" ng-show="visible" ng-controller="JobHistoryController" style="width:100%;height:100%;">
                    <div style="text-align: center; padding:20px;width:95%;margin:auto;">
                        <h3 style="color:#333;">Batch Job History</h3>
                        <div>
                            <table class="job-table">
                                <thead style="text-align:left;border-bottom: 1px solid #999">
                                <tr>
                                    <th>Job Id</th>
                                    <th>File name</th>
                                    <th>Submitted by</th>
                                    <th>Records</th>
                                    <th>Started</th>
                                    <th>Completed</th>
                                    <th>Status</th>
                                    <th>Download Link</th>
                                </tr>
                                </thead>
                                <tr ng-hide="allProcesses">
                                    <td>No files have been processed.</td>
                                </tr>
                                <tr ng-repeat="process in allProcesses">
                                    <td>{{process.processId}}</td>
                                    <td>{{process.process.sourceFileName}}</td>
                                    <td>{{process.process.requestorEmail}}</td>
                                    <td>{{process.process.recordCount}}</td>
                                    <td>{{process.startTime | date:'medium'}}</td>
                                    <td>{{process.completeTime | date:'medium'}}</td>
                                    <td ng-style="getConditionStyle(process.condition)">{{process.condition | conditionFilter}}</td>
                                    <td>
                                        <a ng-show="process.condition | conditionSuccess" style="padding:2px;" ng-href="${downloadBaseUrl}{{process.process.fileName}}">Download</a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </jsp:body>
</sage:wrapper>
</body>
</html>
