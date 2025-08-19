package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.client.response.job.JobActionResponse;
import gov.nysenate.sage.client.response.job.JobUploadErrorResponse;
import gov.nysenate.sage.client.response.job.JobUploadSuccessResponse;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.model.job.SqlJobProcessDao;
import gov.nysenate.sage.model.job.*;
import gov.nysenate.sage.model.result.JobErrorResult;
import gov.nysenate.sage.service.job.JobBatchProcessor;
import gov.nysenate.sage.util.FileUtil;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.auth.JobUserAuth;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static gov.nysenate.sage.util.controller.ConstantUtil.DOWNLOAD_BASE_URL;
import static gov.nysenate.sage.util.controller.JobControllerUtil.*;

@RestController
@RequestMapping(value = "/job")
public class JobController {
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    private static final String JOB_LOGIN_JSP = "/WEB-INF/views/joblogin.jsp";
    private final Environment env;
    private final JobUserAuth jobUserAuth;
    private final SqlJobProcessDao sqlJobProcessDao;
    private final JobBatchProcessor jobBatchProcessor;


    @Autowired
    public JobController(Environment env, JobUserAuth jobUserAuth, SqlJobProcessDao sqlJobProcessDao,
                         JobBatchProcessor jobBatchProcessor) {
        this.env = env;
        this.jobUserAuth = jobUserAuth;
        this.sqlJobProcessDao = sqlJobProcessDao;
        this.jobBatchProcessor = jobBatchProcessor;
    }

    /**
     * Job Logout Api
     * ---------------------
     * Logs a job user out of the batch job section of Sage
     * Usage:
     * (GET)    /job/logout
     *
     */
    @GetMapping(value = "/logout")
    public void jobLogout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SecurityUtils.getSubject().logout();
        request.getRequestDispatcher(JOB_LOGIN_JSP).forward(request, response);
    }


    /**
     * Job Login Api
     * ---------------------
     * Logs a job user into the batch job section of Sage
     * Usage:
     * (POST)    /job/login
     *
     */
    @PostMapping(value = "/login")
    public void jobLogin(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam String email, @RequestParam String password)
            throws ServletException, IOException {
        String ipAddr = ApiControllerUtil.getIpAddress(request);

        JobUser jobUser = jobUserAuth.getJobUser(email, password);
        if (jobUser != null) {
            SecurityUtils.getSubject().login(new UsernamePasswordToken(email, jobUser.getPassword(), ipAddr));
            setJobUser(request, jobUser);
            getJobRequest(request).clear();
            request.setAttribute("downloadBaseUrl", request.getContextPath() + DOWNLOAD_BASE_URL);
            response.sendRedirect(request.getContextPath() + "/job/home");
        } else {
            request.setAttribute("errorMessage", "Invalid credentials");
            request.getRequestDispatcher(JOB_LOGIN_JSP).forward(request, response);
        }
    }

    /**
     * Job Upload Api
     * ---------------------
     * Upload a batch job file to Sage
     * Usage:
     * (POST)    /job/upload
     *
     */
    @PostMapping(value = "/upload")
    public Object jobUpload(HttpServletRequest request, @RequestParam String qqfile) throws Exception {
        return doUpload(request, qqfile);
    }

    /**
     * Job Submit Api
     * ---------------------
     * Submit a batch job file to Sage. This begins the processing of the job batch file
     * Usage:
     * (POST)    /job/submit
     *
     */
    @PostMapping(value = "/submit")
    public Object jobSubmit(HttpServletRequest request) {
        logger.info("Processing Job Request Submission.");
        JobRequest jobRequest = getJobRequest(request);

        if (jobRequest.getProcesses() != null && !jobRequest.getProcesses().isEmpty()) {
            for (JobProcess jobProcess : jobRequest.getProcesses()) {
                /* Store the job process and status */
                int processId = sqlJobProcessDao.addJobProcess(jobProcess);
                if (processId > -1) {
                    JobProcessStatus status = new JobProcessStatus(processId);
                    sqlJobProcessDao.setJobProcessStatus(status);
                    logger.info("Added job process and status for file {}", jobProcess.getFileName());
                } else {
                    logger.error("Failed to add job process for file {}", jobProcess.getFileName());
                }
            }
            getJobRequest(request).clear();
            return new JobActionResponse(true, null);
        } else {
            getJobRequest(request).clear();
            return new JobActionResponse(false, "You must upload a file before submitting.");
        }
    }

    /**
     * Remove Job Api
     * ---------------------
     * Remove a job from the job processor queue
     * Usage:
     * (POST)    /job/remove
     *
     */
    @PostMapping(value = "/remove")
    public Object jobRemove(HttpServletRequest request, @RequestParam String fileName) {
        logger.info("User requested job file removal prior to submission");
        JobRequest jobRequest = getJobRequest(request);
        if (fileName != null && jobRequest.getProcesses() != null && !jobRequest.getProcesses().isEmpty()) {
            Iterator<JobProcess> itr = jobRequest.getProcesses().iterator();
            while (itr.hasNext()) {
                if (itr.next().getFileName().equalsIgnoreCase(fileName)) {
                    itr.remove();
                    return new JobActionResponse(true, "Removed " + fileName);
                }
            }
        }
        return new JobActionResponse(false, "The removal request was unsuccessful.");
    }

    /**
     * Cancel Job Api
     * ---------------------
     * Sets the condition of a job process status to cancelled
     * Usage:
     * (POST)    /job/cancel
     *
     */
    @PostMapping(value = "/cancel")
    public Object jobCancel(@RequestParam int id) {
        logger.info("Cancelling job process");
        try {
            JobProcessStatus jps = sqlJobProcessDao.getJobProcessStatus(id);
            jps.setCondition(JobProcessStatus.Condition.CANCELLED);
            jps.setCompleted(false);
            jps.setMessages(Arrays.asList("Cancelled by user", ""));
            int update = sqlJobProcessDao.setJobProcessStatus(jps);
            if (update > 0) {
                return new JobActionResponse(true, "Job " + id + " has been cancelled.");
            }
        } catch (NumberFormatException ex) {
            logger.warn("Failed to parse job process id for cancellation!");
        }
        return new JobActionResponse(false, "Failed to cancel job process!");
    }

    /**
     * Cancel Running Job Api
     * ---------------------
     * Cancel a running job in the job processor
     * Usage:
     * (POST)    /job/cancel/running
     *
     */
    @PostMapping(value = "/cancel/running")
    public void jobCancelRunning() {
        jobBatchProcessor.cancelRunningJobs();
    }

    /**
     * Job Run Api
     * ---------------------
     * Run a job in the queue for the job processor
     * Usage:
     * (POST)    /job/run
     *
     */
    @PostMapping(value = "/run")
    public void jobRun(HttpServletRequest request) throws Exception {
        getJobRequest(request);
        jobBatchProcessor.run();
    }

    /**
     * Uploads the job file and verifies that it meets the criteria. If it does the file is copied
     * to the upload dir and a success response is sent. Otherwise, an error response is sent.
     *
     * @param request  http request from client
     */
    public Object doUpload(HttpServletRequest request, String qqfile) throws Exception {
        String uploadDir = env.getJobUploadDir();

        JobRequest jobRequest = getJobRequest(request);
        Object uploadResponse = null;
        String sourceFilename = qqfile;

        /* Check for multi-part upload body if filename was not included as query parameter */
        if (sourceFilename == null || sourceFilename.isEmpty()) {
            boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
            if (isMultiPart) {
                var factory = new DiskFileItemFactory();
                var upload = new ServletFileUpload(factory);
                try {
                    List<FileItem> fileItems = upload.parseRequest(request);
                    if (fileItems != null && fileItems.size() == 1) {
                        sourceFilename = fileItems.get(0).getName();
                    }
                } catch (FileUploadException ex) {
                    logger.error("File upload exception using Apache FileUpload!", ex);
                }
            } else {
                uploadResponse = new JobUploadErrorResponse("No file upload request found.");
            }
        }

        if (sourceFilename == null || sourceFilename.isEmpty()) {
            return uploadResponse;
        }
        /* Save the uploaded file to a temporary location */
        String tempDir = System.getProperty("java.io.tmpdir", "/tmp");
        String targetFileName = new Date().getTime() + "-" + sourceFilename.replaceAll("( |%20)", "_");
        File tempFile = File.createTempFile(targetFileName, null, new File(tempDir));
        var fileOutputStream = new FileOutputStream(tempFile);
        IOUtils.copy(request.getInputStream(), fileOutputStream);
        fileOutputStream.close();
        logger.debug("Saved uploaded file to temp location: {}", tempFile.getAbsolutePath());

        /* Determine the formatting by inspecting the header */
        CsvPreference preference = FileUtil.getCsvPreference(tempFile);
        if (preference == null) {
            return new JobUploadErrorResponse("Sorry, the formatting for the file is not supported. " +
                    "Please use tab, comma, or semi-colon to delimit the data.");
        }

        BufferedReader sourceReader = null;
        try {
            sourceReader = new BufferedReader(new FileReader(tempFile));
            var jobReader = new CsvListReader(sourceReader, preference);
            String[] header = jobReader.getHeader(true);
            jobReader.close();
            var jobFile = new JobFile(header);
            logger.debug("Header: {}", FormatUtil.toJsonString(header));

            /* Close and re-open the source reader */
            sourceReader.close();
            sourceReader = new BufferedReader(new FileReader(tempFile));

            /* Check for address fields in header */
            if (!jobFile.hasAddress()) {
                logger.error("Uploaded job file does not have any address fields!");
                uploadResponse = new JobUploadErrorResponse("Uploaded file does not have the required address columns!");
            }
            /* Check for geocoding or district assignment fields in header */
            else if (!jobFile.requiresGeocode() && !jobFile.requiresDistrictAssign()) {
                logger.error("Uploaded job file does not have any geocode or district assignment columns.");
                uploadResponse = new JobErrorResult("Uploaded job file does not have any geocode or district assignment columns!");
            }
            /* Save file into upload directory where it will be picked up by a job process */
            else {
                File targetFile = new File(uploadDir, targetFileName);
                FileUtils.copyFile(tempFile, targetFile);
                logger.debug("Copied file to: {}", targetFile.getAbsolutePath());

                /* Count the number of rows. Start at -1 so that the header is not included. */
                int recordCount = -1;
                while (sourceReader.readLine() != null) {
                    recordCount++;
                }

                logger.debug("Counted {} records in file.", recordCount);

                /* Create a new job process and store in the current session */
                var process = new JobProcess();
                process.setSourceFileName(sourceFilename);
                process.setFileName(targetFile.getName());
                process.setRecordCount(recordCount);
                process.setRequestor(getJobUser(request));
                process.setValidationRequired(jobFile.requiresAddressValidation());
                process.setGeocodeRequired(jobFile.requiresGeocode());
                process.setDistrictRequired(jobFile.requiresDistrictAssign());
                jobRequest.addProcess(process);

                logger.debug("{} queued for this request.", jobRequest.getProcesses().size());

                /* Send a success status back to the ajax uploader */
                uploadResponse = new JobUploadSuccessResponse(process);
            }
        } catch (IOException ex) {
            logger.error("IO Exception during file upload processing!", ex);
        } finally {
            if (sourceReader != null) {
                sourceReader.close();
            }
        }
        return uploadResponse;
    }
}