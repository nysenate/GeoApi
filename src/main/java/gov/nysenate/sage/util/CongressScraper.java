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

import org.apache.log4j.Logger;



public class CongressScraper {
	private Logger logger = Logger.getLogger(CongressScraper.class);
	
	private final String HOUSE_URL = "http://www.house.gov/house/MemberWWW_by_State.shtml";
	
	public void index() {
		Connect connect = new Connect();
		
		List<Congressional> persons =  getCongressPersons();
			
		if(persons != null && !persons.isEmpty()) {
			try {
				logger.info("Deleting Congress Member data from the database");
				connect.deleteObjectById(Member.class, "type", Member.MemberType.Congress.value() + "");
				connect.deleteObjects(Congressional.class, false);
			}
			catch (Exception e) {
				logger.warn(e);
			}
			
			logger.info("Persisting new assembly data");
			for(Congressional c:persons) {
				connect.persist(c);
			}
		}
			
		connect.close();
	}
	
	public List<Congressional> getCongressPersons() {
		List<Congressional> ret = new ArrayList<Congressional>();
		
		try {
			Pattern nyp = Pattern.compile("<A name=\"ny\" id=\"ny\"></A>New York</H3>");
			Pattern pp = Pattern.compile("<LI><A href=\"(.*?)\" class=\"Bodylink\">(.*?)</A>, New York, (\\d+)\\w+( - <strong>Vacancy</strong>)?</LI>");
			Matcher m = null;
			
			logger.info("Connect to " + HOUSE_URL);
			BufferedReader br = new BufferedReader(new InputStreamReader(new URL(HOUSE_URL).openStream()));
			
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
						logger.info("Fetching congress person " + m.group(2));
						Congressional c = new Congressional("Congressional District " + m.group(3),new Member(m.group(2).replaceAll("'|\"", ""),m.group(1), Member.MemberType.Congress));
						ret.add(c);
					}
				}
			}
			br.close();
		}
		catch (IOException ioe) {
			logger.warn(ioe);
		}
		
		return ret;
	}
}
