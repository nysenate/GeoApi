package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.dao.model.JobProcessDao;
import gov.nysenate.sage.model.result.JobErrorResult;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JobStatusController extends BaseJobController
{
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (isAuthenticated(request)) {
            String method = request.getPathInfo();
            if (method != null) {
                JobProcessDao jobProcessDao = new JobProcessDao();
                switch (method) {
                    case "process" : {
                        break;
                    }
                    case "active" : {
                        break;
                    }
                    case "inactive" : {
                        break;
                    }
                    case "all" : {
                        break;
                    }
                }
            }
        }
        else {
            setJobResponse(new JobErrorResult("You must be logged in to access job status."), response);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {}
}
