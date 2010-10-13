package scrapers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import model.districts.Senate;
import model.districts.Senator;

public class NYSenateScraper {

	static final String DISTRICT_URL = "http://www.nysenate.gov/district/";
	static final String CONTACT = "/contact";
	static final String NYSENATE = "http://www.nysenate.gov";
	static final String EMAIL = "@senate.state.ny.us";
	static final String IMAGE_URL = "http://www.nysenate.gov/files/imagecache/senator_teaser/profile-pictures/";
	
	public static Senate populateSenateData(int district) throws MalformedURLException, IOException {
		Pattern senatorData = Pattern.compile("<div class=\"senator_name\"><a href=\"(/senator/.*?)\">(.*?)</a></div>");
		Pattern senatorImage = Pattern.compile("http://www\\.nysenate\\.gov/files/imagecache/senator_teaser/profile\\-pictures/(.+?\\.(JPG|jpg|gif|GIF))");
		
		Senate senate = null;
		
		senate = new Senate();
		senate.setSenator(new Senator());
		
		String url = DISTRICT_URL + (district < 10 ? "0" + district:district);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		
		String in = null;
		
		while((in = br.readLine()) != null) {
			Matcher m = senatorData.matcher(in);
			
			if(m.find()) {
				senate.getSenator().setName(m.group(2));
				senate.getSenator().setContact(senatorContact(NYSENATE + m.group(1) + CONTACT));
				senate.getSenator().setUrl(NYSENATE + m.group(1));
				senate.setDistrictUrl(url);
				senate.setDistrict("State Senate District " + district);
			}
			else {
				m = senatorImage.matcher(in);
				
				if(m.find()) {
					senate.getSenator().setImageUrl(IMAGE_URL + m.group(1));
				}
			}
		}
		
		br.close();
		
		return senate;
	}
	
	public static String senatorContact(String url) throws MalformedURLException, IOException {
		Pattern p = Pattern.compile("<span class=\"field-content\"><span class=\"spamspan\">" +
				"<span class=\"u\">(.*?)</span> \\[at\\] (.*?)</span></span></span>");
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		String ret = null;
		String in = null;
		
		while((in = br.readLine()) != null) {
			Matcher m = p.matcher(in);
			
			if(m.find()) {
				ret = m.group(1) + "@" + m.group(2).replaceAll("<.*?>", "").replaceAll("\\s*\\[dot\\]\\s*",".");
			}
		}
		
		br.close();
		
		return ret;
	}
	
	public static String getDistrict(String district) {
		Pattern p = Pattern.compile("State Senate District (\\d{1,2})");
		Matcher m = p.matcher(district);
		if(m.find()) {
			return m.group(1);
		}
		//this should never happen
		return null;
	}
}
