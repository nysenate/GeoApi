package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.DistrictService;
import gov.nysenate.sage.service.DistrictService.DistAssignInterface;
import gov.nysenate.sage.service.DistrictService.DistException;
import gov.nysenate.sage.service.DistrictService.TYPE;
import gov.nysenate.sage.util.Resource;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoServer implements DistAssignInterface {

    public class ParallelRequest implements Callable<Result> {
        public final DistAssignInterface adapter;
        public final Address address;
        public final List<DistrictService.TYPE> types;

        ParallelRequest(DistAssignInterface adapter, Address address, List<DistrictService.TYPE> types) {
            this.address = address;
            this.adapter = adapter;
            this.types = types;
        }

        @Override
        public Result call() throws DistException {
            return adapter.assignDistricts(address,types);
        }
    }


    private final Logger logger;
    public String API_BASE;

    HashMap<Integer, Integer> COUNTY_CODES;

    public GeoServer() throws Exception {
        API_BASE = new Resource().fetch("geoserver.url")+"/wfs?service=WFS&version=1.1.0&request=GetFeature";
        logger = Logger.getLogger(this.getClass());

        COUNTY_CODES = new HashMap<Integer, Integer>();
        File county_code_file = FileUtils.toFile(this.getClass().getClassLoader().getResource("county_codes.tsv"));
        @SuppressWarnings("unchecked")
        List<String> lines = FileUtils.readLines(county_code_file, "UTF-8");
        for (String line : lines) {
            String[] parts = line.split("\t");
            COUNTY_CODES.put(Integer.parseInt(parts[2]), Integer.parseInt(parts[0]));
        }
    }
    public Result lookupByName(String name, DistrictService.TYPE type) {
        String filter;
        Result result = new Result();
        String geotype = "typename=nysenate:"+type.toString().toLowerCase();

        if (type == DistrictService.TYPE.ELECTION) {
            return null; //Election districts are non-unique and can't be looked up
        } else if (type == DistrictService.TYPE.SCHOOL || type == DistrictService.TYPE.TOWN) {
            filter = String.format("NAME LIKE '%s'",name);
        } else {
            filter = String.format("NAMELSAD LIKE '%s'",name);
        }

        try {
            result.source = String.format(API_BASE+"&%s&CQL_FILTER=%s&outputformat=JSON", geotype, URLEncoder.encode(filter,"UTF-8"));
            logger.debug(result.source);

            Content page = Request.Get(result.source).execute().returnContent();
            JSONObject response = new JSONObject(page.asString());
            JSONArray features = response.getJSONArray("features");

            // Should only match one feature as a point intersection
            if (features.length()==0) {
                result.status_code = "1";
                result.messages.add("No matching features found");
            } else if (features.length() > 1) {
                return null;
            } else {
                JSONObject feature = features.getJSONObject(0);
                JSONObject properties = feature.getJSONObject("properties");
                result.status_code = "0";
                result.address = new Address(name);
                result.address.setGeocode(properties.getDouble("INTPTLAT"), properties.getDouble("INTPTLON"), 100);
            }
            return result;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Result> getNearByDistricts(Address address, DistrictService.TYPE type, double distanceFeet) {
        double distance;
        String url;
        String filter;
        String geotype = "typename=nysenate:"+type.toString().toLowerCase();
        String url_format = API_BASE+"&%s&CQL_FILTER=%s&outputformat=JSON";
        String filter_format = "CROSS(the_geom, LINESTRING(%f %f, %f %f)) OR CROSS(the_geom, LINESTRING(%f %f, %f %f))";
        try {
            distance = distanceFeet/364400; //Approx feet per degree at our altitude
            double x = address.latitude;
            double y = address.longitude;
            filter = String.format(filter_format, x, y-distance, x, y+distance, x-distance, y, x+distance, y);

            System.out.println(filter);
            url = String.format(url_format, geotype, URLEncoder.encode(filter,"UTF-8"));

            logger.debug(url);

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Result assignDistrict(Address address, DistrictService.TYPE type) throws DistException {
        return assignDistrict(new ArrayList<Address>(Arrays.asList(new Address[]{address})), type).get(0);
    }

    @Override
    public ArrayList<Result> assignDistrict(ArrayList<Address> addresses, DistrictService.TYPE type) throws DistException {
        String filter = "";
        Content page= null;
        Result result = null;
        String geotype = "typename=nysenate:"+type.toString().toLowerCase();
        String filter_format = "INTERSECTS(the_geom, POINT ( %f %f ))";
        ArrayList<Result> results = new ArrayList<Result>();

        try {
            String url_format = API_BASE+"&%s&CQL_FILTER=%s&outputformat=JSON";
            for (Address address : addresses) {
                result = new Result();
                result.address = address.clone();//new Address(address);

                filter = String.format(filter_format, address.latitude, address.longitude);
                result.source = String.format(url_format, geotype, URLEncoder.encode(filter,"UTF-8"));

                logger.debug(result.source);
                page = Request.Get(result.source).execute().returnContent();
                JSONObject response = new JSONObject(page.asString());
                JSONArray features = response.getJSONArray("features");

                // Should only match one feature as a point intersection
                if (features.length()==0) {
                    result.status_code = "1";
                    result.messages.add("No matching "+type+" features found for "+address.toString());
                    results.add(result);
                    continue;
                } else if (features.length() > 1) {
                    result.messages.add("Multiple matching features found. Using the first one.");
                }

                JSONObject feature = features.getJSONObject(0);
                JSONObject properties = feature.getJSONObject("properties");

                switch (type) {
                case SCHOOL:
                    result.address.school_name = properties.getString("NAME");
                    result.address.school_code = properties.getString("TFCODE");
                    break;
                case TOWN:
                    result.address.town_name = properties.getString("NAME");
                    result.address.town_code = properties.getString("ABBREV");
                    break;
                case ELECTION:
                    result.address.election_code = properties.getInt("ED");
                    result.address.election_name = "ED "+address.school_code;
                    break;
                case CONGRESSIONAL:
                    // Accommodate both old shape files and new 2012 shape files
                    result.address.congressional_name = properties.has("NAMELSAD") ? properties.getString("NAMELSAD") : properties.getString("NAME");
                    result.address.congressional_code = properties.has("CD111FP") ? properties.getInt("CD111FP") : properties.getInt("DISTRICT");
                    break;
                case COUNTY:
                    result.address.county_name = properties.getString("NAMELSAD"); // or NAME
                    result.address.county_code = COUNTY_CODES.get(properties.getInt("COUNTYFP"));
                    break;
                case ASSEMBLY:
                    // Accommodate both old shape files and new 2012 shape files
                    result.address.assembly_name = properties.has("NAMELSAD") ? properties.getString("NAMELSAD") : properties.getString("NAME");
                    result.address.assembly_code = properties.has("SLDLST") ? properties.getInt("SLDLST") : properties.getInt("DISTRICT");
                    break;
                case SENATE:
                    // Accommodate both old shape files and new 2012 shape files
                    result.address.senate_name = properties.has("NAMELSAD") ? properties.getString("NAMELSAD") : properties.getString("NAME");
                    result.address.senate_code = properties.has("SLDUST") ? properties.getInt("SLDUST") : properties.getInt("DISTRICT");
                    break;
                }
                results.add(result);
            }
            return results;

        } catch (IOException e) {
            String msg = "Error opening API resource '"+result.source+"'";
            logger.error(msg, e);
            throw new DistException(msg, e);

        } catch (JSONException e) {
            String msg = "Malformed JSON response for '"+result.source+"'\n"+page.asString();
            logger.error(msg, e);
            throw new DistException(msg, e);
        }
    }

    @Override
    public ArrayList<Result> assignDistricts(ArrayList<Address> addresses, List<TYPE> types) throws DistException {
        ArrayList<Result> results = new ArrayList<Result>();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        ArrayList<Future<Result>> futureResults = new ArrayList<Future<Result>>();

        for (Address address : addresses) {
            futureResults.add(executor.submit(new ParallelRequest(this, address, types)));
        }

        for (Future<Result> result : futureResults) {
            try {
                results.add(result.get());
            } catch (InterruptedException e) {
                throw new DistException(e);
            } catch (ExecutionException e) {
                throw new DistException(e.getCause());
            }
        }
        executor.shutdown();
        return results;
    }


    @Override
    public Result assignDistricts(Address address, List<TYPE> types) {
        if (address==null) return null;

        Result result = new Result();
        for (TYPE type : types) {
            try {
                Result typeResult = assignDistrict(address, type);

                // Don't quit on one bad lookup, try them all but alter that status code
                if (!typeResult.status_code.equals("0")) {
                    result.status_code = typeResult.status_code;
                    result.messages.addAll(typeResult.messages);
                } else {
                    // Accumulate districts on the result address
                    address = typeResult.address;
                }
            } catch (DistException e) {
                return null;
            }
        }
        result.address = address;
        return result;
    }
}
