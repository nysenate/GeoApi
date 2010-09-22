package control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasypt.util.password.BasicPasswordEncryptor;

import scrapers.AssemblyScraper;
import scrapers.CongressScraper;
import scrapers.NYSenateScraper;


import model.ApiUser;
import model.Metric;
import model.districts.Senate;

public class ApiController {
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		Connect c = new Connect();		
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String in = "";
		
		System.out.print("> ");
		while(!(in = br.readLine()).equals("exit")) {
			if(in.equals("create all")) {
				System.out.println("indexing assembly... ");
				AssemblyScraper.scrape();
				System.out.println("indexing congress... ");
				CongressScraper.scrape();
				System.out.print("indexing senate... ");
				for(int i = 1; i <= 62; i++) {
					System.out.print(i + ",");
					c.persistObject(NYSenateScraper.populateSenateData(i));
				}
				System.out.println();
				
			}
			if(in.equals("create sen")) {
				System.out.print("indexing... ");
				for(int i = 1; i <= 62; i++) {
					System.out.print(i + ",");
					c.persistObject(NYSenateScraper.populateSenateData(i));
				}
				System.out.println();
			}
			if(in.equals("destroy sen")) {
				System.out.println("dropping senate index");
				c.deleteObjects(Senate.class);
			}
			if(in.startsWith("add user")) {
				Pattern p = Pattern.compile("add user \"(.+?)\" \"(.+?)\" \"(.+?)\"");
				Matcher m = p.matcher(in);
				if(m.find()) {
					System.out.println("key for user is... " + new ApiController().addUser(m.group(3), m.group(1), m.group(2)));
				}
				else {
					System.out.println("proper format is: add user \"<name>\" \"<description>\" \"<key>\"");
				}
			}
			if(in.startsWith("delete user")) {
				
			}
			if(in.equals("default senate user")) {
				new Connect().persistObject(new ApiUser(Resource.get("user.default"), "general", "everyone for now"));
			}
			System.out.print("> ");
		}
		
	}
	
	public boolean addMetric(int userId, String command, String host) {		
		Connect con = new Connect();
		
		boolean ret = con.persistObject(new Metric(userId, command, host));
		
		con.close();
		
		return ret;
	}
	
	public String addUser(String apiKey, String name, String description) {
		Connect con = new Connect();
		
		BasicPasswordEncryptor pe = new BasicPasswordEncryptor();
		String ep = pe.encryptPassword(apiKey);
		ep = ep.replaceAll("\\W", "");
		
		con.persistObject(new ApiUser(ep, name, description));
		
		con.close();
		
		return ep;
	}
	
	public ApiUser getUser(String apiKey) {
		Connect con = new Connect();
		ResultSet rs = con.getObjectById(ApiUser.class, "apikey",apiKey);
		ApiUser user = null;
		
		try {
			if(rs.next()) {
				user = (ApiUser)con.objectFromOpenResultSet(ApiUser.class, rs);
				user.setId(rs.getInt("id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		con.close();
		
		return user;
	}
	
}
