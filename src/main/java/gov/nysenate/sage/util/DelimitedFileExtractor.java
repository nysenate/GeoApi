package gov.nysenate.sage.util;

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
