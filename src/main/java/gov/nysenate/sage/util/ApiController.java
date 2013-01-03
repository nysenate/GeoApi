package gov.nysenate.sage.util;

import gov.nysenate.sage.Result;
import gov.nysenate.sage.adapter.GeoServer;
import gov.nysenate.sage.model.ApiUser;
import gov.nysenate.sage.model.SenateMapInfo;
import gov.nysenate.sage.model.districts.Senate;
import gov.nysenate.sage.service.DistrictService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jasypt.util.password.BasicPasswordEncryptor;

import com.google.gson.Gson;

public class ApiController {
	private final GeoServer geoserver;

	public ApiController() throws Exception {
	    geoserver = new GeoServer();
	}

	public static void main(String[] args) throws Exception {
	    Connect db = new Connect();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String in = "";

		System.out.print("> ");
		while(!(in = br.readLine()).equals("exit")) {
			if(in.equals("create all")) {
				System.out.println("indexing assembly... ");
				new AssemblyScraper().index();
				System.out.println("indexing congress... ");
				new CongressScraper().index();
				System.out.print("indexing senate... ");
				new NYSenateServices().index();
				System.out.println();
			}
			else if(in.equals("create sen")) {
				System.out.print("indexing senate... ");
				new NYSenateServices().index();
				System.out.println();
			}
			else if(in.equals("create ass")) {
				System.out.println("indexing assembly... ");
				new AssemblyScraper().index();
			}
			else if(in.equals("create con")) {
				System.out.println("indexing congress... ");
				new CongressScraper().index();
			}
			else if(in.startsWith("add user")) {
				Pattern p = Pattern.compile("add user \"(.+?)\" \"(.+?)\" \"(.+?)\"");
				Matcher m = p.matcher(in);
				if(m.find()) {
					System.out.println("key for user is... " + new ApiController().addUser(m.group(3), m.group(1), m.group(2), db));
				}
				else {
					System.out.println("proper format is: add user \"<name>\" \"<description>\" \"<key>\"");
				}
			}
			System.out.print("> ");
		}
		db.close();
	}

    public String addUser(String apiKey, String name, String description, Connect db) {
        BasicPasswordEncryptor pe = new BasicPasswordEncryptor();
        String ep = pe.encryptPassword(apiKey).replaceAll("\\W", "");
        db.persist(new ApiUser(ep, name, description));
        return ep;
    }

    public ApiUser getUser(String apiKey, Connect db) {
        ApiUser user = null;
        try {
            user = (ApiUser) db.getObject(ApiUser.class, "apikey",apiKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

	/*
	 * used to write the json used for maps and for raw data
	 */
	public void writeJson(File writeDirectory, File zoomFile, boolean geo) throws Exception {
		Connect c = new Connect();
		Gson gson = new Gson();

		FileUtils.forceMkdir(writeDirectory);

		for(int i = 1; i <= 63; i++) {
			FileWriter fw = new FileWriter(new File(writeDirectory,"/sd" + i + ".json"));

			PrintWriter pw = new PrintWriter(fw);

			Result result = geoserver.getFeatures("DISTRICT="+i, DistrictService.TYPE.SENATE);
			if (!result.status_code.equals("0")) {
			    System.out.println("ERROR: "+result.messages.get(0));
			    continue;
			}

			SenateMapInfo smi;
			Senate senate = (Senate) c.getObject(Senate.class, "district", "State Senate District " + i);

			smi = new SenateMapInfo(
			        result.address.latitude,
                    result.address.longitude,
					0,null,null,senate);

            if(geo)
                pw.write(gson.toJson(smi));
            else
                pw.write(gson.toJson(senate));

			pw.close();
		}
	}
}
