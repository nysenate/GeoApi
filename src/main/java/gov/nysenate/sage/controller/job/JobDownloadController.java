package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.config.Environment;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping(value = "/job")
public class JobDownloadController {
    private final String downloadDir;

    @Autowired
    public JobDownloadController(Environment env) {
        this.downloadDir = env.getJobDownloadDir();
    }

    /**
     * Job Download Api
     * ---------------------
     * Download a completed job file
     * Usage:
     * (GET)    /job/download/{fileName:.+}
     *
     */
    @GetMapping(value = "/download/{fileName:.+}")
    public void jobDownload(HttpServletResponse response, @PathVariable String fileName) throws IOException {
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("JOB_USER")) {
            if (fileName != null) {
                File file = new File(downloadDir + fileName);
                if (file.exists()) {
                    response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
                    try (InputStream inputStream = new FileInputStream(file)) {
                        int c;
                        while ((c = inputStream.read()) != -1) {
                            response.getWriter().write(c);
                        }
                    }
                }
                else {
                    response.getWriter().write("File does not exist");
                }
            }
        }
    }
}
