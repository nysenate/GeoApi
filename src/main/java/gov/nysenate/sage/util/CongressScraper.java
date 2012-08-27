package gov.nysenate.sage.util;

import gov.nysenate.sage.model.districts.Congressional;
import gov.nysenate.sage.model.districts.Member;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;



public class CongressScraper {
	private final Logger logger = Logger.getLogger(CongressScraper.class);

	private final String HOUSE_URL = "http://www.house.gov/house/MemberWWW_by_State.shtml";

	public void index() throws IOException {
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

	public static void main(String[] args) throws IOException {
		CongressScraper s = new CongressScraper();
		s.index();
	}

	public List<Congressional> getCongressPersons()  {
		List<Congressional> ret = new ArrayList<Congressional>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new URL(HOUSE_URL).openStream()));

			Pattern newYorkPattern = Pattern.compile("<h2 id=\"state_ny\">New York</h2>");
			Pattern bodyStartPattern = Pattern.compile("<tbody>");
			Pattern bodyEndPattern = Pattern.compile("</tbody>");
			Pattern dataStartPattern = Pattern.compile("<tr>");
			Pattern dataEndPattern = Pattern.compile("</tr>");

			Matcher m = null;

			boolean stateOn = false;;
			boolean bodyOn = false;
			boolean dataOn = false;

			StringBuffer data = null;

			String in = null;

			while((in = br.readLine()) != null) {
				if(!stateOn && (m = newYorkPattern.matcher(in)) != null && m.find()) {
					stateOn = true;
				}
				if(bodyOn && (m = bodyEndPattern.matcher(in)) != null && m.find()) {
					stateOn = false;
					bodyOn = false;
				}
				if(dataOn && (m = dataEndPattern.matcher(in)) != null && m.find()) {
					dataOn = false;

					if(data != null) {
						String[] tuple = data.toString().split("\n");

						String district = tuple[0].replaceAll("<.*?>", "");

						String name = tuple[2].replaceAll("<.*?>", "").trim();

						String url = tuple[1].replaceAll("(?i)<td><a href=\"(.*?)\">","$1");

						Congressional c = new Congressional(
								"Congressional District " + district,new Member(name, url, Member.MemberType.Congress));

						ret.add(c);

						data = null;
					}
				}

				if(dataOn) {
					if(data == null)
						data = new StringBuffer();
					data.append(in).append("\n");
				}

				if(stateOn && !bodyOn && (m = bodyStartPattern.matcher(in)) != null && m.find()) {
					bodyOn = true;
				}
				if(bodyOn && !dataOn && (m = dataStartPattern.matcher(in)) != null && m.find()) {
					dataOn = true;
				}
			}

			br.close();
		}
		catch(IOException e) {
			logger.error(e);
		}

		return ret;
	}
}
