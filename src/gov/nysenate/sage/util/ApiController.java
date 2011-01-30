package gov.nysenate.sage.util;


import gov.nysenate.sage.model.ApiUser;
import gov.nysenate.sage.model.Metric;
import gov.nysenate.sage.model.districts.Assembly;
import gov.nysenate.sage.model.districts.Congressional;
import gov.nysenate.sage.model.districts.Senate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasypt.util.password.BasicPasswordEncryptor;




public class ApiController {
	
	public static void main(String[] args) throws Exception {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String in = "";
		
		System.out.print("> ");
		while(!(in = br.readLine()).equals("exit")) {
			if(in.equals("truncate all")) {
				new Connect().deleteObjects(Senate.class);
				new Connect().deleteObjects(Assembly.class);
				new Connect().deleteObjects(Congressional.class);
			}
			if(in.equals("create all")) {
				System.out.println("indexing assembly... ");
				AssemblyScraper.index();
				System.out.println("indexing congress... ");
				CongressScraper.index();
				System.out.print("indexing senate... ");
				NYSenateServices.index();
				System.out.println();
				
			}
			else if(in.equals("create sen")) {
				System.out.print("indexing senate... ");
				NYSenateServices.index();
				System.out.println();
			}
			else if(in.equals("create ass")) {
				System.out.println("indexing assembly... ");
				AssemblyScraper.index();
			}
			else if(in.equals("create con")) {
				System.out.println("indexing congress... ");
				CongressScraper.index();
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
			else if(in.startsWith("delete user")) {
				
			}
			else if(in.equals("default senate user")) {
				new Connect().persist(new ApiUser(Resource.get("user.default"), "general", "everyone for now"));
				new Connect().persist(new ApiUser(Resource.get("sheldon.default"), "sheldon", "nysenate stuff"));
				new Connect().persist(new ApiUser(Resource.get("nathan.default"), "nathan", "mobile dev"));
				new Connect().persist(new ApiUser(Resource.get("sts.default"), "sts", "financial apps and what not"));
				new Connect().persist(new ApiUser(Resource.get("crm.default"), "crm", "for crm/bluebird"));
			}
			System.out.print("> ");
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
	
}
