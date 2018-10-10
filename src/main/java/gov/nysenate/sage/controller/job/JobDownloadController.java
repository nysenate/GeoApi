package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.util.Config;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;

public class JobDownloadController extends BaseJobController implements Observer
{
    private static Logger logger = LogManager.getLogger(JobDownloadController.class);
    private static Config config = ApplicationFactory.getConfig();
    private static String DOWNLOAD_DIR;

    @Override
    public void init(ServletConfig config) throws ServletException {
        update(null,null);
    }

    @Override
    public void update(Observable o, Object arg) {
        DOWNLOAD_DIR = config.getValue("job.download.dir");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String fileName = request.getPathInfo();
        if (fileName != null) {
            fileName = fileName.replaceFirst("/", "");
            File file = new File(DOWNLOAD_DIR + fileName);
            if (file.exists()) {
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
                InputStream inputStream = new FileInputStream(file);
                try {
                    int c;
                    while ((c = inputStream.read()) != -1) {
                        response.getWriter().write(c);
                    }
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }
            else {
                response.getWriter().write("File does not exist");
            }
        }
    }
}
