package scrapers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import control.Connect;

import model.districts.Assembly;
import model.districts.Member;

public class AssemblyScraper {

	static final String ASSEMBLY = "http://assembly.state.ny.us";
	static final String MEM = "/mem/";
	static final String AD = "?ad=";
	
	public static void scrape() throws IOException {
		Pattern p = Pattern.compile("<li><a href=\"(/mem/)?\\?ad=[0]*(\\d+?)\">(.*?)</a></li>");
		Matcher m = null;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL(ASSEMBLY + MEM).openStream()));
		Connect c = new Connect();
		
		String in = null;
		
		while((in = br.readLine()) != null) {
			m = p.matcher(in);
			if(m.find()) {
				Assembly a = new Assembly("Assembly District " + m.group(2),new Member(m.group(3).replaceAll("'|\"", ""),ASSEMBLY+MEM+AD+m.group(2)));
				c.persist(a);
			}
		}
		c.close();
		br.close();
	}
}
