package gov.nysenate.sage.util;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

    public static String jsonGetString(JSONObject json, String key) {
        try {
            return json.has(key) && !json.isNull(key) ? json.getString(key) : "";
        } catch (JSONException e) {
            return null;
        }
    }

    public static Double jsonGetDouble(JSONObject json, String key) {
        try {
            return json.has(key) && !json.isNull(key) && !json.getString(key).equals("") ? json.getDouble(key) : 0;
        } catch (JSONException e) {
            return null;
        }
    }

    public static Integer jsonGetInteger(JSONObject json, String key) {
        try {
            return json.has(key) && !json.isNull(key) && !json.getString(key).equals("") ? json.getInt(key) : 0;
        } catch (JSONException e) {
            return null;
        }
    }

}
