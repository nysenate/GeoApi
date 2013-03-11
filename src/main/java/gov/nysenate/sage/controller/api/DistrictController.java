package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.service.ServiceProviders;
import gov.nysenate.sage.service.district.DistrictService;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DistrictController extends BaseApiController
{
    private Logger logger = Logger.getLogger(DistrictController.class);
    private ServiceProviders<DistrictService> districtServiceProviders;

    @Override
    public void init(ServletConfig config) throws ServletException
    {

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
