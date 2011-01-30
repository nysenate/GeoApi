package gov.nysenate.sage.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GeoCheck {
	static int SIZE = 5000;
	public static void main(String[] args) throws IOException {
		double[] geocoderLat = new double[SIZE];
		double[] geocoderLon = new double[SIZE];
		String[] geocoderScore = new String[SIZE];
		String[] geocoderUrl = new String[SIZE];
		
		double[] yahooLat = new double[SIZE];
		double[] yahooLon = new double[SIZE];
		
		BufferedReader br = new BufferedReader(new FileReader(new File("etc/gt10")));
		
		String in = null;
		
		for(int i = 0; i < SIZE; i++) {
			in = br.readLine();
			String[] tuple = in.split(", ");
			/* tuple[0] = lat
			 * 		[1] = lon */
			
			geocoderLat[i] = new Double(tuple[0]);
			geocoderLon[i] = new Double(tuple[1]);
			geocoderScore[i] = tuple[2];
			geocoderUrl[i] = in.replaceFirst(".+?,.+?,.+?,", "");
		}
		
		br.readLine();br.readLine();br.readLine();br.readLine();br.readLine();br.readLine();br.readLine();
				
		for(int i = 0; i < SIZE; i++) {
			in = br.readLine();
			String[] tuple = in.split(", ");
			/* tuple[0] = lat
			 * 		[1] = lon */
			
			yahooLat[i] = new Double(tuple[0]);
			yahooLon[i] = new Double(tuple[1]);
		}
		br.close();
		
		double diffLatTotal = 0.0;
		double diffLonTotal = 0.0;
		
		double maxLat = 0.0;
		double maxLon = 0.0;
		
		int count = 0;
		
		for(int i = 0; i < SIZE ; i++) {
			double diffLat = Math.abs(yahooLat[i] - geocoderLat[i]);
			double diffLon = Math.abs(yahooLon[i] - geocoderLon[i]);
			
			diffLatTotal += diffLat;
			diffLonTotal += diffLon;
			
			if(diffLat > maxLat) {
				maxLat = diffLat;
			}
			if(diffLon > maxLon) {
				maxLon = diffLon;
			}
			
			if(diffLat > .1 || diffLon > .1) {
				count++;
				System.out.println(diffLat + ", " + diffLon + ", " + geocoderScore[i] + ": (" + yahooLat[i] + ", " + yahooLon[i] + ")" + geocoderUrl[i]);
			}
			
//			if(diffLat < .001 && diffLat > .0009 && diffLon < .001 && diffLat > .0009) {
//				System.out.println(diffLat + ", " + diffLon + ", " + geocoderScore[i] + ": (" + yahooLat[i] + ", " + yahooLon[i] + ")" + geocoderUrl[i]);
//				System.exit(0);
//			}
			
		}
//		System.out.println(count);
		System.out.println("lat: " +maxLat + "\nlon " + maxLon 
				+ "\navg lat: " + (diffLatTotal/SIZE) + "\navg lon " + (diffLonTotal/SIZE));
	}
}
