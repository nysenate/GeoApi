package gov.nysenate.sage.servlets;

import gov.nysenate.sage.BulkProcessing.Processor;
import gov.nysenate.sage.model.BulkProcessing.BulkFileType;
import gov.nysenate.sage.model.BulkProcessing.JobProcess;
import gov.nysenate.sage.util.Connect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class UploadServlet
 */
public class UploadServlet extends HttpServlet {
    private final Logger logger = Logger.getLogger(UploadServlet.class);

    private static final long serialVersionUID = 1L;

    private static String realPath;
    private static final String sep = System.getProperty("file.separator");

    public UploadServlet() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        realPath = getServletContext().getRealPath(sep).replaceAll(sep + "GeoApi.*$", "")
            + sep
            + "upload"
            + sep;
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/upload.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        PrintWriter writer = response.getWriter();

        String uri = URLDecoder.decode(request.getRequestURI(), "utf-8");

        String command = uri.replaceAll("/?(GeoApi)?/?upload/?","");

        String oldFilePath = (String) session.getAttribute("currentFilePath");
        File oldFile = null;

        if(oldFilePath != null)
            oldFile = new File(oldFilePath);

        if(command.equals("")) {
            logger.info("Processing form submission");

            String email = request.getParameter("email");
            String fileName = request.getParameter("fileName");
            String header = request.getParameter("header");

            try {
                if(oldFile == null || !oldFile.exists())
                    throw new SubmitException();
                else {
                    BulkFileType bulkFileType = (BulkFileType) session.getAttribute("bulkFileType");

                    if(bulkFileType.header().equals(header) &&
                            oldFilePath.matches(".*" + fileName + "$")) {

                        JobProcess jp = new JobProcess(email, fileName, bulkFileType.clazz().getName());

                        Connect connect = new Connect();
                        connect.persist(jp);
                        connect.close();

                        session.removeAttribute("bulkFileType");
                        session.removeAttribute("currentFilePath");

                        session.setAttribute("email", email);
                        response.sendRedirect("/GeoApi/upload/message");
                    }
                    else
                        throw new SubmitException();
                }
            }
            catch (SubmitException se) {
                request.setAttribute("email", email);
                request.setAttribute("error","There was an internal issue, please try again.");
                request.getRequestDispatcher("/upload.jsp").forward(request, response);
            }
        }
        else if(command.equals("file")) {
            logger.info("Processing file upload");

            doUpload(session, request, response, writer, oldFile);
        }
    }

    public void doUpload(HttpSession session, HttpServletRequest request,
            HttpServletResponse response, PrintWriter writer, File oldFile) throws IOException {

        BufferedReader br = null;
        BufferedWriter bw = null;

        String fileName = request.getHeader("X-File-Name").replaceAll("( |%20)","_");
        String filePath = realPath + (new Date().getTime()) + "-" + fileName;

        logger.info("Creating file: " + filePath);

        File file = new File(filePath);
        try {
            br = new BufferedReader(new InputStreamReader(request.getInputStream()));

            String newLineDelim = Processor.getNewLineDelim(br);

            bw = new BufferedWriter(new FileWriter(file));
            int count = 0;

            String header = br.readLine();
            bw.write(header + newLineDelim);

            //Check BulkFileType enum to see if header is ok for processing
            BulkFileType bulkFileType = this.getBulkFileType(header);

            if(bulkFileType == null)
                writer.print("{success:false,type:\"null\"}");
            else {
                logger.info("Request type: " + bulkFileType.clazz().getSimpleName());

                String in = null;
                while((in = br.readLine()) != null) {
                    bw.write(in + newLineDelim);

                    count++;
                }

                session.setAttribute("currentFilePath", filePath);
                session.setAttribute("bulkFileType", bulkFileType);
                response.setStatus(HttpServletResponse.SC_OK);
                writer.print("{success:true,file:\"" + fileName + "\",count:" + count +
                        ",systemFile:\"" + file.getName() + "\",header:\"" + header +
                        "\",type:\"" + bulkFileType.type() + "\"}");
            }
        }
        catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.error(e);
            writer.print("{success:false}");

            if(file.exists()) {
                file.delete();
            }
        }
        finally {
            br.close();
            bw.close();

            if(oldFile != null && oldFile.exists())
                oldFile.delete();
        }

        writer.flush();
        writer.close();
    }

    public BulkFileType getBulkFileType(String header) {
        for(BulkFileType bulkFileType:BulkFileType.values()) {
            if(bulkFileType.header().equals(header)) {
                return bulkFileType;
            }
        }
        return null;
    }

    class SubmitException extends Exception {
        private static final long serialVersionUID = 1L;

        public SubmitException() {
            super();
        }

        public SubmitException(String message) {
            super(message);
        }
    }
}
