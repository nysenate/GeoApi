package gov.nysenate.sage.servlets;

import gov.nysenate.sage.BulkProcessing.Processor;
import gov.nysenate.sage.model.BulkProcessing.BulkFileType;
import gov.nysenate.sage.model.BulkProcessing.JobProcess;
import gov.nysenate.sage.util.Connect;
import gov.nysenate.sage.util.Resource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class UploadServlet
 */
@SuppressWarnings("serial")
public class BulkServlet extends HttpServlet {
    private Logger logger;
    private File uploadDir;
    private Resource appConfig;

    class SubmitException extends Exception {
        public SubmitException() { super(); }
        public SubmitException(String message) { super(message); }
    }

    class UploadException extends Exception {
        public UploadException() { super(); }
        public UploadException(String message) { super(message); }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        logger = Logger.getLogger(BulkServlet.class);

        try {
            appConfig = new Resource();
        } catch (IOException e) {
            throw new ServletException("Could not open app.properties for reading.", e);
        }

        try {
            uploadDir = new File(appConfig.fetch("bulk.uploads"));
            FileUtils.forceMkdir(uploadDir);
        } catch (IOException e) {
            throw new ServletException("Could not create uploads folder.", e);
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/upload.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String command = request.getPathInfo() == null ? "" : request.getPathInfo();
        if (command.equals("/submit")) {
            processSubmission(request, response);
        } else if (command.equals("/upload")) {
            processUpload(request, response);
        } else {
            logger.info("Invalid command '"+command+"'");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void processSubmission(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        logger.info("Processing Upload Submission.");
        HttpSession session = request.getSession();
        String email = request.getParameter("email");
        String fileName = request.getParameter("fileName");
        String header = request.getParameter("header");
        String uploadedFilename = (String)session.getAttribute("uploadedFilename");
        BulkFileType bulkFileType = (BulkFileType) session.getAttribute("bulkFileType");

        try {
            if (email == null || fileName == null || header == null)
                throw new SubmitException("Missing form parameters.");

            if(uploadedFilename == null || bulkFileType == null)
                throw new SubmitException("Missing session parameters");

            File oldFile = new File(uploadDir, uploadedFilename);
            if (!oldFile.exists())
                throw new SubmitException("Uploaded file not found.");

            if (!bulkFileType.header().equals(header) || !uploadedFilename.matches(".*" + fileName + "$"))
                throw new SubmitException("Form and session parameters do not match.");

            // Save the job and return success
            Connect connect = new Connect();
            connect.persist(new JobProcess(email, fileName, bulkFileType.clazz().getName()));
            connect.close();
            request.getRequestDispatcher("/message.jsp").forward(request, response);

        } catch (SubmitException e) {
            logger.error(e);
            request.setAttribute("error","The following internal error occured while submitting the job.\n"+e.getMessage());
            request.getRequestDispatcher("/upload.jsp").forward(request, response);

        } finally {
            // Reset the session variables for this submission.
            session.removeAttribute("bulkFileType");
            session.removeAttribute("currentFilePath");
        }
    }

    private void processUpload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Processing File Upload");

        try {
            String sourceFilename = request.getHeader("X-File-Name");
            if (sourceFilename == null)
                throw new UploadException("X-File-Name not found on submission.");

            // Open the uploaded file and a writer to its new destination
            File targetFile = new File(uploadDir, + (new Date().getTime()) + "-" + sourceFilename.replaceAll("( |%20)","_"));
            BufferedReader source = new BufferedReader(new InputStreamReader(request.getInputStream()));
            BufferedWriter target = new BufferedWriter(new FileWriter(targetFile));

            //Check BulkFileType enum to see if header is ok for processing
            String header = source.readLine();
            BulkFileType bulkFileType = this.getBulkFileType(header);
            if(bulkFileType == null)
                throw new UploadException("Unrecognized Bulk file type");

            logger.info("Request type: " + bulkFileType.clazz().getSimpleName());
            logger.info("Creating file: " + targetFile.getAbsolutePath());

            // Transfer the file contents,
            //  * Count the records for estimation purposes.
            //  * Use the same line delimiter as the source file.
            String newLine = Processor.getNewLineDelim(source);
            int count = 0;
            String in = null;
            target.write(header + newLine);
            while((in = source.readLine()) != null) {
                target.write(in + newLine);
                count++;
            }
            source.close();
            target.close();

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print(new JSONObject()
                .put("success", true)
                .put("file", sourceFilename)
                .put("count", count)
                .put("systemFile", targetFile.getName())
                .put("header", header)
                .put("type", bulkFileType.type())
            );

            HttpSession session = request.getSession();
            session.setAttribute("bulkFileType", bulkFileType);
            session.setAttribute("uploadedFilename", targetFile.getName());

        } catch (UploadException e) {
            logger.error(e);
            response.getWriter().print("{\"success\":false}");
        } catch (JSONException e) {
            logger.error(e);
            response.getWriter().print("{\"success\":false}");
        }
    }

    public BulkFileType getBulkFileType(String header) {
        for(BulkFileType bulkFileType:BulkFileType.values()) {
            if(bulkFileType.header().equals(header)) {
                return bulkFileType;
            }
        }
        return null;
    }
}
