package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.DistrictService;
import gov.nysenate.sage.service.DistrictService.DistAssignInterface;
import gov.nysenate.sage.util.Resource;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoServer implements DistAssignInterface {
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
            logger.info(result.source);

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

            logger.info(url);

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Result distAssign(Address address, DistrictService.TYPE type) {
        return distAssign(new ArrayList<Address>(Arrays.asList(new Address[]{address})), type).get(0);
    }

    @Override
    public ArrayList<Result> distAssign(ArrayList<Address> addresses, DistrictService.TYPE type) {
        String filter = "";
        Content page= null;
        String geotype = "typename=nysenate:"+type.toString().toLowerCase();
        String filter_format = "INTERSECTS(the_geom, POINT ( %f %f ))";
        ArrayList<Result> results = new ArrayList<Result>();

        logger.info(addresses);
        String url_format = API_BASE+"&%s&CQL_FILTER=%s&outputformat=JSON";
        for (Address address : addresses) {
            Result result = new Result();
            result.address = address.clone();//new Address(address);
            try {
                filter = String.format(filter_format, address.latitude, address.longitude);
                result.source = String.format(url_format, geotype, URLEncoder.encode(filter,"UTF-8"));

                logger.info(result.source);
                page = Request.Get(result.source).execute().returnContent();
                JSONObject response = new JSONObject(page.asString());
                JSONArray features = response.getJSONArray("features");

                // Should only match one feature as a point intersection
                if (features.length()==0) {
                    result.status_code = "1";
                    result.messages.add("No matching features found");
                    results.add(result);
                } else if (features.length() > 1) {
                    result.messages.add("Multiple matching features found. Using the first one.");
                }

                JSONObject feature = features.getJSONObject(0);
                JSONObject properties = feature.getJSONObject("properties");

                switch (type) {
                case SCHOOL:
                    result.address.school_name = properties.getString("NAME");
                    result.address.school_code = properties.getInt("TFCODE");
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
                    result.address.congressional_name = properties.getString("NAMELSAD");
                    result.address.congressional_code = properties.getInt("CD111FP");
                    break;
                case COUNTY:
                    result.address.county_name = properties.getString("NAMELSAD"); // or NAME
                    result.address.county_code = COUNTY_CODES.get(properties.getInt("COUNTYFP"));
                    break;
                case ASSEMBLY:
                    result.address.assembly_name = properties.getString("NAMELSAD");
                    result.address.assembly_code = properties.getInt("SLDLST");
                    break;
                case SENATE:
                    result.address.senate_name = properties.getString("NAMELSAD");
                    result.address.senate_code = properties.getInt("SLDUST");
                    break;
                }
                results.add(result);
            } catch (IOException e) {
                logger.error("Error opening API resource '"+result.source+"'", e);
                return null;
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                return null;
            }
        }
        logger.warn(results);
        return results;
    }

    /**
     * The following are connectors for GeoServer, they provide construct[FILTER] methods
     * that create urls for various GeoServer WFS calls
     */
    /*
    public class WFS_REQUEST extends WFS_ {
        String GEO_TYPE = "&typename=";
        String GEO_PROPERTY = "NAMELSAD,INTPTLAT,INTPTLON,ALAND,AWATER";
        String GEO_FILTER_TYPE="NAMELSAD";

        public WFS_REQUEST(DistrictAssign.TYPE districtType) {
            GEO_TYPE += "nysenate:" + districtType.toString().toLowerCase();

            if(districtType == DistrictAssign.TYPE.ELECTION){
                GEO_PROPERTY = "ED,AREA,AREA1,EDS_COPY_,EDS_COPY_I,MCD2,WARD,EDP";
                GEO_FILTER_TYPE="ED";
            }
            else if(districtType == DistrictAssign.TYPE.COUNTY) {
                GEO_PROPERTY += ",COUNTYFP";
            }
        }

        @Override
        public String construct(double x, double y) {
            String urlFormat = "%s&typename=nysenate:%s&propertyname=%s&CQL_FILTER=INTERSECTS(the_geom, POINT ( %d %d ))&outputformat=JSON";
            return String.format(urlFormat, API_BASE, GEO_TYPE, GEO_PROPERTY, x, y);
        }

        @Override
        public String construct(String value) {
            return Resource.get("geoserver.url") + GEO_TYPE + GEO_PROPERTY + "&CQL_FILTER="
                    + GEO_FILTER_TYPE + "%20LIKE%20" + "'" + value + "'" + "&outputformat=JSON";
        }

        public String constructBoundingBox(double x, double y) {
            return Resource.get("geoserver.url") + GEO_TYPE + GEO_PROPERTY + "&bbox=" + x
                    + "," + y + "," + x + "," + y + "&outputformat=JSON";
        }

        public String constructCross(double x, double y, boolean xOrY, double amt) {
            return Resource.get("geoserver.url") + GEO_TYPE + GEO_PROPERTY + "&CQL_FILTER=" +
                "CROSS(the_geom,%20LINESTRING("
                    + ((xOrY) ? x + amt:x) + "%20"
                    + ((xOrY) ? y:y + amt) + ","
                    + ((xOrY) ? x - amt:x) + "%20"
                    + ((xOrY) ? y:y - amt) + "))" + "&outputformat=JSON";
        }
    }



    public class WFS_POLY extends WFS_ {
        String GEO_TYPE = "&typename=";
        //the only time the filter is not NAMESLAD is for election layer
        String GEO_FILTER_TYPE="NAMELSAD";

        public WFS_POLY(DistrictType districtType) {
            setGeoType(districtType);
        }

        private void setGeoType(DistrictType districtType) {
            Pattern p = Pattern.compile(POLY_NAMES);
            Matcher m = p.matcher(districtType.type);
            if(m.find()) {
                GEO_TYPE += "nysenate:" + m.group(1);
            }

            if(districtType == DistrictType.ELECTION){
                GEO_FILTER_TYPE="ED";
            }
        }

        @Override
        public String construct(double x, double y) {
            return String.format("%s&CQL_FILTER=INTERSECTS(the_geom, POINT (%d, %d))&outputformat=JSON", Resource.get("geoserver.url") + GEO_TYPE ,x, y);
        }

        @Override
        public String construct(String value) {
            return Resource.get("geoserver.url") + GEO_TYPE + "&CQL_FILTER="
                    + GEO_FILTER_TYPE + GEO_CQL_LIKE + "'" + value + "'" + "&outputformat=JSON";
        }
    }
    public abstract class WFS_ {
        public abstract String construct(double x, double y);
        public abstract String construct(String value);
    }
    */
}
