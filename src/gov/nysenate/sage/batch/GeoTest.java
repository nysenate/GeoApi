package gov.nysenate.sage.batch;

import gov.nysenate.sage.util.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class GeoTest {
	static int SIZE = 500;
	
	public static void main(String[] args) throws IOException {
		int count = 0;
		
		String url = Resource.get("geocode.url");
		
		long start = System.nanoTime();
		
		BufferedReader rens = new BufferedReader(new FileReader(new File(
				"etc/rensco_addresses.psv")));
		String rensIn = null;
		
		for (int i = 0; i < SIZE && (rensIn = rens.readLine()) != null; i++) {
			count ++;
			rensIn = rensIn.replaceAll("\\|\\|", "\\| \\|");

			String[] tuple = rensIn.split("\\|");

			/*
			 * tuple[0] = street number [2] = street [3] = apartment [4] = bldg?
			 * [6] = city [7] = state [8] = zip [9] = zip4
			 */

			String num = tuple[0];
			String street = tuple[2];
			String city = tuple[6];
			String state = tuple[7];
			String zip = tuple[8];

			String requestUrl = url + "&number=" + num + "&street=" + street
					+ "&city=" + city + "&state=" + state + "&zip5=" + zip;
			
//			String requestUrl = url + "address=" + num + " " + street
//				+ ", " + city + ", " + state + " " + zip;

			BufferedReader geo = new BufferedReader(new InputStreamReader(
					new URL(requestUrl.replaceAll(" ", "%20")).openStream()));
			String line = geo.readLine();
			geo.close();
			
			System.out.println(line + ": " + requestUrl);
			
//			Pattern p = Pattern.compile("\"score\":\"?(.+?)\"?,");
//			Matcher m = p.matcher(line);
//			
//			String score = null;
//			if(m.find()) score = m.group(1);
//			if(score == null) score=" ";
//			if(score.equals("null")) score = "-1";
//			
//			p = Pattern.compile("\"lon\":\"?(.+?)\"?,.+?\"lat\":\"?(.+?)\"?[,\\}]");
//			m = p.matcher(line);
			
//			if(m.find())
//				System.out.println(count + ": " + m.group(2) + ", " + m.group(1) + ", " + score + ", " + requestUrl);
//			else {
//				System.err.println(count + ": NO COORD FOUND!!  " + requestUrl);
//			}
			
		}
		rens.close();
		
		System.out.println("\n" + (System.nanoTime()-start)/1000000f + "ms");
		
//		System.out.println("\n\nBEGIN YAHOO\n\n");
//		
//		
//
//		start = System.nanoTime();
//		rens = new BufferedReader(new FileReader(new File("etc/rensco_addresses.psv")));
//		rensIn = null;
//		
//		YahooConnect yc = new YahooConnect();
//
//		for (int i = 0; i < SIZE && (rensIn = rens.readLine()) != null; i++) {
//			rensIn = rensIn.replaceAll("\\|\\|", "\\| \\|");
//
//			String[] tuple = rensIn.split("\\|");
//
//			String num = tuple[0];
//			String street = tuple[2];
//			String city = tuple[6];
//			String state = tuple[7];
//			String zip = tuple[8];
//			
//			Point p = yc.doParsing(num + " " + street + ", " + city + ", " + state + " " + zip);
//			
//			System.out.println(p.lat + ", " + p.lon);
//		}
//		rens.close();
//		
//		System.out.println("\n" + (System.nanoTime()-start)/1000000f + "ms");
	}
}
