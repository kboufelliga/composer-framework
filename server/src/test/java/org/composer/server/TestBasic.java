package org.composer.server;

import org.composer.engine.annotations.DomainPath;
import org.composer.engine.annotations.ContextPath;
import org.composer.engine.core.ResourceManager;

import org.composer.server.core.Application;
import org.composer.server.core.Membership;
import org.composer.server.utils.JenaUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import org.junit.Test;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 15, 2008
 * Time: 4:00:09 PM
 * To change this template use File | Settings | File Templates.
 */
@DomainPath("/cafepress")
@ContextPath("/web3.0")
public class TestBasic extends TestCase {
    private static Log log = LogFactory.getLog(TestBasic.class);
    private ResourceManager resourceManager = ResourceManager.getInstance();
    private Application app = new Application(resourceManager);
    private Membership membership = new Membership(resourceManager);


    public TestBasic() {
        try {
            BasicDataSource ds = new BasicDataSource();
             Configuration config = new PropertiesConfiguration("datasource.properties");

            ds.setDriverClassName(config.getString("className"));
            ds.setUsername(config.getString("username"));
            ds.setPassword(config.getString("password"));
            ds.setUrl(config.getString("url"));

            ResourceManager.setDataSource(ds);

            if (config.getString("databaseType") != null) {
                ResourceManager.setDatabaseType(config.getString("databaseType"));
            }
        } catch (Exception e) {
            log.error("datasource set up failed "+e);
        }
    }

    @DomainPath("/cp/tests")
    public void ask() {
            resourceManager.ask("memberships","register","userId","userId");
        }


    @DomainPath("/cp/apps")
    public void askUser() {
        if (!resourceManager.ask("memberships","dummy500","userId","user100")) {
            try {
                String key = app.registerUser("dummy500","user100","kboufelliga@composerlab.org");
                assertEquals(true, resourceManager.ask("memberships","dummy500","userId","user100"));
            } catch (Exception e) {

            }
        }
    }

    @DomainPath("/cp/apps")
        public String addUser(String appName, String userId, String userEmail) {

            String userKey = "";

        // new user
        if (!resourceManager.ask(appName,userId,"userId",userId)) {

            try {
                userKey = app.registerUser(appName,userId,userEmail);
                assertEquals(true, resourceManager.ask(appName,userId,"userId",userId));


            } catch (Exception e) {
                log.error("json error registering user: "+ e);
            }
        }

        JSONArray result = new JSONArray();
        JSONObject list = new JSONObject();

        try {
            Collection<String> properties = new ArrayList(1);
            properties.add("key");

            result = JenaUtils.resultSet(resourceManager.read(appName,userId,properties));
            list = result.getJSONObject(0);
            JSONObject key = list.getJSONObject("key");

            log.info("...............................test fetch key result: "+result.toString());

            userKey = key.get("value").toString();

        } catch (Exception e){
            log.error("json error etting property user: "+ e);
        }

        return userKey;

    }

    public void activateUser(String appName, String userId, String userKey) {
        try {
            app.activateUser(appName,userId,userKey);

            //assertEquals("active",resourceManager.getPropertyValue(appName, userId+".status"));
        } catch (Exception e) {
            log.error("...............................test activate user exception: "+ e.toString());
        }

    }

    public void printAll() {
            resourceManager.printAll();
    }

    @DomainPath("/miso/members")
    public void membership(){
        if (!membership.exists("misotime")) {
           membership.register("misotime","kboufelliga@cafepress.com");
           assertEquals(true,membership.exists("misotime"));
        }
    }

    @DomainPath("/cp/tests")
    @ContextPath("/web3.0")
    public void create(String resourceName) {
        resourceManager.write("web3.0",resourceName);
    }

    @Test
    public void test() {
        String appName = "appTZ100";
        String userId = "kboufelliga";
        String userEmail = "kacem_boufelliga@yahoo.com";

        printAll();
        
        // register user
        String userKey = addUser(appName,userId,userEmail);
        log.info(".....................................................user key: "+userKey);

        // activate user
        activateUser(appName,userId,userKey);

        resourceManager.write("finance.cafecash","kboufelliga","member","123-456-890");
        resourceManager.write("finance.cafecash","kboufelliga","store","123");

        resourceManager.write("store.123","ORDER#123456789","commission","$20.30");

        // create list of
        Collection<String> proplist = new ArrayList<String>();
        proplist.add("member");
        proplist.add("store");

        String result = resourceManager.read("finance.cafecash","kboufelliga",proplist);
        log.info("collections: "+result);



        proplist = new ArrayList<String>();
        proplist.add("email");
        proplist.add("key");

        resourceManager.read("memberships","misotime",proplist);

        membership();


                
    }
}
