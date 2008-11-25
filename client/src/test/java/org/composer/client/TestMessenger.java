package org.composer.client;

import junit.framework.TestCase;
import org.junit.Test;
import org.composer.client.Messenger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 15, 2008
 * Time: 1:34:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestMessenger extends TestCase {
    private static Log log = LogFactory.getLog(TestMessenger.class);
    private Messenger messenger = new Messenger.Builder("http://192.168.161.242:9111/services/messenger","orders","commission", "cafepress", "cafe1111").build();

    private String send() {
        String routingKey = "";
        Collection<String> proplist = new ArrayList<String>();
        proplist.add("email");
        proplist.add("key");

        try {
            JSONArray response = messenger.send("memberships","misotime",proplist);

            JSONObject result = response.getJSONObject(0);
            routingKey = result.get("routingKey").toString();

            log.info(".................... response: "+response.toString());
        } catch (Exception e) {
            log.error("error reading json response");
        }


        return routingKey;

    }

    private void receive(String routingKey) {
        try {
            JSONArray response = messenger.receive(routingKey);
            log.info(".................... received: "+response.toString());
        } catch (Exception e) {
            log.error("error reading json response");
        }
    }

    @Test
    public void test() {
           String routingKey = send();
           receive(routingKey);
    }

}
