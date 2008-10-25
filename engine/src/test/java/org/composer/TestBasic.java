package org.composer;

import org.composer.annotations.Domain;
import org.composer.annotations.Context;
import org.composer.core.ResourceManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 15, 2008
 * Time: 4:00:09 PM
 * To change this template use File | Settings | File Templates.
 */
@Domain("class.cafepress,/classes/cafepress#testbasic")
@Context("web3.0")
public class TestBasic extends TestCase {
    private static Log log = LogFactory.getLog(TestBasic.class);
    private ResourceManager resourceManager = ResourceManager.getInstance();

    public TestBasic() {
        try {
            BasicDataSource ds = new BasicDataSource();
             Configuration config = new PropertiesConfiguration("datasource.properties");

            ds.setDriverClassName(config.getString("className"));
            ds.setUsername(config.getString("username"));
            ds.setPassword(config.getString("password"));
            ds.setUrl(config.getString("url"));

            ResourceManager.setDataSource(ds);
        } catch (Exception e) {
            log.error("datasource set up failed "+e);
        }
    }
    
    @Domain(name="cafepress",uri="/cp/tests")
    public void testProperties() {
            resourceManager.properties("register");
        }

    @Domain(name="cafepress",uri="/cp/tests")
    public void testAddProperty() {
            resourceManager.addProperty("register","userId");
        }

    @Domain(name="cafepress",uri="/cp/tests")
    public void testAsk() {
            resourceManager.ask("register","userId","userId");
        }

    @Domain(name="cpApps",uri="/cp/apps")
    public void testAsk1() {
        if (!resourceManager.ask("dummy500","appName","dummy500")) {
            String key = resourceManager.register("dummy500","appName");
            assertEquals(true, resourceManager.ask("dummy500","appName","dummy500"));
        }
    }

    @Domain(name="cpApps",uri="/cp/apps")
    public void testAskAppUser() {
        if (!resourceManager.ask("dummy500","userId","user100")) {
            String key = resourceManager.registerAppUser("dummy500","user100");
            assertEquals(true, resourceManager.ask("dummy500","userId","user100"));
        }
    }

    public void testQuery() {
        resourceManager.search("register",new ByteArrayOutputStream());
    }

    public void testPrintAll() {
            resourceManager.printAll();
    }

    @Domain(name="mistotime",uri="/miso/users#")
    public void register(){

    }

    @Domain(name="cafepress",uri="/cp/tests")
    @Context("web3.0")    
    public void testCreate(String resourceName) {
        resourceManager.addResourceName(resourceName);
    }


    @org.junit.Test
    public void test() {
        testCreate("register");

        testPrintAll();

        testQuery();

        testAddProperty();

        testProperties();

        testAsk();

        testAsk1();

        testAskAppUser();

    }
}
