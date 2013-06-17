package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.controller.api.BaseApiController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminViewController extends BaseApiController
{
    @Override
    public void init(ServletConfig config) throws ServletException {}

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/adminView.jsp").forward(request, response);
    }
}
