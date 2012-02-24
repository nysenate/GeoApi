package gov.nysenate.sage.util;

import generated.geoserver.json.GeoResult;
import gov.nysenate.sage.connectors.GeoServerConnect;
import gov.nysenate.sage.connectors.DistrictServices.DistrictType;
import gov.nysenate.sage.connectors.GeoServerConnect.WFS_REQUEST;
import gov.nysenate.sage.model.ApiUser;
import gov.nysenate.sage.model.Metric;
import gov.nysenate.sage.model.SenateMapInfo;
import gov.nysenate.sage.model.districts.Senate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasypt.util.password.BasicPasswordEncryptor;

import com.google.gson.Gson;

public class ApiController {
	
	private static final String DEFAULT_WRITE_DIRECTORY = Resource.get("json.directory");
	private static final String DEFAULT_RAW_WRITE_DIRECTORY = Resource.get("json.raw_directory");
	private static final String DEFAULT_ZOOM_PATH = Resource.get("json.zoom");

	public static void main(String[] args) throws Exception {
		if(args.length == 1) {
			if(args[0].equals("regen")) {
				ApiController a = new ApiController();
				
				System.out.println("indexing assembly... ");
				new AssemblyScraper().index();
				System.out.println("indexing congress... ");
				new CongressScraper().index();
				System.out.print("indexing senate... ");
				new NYSenateServices().index();
				System.out.println();
								
				a.writeJson(DEFAULT_WRITE_DIRECTORY, null, true);
				a.writeJson(DEFAULT_RAW_WRITE_DIRECTORY, null, false);
			}
		}
		else {
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
						System.out.println("key for user is... " + new ApiController().addUser(m.group(3), m.group(1), m.group(2)));
					}
					else {
						System.out.println("proper format is: add user \"<name>\" \"<description>\" \"<key>\"");
					}
				}
				else if(in.equals("default senate user")) {
					new Connect().persist(new ApiUser(Resource.get("user.default"), "general", "everyone for now"));
				}
				System.out.print("> ");
			}
		}
	}
	
	public boolean addMetric(int userId, String command, String host) {		
		Connect con = new Connect();
		
		boolean ret = con.persist(new Metric(userId, command, host));
		
		con.close();
		
		return ret;
	}
	
	public String addUser(String apiKey, String name, String description) {
		Connect con = new Connect();
		
		BasicPasswordEncryptor pe = new BasicPasswordEncryptor();
		String ep = pe.encryptPassword(apiKey);
		ep = ep.replaceAll("\\W", "");
		
		con.persist(new ApiUser(ep, name, description));
		
		con.close();
		
		return ep;
	}
	
	public ApiUser getUser(String apiKey) {
		Connect con = new Connect();
		ApiUser user = null;
		try {
			
			user = (ApiUser) con.getObject(ApiUser.class, "apikey",apiKey);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		con.close();
		
		return user;
	}
	
	
	/*
	 * used to write the json used for maps and for raw data
	 */
	public void writeJson(String writeDirectory, String zoomPath, boolean geo) throws Exception {
		if(writeDirectory == null)
			writeDirectory = DEFAULT_WRITE_DIRECTORY;
		if(zoomPath == null)
			zoomPath = DEFAULT_ZOOM_PATH;
		
		Connect c = new Connect();
		
		Gson gson = new Gson();
		
		GeoServerConnect gsCon = new GeoServerConnect();
		
		HashMap<Integer,String> map = new HashMap<Integer,String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(zoomPath)));
		
		String in = null;
		
		while((in = br.readLine()) != null) {
			map.put(new Integer(in.split(":")[0]), in.split(":")[1]);
		}
		br.close();
		
		for(int i = 1; i <= 62; i++) {
			FileWriter fw = new FileWriter(writeDirectory + "/sd" + i + ".json");
			new File(writeDirectory + "/sd" + i + ".json").createNewFile();
			
			PrintWriter pw = new PrintWriter(fw);
			
			WFS_REQUEST sen = gsCon.new WFS_REQUEST(DistrictType.SENATE);
			
			GeoResult gr = gsCon.fromGeoserver(sen,"State Senate District " + i);
			
			double lat = new Double(gr.getFeatures().iterator().next().getProperties().getINTPTLAT());
			double lon = new Double(gr.getFeatures().iterator().next().getProperties().getINTPTLON());
			
			Senate senate = (Senate) c.getObject(Senate.class, "district", "State Senate District " + i);
			
			Pattern p = Pattern.compile("(\\d+) \\((.*?),(.*?)\\)");
			Matcher m = p.matcher(map.get(i));
			
			if(m.find()) {
				SenateMapInfo smi = new SenateMapInfo(lat,lon,
						new Double(m.group(1)),
						new Double(m.group(2)),
						new Double(m.group(3)),senate);
				
				if(geo)
					pw.write(gson.toJson(smi));
				else
					pw.write(gson.toJson(senate));
				
			}
			else {
				SenateMapInfo smi = new SenateMapInfo(lat,lon,
						new Double(map.get(i)),null,null,senate);
				
				if(geo)
					pw.write(gson.toJson(smi));
				else
					pw.write(gson.toJson(senate));
			}
						
			pw.close();
		}
	}
}
