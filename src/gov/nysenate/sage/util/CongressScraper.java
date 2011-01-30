package gov.nysenate.sage.util;

import gov.nysenate.sage.model.districts.Congressional;
import gov.nysenate.sage.model.districts.Member;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;



public class CongressScraper {
	
	static final String HOUSE = "http://www.house.gov/house/MemberWWW_by_State.shtml";
	
	public static void index() throws IOException {
		Connect connect = new Connect();
		
		List<Congressional> persons =  getCongressPersons();
		
		for(Congressional c:persons) {
			connect.persist(c);
		}
		
		connect.close();
	}
	
	public static List<Congressional> getCongressPersons() throws IOException {
		List<Congressional> ret = new ArrayList<Congressional>();
		
		Pattern nyp = Pattern.compile("<A name=\"ny\" id=\"ny\"></A>New York</H3>");
		Pattern pp = Pattern.compile("<LI><A href=\"(.*?)\" class=\"Bodylink\">(.*?)</A>, New York, (\\d+)\\w+( - <strong>Vacancy</strong>)?</LI>");
		Matcher m = null;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL(HOUSE).openStream()));
		
		String in = null;
		boolean on = false;
		
		while((in = br.readLine()) != null) {
			if(!on) {
				m = nyp.matcher(in);
				if(m.find()) {
					on = true;
				}
			}
			else {
				m = pp.matcher(in);
				if(m.find()) {
					Congressional c = new Congressional("Congressional District " + m.group(3),new Member(m.group(2).replaceAll("'|\"", ""),m.group(1)));
					ret.add(c);
				}
			}
		}
		br.close();
		
		return ret;
	}
	
}
