package control;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;


import model.Ignore;
import model.PersistentObject;

public class Connect {
	
	public static final String GET = "get";
	public static final String SET = "set";
	public static final String DATABASE = "jdbc:mysql://localhost/geoapi";
	public static final String USER = "geoapi";
	public static final String PASS = "ga2010";
	
	Connection connection;
	Properties properties;
	
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Connection getConnection() {
		if(connection == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				connection = DriverManager.getConnection(DATABASE,USER,PASS);
			} catch (Exception e) {
				System.err.println("ERROR: Connect.getConnection() could not open the database listed in ");
				e.printStackTrace();
			}
		}
		return connection;
	}
	
	public boolean persistObject(Object o) {
		Statement s = null;
		try {
			s = getConnection().createStatement();
		} catch (SQLException e) {
			System.err.println("Error: Connect.persistObject() could not create statement");
			e.printStackTrace();
			return false;
		}
		
		HashMap<String,String> map = getObjectMap(o);
		
		if(map.isEmpty()) {
			return false;
		}
		
		String fields = "";
		String values = "";
		
		for(String key:map.keySet()) {
			fields = fields + ((!fields.equals("")) ? ",":"") + key;
			values = values + ((!values.equals("")) ? ",":"") + "'" + map.get(key) + "'";
		}
		
		String query = "INSERT INTO " 
			+ o.getClass().getSimpleName().toLowerCase() + " (" 
			+ fields
			+ ") VALUES (" 
			+ values
			+ ")";
		
		try {
			s.executeUpdate(query);
			
		}
		catch (Exception e) {
			System.err.println("ERROR: Connect.persistObject() unable to execute insert");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public ResultSet getQuery(String query) {
		Statement s = null;
		try {
			s = getConnection().createStatement();
		}
		catch(SQLException e) {
			System.err.println("Error: Connect.getQuery() could not create statement");
			e.printStackTrace();
			return null;
		}
		try {
			s.executeQuery(query);
		} catch (SQLException e) {
			System.err.println("ERROR: Connect.getQuery() unable to execute insert");
			e.printStackTrace();
			return null;
		}
		ResultSet rs = null;
		try {
			rs = s.getResultSet();
		} catch (SQLException e) {
			System.err.println("ERROR: Connect.getQuery() unable to retrieve result set for query");
			e.printStackTrace();
			return null;
		}
		return rs;
	}
	
	public ResultSet getObjects(Class<?> clazz) {		
		String query = "SELECT * from " + clazz.getSimpleName().toLowerCase();
		
		return getQuery(query);
	}
	
	public ResultSet getObjectById(Class<?> clazz, String field, String id) {		
		String query = "SELECT * FROM " 
			+ clazz.getSimpleName().toLowerCase() 
			+ " WHERE "
			+ field
			+ "="
			+ "'" + id + "'";
				
		return getQuery(query);
	}
	
	public boolean deleteObjects(Class<?> clazz) {
		Statement s = null;
		try {
			s = getConnection().createStatement();			
		}
		catch (SQLException e) {
			System.err.println("Error: Connect.deleteObjectById() could not create statement");
			e.printStackTrace();
			return false;
		}
		
		String query = "TRUNCATE TABLE " + clazz.getSimpleName();
		
		try {
			s.executeUpdate(query);
		} catch (SQLException e) {
			System.err.println("ERROR: Connect.deleteObjectById() unable to execute insert");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean deleteObjectById(Class<?> clazz, String field, String id) {
		Statement s = null;
		try {
			s = getConnection().createStatement();			
		}
		catch (SQLException e) {
			System.err.println("Error: Connect.deleteObjectById() could not create statement");
			e.printStackTrace();
			return false;
		}
		
		String query = "DELETE FROM " 
			+ clazz.getSimpleName().toLowerCase() 
			+ " WHERE "
			+ field
			+ "="
			+ "'" + id + "'";
		try {
			s.executeUpdate(query);
		} catch (SQLException e) {
			System.err.println("ERROR: Connect.deleteObjectById() unable to execute insert");
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	public HashMap<String,String> getObjectMap(Object o) {
		HashMap<String,String> map = new HashMap<String,String>();
		
		if(o == null) {
			return map; 
		}
		
		Field[] fields = o.getClass().getDeclaredFields();
		
		for(Field f: fields) {
			
			if(f.getAnnotation(Ignore.class) != null) {
				continue;
			}
			if(f.getAnnotation(PersistentObject.class) != null) {
				try {
					map.putAll(getObjectMap(o.getClass().getMethod(GET + fixFieldName(f.getName())).invoke(o)));
				}
				catch (Exception e) {
					System.err.println("Error: Connect.getObjectData() unable to retrieve get function for parameter " + f.getName());
					e.printStackTrace();
					continue;
				}
			}
			else {
				String fieldName = f.getName().toLowerCase();
				 
				Method fieldMethod = null;
				try {
					fieldMethod = o.getClass().getMethod(GET + fixFieldName(f.getName()));
				} catch (Exception e) {
					System.err.println("Error: Connect.getObjectData() unable to retrieve get function for parameter " + f.getName());
					e.printStackTrace();
					continue;
				}
				
				Object fieldObject = null;
				try {
					fieldObject = fieldMethod.invoke(o);
				} catch (Exception e) {
					System.err.println("Error: Connect.getObjectData() unable to invoke method " + fieldMethod.getName());
					e.printStackTrace();
					continue;
				}
				
				if(f.getType().equals(String.class)) {
					fieldObject = cleanse((String)fieldObject);
				}
				
				map.put(fieldName, (fieldObject != null) ? fieldObject.toString(): "");
			}
		}
		return map;
	}
	
	public String cleanse(String s) {
		return s.replaceAll("\"","&quot;").replaceAll("'", "&sing;");
	}
	
	public String fixFieldName(String s) {
		char[] chars = s.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}
	
	public Object objectFromClosedResultSet(Class<?> clazz, ResultSet rs) {
		Object o = null;
		try {
			if(rs.next()) {
				o = objectFromOpenResultSet(clazz, rs);
			}
		} catch (SQLException e) {
			System.err.println("Error: Connect.objectFromClosedResultSet() unable to open result set");
			e.printStackTrace();
		}
		return o;
	}
	
	public Object objectFromOpenResultSet(Class<?> clazz, ResultSet rs) {
		Object o = null;
		
		try {
			o = clazz.newInstance();
			
			Field[] fields = clazz.getDeclaredFields();
			
			for(Field f:fields) {
				if(f.getAnnotation(Ignore.class) != null) {
					continue;
				}
				if(f.getAnnotation(PersistentObject.class) != null) {
					Method m = clazz.getMethod(SET + fixFieldName(f.getName()), f.getType());
					m.invoke(o, objectFromOpenResultSet(f.getType(),rs));
				}
				else {
					Method m = clazz.getMethod(SET + fixFieldName(f.getName()), f.getType());
					m.invoke(o, (f.getType().equals(String.class) ? uncleanse((String)rs.getObject(f.getName().toLowerCase())):rs.getObject(f.getName().toLowerCase())));
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return o;
	}
	
	public String uncleanse(String s) {
		return s.replaceAll("&quot;", "\"").replaceAll("&sing;", "'");
	}
	
}
