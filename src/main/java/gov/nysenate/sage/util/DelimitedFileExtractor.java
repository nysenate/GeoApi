package gov.nysenate.sage.util;

import generated.geoserver.json.GeoResult;
import gov.nysenate.sage.connectors.GeoServerConnect;
import gov.nysenate.sage.connectors.YahooConnect;
import gov.nysenate.sage.model.Point;
import gov.nysenate.sage.model.BulkProcessing.PublicWebsiteTSV;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class DelimitedFileExtractor {	
	private String  delim;
	private String header;
	private Class<?> clazz;
	private String[] format;
	HashMap<String, Method> methodMap;
	
	public DelimitedFileExtractor(String delim, String header, Class<?> clazz) {
		this.delim = delim;
		this.header = header;
		this.clazz = clazz;
		this.format = splitHeader();
		this.methodMap = processHeader();
	}
	
	
	public Object processTuple(String input){
		Object o = null;
		try {
			o = clazz.newInstance();
			
			input = input.replaceAll(delim + delim, delim + " " + delim);
			String[] tuple = input.split(delim);
			for (int i = 0; i < tuple.length; i++) {
				Method fieldMethod = methodMap.get(format[i]);
				fieldMethod.invoke(o, tuple[i].replaceAll("(^\"|\"$)", ""));
			}

			return o;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
			
		return o;
	}

	private HashMap<String, Method> processHeader() {
		HashMap<String, Method> methodMap = new HashMap<String, Method>();
		try {			
			for (int i = 0; i < format.length; i++) {
				 Method fieldMethod = clazz.getDeclaredMethod("set" + format[i],String.class);
				 methodMap.put(format[i], fieldMethod);
			}
		}
		catch(SecurityException se) {
			se.printStackTrace();
		}
		catch(NoSuchMethodException nsme) {
			nsme.printStackTrace();
		}
		return methodMap;
	}

	private String[] splitHeader() {
		String[] format = header.split(delim);
		for (int i = 0; i < format.length; i++) {
			format[i] = firstLetterCase(fixFieldName(format[i]), false);
		}
		return format;
	}

	private String fixFieldName(String s) {
		return s.replaceAll("( |\\W)", "");
	}

	private String firstLetterCase(String s, boolean lowerCase) {
		char[] chars = s.toCharArray();
		chars[0] = (lowerCase ? Character.toLowerCase(chars[0]) : Character
				.toUpperCase(chars[0]));
		return new String(chars);
	}
	
	public static void main(String[] args) throws IOException {
		HashMap<String, BufferedWriter> writerMap = new HashMap<String, BufferedWriter>();
		
		String writeBase = "src/main/resources/etc/ryan/";
		String outOfStateName = writeBase + "out-of-state-veteran-nominations.csv";
		String header = "\"Time\",\"IP Address\",\"Username\",\"Your Name\",\"Your Address\"," +
				"\"City/Town1\",\"Zip Code1\",\"Your Telephone Number\",\"Your Email Address\"," +
				"\"Name of Veteran\",\"Branch of Military Veteran Served in\",\"Why are you nominating " +
				"this Veteran to be a member of the New York State Senate Veteran Hall of Fame?\"," +
				"\"Street Address of Veteran\",\"City/Town2\",\"Zip Code2\",\"Senate District\"";
		String in = null;
		
		BufferedReader br = new BufferedReader(new FileReader(new File("src/main/resources/etc/ryan/nominate_cleansed.tsv")));
		DelimitedFileExtractor dfe = new DelimitedFileExtractor("\t", br.readLine(), PublicWebsiteTSV.class);
		
		GeoResult gr = null;
		GeoServerConnect gsCon = new GeoServerConnect();
		YahooConnect yc = new YahooConnect();

		while((in = br.readLine()) != null) {
			PublicWebsiteTSV tuple = (PublicWebsiteTSV) dfe.processTuple(in);
			
			if(tuple.getZipCode1().matches("\\d{5}([\\- ]\\d{4})?") || tuple.getZipCode2().matches("\\d{5}([\\- ]\\d{4})?")) {
				String address = tuple.getYourAddress() + ", " + tuple.getCityTown1() + ", " + tuple.getZipCode1();
				
				Point p = yc.doParsing(address);
				gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST("senate"), p);
				
				String sd = null;
				try {
					sd = gr.getFeatures().iterator().next().getProperties().getNAMELSAD().replace("State Senate District ","");
				}
				catch(Exception e) { 
					BufferedWriter bw = null;
					if((bw = writerMap.get(outOfStateName)) == null) {
						bw = new BufferedWriter(new FileWriter(new File(outOfStateName)));
						bw.write(header + "\n");
						writerMap.put(outOfStateName, bw);
					}
					bw.write((tuple.toString() + ",\"" + "\"\n").replaceAll("<br/>", "\n"));
					continue; 
				}
			
				BufferedWriter bw = null;
				if((bw = writerMap.get(sd)) == null) {
					bw = new BufferedWriter(new FileWriter(new File(writeBase + "sd" + sd + "-veteran-nominations.csv")));
					bw.write(header + "\n");
					writerMap.put(sd, bw);
				}
				
				bw.write((tuple.toString() + ",\"" + sd + "\"\n").replaceAll("<br/>", "\n"));
			}
		}
		
		for(BufferedWriter bw:writerMap.values()) {
			bw.close();
		}
		
		br.close();
		
//		String in = null, file = "";
//		while((in = br.readLine()) != null) file += in + "\n";
//		System.out.println(file.replaceAll("(?!(\t|\n))\\p{Cntrl}","").replaceAll("\n\n","<br/>"));
	}
	
	public static void generateClassValuesFromHeader(String header, String delim) {
		header = header.replace(delim + delim,delim + " " + delim);
		String[] fields = header.split(delim);
		
		String classFields = "";
		String emptyFields = "";
		String toString = "";
		
		for(String field:fields) {
			field = field.replaceAll("( |\\W)", "");
			
			classFields += "String " + field + ";\n";
			emptyFields += field + "=\"\";\n";
			if(toString.equals(""))
				toString = field;
			else 
				toString += " + \"" + delim + "\" + " + field;
		}
		
		System.out.println(classFields + "\n\n\n" + emptyFields + "\n\n\n" + toString);
	}
}
