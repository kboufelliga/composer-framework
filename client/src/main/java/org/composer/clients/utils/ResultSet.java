package org.composer.clients.utils;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 25, 2008
 * Time: 12:46:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResultSet {
    public static JSONObject getBindings(JSONArray response) throws Exception {
        JSONObject returnSet = new JSONObject();
        if (response.length() > 0) {
            JSONObject resultSet = response.getJSONObject(0);
            if (resultSet.has("results")) {
                JSONArray array = ((JSONObject)resultSet.get("results")).getJSONArray("bindings");
                if (array.length() > 0)
                   returnSet = array.getJSONObject(0);

            }
        }

        return returnSet;
    }
}
