package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.dao.model.JobProcessDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.job.*;
import gov.nysenate.sage.model.job.JobFile;
import gov.nysenate.sage.model.result.JobErrorResult;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.auth.JobUserAuth;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.*;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.List;

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

            request.setAttribute("downloadDir", config.getValue("job.user.download.dir"));
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
            request.getRequestDispatcher("/joblogin.jsp").forward(request, response);
        }
    }

    public void doUpload(HttpServletRequest request, HttpServletResponse response)
    {
        String uploadDir = config.getValue("job.upload.dir");

        Object uploadResponse = null;
        BufferedReader source = null;
        ICsvListReader jobReader = null;
        ICsvListWriter jobWriter = null;

        logger.debug("Performing file upload...");
        try {
            String sourceFilename = request.getHeader("X-File-Name");
            if (sourceFilename == null) {
                logger.error("X-File-Name not found on submission.");
                uploadResponse = new JobErrorResult("Failed to retrieve file from payload");
            }
            else {
                JobFile jobFile = new JobFile();

                /** Open the uploaded file and a writer to its new destination */
                source = new BufferedReader(new InputStreamReader(request.getInputStream()));
                File targetFile = new File(uploadDir, + (new Date().getTime()) + "-" + sourceFilename.replaceAll("( |%20)","_"));
                FileWriter fileWriter = new FileWriter(targetFile);

                logger.debug("Setting target upload file as " + targetFile.getAbsolutePath());

                jobReader = new CsvListReader(source, CsvPreference.TAB_PREFERENCE);
                jobWriter = new CsvListWriter(fileWriter, CsvPreference.TAB_PREFERENCE);

                String[] header = jobReader.getHeader(true);
                jobWriter.writeHeader(header);
                header = jobFile.processHeader(header);

                logger.debug("Processed file header: " + FormatUtil.printObject(header));

                /** Check JobFileType enum to see if header is ok for processing */
                if (!jobFile.hasAddress()) {
                    logger.error("Uploaded job file does not have any address fields!");
                    uploadResponse = new JobErrorResult("Uploaded job file does not have required address columns!");
                }
                else if(!jobFile.requiresGeocode() && !jobFile.requiresDistrictAssign()) {
                    logger.error("Uploaded job file does not have any geocode or district assignment columns.");
                    uploadResponse = new JobErrorResult("Uploaded job file does not have any geocode " +
                                                        "or district assignment columns!");
                }
                else {
                    logger.info("Creating file: " + targetFile.getAbsolutePath());

                    /** Transfer file contents and count the records. Use the same line delimiter as the source file. */
                    int count = 0;
                    final CellProcessor[] processors = jobFile.getProcessors().toArray(new CellProcessor[0]);
                    List<Object> row;
                    while ((row = jobReader.read(processors)) != null) {
                        jobWriter.write(row, processors);
                        count++;
                    }

                    logger.debug("Wrote " + count + " records into target file.");

                    /** Create a new job process for every uploaded file */
                    JobProcess process = new JobProcess();
                    process.setSourceFileName(sourceFilename);
                    process.setFileName(targetFile.getName());
                    process.setRecordCount(count);
                    process.setRequestor(getJobUser(request));
                    process.setGeocodeRequired(jobFile.requiresGeocode());
                    process.setDistrictRequired(jobFile.requiresDistrictAssign());
                    getJobRequest(request).addProcess(process);

                    logger.debug(getJobRequest(request).getProcesses().size() + " queued for this request.");

                    /** Send a success status back to the ajax uploader */
                    JobUploadStatus uploadStatus = new JobUploadStatus();
                    uploadStatus.setSuccess(true);
                    uploadStatus.setProcess(process);
                    uploadResponse = uploadStatus;

                    logger.debug("Replying to upload request with " + FormatUtil.toJsonString(uploadResponse));
                }
            }
        }
        catch(IOException ex) {
            logger.error("Failed to read file", ex);
            uploadResponse = new JobErrorResult("Failed to read file!");
        }
        finally {
            IOUtils.closeQuietly(source);
            IOUtils.closeQuietly(jobReader);
            IOUtils.closeQuietly(jobWriter);
            logger.debug("Closed resources.");
        }
        setJobResponse(uploadResponse, response);
    }

    public void doSubmit(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        logger.info("Processing Job Request Submission.");
        JobProcessDao jobProcessDao = new JobProcessDao();
        JobRequest jobRequest = getJobRequest(request);

        FormatUtil.printObject(jobRequest);
        FormatUtil.printObject(request.getSession().getAttribute("authenticated"));
        FormatUtil.printObject(request.getSession().getAttribute("jobuser"));
        FormatUtil.printObject(request.getSession().getAttribute("jobrequest"));

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
}