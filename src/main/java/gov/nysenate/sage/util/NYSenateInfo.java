package gov.nysenate.sage.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nysenate.services.model.Senator;
import gov.nysenate.services.model.Social;
import org.apache.log4j.Logger;

public class NYSenateInfo
{
    private static Logger logger = Logger.getLogger(NYSenateInfo.class);
    private NYSenateInfo senateServices;
    private Map<Integer, Senator> senatorMap;

    public void NYSenateServices(Config config)
    {
        String key = config.getValue("nysenate.key");
        String domain = config.getValue("nysenate.domain");
        //this.senateServices = new NYSenateServices()
        //List<Senator> senators = senateServices.getSenators();
    }

    public List<Senator> getSenators()
    {
        List<Senate> ret = new ArrayList<Senate>();
        XmlRpc rpc = new XmlRpc();
        Object[] objects = (Object[])rpc.getView("senators", null, null, null, false, null);

        for (Object o : objects) {
            Senate senate = null;
            Senator senator = null;
            ArrayList<Office> offices = new ArrayList<Office>();
            Social social = null;

      /*Current map from Senator view */
            HashMap<String,Object> map = (HashMap<String,Object>)o;
            String sid = (String)map.get("nid");
            String did = (String)map.get("node_data_field_status_field_senators_district_nid");
            String name = (String)map.get("node_title");

      /*District Node*/
            HashMap<String,Object> disNode = (HashMap<String,Object>) rpc.getNode(new Integer(did));
            HashMap<String,Object> dNumberMap = rpc.getMap(disNode.get("field_district_number"));
            String dNumber ="State Senate District " +(String)dNumberMap.get("value");
            String dPath = "http://www.nysenate.gov/" + (String)disNode.get("path");

      /*Senator Node*/
            HashMap<String,Object> senNode = (HashMap<String,Object>) rpc.getNode(new Integer(sid));
            String sPath = "http://www.nysenate.gov/" + (String)senNode.get("path");
            HashMap<String,Object> sEmailMap = rpc.getMap(senNode.get("field_email"));
            String sEmail = (String)sEmailMap.get("email");
            HashMap<String,Object> sPictureMap = rpc.getMap(senNode.get("field_profile_picture"));
            String sPicture = "http://www.nysenate.gov/" + (String)sPictureMap.get("filepath");

            Pattern p = Pattern.compile("(http\\://www\\.nysenate\\.gov/files/)(profile-pictures/.+)");
            Matcher m = p.matcher(sPicture);

            if (m.find()) {
                sPicture = m.group(1) + "imagecache/senator_teaser/" + m.group(2);
            }

            HashMap<String,Object> sFacebookMap = rpc.getMap(senNode.get("field_facebook_link"));
            String sFacebook = (String)sFacebookMap.get("url");
            HashMap<String,Object> sTwitterMap = rpc.getMap(senNode.get("field_twitter_link"));
            String sTwitter = (String)sTwitterMap.get("url");
            HashMap<String,Object> sYoutubeMap = rpc.getMap(senNode.get("field_youtube_link"));
            String sYoutube = (String)sYoutubeMap.get("url");

            HashMap<String,Object> sFlickrMap = rpc.getMap(senNode.get("field_flickr_link"));
            String sFlickr = (String)sFlickrMap.get("url");

            //social = new Social(sFacebook, sTwitter, sYoutube, sFlickr,
              //      sPath + "/content/feed");

      /* Senator Offices */
            Object[] locations = (Object[])senNode.get("locations");
            for (Object location : locations) {
                HashMap<String,Object> sLocationMap = (HashMap<String,Object>)location;

                String sStreet = (String)sLocationMap.get("street");
                String sCity = (String)sLocationMap.get("city");
                String sProvince = (String)sLocationMap.get("province");
                String sPostalCode = (String)sLocationMap.get("postalcode");
                String sLongitude = (String)sLocationMap.get("longitude");
                String sLatitude = (String)sLocationMap.get("latitude");
                String sName = (String)sLocationMap.get("name");
                String sPhone = (String)sLocationMap.get("phone");
                String sFax = (String)sLocationMap.get("fax");

                if (sLatitude.startsWith("0") || sLongitude.startsWith("0")) {
                    logger.warn("Senator " + name + " contains a bad office record");
                    continue;
                }

                offices.add(new Office(sStreet, sCity, sProvince, sPostalCode, new
                        Double(sLongitude), new Double(sLatitude), sName, sPhone, sFax));
            }

            senator = new Senator(name, sEmail, sPath, sPicture, social, offices);
            senate = new Senate(dNumber, dPath, senator);
            logger.info("Adding senator " + name + " with " + offices.size() + " offices");
            ret.add(senate);
        }
        return ret;
    } // getSenators()
}
