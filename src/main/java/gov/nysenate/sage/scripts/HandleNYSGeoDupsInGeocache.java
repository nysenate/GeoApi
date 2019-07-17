package gov.nysenate.sage.scripts;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.NYSGeoAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.StreetAddressParser;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HandleNYSGeoDupsInGeocache {

    private Config config;
    private QueryRunner tigerRun;
    private static final int limit = 2000;
    private static int offset = 0; // offset 1435308 for 447 73rd Street, Niagara Falls, NY, USA
    private static final String table = "public.addresspoints_sam";

    public HandleNYSGeoDupsInGeocache() {
        config = ApplicationFactory.getConfig();
        tigerRun = new QueryRunner(ApplicationFactory.getTigerDataSource());
    }

    public Config getConfig() {
        return config;
    }

    public QueryRunner getTigerRun() {
        return tigerRun;
    }

    public static int getLimit() {
        return limit;
    }

    public static int getOffset() {
        return offset;
    }

    public static String getTable() {
        return table;
    }

    public static void main(String[] args) {
        /* Load up the configuration settings */
        if (!ApplicationFactory.bootstrap()) {
            System.err.println("Failed to configure application");
            System.exit(-1);
        }

        try {
            if (args.length > 0 && args.length < 2) {
                offset = Integer.parseInt(args[0]);
            }
            System.out.println("NYS Geo Duplicates input offset: " + offset);
        } catch (Exception e) {
            System.err.println("Check supplied arguments. The only argument should be an int");
            System.exit(-1);
        }

        HandleNYSGeoDupsInGeocache nysGeoDupsInGeocache = new HandleNYSGeoDupsInGeocache();

        String DUP_TOTAL_COUNT_SQL = "SELECT count(*)\n" +
                "FROM addresspoints_sam x\n" +
                "         JOIN (SELECT t.addresslabel\n" +
                "               FROM addresspoints_sam t\n" +
                "               GROUP BY t.addresslabel\n" +
                "               HAVING COUNT(t.addresslabel) > 1) y ON y.addresslabel = x.addresslabel;";

        String DUP_BATCH_SQL = "SELECT  x.addresslabel, x.citytownname, x.state, x.zipcode, x.latitude, x.longitude, x.pointtype\n" +
                "FROM addresspoints_sam x\n" +
                "         JOIN (SELECT t.addresslabel\n" +
                "               FROM addresspoints_sam t\n" +
                "               GROUP BY t.addresslabel\n" +
                "               HAVING COUNT(t.addresslabel) > 1) y ON y.addresslabel = x.addresslabel\n" +
                "limit ?\n" +
                "offset ?;";

        String SQL_UPDATE_CACHE_ENTRY = "update cache.geocache\n" +
                "set latlon = ST_GeomFromText(?), method = ?, quality = ?, zip4 = ?, updated = now()\n" +
                "where bldgnum = ?  and street = ? and streettype = ? and predir = ? and postdir = ? and zip5 = ? and location = ?;";

        BeanListHandler<NYSGeoAddress> nysGeoAddressBeanListHandler
                = new BeanListHandler<>(NYSGeoAddress.class);

        HttpClient httpClient = HttpClientBuilder.create().build();

        int updatedRecords = 0;
        int badRecords = 0;
        int nysGeoDups = 0;

        try {
            //Get total number of addresses that will be used to update our geocache
            int total = nysGeoDupsInGeocache.getTigerRun().query(DUP_TOTAL_COUNT_SQL, new ResultSetHandler<Integer>() {
                @Override
                public Integer handle(ResultSet rs) throws SQLException {
                    rs.next();
                    return rs.getInt("count");
                }
            });
            System.out.println("NYS Geo Dup total iteration count: " + total);

            StreetAddress lastAddress = null;


            while (total > offset) {
                //Get batch of 2000
                List<NYSGeoAddress> nysGeoAddresses = nysGeoDupsInGeocache.getTigerRun().query(DUP_BATCH_SQL,
                        nysGeoAddressBeanListHandler, limit, offset);
                System.out.println("At offset: " + offset);
                offset = limit + offset;

                //For some reason NYS GEO has the same address multiple times
                //which is different from the issue we are trying to address with this script
                //if the same address comes up again sequentially we need to skip it

                for (NYSGeoAddress nysGeoAddress : nysGeoAddresses) {
                    StreetAddress nysStreetAddress = nysGeoAddress.toStreetAddress();
                    Address nysAddress = nysStreetAddress.toAddress();
                    StreetAddress uspsStreetAddress = null;

                    if (lastAddress != null && nysStreetAddress.equals(lastAddress)) {
                        nysGeoDups++;
                        continue;
                    }
                    else {
                        lastAddress = nysStreetAddress;
                    }

                    String url = nysGeoDupsInGeocache.config.getValue("usps.ams.api.url") + "validate?detail=true&addr1=%s&addr2=%s&city=%s&state=%s&zip5=%s";
                    url = String.format(url, nysAddress.getAddr1(), nysAddress.getAddr2() ,nysAddress.getCity(), nysAddress.getState(), nysAddress.getZip5());
                    url = url.replaceAll(" ", "%20");
                    url = StringUtils.deleteWhitespace(url);
                    url = url.replaceAll("`", "");
                    url = url.replaceAll("#", "");

                    try {
                        HttpGet request = new HttpGet(url);
                        HttpResponse response = httpClient.execute(request);

                        JSONObject uspsJson = new JSONObject(UrlRequest.convertStreamToString(response.getEntity().getContent()));
                        if (uspsJson.getBoolean("validated")) {
                            JSONObject uspsAddressJson = uspsJson.getJSONObject("address");
                            uspsStreetAddress = StreetAddressParser.parseAddress(new Address(
                                    uspsAddressJson.getString("addr1"), uspsAddressJson.getString("addr2"),
                                    uspsAddressJson.getString("city"), uspsAddressJson.getString("state"),
                                    uspsAddressJson.getString("zip5"), uspsAddressJson.getString("zip4")));
                        }
                    }
                    catch (IOException e) {
                        badRecords++;
                        continue;
                    }


//                    uspsStreetAddress = StreetAddressParser.parseAddress(
//                            ApplicationFactory.getAddressServiceProvider()
//                                    .validate(nysAddress,null,false).getAddress());

                    if (uspsStreetAddress == null) {
                        badRecords++;
                        continue;
                    }

                    if (nysGeoAddress.getPointtype() == 1) {
                        nysGeoDupsInGeocache.getTigerRun().update(SQL_UPDATE_CACHE_ENTRY,
                                "POINT(" + nysGeoAddress.toGeocode().getLon() + " " + nysGeoAddress.toGeocode().getLat() + ")",
                                nysGeoAddress.toGeocode().getMethod(), nysGeoAddress.toGeocode().getQuality().name(), uspsStreetAddress.getZip4(),
                                uspsStreetAddress.getBldgNum(), uspsStreetAddress.getStreetName(),
                                uspsStreetAddress.getStreetType(), uspsStreetAddress.getPreDir(),
                                uspsStreetAddress.getPostDir(),
                                uspsStreetAddress.getLocation(),
                                uspsStreetAddress.getZip5()
                        );
                        updatedRecords++;

                    }
                    else {
                        badRecords++;
                    }

                }
            }

            System.out.println("Updated Records: " + updatedRecords);
            System.out.println("Bad Records (Not found by USPS or not rooftop level quality): " + badRecords);
            System.out.println("NYS Geo duplicate address records: " + nysGeoDups);
        }
        catch (SQLException e) {
            System.err.println("Error reading from the geocoder database");
        }
    }
}
