package batch;

import generated.geoserver.json.GeoFeatures;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import control.Connect;
import control.DistrictServices;
import control.DistrictServices.WFS_REQUEST;
import control.GeoCode;

import model.Point;
import model.districts.Senate;
//16 division st, oneonta 13820
public class DonBatch {
	public static void main(String[] args) throws SQLException, Exception {
		
		/*BufferedReader br = new BufferedReader(new FileReader(new File("etc/12-1edcross")));
		
		String in = null;
		int c1 = 0, c2 = 0;
		
		while((in = br.readLine()) != null) {
			String[] eds = in.split(":")[0].split(", ");
			String data = in.split(": ")[1];
			
			if(!eds[0].equals(eds[1])) {
				if(data.startsWith("(")) {
					System.out.println(in);
					c2++;
				}
			}
			c1++;
		}
		
		System.out.println(c1 + " : " + c2);
		
		br.close();*/
		
		
		
		
		
		
		
		/* cross section of ed */
		DistrictServices ds = new DistrictServices();
		
		Connect c = new Connect();
		
		BufferedReader br = new BufferedReader(new FileReader(new File("etc/12-1edgeo")));
		
		String one = null;
		String two = null;
		String three = null;
		String four = null;
		String five = null;
		
		int count = 0;
		double lat = 0.0;
		double lon = 0.0;
		
		while((one = br.readLine()) != null) {
			two = br.readLine();
			three = br.readLine();
			four = br.readLine();
			five = br.readLine();
			
			String[] eds = one.split(",");
			
			int ded = new Integer(eds[1]);
			int ned = new Integer(eds[2]);
						
			if(ded != ned) {
				count++;
			}
			
			four = four.replace(" |----> ", "");
			four = four.replace("\t ", ",");
			
			String[] gCoords = four.split(",");
			Point  p = new Point(new Double(gCoords[0]), new Double(gCoords[1]));
			
			
			/*WFS_REQUEST req = ds.new WFS_REQUEST("election");
			
			GeoResult gr = ds.fromGeoserver(req, p);
			
			GeoProperty gp = gr.getFeatures().iterator().next().getProperties();
			
			String district = eds[2];
			
			HashSet<String> districts = new HashSet<String>();
			
			GeoResult gr2 = ds.handleGeoserverJson(ds.flatten(req.constructCross(p.lat, p.lon, true, .01)));
			
			for(GeoFeatures gf:gr2.getFeatures()){
				if(!gf.getProperties().getED().equals(district)) {
					districts.add(gf.getProperties().getED());
				}
			}
			
			gr2 = ds.handleGeoserverJson(ds.flatten(req.constructCross(p.lat, p.lon, false, .01)));
			
			
			for(GeoFeatures gf:gr2.getFeatures()){
				if(!gf.getProperties().getED().equals(district)) {
					districts.add(gf.getProperties().getED());
				}
			}
			
			System.out.print(ded + ", " + district + ": ");
			for(String key:districts) {
				System.out.print(key + " ");
			}
			System.out.println("(" + p.lat + ", " + p.lon + ")");*/
			
			five = five.replace(" |----> ", "");
			five = five.replace("\t ", ",");
			
			String[] coords = five.split(",");
			double nlat = new Double(coords[0]);
			double nlon = new Double(coords[1]);
			
			lat += nlat;
			lon += nlon;
			
		}
		
		System.out.println(count);
		System.out.println(lat/1000);
		System.out.println(lon/1000);
		
		
		br.close();
		c.close();
		
		
		
		
		/*int off = 0;
		int lc = 0;
		
		BufferedReader br = new BufferedReader(new FileReader(new File("etc/out2")));
		
		String one = null;
		String two = null;
		
		while((one = br.readLine()) != null && (two = br.readLine()) != null) {
			br.readLine();
			lc++;
			
			String ed1 = null, ed2 = null;
			
			Pattern p1 = Pattern.compile("ed: (\\d+)");
			Matcher m1 = p1.matcher(one);
			
			if(m1.find()) {
				ed1 = m1.group(1);
			}
			
			Pattern p2 = Pattern.compile("\\d+");
			Matcher m2 = p2.matcher(two);
			
			if(m2.find()) {
				ed2 = m2.group(0);
			}
			
			if(!new Integer(ed1).equals(new Integer(ed2))) {
				off++;
			}
			
		}

		System.out.println(lc + " : " + off + " : " + new Double(off) / new Double(lc));
		
		br.close();*/
		
		
		/* reading and geocoding */
/*		DistrictServices ds = new DistrictServices();
		
		BufferedReader br = new BufferedReader(new FileReader(new File("etc/12-1ed")));
		
		String in = null;
		
		DonFormat df = new DonFormat();
		
		String[] format = br.readLine().split("\t");
		
		HashMap<String, Method> methodMap = new HashMap<String,Method>();
		
		for(int i = 0; i < format.length; i++) {
			String fName = firstLetterCase(fixFieldName(format[i]), false);
			
			Method fieldMethod = df.getClass().getDeclaredMethod("set" + fName, String.class);
						
			methodMap.put(fName, fieldMethod);
			
			format[i] = fName;
		}
		
		while((in = br.readLine()) != null) {
			String ed = in.split(":")[0];
			
			String[] tuple = in.split(":")[1].split("\t");
			
			for(int i = 0; i < tuple.length; i++) {
				Method fieldMethod = methodMap.get(format[i]);
				
				fieldMethod.invoke(df, tuple[i]);

			}
			
			Point p = GeoCode.getGeoCodedResponse(df.houseNo + " " + df.street + ", " + df.city + ", NY " + df.zIP, "yahoo");
			
			WFS_REQUEST req = ds.new WFS_REQUEST("election");
			
			GeoResult gr = ds.fromGeoserver(req, p);
			
			GeoProperty gp = gr.getFeatures().iterator().next().getProperties();
			
			System.out.println(ed + "," + gp.getED()
					+ "\n |--> " + df.houseNo + " " + df.street + ", " + df.city + ", NY " + df.zIP
					+ "\n |----> " + df.latitude + "\t " + df.longitude
					+ "\n |----> " + p.lat + "\t " + p.lon
					+ "\n |----> " + Math.abs(new Double(df.latitude) - p.lat) + "\t " + Math.abs(new Double(df.longitude) - p.lon));
			
			df = new DonFormat();
		}
		br.close();*/
		
		
		/*DistrictServices ds = new DistrictServices();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("etc/12-1ed")));
		
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
		
		while((in = br.readLine()) != null && count < 1000) {
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
				if(df.ward == null || df.ward.equals("") || df.ward.equals(" ")) {

					Point p = new Point(new Double(df.latitude), new Double(df.longitude));
					
					WFS_REQUEST req = ds.new WFS_REQUEST("election");
					
					GeoResult gr = ds.fromGeoserver(req, p);
					
					GeoProperty gp = gr.getFeatures().iterator().next().getProperties();
					
					
					if(!gp.getNAMELSAD().replaceAll("State Senate District ", "").equals(df.sd)) {
						bw.write(in);
						count++;
						
						System.out.println(count + " : " + df.sd + " : " + gp.getNAMELSAD().replaceAll("State Senate District ", ""));
					}
					
					if(!new Integer(gp.getED()).equals(new Integer(df.ed))) {
						bw.write(gp.getED() + "," + df.ed + ":" + in + "\n");
						count++;
						System.out.println(count);
					}
					
					//bw.write("\n\ned: " +gp.getED() + ", EDP:" + gp.getEDP() + ", EDS_COPY_:" + gp.getEDS_COPY_() + ", MCD2:" + gp.getMCD2() + ", WARD:" + gp.getWARD()+ ", EDS_COPY_I:" + gp.getEDS_COPY_I());
					//bw.write("\n" + df.ward + " : " + df.ed);
					
				}
				
			}
			
			
			
						
			df = new DonFormat();
		}
				
		br.close();
		bw.close();*/
		
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
