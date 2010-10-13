package batch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShapeTest {

	public static void main(String[] args) throws IOException {
//		BufferedReader br = new BufferedReader(new InputStreamReader(new URL("http://www.usps.com/ncsc/lookups/abbr_suffix.txt").openStream()));
//		
//		String in = null;
//		
//		br.readLine();
//		br.readLine();
//		br.readLine();
//		br.readLine();
//		br.readLine();
//		
//		ArrayList<String> list = new ArrayList<String>();
//		
//		while((in = br.readLine()) != null) {
//			if(!list.contains(in.replaceAll("^\\w+?\\s+?\\w+?\\s+", ""))) {
//				list.add(in.replaceAll("^\\w+?\\s+?\\w+?\\s+", ""));
//			}
//		}
//		
//		String res = "(";
//		
//		for(String s:list) {
//			res += s + "|";
//		}
//		
//		res += ")";
//		
//		System.out.println(res);
//		
//		br.close();
		
//		String file = "C:\\Documents and Settings\\Wililiams\\Desktop\\schdist04\\schdist04.shp";
//
//		RandomAccessFile raf = new RandomAccessFile(file, "rw");
//
//		int fileCode = getBigEndianInt(4, raf); //0
//
//		getBigEndianInt(20, raf); //4 unused
//
//		int fileLength = getBigEndianInt(4, raf); //24
//
//		int version = getLittleEndianInt(4, raf); //28
//
//		int shapeType = getLittleEndianInt(4, raf); //32
//
//		getBigEndianInt(32, raf); //36 xmin(8) - ymin(8) - xmax(8) - ymax(8)
//
//		getBigEndianInt(16, raf); //68 zmin(8) - zmax(8)
//
//		getBigEndianInt(16, raf); //84 mmax(8) - mmin(8)
//
//		getBigEndianInt(4,raf); //100 record number
//		
//		int length = getBigEndianInt(4,raf); //104 record length
//		
//		int recordShapeType = getLittleEndianInt(4,raf); //108 record shape type
//				
//		getLittleEndianInt(32,raf); //112 record box
//		
//		int numParts = getLittleEndianInt(4,raf); //144 numparts
//		
//		int numPoints = getLittleEndianInt(4,raf); //148 numpoints
//		
//		int parts = getLittleEndianInt(4,raf); //152 parts
//				
//		for(int i = 0; i < numPoints+100000000; i++) {
//			double x =  Double.longBitsToDouble(Long.reverseBytes(raf.readLong()));
//			
//			double y =  Double.longBitsToDouble(Long.reverseBytes(raf.readLong()));
//			
//			raf.readLong();
//			
////			System.out.println(x + " : " + y);
//		}
//		
//		System.out.println(raf.length() + " : " + length);

	}

	public static int getBigEndianInt(int length, RandomAccessFile f)
			throws IOException {
		int res = 0;

		for (int i = 0; i < length; i++) {
			short sh = (short) (f.readByte() & 0xff);
			if (i == 0) {
				res = sh;
			} else {
				res = (res << 8) | sh;
			}
		}

		return res;
	}
	
	public static int getLittleEndianInt(int length, RandomAccessFile f)
			throws IOException {
		int res = 0;

		for (int i = 0; i < length; i++) {
			short sh = (short) (f.readByte() & 0xff);
			if (i == 0) {
				res = sh;
			} else {
				res = res | (sh << (i * 8));
			}
		}

		return res;
	}
}
