package scrapers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import control.Connect;

import model.districts.Congressional;
import model.districts.Member;

public class CongressScraper {
	
	static final String HOUSE = "http://www.house.gov/house/MemberWWW_by_State.shtml";
	
	public static void scrape() throws IOException {
		Pattern nyp = Pattern.compile("<A name=\"ny\" id=\"ny\"></A>New York</H3>");
		Pattern pp = Pattern.compile("<LI><A href=\"(.*?)\" class=\"Bodylink\">(.*?)</A>, New York, (\\d+)\\w+( - <strong>Vacancy</strong>)?</LI>");
		Matcher m = null;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL(HOUSE).openStream()));
		Connect connect = new Connect();
		
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
					connect.persistObject(c);
				}
			}
		}
		connect.close();
		br.close();
	}
	
}
