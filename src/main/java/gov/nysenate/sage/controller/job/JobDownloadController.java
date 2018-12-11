package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.config.Environment;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping(value = "job")
public class JobDownloadController
{
    private static Logger logger = LoggerFactory.getLogger(JobDownloadController.class);
    private static String DOWNLOAD_DIR;
    private final Environment env;

    @Autowired
    public JobDownloadController(Environment env) {
        this.env = env;
        DOWNLOAD_DIR = env.getJobDownloadDir();
    }

    @RequestMapping(value = "/download/{fileName:.+}", method = RequestMethod.GET)
    public void jobDownload(HttpServletRequest request, HttpServletResponse response, @PathVariable String fileName) throws IOException {
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("JOB_USER")) {
            if (fileName != null) {
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
                        inputStream.close();
                    }
                }
                else {
                    response.getWriter().write("File does not exist");
                }
            }
        }
    }
}
