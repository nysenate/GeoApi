package gov.nysenate.sage.util;

import gov.nysenate.sage.deprecated.districts.Assembly;
import gov.nysenate.sage.deprecated.districts.Member;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


public class AssemblyScraper
{
  private final Logger logger = Logger.getLogger(AssemblyScraper.class);

  static final String ASSEMBLY_URL = "http://assembly.state.ny.us";
  static final String ASSEMBLY_DIRECTORY_URL = ASSEMBLY_URL+"/mem/?sh=email";


  public void index(Connect dbconn) throws IOException
  {
    List<Assembly> persons = getAssemblyPersons();

    if (persons != null && !persons.isEmpty()) {
      try {
        logger.info("Deleting Assembly Member data from the database");
        dbconn.deleteObjectById(Member.class, "type", Member.MemberType.Assembly.value() + "");
        dbconn.deleteObjects(Assembly.class, false);
      }
      catch (Exception e) {
        logger.warn(e);
      }

      logger.info("Persisting new Assembly data");
      for (Assembly a : persons) {
        dbconn.persist(a);
      }
    }
  } // index()


  public void index() throws IOException
  {
    Connect c = new Connect();
    index(c);
    c.close();
  } // index()


  public List<Assembly> getAssemblyPersons()
  {
    List<Assembly> ret = new ArrayList<Assembly>();

    try {
      Pattern p = Pattern.compile("<div class=\"email1\"><a href=\"(.+?)\">(.+?)</a></div>");
      Pattern p2 = Pattern.compile("<div class=\"email2\">(\\d+).*?</div>");
      Matcher m = null;

      logger.info("Connecting to " + ASSEMBLY_DIRECTORY_URL);

      BufferedReader br = new BufferedReader(
          new InputStreamReader(new URL(ASSEMBLY_DIRECTORY_URL).openStream()));

      String in = null;
      while ((in = br.readLine()) != null) {
        m = p.matcher(in);

        if (m.find()) {
          logger.info("Fetching assembly member " + m.group(2));
          Matcher m2 = p2.matcher(br.readLine());
          if (m2.find()) {
            Assembly a = new Assembly("Assembly District " + m2.group(1),
                new Member(m.group(2), ASSEMBLY_URL + m.group(1), Member.MemberType.Assembly));
            ret.add(a);
          }
        }
      }

      br.close();
    }
    catch (IOException ioe) {
      logger.warn(ioe);
    }

    return ret;
  } // getAssemblyPersons()
}
