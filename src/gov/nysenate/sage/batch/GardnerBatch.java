package gov.nysenate.sage.batch;

import gov.nysenate.sage.model.ValidateResponse;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.google.gson.Gson;

public class GardnerBatch {
	
	public static String GEO_EXTENDED_BASE = "http://geo.nysenate.gov/api/json/validate/extended?";
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("limit.csv"));
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("out", true));
		
		String in = null;
				
		while((in = br.readLine()) != null) {
			if(in.startsWith("*")) {
				bw.write(in.replaceAll("\\*","") + "\n");
			}
			else{
				bw.write(in + "\n");
			}
		}
				
		br.close();
		
		bw.close();
	}
	
//	public static void main(String[] args) throws IOException {
//		String nuxrefve = null;
//		String cat = null;
//		String street1 = null;
//		String street2 = null;
//		String city = null;
//		String state = null;
//		String zip = null;
//		
//		Pattern p = Pattern.compile("\"(.*?)\"");
//		
//		Gson gson = new Gson();
//		
//		BufferedReader br = new BufferedReader(new FileReader("limit"));
//		
//		BufferedWriter bw = new BufferedWriter(new FileWriter("out", true));
//		
//		String in = null;
//		
//		while((in = br.readLine()) != null) {
//			String val = in;
//			try {
//				Matcher m = p.matcher(in);
//				while(m.find()) {
//					String r = in.substring(m.start(),m.end());
//					r = r.replaceAll(",", "&comm;");
//					in = in.substring(0,m.start()) + r + in.substring(m.end());
//				}
//				
//				String[] strings = in.split(",");
//				
//				nuxrefve = strings[0];
//				cat = strings[1];
//				street1 = strings[2];
//				street2 = strings[3].replaceAll("-"," ");
//				city = strings[4];
//				state = strings[5];
//				zip = strings[6];
//				
//				ValidateResponse resp = gson.fromJson(
//						fromGeoApi(street1 + (street2 != null ? " " + street2:""), city, state, zip),
//						ValidateResponse.class);
//				
//				bw.newLine();
//				bw.write(nuxrefve 	
//						+ "," 
//						+ cat 
//						+ "," 
//						+ (resp.getAddress2().contains(",") ? "\"" + resp.getAddress2() + "\"" : resp.getAddress2())
//						+ ",,"
//						+ resp.getCity()
//						+ ","
//						+ resp.getState()
//						+ ","
//						+ resp.getZip5() + "-" + resp.getZip4()
//						);
//			}
//			catch (Exception e) {
//				System.err.println("ERROR ON LINE: " + val);
//				e.printStackTrace();
//				bw.newLine();
//				bw.write("************" + val + "************");
//			}
//			
//			
//		}
//		
//		br.close();
//		bw.close();
//		
//	}
	
	public static String fromGeoApi(String addr, String city, String state, String zip)
												throws MalformedURLException, IOException {
		
		addr = addr.replaceAll("&comm;",",").replaceAll("\"","");
		
		String url = GEO_EXTENDED_BASE 
						+ "addr2=" + addr 
						+ "&city=" + city 
						+ "&state=" + state
						+ "&zip=" + zip;
		
		url = url.replaceAll(" ","%20");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		
		String ret = br.readLine();
		
		return ret;
	}
}



















