package gov.nysenate.sage.util;

import gov.nysenate.sage.model.ApiUser;
import gov.nysenate.sage.util.Connect;

import org.jasypt.util.password.BasicPasswordEncryptor;


public class ApiAuth
{
  public String addUser(String apiKey, String name, String description,
                        Connect dbconn)
  {
    BasicPasswordEncryptor pe = new BasicPasswordEncryptor();
    String ep = pe.encryptPassword(apiKey).replaceAll("\\W", "");
    dbconn.persist(new ApiUser(ep, name, description));
    return ep;
  } // addUser()


  public String addUser(String apiKey, String name, String description)
  {
    Connect dbconn = new Connect();
    String ep = addUser(apiKey, name, description, dbconn);
    dbconn.close();
    return ep;
  } // addUser()


  public ApiUser getUser(String apiKey, Connect dbconn)
  {
    ApiUser user = null;
    try {
      user = (ApiUser)dbconn.getObject(ApiUser.class, "apikey", apiKey);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return user;
  } // getUser()


  public ApiUser getUser(String apiKey) throws Exception
  {
    Connect dbconn = new Connect();
    ApiUser user = getUser(apiKey, dbconn);
    dbconn.close();
    return user;
  } // getUser()
}
