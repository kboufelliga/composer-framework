package org.composer.client;

import org.junit.Test;
import org.composer.client.utils.ResultSet;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 16, 2008
 * Time: 11:10:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestRepository extends TestCase {
    private static Log log = LogFactory.getLog(TestRepository.class);
    Repository repository = new Repository.Builder("http://localhost:9111/services/resources", "composer", "composer1234").build();
    Application app = new Application(repository);

    public String addUser(String appName, String userId, String userEmail) {
        String userKey = "";
        try {
            if (!repository.ask(appName,userId,"userId",userId)) {
                userKey = app.registerUser(appName,userId,userEmail);
                assertEquals(true, repository.ask(appName,userId,"userId",userId));

            }

            Collection<String> properties = new ArrayList(1);
            properties.add("key");

            JSONObject response = ResultSet.fromJenaResult(repository.read(appName,userId,properties));

            if (response.has("key")) {
                userKey = response.getJSONObject("key").getString("value");
            }

            log.info("...............................test fetch key result: "+response.toString());


        } catch (Exception e){
            log.error("json error setting property user: "+ e);
        }

        return userKey;

    }

    public void activateUser(String appName, String userId, String userKey) {
        try {
            app.activateUser(appName,userId,userKey);

            String status="";

            Collection<String> properties = new ArrayList(1);
            properties.add("status");

            JSONObject response = ResultSet.fromJenaResult(repository.read(appName, userId, properties));

            if (response.has("status")) {
                JSONObject statusKey = response.getJSONObject("status");
                if (statusKey.get("value") != null)
                    status = statusKey.getString("value");
            }

            assertEquals("active",status);
        } catch (Exception e) {
            log.error("...............................test activate user exception: "+ e.toString());
        }

    }

    @Test
    public void test() {

        // create member properties
        Collection<String> propList = new ArrayList();
        propList.add("name");
        propList.add("email");
        propList.add("status");
        try {
            repository.write("memberships","memberId",propList);

            JSONObject response = ResultSet.fromJenaResult(repository.read("memberships","memberId",propList));

            JSONObject name = response.getJSONObject("name");
            assertEquals("property",name.get("value"));

            JSONObject email = response.getJSONObject("email");
            assertEquals("property",email.get("value"));

            JSONObject status = response.getJSONObject("status");
            assertEquals("property",status.get("value"));

         } catch (Exception e) {
            System.out.println("exception in test() "+e);
        }


        String appName = "appTZ500";
        String userId = "kboufelliga";
        String userEmail = "kacem_boufelliga@yahoo.com";

        try {
            if (repository.ask(appName,userId,"status","active")) {
                // user already registered
            } else {
                // register user
                String userKey = addUser(appName,userId,userEmail);

                // activate user
                activateUser(appName,userId,userKey);
            }     
        } catch (Exception e) {

        }
    }

}
