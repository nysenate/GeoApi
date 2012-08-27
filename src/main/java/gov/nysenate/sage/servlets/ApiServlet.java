package gov.nysenate.sage.servlets;

import gov.nysenate.sage.api.exceptions.ApiAuthenticationException;
import gov.nysenate.sage.api.exceptions.ApiCommandException;
import gov.nysenate.sage.api.exceptions.ApiFormatException;
import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.api.methods.BluebirdMethod;
import gov.nysenate.sage.api.methods.CityStateLookupMethod;
import gov.nysenate.sage.api.methods.DistrictsMethod;
import gov.nysenate.sage.api.methods.GeoCodeMethod;
import gov.nysenate.sage.api.methods.PolyMethod;
import gov.nysenate.sage.api.methods.PolySearchMethod;
import gov.nysenate.sage.api.methods.RevGeoMethod;
import gov.nysenate.sage.api.methods.StreetLookupMethod;
import gov.nysenate.sage.api.methods.ValidateMethod;
import gov.nysenate.sage.api.methods.ZipCodeLookupMethod;
import gov.nysenate.sage.model.ApiMethod;
import gov.nysenate.sage.model.ApiUser;
import gov.nysenate.sage.model.ErrorResponse;
import gov.nysenate.sage.model.Metric;
import gov.nysenate.sage.model.Point;
import gov.nysenate.sage.model.ValidateResponse;
import gov.nysenate.sage.model.districts.DistrictResponse;
import gov.nysenate.sage.util.ApiController;
import gov.nysenate.sage.util.Connect;
import gov.nysenate.sage.util.Resource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ApiServlet extends HttpServlet {
    private final Logger logger = Logger.getLogger(ApiServlet.class);
    private Resource APP_CONFIG;
    private static final long serialVersionUID = 1L;

    HashMap<String,ApiMethod> methods = null;
    ApiController control = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.info("Initializing ApiServlet");

        try {
            super.init(config);
            APP_CONFIG = new Resource();
            methods = getMethods();
            control = new ApiController();
        } catch (Exception e) {
            throw new ServletException(e);
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        Connect db = new Connect();
        String uri = java.net.URLDecoder.decode(request.getRequestURI(),"utf-8").toLowerCase().replaceAll("(/geoapi/|api/)", "");
        logger.info("Request URI: " + uri);

        StringTokenizer stok = new StringTokenizer(uri,"/");

        String key = request.getParameter("key");
        String format = null;
        String command = null;
        String type = null;

        try {
            ApiUser user = null;
            if(key == null)
                user = control.getUser(APP_CONFIG.fetch("user.default"),db);
            else
                user = control.getUser(key,db);

            if(user == null)
                throw new ApiAuthenticationException();

            format = stok.nextToken();
            command = stok.nextToken();
            type = stok.nextToken();

            ApiMethod method = null;
            if((method = methods.get(command)) != null ) {
                if(!method.outputFormats.contains(format))
                    throw new ApiFormatException(format);

                if(!method.inputTypes.contains(type))
                    throw new ApiTypeException(type);

                Object obj = method.executor.execute(request, response, getMore(format, type, stok));

                if(obj == null)
                    throw new ApiInternalException();

                if(format.equals("xml")) {
                    out.print(method.executor.toXml(obj, method.xstreamClasses));
                } else if(format.equals("json")) {
                    out.print(method.executor.toJson(obj));
                } else {
                    out.print(method.executor.toOther(obj, format));
                }

                if(method.writeMetric) {
                    addMetric(user.getId(), request.getRequestURL()+"?"+request.getQueryString(), request.getRemoteAddr(), db);
                }
            }
            else {
                throw new ApiCommandException(command);
            }
        }
        catch (ApiCommandException ace) {
            out.write(getError("error", "Invalid command: " + ace.getMessage()
                    + ", please view API documentation.", format));
            logger.warn(ace);
        }
        catch (ApiFormatException afe) {
            out.write(getError("error", "Invalid format: " + afe.getMessage()  +
                    " for command: " + command + ", please review API documentation.", format));
            logger.warn(afe);
        }
        catch (ApiTypeException ate) {
            out.write(getError("error", "Invalid input " + ate.getMessage()
                    + " for command: " + command +", please review API documentation.", format));
            logger.warn(ate);
        }
        catch (ApiAuthenticationException aae) {
            out.write(getError("error", "Could not be authorized.", format));
            logger.warn(aae);
        }
        catch (Exception e) {
            out.write(getError("error", "Invalid request " + request.getRequestURL()+"?"+request.getQueryString()
                    + ", please check that your input is properly formatted " +
                            "and review the API documentation.", format));
            logger.warn(e);
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public String getError(String name, String reason, String format) {
        logger.warn("Error! name: " + name + ", reason: " + reason);

        ErrorResponse e = new ErrorResponse(reason);
        if(format != null && format.matches("xml|kml")) {
            XStream xstream = new XStream(new DomDriver());
            xstream.processAnnotations(new Class[]{ValidateResponse.class, ErrorResponse.class});
            return xstream.toXML(e);
        }
        else if(format != null && format.matches("json")) {
            Gson gson = new Gson();
            return gson.toJson(e);
        }
        else {
            return "ERROR," + reason.replaceAll(",", "");
        }

    }

    public ArrayList<String> getMore(String format, String type, StringTokenizer stok) {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add(format);
        strings.add(type);
        while(stok.hasMoreTokens()) {
            strings.add(stok.nextToken());
        }
        return strings;
    }


    public boolean addMetric(int userId, String command, String host, Connect db) {
        logger.info("writing metric");
        boolean ret = db.persist(new Metric(userId, command, host));
        db.close();
        return ret;
    }


    @SuppressWarnings({ "unchecked", "serial" })
    public static HashMap<String, ApiMethod> getMethods() throws Exception {
        return new HashMap<String, ApiMethod>() {{

            put("geocode", new ApiMethod("geocode",
                new GeoCodeMethod(),
                false,
                new ArrayList<String>(Arrays.asList("addr", "extended", "bulk")),
                new ArrayList<String>(Arrays.asList("csv", "json", "xml")),
                new ArrayList<Class<? extends Object>>(Arrays.asList(Point.class))));

            put("revgeo", new ApiMethod("revgeo",
                new RevGeoMethod(),
                true,
                new ArrayList<String>(Arrays.asList("latlon")),
                new ArrayList<String>(Arrays.asList("json", "xml")),
                new ArrayList<Class<? extends Object>>(Arrays.asList(Point.class))));

            put("districts", new ApiMethod("districts",
                new DistrictsMethod(),
                true,
                new ArrayList<String>(Arrays.asList("addr", "extended","latlon")),
                new ArrayList<String>(Arrays.asList("json","xml")),
                new ArrayList<Class<? extends Object>>(Arrays.asList(DistrictResponse.class, Point.class))));

            put("validate", new ApiMethod("validate",
                new ValidateMethod(),
                true,
                new ArrayList<String>(Arrays.asList("extended")),
                new ArrayList<String>(Arrays.asList("json", "xml")),
                new ArrayList<Class<? extends Object>>(Arrays.asList(ValidateResponse.class, ErrorResponse.class))));

            put("poly", new ApiMethod("poly",
                new PolyMethod(),
                true,
                new ArrayList<String>(Arrays.asList("senate", "assembly","congressional")),
                new ArrayList<String>(Arrays.asList("json", "kml", "xml")),
                null));

            put("polysearch", new ApiMethod("polysearch",
                new PolySearchMethod(),
                true,
                new ArrayList<String>(Arrays.asList("senate", "assembly","congressional", "election", "county")),
                new ArrayList<String>(Arrays.asList("json", "kml", "xml")),
                null));

            put("citystatelookup", new ApiMethod("citystatelookup",
                new CityStateLookupMethod(),
                true,
                new ArrayList<String>(Arrays.asList("extended")),
                new ArrayList<String>(Arrays.asList("json", "xml")),
                new ArrayList<Class<? extends Object>>(Arrays.asList(ValidateResponse.class, ErrorResponse.class))));

            put("zipcodelookup", new ApiMethod("zipcodelookup",
                new ZipCodeLookupMethod(),
                true,
                new ArrayList<String>(Arrays.asList("extended")),
                new ArrayList<String>(Arrays.asList("json", "xml")),
                new ArrayList<Class<? extends Object>>(Arrays.asList(ValidateResponse.class, ErrorResponse.class))));

            put("streetlookup", new ApiMethod("streetlookup",
                new StreetLookupMethod(),
                true,
                new ArrayList<String>(Arrays.asList("zip")),
                new ArrayList<String>(Arrays.asList("json", "xml")),
                new ArrayList<Class<? extends Object>>()));

            put("bluebirddistricts", new ApiMethod("bluebirddistricts",
                new BluebirdMethod(),
                true,
                new ArrayList<String>(Arrays.asList("addr", "extended","latlon")),
                new ArrayList<String>(Arrays.asList("json","xml")),
                new ArrayList<Class<? extends Object>>(Arrays.asList(DistrictResponse.class, Point.class, ValidateResponse.class))));
        }};
    }
}
