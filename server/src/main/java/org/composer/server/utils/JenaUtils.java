package org.composer.server.utils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 5, 2008
 * Time: 12:17:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class JenaUtils {
    private static Log log = LogFactory.getLog(JenaUtils.class);

    public static JSONArray resultSet(JSONObject returnSet) {
       JSONArray array = null;

       array = ((JSONObject)returnSet.get("results")).getJSONArray("bindings");

       return array;
    }

    public static JSONArray resultSet(String output) {
       JSONArray array = null;

       JSONObject returnSet = JSONObject.fromString(output);
       array = ((JSONObject)returnSet.get("results")).getJSONArray("bindings");


       return array;
    }
}
