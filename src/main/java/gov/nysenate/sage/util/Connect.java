package gov.nysenate.sage.util;

import gov.nysenate.sage.model.MappedFields;
import gov.nysenate.sage.model.annotations.ForeignKey;
import gov.nysenate.sage.model.annotations.Ignore;
import gov.nysenate.sage.model.annotations.ListType;
import gov.nysenate.sage.model.annotations.PersistentObject;
import gov.nysenate.sage.model.annotations.PrimaryKey;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * @author Jared Williams
 *
 */
public class Connect {

	Logger logger = Logger.getLogger(Connect.class);

	public static final String ADD = "add";
	public static final String GET = "get";
	public static final String SET = "set";
	public final Resource APP_CONFIG;
	public final String DATABASE;
	public final String USER;
	public final String PASS;

	Connection connection;
	Properties properties;
	public Connect() throws IOException {
	    APP_CONFIG = new Resource();
	    DATABASE = APP_CONFIG.fetch("database.url");
	    USER = APP_CONFIG.fetch("database.user");
	    PASS = APP_CONFIG.fetch("database.pass");
	}

	public Connection getConnection() {
		if(connection == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				connection = DriverManager.getConnection(DATABASE,USER,PASS);
			} catch (Exception e) {
				logger.warn(e);
			}
		}
		return connection;
	}

	public void close() {
		try {
			if(connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			logger.warn(e);
		}
	}

	public boolean persist(Object o) {
		return persist(o, null);
	}

	/**
	 * recursively processes and persists a generic object using annotations provided in class structure (@PersistentObject,
	 * @PrimaryKey, and @ForeignKey).  This will work for single-tier objects (i.e. objects with only primitives) as well as
	 * multi-tier list based objects.  Based on annotations it will building automatically insert defined foreign-keys and for
	 * auto-increment ids it will automatically use the given id as the foreign key for any child objects.
	 *
	 * @param o generic object to be persisted
	 * @param foreignKey foreign key associated with given object and parent object, only necessary
	 * if object being passed in (o) contains dependent, foreign key relationships.
	 */
	@SuppressWarnings("unchecked")
	public boolean persist(Object o, Object foreignKey) {
		Class<? extends Object> clazz = o.getClass();

		try {
			/* generate object relationships */
			MappedFields mf = buildObjectFieldLists(clazz,o);

			/* if foreign keys are present set necessary values */
			for(Field f:mf.getForeigns()) {
				clazz.getDeclaredMethod(SET + fixFieldName(f.getName()), f.getType()).invoke(o, foreignKey);
			}

			 /* If key returns != null then the query executed returned an auto-increment foreign key,
			 * this reassigns the primary key to that value so any child objects will maintain the
			 * correct reference to their parent */
			Integer key = null;
			if((key = getKeyFromResultSet(persistObject(o, mf.getPersistableFields()))) != null) {
				if(mf.getPrimary() != null) {
					clazz.getDeclaredMethod(SET + fixFieldName(mf.getPrimaryField().getName()),
							mf.getPrimaryField().getType()).invoke(o, key);
					mf.setPrimary(key);
				}
			}

			/* recursively process @PersistentObjects now that foreign key and primary
			 * keys have been successfully applied */
			for(Field f:mf.getPersistents()) {
				Method m = clazz.getDeclaredMethod(GET + fixFieldName(f.getName()));

				Object invokedObject = m.invoke(o);


				/* if @PersistentObject is a list */
				if(invokedObject.getClass().equals(ArrayList.class)) {
					ArrayList<Object> invokedList = (ArrayList<Object>)invokedObject;
					for(Object objFromInvokedList:invokedList) {
						persist(objFromInvokedList, mf.getPrimary());
					}
				}
				else {
					persist(invokedObject, mf.getPrimary());
				}
			}
		}
		catch(Exception e) {
			logger.warn(e);
			return false;
		}
		return true;
	}

	private ResultSet persistObject(Object o, ArrayList<Field> persistentFields) {
		ResultSet generatedId = null;
		Statement s = null;
		try {
			s = getConnection().createStatement();
		} catch (SQLException e) {
			logger.warn(e);
			return null;
		}

		HashMap<String,Object> map = getObjectMap(o, persistentFields);

		if(map.isEmpty()) {
			return null;
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

			s.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = s.getGeneratedKeys();
			if(rs.next()) {
				generatedId = rs;
			}
		}
		catch (Exception e) {
			logger.warn(e);
			return null;
		}
		return generatedId;
	}

	public ResultSet getResultSetMany(Class<?> clazz) throws SQLException {
		String query = "SELECT * from " + clazz.getSimpleName().toLowerCase();

		return getResultSetFromQuery(query);
	}



	public ResultSet getResultsetById(Class<?> clazz, String field, Object value) throws SQLException {
		String query = "SELECT * FROM "
			+ clazz.getSimpleName().toLowerCase()
			+ " WHERE "
			+ field
			+ "="
			+ "'" + value + "'";

		return getResultSetFromQuery(query);
	}

	public ResultSet getResultSetFromQuery(String query) throws SQLException {

		Statement s = getConnection().createStatement();

		s.executeQuery(query);

		return s.getResultSet();
	}

	/**
	 * Called from getObject, iterates Persistable fields from object and
	 *
	 * (i.e Obj1 has fields (String,String,Obj2), the set of
	 * 		Strings would be processed via getResultsetById and listFromClosedResultSet in getObject
	 * 		but Obj2 handled here and passed in to a new process of getObject()
	 *
	 * 		Obj1--
	 * 			  \-getObject(Obj1,field,value,false)
	 * 						\- handleGetObjectPersistableFields(obj,Obj1)
	 * 								\				(field and value dependent upon @PrimaryKey from Obj1)
	 * 								 \-getObject(Obj2,field,value, (isList: true if @ListType
	 * 															annotation present, otherwise false)
	 * 																\-etc...
	 *
	 * @param o Object from getObject
	 * @param clazz class being processed
	 * @returns processed Object
	 * @throws Exception
	 */
	private Object handleGetObjectPersistableFields(Object o, Class<?> clazz) throws Exception {
		MappedFields mf = buildObjectFieldLists(clazz,o);
		boolean isList = false;

		for(Field f:mf.getPersistents()) {
			Class<?> fieldClass = f.getType();
			if(f.getType().equals(ArrayList.class)) {
				fieldClass = f.getAnnotation(ListType.class).value();
				isList = true;
			}

			clazz.getDeclaredMethod(SET + fixFieldName(f.getName()),
					f.getType()).invoke(o,
						getObject(fieldClass,
								mf.getPrimaryField().getName(),
								mf.getPrimary(), isList));
		}
		return o;
	}

	public Object getObject(Class<?> clazz, String field, Object value) throws Exception {
		return getObject(clazz, field, value, false);
	}

/*	public static void main(String[] args) throws Exception {
		Connect c = new Connect();

		List<Object> objs = c.listFromClosedResultSet(Senate.class,c.getResultSetMany(Senate.class));

		for(Object obj:objs) {
			System.out.println(obj);
		}
	}*/

	public List<?> getObjects(Class<?> clazz) throws SQLException, Exception {
		return listFromClosedResultSet(clazz, getResultSetMany(clazz));
	}

	/**
	 * Uses reflection to recursively populate multi-tiered Objects.  This method makes the base query and then
	 * passes the object to handleGetObjectPersistableFields to process any fields with @PersistableObject
	 * annotation
	 *
	 *
	 * @param clazz class being retreieved from the database
	 * @param field associated column from the table
	 * @param value value of the column
	 * @param isList true if param is a list, false otherwise (generally this can be passed in as false by default)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object getObject(Class<?> clazz, String field, Object value, boolean isList) throws Exception {
		Object o = null;

		ArrayList<Object> rsList = listFromClosedResultSet(clazz,getResultsetById(clazz, field, value));

		if(isList) {
			o = new ArrayList<Object>();
			for(Object rsListObject:rsList) {
				((ArrayList<Object>) o).add(handleGetObjectPersistableFields(rsListObject, clazz));
			}
		}
		else{
			if(!rsList.isEmpty()) {
				o = handleGetObjectPersistableFields(rsList.iterator().next(), clazz);
			}
		}

		return o;
	}

	public boolean deleteObjects(Class<?> clazz, boolean truncateChildren) throws Exception {

		if(truncateChildren) {
			MappedFields mf = buildObjectFieldLists(clazz, null);
			ArrayList<Field> persistentObjects = mf.getPersistents();

			for(Field field:persistentObjects) {
				ListType lt = null;
				if((lt = field.getAnnotation(ListType.class)) != null) {
					deleteObjects(lt.value(), true);
				}
				else {
					deleteObjects(field.getType(), true);
				}
			}
		}


		Statement s = null;
		try {
			s = getConnection().createStatement();
		}
		catch (SQLException e) {
			logger.warn(e);
			return false;
		}

		String query = "TRUNCATE TABLE " + clazz.getSimpleName().toLowerCase();

		try {
			s.executeUpdate(query);
		} catch (SQLException e) {
			logger.warn(e);
			return false;
		}

		return true;
	}

	public boolean deleteObjectById(Class<?> clazz, String field, String value) {
		Statement s = null;
		try {
			s = getConnection().createStatement();
		}
		catch (SQLException e) {
			logger.warn(e);
			return false;
		}

		String query = "DELETE FROM "
			+ clazz.getSimpleName().toLowerCase()
			+ " WHERE "
			+ field
			+ "="
			+ "'" + value + "'";
		try {
			s.executeUpdate(query);
		} catch (SQLException e) {
			logger.warn(e);
			return false;
		}

		return true;

	}

	/**
	 * This does the same thing as objectFromOpenResultSet, but the ResultSet is passed in
	 * unopened and instead of returning a single Object it will return a list of all
	 * returned objects.
	 *
	 * @param clazz Class being populated
	 * @param rs ResultSet from associated query
	 * @throws Exception
	 * @returns generated list of results from ResultSet
	 */
	public ArrayList<Object> listFromClosedResultSet(Class<?> clazz, ResultSet rs) throws Exception {
		ArrayList<Object> ret = new ArrayList<Object>();

		while(rs.next()) {
			ret.add(objectFromOpenResultSet(clazz, rs));
		}

		return ret;
	}

	/**
	 * Based on fields in clazz (and skipping over fields labled as @PersistentObject)
	 * this method finds the appropriate set method for the given Field
	 * and sets matching name value from ResultSet as its data.  This will only work with
	 * a ResultSet that has already been opened.
	 *
	 * @param clazz Class being populated
	 * @param rs ResultSet from associated query
	 * @return generated object from ResultSet
	 */
	public Object objectFromOpenResultSet(Class<?> clazz, ResultSet rs) throws Exception {
		Object o = null;

		o = clazz.newInstance();

		Field[] fields = clazz.getDeclaredFields();

		for(Field f:fields) {
			if(f.getAnnotation(PersistentObject.class) != null) {
				//skip, likely being processed elsewhere: (getObject(Class,String,Object,boolean))
			} else if (f.getAnnotation(Ignore.class) != null) {
			    // Skip ignored fields, I think
			}
			else {
				Method m = clazz.getMethod(SET + fixFieldName(f.getName()), f.getType());
				m.invoke(o, (f.getType().equals(String.class) ? uncleanse((String)rs.getObject(f.getName().toLowerCase())):rs.getObject(f.getName().toLowerCase())));
			}
		}

		return o;
	}

	/**
	 * Used during object persistence.  Builds HashMap<String,Object> of the object being passed in,
	 * ignoring fields with @Ignore annotation (a use of this could be an auto-generated id that
	 * the database will set for you which you would not typically set yourself).  The map returned
	 * contains values <fieldName, fieldData> and is use in building insert statements in persistObject(Object,ArrayList<Field>)
	 *
	 * This should only be used for objects with a valid toString function, more complex objects can be processed
	 * separately using the @PersistentObject annotation.
	 *
	 * @param o is the object being processed
	 * @param fields is the fields from object o that should be processed
	 * @returns map of <fieldName,fieldData>
	 */
	private HashMap<String,Object> getObjectMap(Object o, ArrayList<Field> fields) {
		HashMap<String,Object> map = new HashMap<String,Object>();

		if(o == null) {
			return map;
		}

		for(Field f: fields) {

			if(f.getAnnotation(Ignore.class) != null) {
				continue;
			}
			else {
				String fieldName = f.getName().toLowerCase();

				/* if either the method does not exist or there is an error
				 * invoking the method then continue through the rest of the object */
				Method fieldMethod = null;
				try {
					fieldMethod = o.getClass().getMethod(GET + fixFieldName(f.getName()));
				} catch (Exception e) {
					logger.warn(e);
					continue;
				}

				Object fieldObject = null;
				try {
					fieldObject = fieldMethod.invoke(o);
				} catch (Exception e) {
					logger.warn(e);
					continue;
				}

				/* remove certain special characters from string values */
				if(f.getType().equals(String.class)) {
					fieldObject = cleanse((String)fieldObject);
				}

				map.put(fieldName, (fieldObject != null) ? fieldObject.toString(): "");
			}
		}
		return map;
	}

	/**
	 * looks at an object and dumps fields in to buckets based off of @PersistentObject, @ForeignKey
	 * and @PrimaryKey annotations.  It also saves the value of the current PrimaryKey
	 *
	 * @param o generic object to be mapped
	 * @return MappedFields objeect with fields in their respective buckets
	 * @throws Exception
	 */
	private MappedFields buildObjectFieldLists(Class<?> clazz, Object o) throws Exception {
		MappedFields m = new MappedFields();

		for(Field f:clazz.getDeclaredFields()) {

			if(f.getAnnotation(PrimaryKey.class) != null) {
				if(o != null) {
					if(m.getPrimary() == null) {
						try {
							m.setPrimaryField(f);

							Method get = o.getClass().getDeclaredMethod(GET + fixFieldName(f.getName()));
							m.setPrimary(get.invoke(o));
						}
						catch(Exception e){
							logger.warn(e);
							throw new Exception("error setting primary key for " + o.getClass());
						}
					}
					//another primary should not exist
					else {
						throw new Exception("Primary key already exists.  Multiple primaries currently violates spec, check object " + o.getClass());
					}
				}
			}
			else if(f.getAnnotation(ForeignKey.class) != null) {
				m.addForeign(f);
			}
			else if(f.getAnnotation(PersistentObject.class) != null) {
				m.addPersistent(f);
			}
			else {
				m.addField(f);
			}
		}

		return m;
	}

	/**
	 * When a key is autogenerated the returning ResultSet, if executed in the following manner:
	 *      s.executeUpdate(query, Statement.RETURN_GENERATED_KEYS)
	 * will return the new keys.  This function serves as an easy way to access that information.
	 *
	 * @param rs ResultSet returned from s.executeQuery(String,int) -> s.getGeneratedKeys()
	 * @return value of key
	 */
	private Integer getKeyFromResultSet(ResultSet rs) {
		try {
			if(rs != null) {
				return rs.getInt(1);
			}
		}
		catch(Exception e) {
			logger.warn(e);
		}
		return null;
	}

	private String fixFieldName(String s) {
		char[] chars = s.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

	private String cleanse(String s) {
		return s != null ? s.replaceAll("\"","&quot;").replaceAll("'", "&sing;").replaceAll("\\\\","&bcksl;"):s;
	}

	private String uncleanse(String s) {
		return s!= null ? s.replaceAll("&quot;", "\"").replaceAll("&sing;", "'").replaceAll("&bcksl;", "\\"):s;
	}
}
