package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.dao.model.JobProcessDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.job.*;
import gov.nysenate.sage.model.result.JobErrorResult;
import gov.nysenate.sage.scripts.ProcessBulkUploads;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.auth.JobUserAuth;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Date;

public class JobController extends BaseJobController
{
    private Logger logger = Logger.getLogger(JobController.class);
    private Config config = ApplicationFactory.getConfig();

    @Override
    public void init(ServletConfig config) throws ServletException {}

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        logger.debug("Accessing job service");
        if (isAuthenticated(request)) {
            logger.debug("Authenticated! Sending to main job page");
            /** Clear out previous info */
            getJobRequest(request).clear();
            request.getRequestDispatcher("/jobmain.jsp").forward(request, response);
        }
        else {
            logger.info("Authentication failed! Sending to login page");
            request.getRequestDispatcher("/joblogin.jsp").forward(request, response);
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
                default : {
                    request.getRequestDispatcher("/joblogin.jsp").forward(request, response);
                }
            }
        }
    }

    public void doLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HttpSession session = request.getSession();
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
            setAuthenticated(request, false);
            request.getRequestDispatcher("/joblogin.jsp").forward(request, response);
        }
    }

    public void doUpload(HttpServletRequest request, HttpServletResponse response)
    {
        String uploadDir = config.getValue("job.upload.dir");

        Object uploadResponse = null;

        BufferedReader source = null;
        BufferedWriter target = null;
        try {
            String sourceFilename = request.getHeader("X-File-Name");
            if (sourceFilename == null) {
                logger.error("X-File-Name not found on submission.");
                uploadResponse = new JobErrorResult("Failed to retrieve file from payload");
            }
            else {
                /** Open the uploaded file and a writer to its new destination */
                File targetFile = new File(uploadDir, + (new Date().getTime()) + "-" + sourceFilename.replaceAll("( |%20)","_"));

                source = new BufferedReader(new InputStreamReader(request.getInputStream()));
                target = new BufferedWriter(new FileWriter(targetFile));

                /** Check JobFileType enum to see if header is ok for processing */
                String header = source.readLine();
                JobFileType jobFileType = this.getBulkFileType(header);

                if(jobFileType == null) {
                    logger.error("Unrecognized Bulk file type");
                    uploadResponse = new JobErrorResult("Unrecognized Bulk file type");
                }
                else {
                    logger.info("Request type: " + jobFileType.clazz().getSimpleName());
                    logger.info("Creating file: " + targetFile.getAbsolutePath());

                    /** Transfer file contents and count the records. Use the same line delimiter as the source file. */
                    String newLine = ProcessBulkUploads.getNewLineDelim(source);
                    int count = 0;
                    String in;
                    target.write(header + newLine);
                    while((in = source.readLine()) != null) {
                        target.write(in + newLine);
                        count++;
                    }

                    /** Create a new job process for every uploaded file */
                    JobProcess process = new JobProcess();
                    process.setSourceFileName(sourceFilename);
                    process.setFileName(targetFile.getName());
                    process.setFileType(jobFileType.type());
                    process.setRecordCount(count);
                    process.setRequestor(getJobUser(request));
                    getJobRequest(request).addProcess(process);

                    /** Send a success status back to the ajax uploader */
                    JobUploadStatus uploadStatus = new JobUploadStatus();
                    uploadStatus.setSuccess(true);
                    uploadStatus.setProcess(process);
                    uploadResponse = uploadStatus;
                }
            }
        }
        catch(IOException ex) {
            logger.error("Failed to read file", ex);
            uploadResponse = new JobErrorResult("Failed to read file");
        }
        finally {
            IOUtils.closeQuietly(source);
            IOUtils.closeQuietly(target);
        }

        FormatUtil.printObject(uploadResponse);
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

    public JobFileType getBulkFileType(String header)
    {
        for(JobFileType jobFileType : JobFileType.values()) {
            if(jobFileType.header().equals(header)) {
                return jobFileType;
            }
        }
        return null;
    }
}