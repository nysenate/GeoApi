package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.client.response.job.JobActionResponse;
import gov.nysenate.sage.client.response.job.JobUploadErrorResponse;
import gov.nysenate.sage.client.response.job.JobUploadSuccessResponse;
import gov.nysenate.sage.dao.model.JobProcessDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.job.*;
import gov.nysenate.sage.model.result.JobErrorResult;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.JobFileUtil;
import gov.nysenate.sage.util.auth.JobUserAuth;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class JobController extends BaseJobController
{
    private Logger logger = Logger.getLogger(JobController.class);
    private Config config = ApplicationFactory.getConfig();

    private static String JOB_MAIN_JSP = "/WEB-INF/views/jobmain.jsp";
    private static String JOB_LOGIN_JSP = "/WEB-INF/views/joblogin.jsp";
    private static String DOWNLOAD_BASE_URL = "/job/download/";

    @Override
    public void init(ServletConfig config) throws ServletException {}

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String method = request.getPathInfo();

        /** Handle log out */
        if (method != null && method.equalsIgnoreCase("/logout")) {
            doLogout(request, response);
            request.getRequestDispatcher(JOB_LOGIN_JSP).forward(request, response);
        }
        /** Handle all other requests */
        else {
            if (isAuthenticated(request)) {
                logger.debug("Authenticated! Sending to main job page");
                /** Clear out previous info */
                getJobRequest(request).clear();
                request.setAttribute("downloadBaseUrl", request.getContextPath() + DOWNLOAD_BASE_URL);
                request.getRequestDispatcher(JOB_MAIN_JSP).forward(request, response);
            }
            else {
                logger.debug("Authentication failed! Sending to login page");
                request.getRequestDispatcher(JOB_LOGIN_JSP).forward(request, response);
            }
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String method = request.getPathInfo();
        if (method != null) {
            switch (method) {
                case "/login" : {
                    doLogin(request, response); break;
                }
                case "/upload" : {
                    doUpload(request, response); break;
                }
                case "/submit" : {
                    doSubmit(request, response); break;
                }
                case "/remove" : {
                    doRemove(request, response); break;
                }
                case "/cancel" : {
                    doCancel(request, response); break;
                }
                case "/logout" : {
                    doLogout(request, response); break;
                }
                default : {
                    request.getRequestDispatcher(JOB_LOGIN_JSP).forward(request, response);
                }
            }
        }
    }

    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        JobUserAuth jobUserAuth = new JobUserAuth();
        JobUser jobUser = jobUserAuth.getJobUser(email, password);
        if (jobUser != null) {
            logger.debug("Granted job service access to " + email);
            setJobUser(request, jobUser);
            response.sendRedirect(request.getContextPath() + "/job/main");
        }
        else {
            logger.debug("Denied job service access to " + email);
            request.setAttribute("errorMessage", "Invalid credentials");
            request.getRequestDispatcher(JOB_LOGIN_JSP).forward(request, response);
        }
    }

    /**
     * Uploads the job file and verifies that it meets the criteria. If it does the file is copied
     * to the upload dir and a success response is sent. Otherwise an error response is sent.
     * @param request
     * @param response
     */
    public void doUpload(HttpServletRequest request, HttpServletResponse response)
    {
        String uploadDir = config.getValue("job.upload.dir");

        JobRequest jobRequest = getJobRequest(request);
        Object uploadResponse = null;
        String sourceFilename = request.getParameter("qqfile");

        CsvListWriter jobWriter = null;
        BufferedReader sourceReader = null;
        CsvListReader jobReader = null;

        /** Check for multi-part upload body if filename was not included as query parameter */
        if (sourceFilename == null || sourceFilename.isEmpty()) {
            boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
            if (isMultiPart) {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                try {
                    List<FileItem> fileItems = upload.parseRequest(request);
                    if (fileItems != null && fileItems.size() == 1) {
                        sourceFilename = fileItems.get(0).getName();
                    }
                }
                catch (FileUploadException ex) {
                    logger.error("File upload exception using Apache FileUpload!", ex);
                }
            }
            else {
                uploadResponse = new JobUploadErrorResponse("No file upload request found.");
            }
        }

        if (sourceFilename != null && !sourceFilename.isEmpty()) {
            try {
                /** Save the uploaded file to a temporary location */
                String tempDir = System.getProperty("java.io.tmpdir", "/tmp");
                String targetFileName = (new Date().getTime()) + "-" + sourceFilename.replaceAll("( |%20)","_");
                File tempFile = File.createTempFile(targetFileName, null, new File(tempDir));
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                IOUtils.copy(request.getInputStream(), fileOutputStream);
                IOUtils.closeQuietly(fileOutputStream);
                logger.debug("Saved uploaded file to temp location: " + tempFile.getAbsolutePath());

                /** Determine the formatting by inspecting the header */
                CsvPreference preference = JobFileUtil.getCsvPreference(tempFile);
                if (preference != null) {
                    JobFile jobFile = new JobFile();
                    sourceReader = new BufferedReader(new FileReader(tempFile));
                    jobReader = new CsvListReader(sourceReader, preference);
                    String[] header = jobFile.processHeader(jobReader.getHeader(true));
                    logger.debug("Header: " + FormatUtil.toJsonString(header));

                    /** Close and re-open the source reader */
                    sourceReader.close();
                    sourceReader = new BufferedReader(new FileReader(tempFile));

                    /** Check for address fields in header */
                    if (!jobFile.hasAddress()) {
                        logger.error("Uploaded job file does not have any address fields!");
                        uploadResponse = new JobUploadErrorResponse("Uploaded file does not have the required address columns!");
                    }
                    /** Check for geocoding or district assignment fields in header */
                    else if (!jobFile.requiresAny()) {
                        logger.error("Uploaded job file does not have any columns for populating data to.");
                        uploadResponse = new JobErrorResult("Uploaded job file does not have any columns for populating data to!");
                    }
                    /** Save file into upload directory where it will be picked up by a job process */
                    else {
                        File targetFile = new File(uploadDir, targetFileName);
                        FileUtils.copyFile(tempFile, targetFile);
                        logger.debug("Copied file to: " + targetFile.getAbsolutePath());

                        /** Count the number of rows. Start at -1 so that the header is not included. */
                        int recordCount = -1;
                        String line = null;
                        while ((line = sourceReader.readLine()) != null) {
                            recordCount++;
                        }

                        logger.debug("Counted " + recordCount + " records in file.");

                        /** Create a new job process and store in the current session */
                        JobProcess process = new JobProcess();
                        process.setSourceFileName(sourceFilename);
                        process.setFileName(targetFile.getName());
                        process.setRecordCount(recordCount);
                        process.setRequestor(getJobUser(request));
                        process.setValidationRequired(jobFile.requiresAddressValidation());
                        process.setGeocodeRequired(jobFile.requiresGeocode());
                        process.setDistrictRequired(jobFile.requiresDistrictAssign());
                        jobRequest.addProcess(process);

                        logger.debug(jobRequest.getProcesses().size() + " queued for this request.");

                        /** Send a success status back to the ajax uploader */
                        uploadResponse = new JobUploadSuccessResponse(process);
                    }
                }
                else {
                    uploadResponse = new JobUploadErrorResponse("Sorry, the formatting for the file is not supported. " +
                                                                "Please use tab, comma, or semi-colon to delimit the data.");
                }
            }
            catch (IOException ex) {
                logger.error("IO Exception during file upload processing!", ex);
            }
            finally {
                IOUtils.closeQuietly(jobWriter);
                IOUtils.closeQuietly(sourceReader);
                IOUtils.closeQuietly(jobReader);
            }
        }
        setJobResponse(uploadResponse, response);
    }

    private void doRemove(HttpServletRequest request, HttpServletResponse response)
    {
        logger.info("User requested job file removal prior to submission");
        String fileName = request.getParameter("fileName");
        JobRequest jobRequest = getJobRequest(request);
        if (fileName != null && jobRequest != null) {
            if (jobRequest.getProcesses() != null && !jobRequest.getProcesses().isEmpty()) {
                Iterator<JobProcess> itr = jobRequest.getProcesses().iterator();
                while (itr.hasNext()) {
                    if (itr.next().getFileName().equalsIgnoreCase(fileName)) {
                        itr.remove();
                        setJobResponse(new JobActionResponse(true, "Removed " + fileName), response);
                        return;
                    }
                }
            }
        }
        setJobResponse(new JobActionResponse(false, "The removal request was unsuccessful."), response);
    }

    /**
     *
     * @param request
     * @param response
     * @throws IOException
     */
    public void doSubmit(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        logger.info("Processing Job Request Submission.");
        JobProcessDao jobProcessDao = new JobProcessDao();
        JobRequest jobRequest = getJobRequest(request);

        if (jobRequest.getProcesses() != null && !jobRequest.getProcesses().isEmpty()) {
            for (JobProcess jobProcess : jobRequest.getProcesses()) {
                /** Store the job process and status */
                int processId = jobProcessDao.addJobProcess(jobProcess);
                if (processId > -1) {
                    JobProcessStatus status = new JobProcessStatus(processId);
                    jobProcessDao.setJobProcessStatus(status);
                    logger.info("Added job process and status for file " + jobProcess.getFileName());
                }
                else {
                    logger.error("Failed to add job process for file " + jobProcess.getFileName());
                }
            }
            setJobResponse(new JobActionResponse(true, null), response);
        }
        else {
            setJobResponse(new JobActionResponse(false, "You must upload a file before submitting."), response);
        }
        /** The request should be cleared out */
        getJobRequest(request).clear();
    }

    /**
     * Sets the condition of a job process status to cancelled.
     * @param request
     * @param response
     */
    public void doCancel(HttpServletRequest request, HttpServletResponse response)
    {
        logger.info("Cancelling job process");
        try {
            int processId = Integer.parseInt(request.getParameter("id"));
            JobProcessDao jobProcessDao = new JobProcessDao();
            JobProcessStatus jps = jobProcessDao.getJobProcessStatus(processId);
            jps.setCondition(JobProcessStatus.Condition.CANCELLED);
            jps.setCompleted(false);
            jps.setMessages(Arrays.asList("Cancelled by user"));
            int update = jobProcessDao.setJobProcessStatus(jps);
            if (update > 0) {
                setJobResponse(new JobActionResponse(true, "Job " + processId + " has been cancelled."), response);
                return;
            }
        }
        catch (NumberFormatException ex) {
            logger.warn("Failed to parse job process id for cancellation!");
        }
        setJobResponse(new JobActionResponse(false, "Failed to cancel job process!"), response);
    }

    /**
     *
     * @param request
     * @param response
     * @throws IOException
     */
    public void doLogout(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        unsetJobUser(request);
    }
}