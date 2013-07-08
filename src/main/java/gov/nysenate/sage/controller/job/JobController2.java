package gov.nysenate.sage.controller.job;

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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JobController2 extends BaseJobController
{
    private Logger logger = Logger.getLogger(JobController2.class);
    private Config config = ApplicationFactory.getConfig();

    @Override
    public void init(ServletConfig config) throws ServletException {}

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String method = request.getPathInfo();

        /** Handle log out */
        if (method != null && method.equalsIgnoreCase("/logout")) {
            doLogout(request, response);
            request.getRequestDispatcher("/joblogin.jsp").forward(request, response);
        }
        /** Handle all other requests */
        else {
            if (isAuthenticated(request)) {
                logger.debug("Authenticated! Sending to main job page");
                /** Clear out previous info */
                getJobRequest(request).clear();
                request.setAttribute("downloadDir", config.getValue("job.user.download.dir"));
                request.getRequestDispatcher("/views/job/jobmain.jsp").forward(request, response);
            }
            else {
                logger.info("Authentication failed! Sending to login page");
                request.getRequestDispatcher("/joblogin.jsp").forward(request, response);
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
                case "/logout" : {
                    doLogout(request, response); break;
                }
                default : {
                    request.getRequestDispatcher("/joblogin.jsp").forward(request, response);
                }
            }
        }
    }

    public void doLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        JobUserAuth jobUserAuth = new JobUserAuth();
        JobUser jobUser = jobUserAuth.getJobUser(email, password);
        if (jobUser != null) {
            logger.info("Granted job service access to " + email);
            setJobUser(request, jobUser);
            response.sendRedirect(request.getContextPath() + "/job/main");
        }
        else {
            logger.info("Denied job service access to " + email);
            request.setAttribute("errorMessage", "Invalid credentials");
            request.getRequestDispatcher("/joblogin.jsp").forward(request, response);
        }
    }

    public void doUpload(HttpServletRequest request, HttpServletResponse response)
    {
        String uploadDir = config.getValue("job.upload.dir");

        JobRequest jobRequest = getJobRequest(request);
        Object uploadResponse = null;
        String sourceFilename = request.getParameter("qqfile");

        FileWriter targetFileWriter = null;
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
                logger.debug("Saved uploaded file to: " + tempFile.getAbsolutePath());

                /** Determine the formatting by inspecting the header */
                CsvPreference preference = JobFileUtil.getCsvPreference(tempFile);
                if (preference != null) {
                    JobFile jobFile = new JobFile();
                    sourceReader = new BufferedReader(new FileReader(tempFile));
                    jobReader = new CsvListReader(sourceReader, preference);
                    String[] header = jobFile.processHeader(jobReader.getHeader(true));

                    /** Check for address fields in header */
                    if (!jobFile.hasAddress()) {
                        logger.error("Uploaded job file does not have any address fields!");
                        uploadResponse = new JobUploadErrorResponse("Uploaded file does not have the required address columns!");
                    }
                    /** Check for geocoding or district assignment fields in header */
                    else if(!jobFile.requiresGeocode() && !jobFile.requiresDistrictAssign()) {
                        logger.error("Uploaded job file does not have any geocode or district assignment columns.");
                        uploadResponse = new JobErrorResult("Uploaded job file does not have any geocode or district assignment columns!");
                    }
                    /** Save file into upload directory where it will be picked up by a job process */
                    else {
                        File targetFile = new File(uploadDir, targetFileName);
                        targetFileWriter = new FileWriter(targetFile);
                        jobWriter = new CsvListWriter(targetFileWriter, preference);

                        int recordCount = 0;
                        final CellProcessor[] processors = jobFile.getProcessors().toArray(new CellProcessor[0]);
                        List<Object> row;
                        while ((row = jobReader.read(processors)) != null) {
                            jobWriter.write(row, processors);
                            recordCount++;
                        }

                        logger.debug("Wrote " + recordCount + " records into target file: " + targetFile.getAbsolutePath());

                        /** Create a new job process and store in the current session */
                        JobProcess process = new JobProcess();
                        process.setSourceFileName(sourceFilename);
                        process.setFileName(targetFile.getName());
                        process.setRecordCount(recordCount);
                        process.setRequestor(getJobUser(request));
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
                IOUtils.closeQuietly(targetFileWriter);
                IOUtils.closeQuietly(jobWriter);
                IOUtils.closeQuietly(sourceReader);
                IOUtils.closeQuietly(jobReader);
            }
        }
        setJobResponse(uploadResponse, response);
    }

    public void doSubmit(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        logger.info("Processing Job Request Submission.");
        JobProcessDao jobProcessDao = new JobProcessDao();
        JobRequest jobRequest = getJobRequest(request);

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
        /** After submission the request should be cleared out */
        getJobRequest(request).clear();

        /** Redirect to main page */
        response.sendRedirect(request.getContextPath() + "/job");
    }

    public void doLogout(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        unsetJobUser(request);
    }
}