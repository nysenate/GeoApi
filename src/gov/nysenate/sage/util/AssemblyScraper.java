package gov.nysenate.sage.util;

import gov.nysenate.sage.model.districts.Assembly;
import gov.nysenate.sage.model.districts.Member;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;



public class AssemblyScraper {

	static final String ASSEMBLY = "http://assembly.state.ny.us";
	static final String MEM = "/mem/";
	static final String AD = "?ad=";
	
	public static void index() throws IOException {
		Connect c = new Connect();
		
		List<Assembly> persons = getAssemblyPersons();
		
		for(Assembly a:persons) {
			c.persist(a);
		}
				
		c.close();
		
	}
	
	public static List<Assembly> getAssemblyPersons() throws MalformedURLException, IOException {
		String CONTACT = "/mem/?sh=email";
		
		List<Assembly> ret = new ArrayList<Assembly>();
		
		Pattern p = Pattern.compile("<div class=\"email1\"><a href=\"(.+?)\">(.+?)</a>.*?email2\">(\\d+)");
		Matcher m = null;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL(ASSEMBLY + CONTACT).openStream()));
		
		String in = null;
		while((in = br.readLine()) != null) {
			m = p.matcher(in);
			
			if(m.find()) {
				Assembly a = new Assembly("Assembly District " + m.group(3),new Member(m.group(2),ASSEMBLY+m.group(1)));
				ret.add(a);
			}
		}
		
		br.close();
		
		return ret;
	}
}
