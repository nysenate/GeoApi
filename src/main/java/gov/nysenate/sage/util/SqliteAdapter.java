package gov.nysenate.sage.util;

import java.sql.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class SqliteAdapter {
	private Logger logger = Logger.getLogger(SqliteAdapter.class);
	
	private static SqliteAdapter sqliteAdapter = null;
	
	public static synchronized SqliteAdapter getInstance() {
		if(sqliteAdapter == null) {
			sqliteAdapter = new SqliteAdapter();
		}
		return sqliteAdapter;
	}
	
	private Connection connection = null;
	private final String sqliteLocation = Resource.get("sqlite.path");
	
	private SqliteAdapter() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			logger.error(e);
		}
	}
	
	private Connection getConnection() {
		try {
			if(connection == null || connection.isClosed()) {
				connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteLocation);
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		
		return connection;
	}
	
	private void closeConnection() {
		try {
			if(!connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			logger.error(e);
		}
	}
	
	public ArrayList<String> getStreetsForZip(String zip) {
		ArrayList<String> results = new ArrayList<String>();
		if(zip.matches("\\d{5}")) {
			PreparedStatement prep = null;
			try {
				prep = getConnection().prepareStatement("select street from feature where zip=?");
				prep.setInt(1, new Integer(zip));
				
				ResultSet rs = prep.executeQuery();
				
				while(rs.next()) {
					results.add(rs.getString("street"));
				}
				
				this.closeConnection();
				
				return results;
			} catch (SQLException e) {
				logger.error(e);
			}
			
		}
		return null;
	}
}
