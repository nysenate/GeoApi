package batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Point;

import control.GeoCode;

public class VarnoBatch {
	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(new File("SD03 071273 Hauppauge.csv")));
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("testout.csv")));
		
		String in = null;
		
		int count = 0;
		
		bw.write(br.readLine() + "\n");
				
		while((in = br.readLine()) != null) {
			Pattern p = Pattern.compile("\"(.*?)\"");
			Matcher m = p.matcher(in);
			while(m.find()) {
				String r = in.substring(m.start(),m.end());
				r = r.replaceAll(",", "&comm;");
				in = in.substring(0,m.start()) + r + in.substring(m.end());
			}
			
			String s[] = in.split(",");
			String addr = s[5] + " "
						+ s[6] + ", "
						+ s[8] + ", "
						+ s[9] + " ";
			addr = addr.replaceAll("\"","").replaceAll("&comm;",",");
			
			Point point = null;
			try {
				point = GeoCode.getGeoCodedResponse(addr, null);
			}
			catch (Exception e) {
				System.out.println("ERROR on " + in);
				break;
			}
			if(point == null) {
				System.out.println("ERROR on " + in);
				break;
			}
						
			String line = "";
			for(String str:s) {
				
				if(str.contains(",")) {
					line += "\"" + str + "\"" + ",";
				}
				else {
					line +=  str + ",";
				}
			}
			
			bw.write(line.replaceAll("&comm;",",") + point.lon + "/" + point.lat + ",schooldata\n");
			System.out.println(count);
			count++;
		}
		
		
		br.close();
		bw.close();
	}
}
