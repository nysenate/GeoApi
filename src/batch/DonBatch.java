package batch;

import generated.geoserver.json.GeoProperty;
import generated.geoserver.json.GeoResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import control.DistrictServices;
import control.DistrictServices.WFS_REQUEST;

import model.Point;

public class DonBatch {
	public static void main(String[] args) throws IOException, SecurityException, NoSuchFieldException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
//		int off = 0;
//		int lc = 0;
//		
//		BufferedReader br = new BufferedReader(new FileReader(new File("etc/out2")));
//		
//		String one = null;
//		String two = null;
//		
//		while((one = br.readLine()) != null && (two = br.readLine()) != null) {
//			br.readLine();
//			lc++;
//			
//			String ed1 = null, ed2 = null;
//			
//			Pattern p1 = Pattern.compile("ed: (\\d+)");
//			Matcher m1 = p1.matcher(one);
//			
//			if(m1.find()) {
//				ed1 = m1.group(1);
//			}
//			
//			Pattern p2 = Pattern.compile("\\d+");
//			Matcher m2 = p2.matcher(two);
//			
//			if(m2.find()) {
//				ed2 = m2.group(0);
//			}
//			
//			if(!new Integer(ed1).equals(new Integer(ed2))) {
//				off++;
//			}
//			
//		}
//
//		System.out.println(lc + " : " + off + " : " + new Double(off) / new Double(lc));
//		
//		br.close();
		
		
		
		DistrictServices ds = new DistrictServices();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("etc/senate")));
		
		BufferedReader br = new BufferedReader(new FileReader(new File("etc/ostegocotest.TXT")));
		
		DonFormat df = new DonFormat();
		String in = null;
		
		String[] format = br.readLine().split("\t");
		
		HashMap<String, Method> methodMap = new HashMap<String,Method>();
		
		for(int i = 0; i < format.length; i++) {
			String fName = firstLetterCase(fixFieldName(format[i]), false);
			
			Method fieldMethod = df.getClass().getDeclaredMethod("set" + fName, String.class);
						
			methodMap.put(fName, fieldMethod);
			
			format[i] = fName;
		}
		
		int count = 0;
		
		while((in = br.readLine()) != null) {
			in = in.replaceAll("\t\t","\t \t");
			String[] tuple = in.split("\t");
			
			for(int i = 0; i < tuple.length; i++) {
				Method fieldMethod = methodMap.get(format[i]);
				
				fieldMethod.invoke(df, tuple[i]);

			}
			
			if(df.latitude == null || df.longitude == null) {
				//geocode
			}
			else {
//				if(df.ward == null || df.ward.equals("") || df.ward.equals(" ")) {

					Point p = new Point(new Double(df.latitude), new Double(df.longitude));
					
					WFS_REQUEST req = ds.new WFS_REQUEST("senate");
					
					GeoResult gr = ds.fromGeoserver(req, p);
					
					GeoProperty gp = gr.getFeatures().iterator().next().getProperties();
					
					
					if(!gp.getNAMELSAD().replaceAll("State Senate District ", "").equals(df.sd)) {
						bw.write(in);
						count++;
						
						System.out.println(count + " : " + df.sd + " : " + gp.getNAMELSAD().replaceAll("State Senate District ", ""));
					}
										
					/*bw.write("\n\ned: " +gp.getED() + ", EDP:" + gp.getEDP() + ", EDS_COPY_:" + gp.getEDS_COPY_() + ", MCD2:" + gp.getMCD2() + ", WARD:" + gp.getWARD()+ ", EDS_COPY_I:" + gp.getEDS_COPY_I());
					bw.write("\n" + df.ward + " : " + df.ed);*/
					
//					System.out.println("\r" + count);
//				}
				
			}
			
			
			
						
			df = new DonFormat();
		}
				
		br.close();
		bw.close();
		
	}
	
	public static String fixFieldName(String s) {
		return s.replaceAll("( |\\W)","");		
	}
	
	public static String firstLetterCase(String s, boolean toggle) {
		char[] chars = s.toCharArray();
		chars[0] = (toggle ? Character.toLowerCase(chars[0]) : Character.toUpperCase(chars[0]));
		return new String(chars);
	}
	
}
