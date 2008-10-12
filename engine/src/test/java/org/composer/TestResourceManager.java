package org.composer;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.composer.core.ResourceManager;
import org.composer.annotations.Domain;
import org.composer.annotations.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.output.ByteArrayOutputStream;
import com.db4o.ObjectContainer;
import com.db4o.Db4o;


/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Sep 26, 2008
 * Time: 8:48:42 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
@Domain("cafepress")
@Context("web3.0")
public class TestResourceManager {
    private Log log = LogFactory.getLog(TestResourceManager.class);

    private ResourceManager resourceManager = ResourceManager.getInstance();
    private ObjectContainer database;

    public void setManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void testQuery() {
        resourceManager.query(new ByteArrayOutputStream());
    }

    public void testPrintAll() {
            resourceManager.printAll();
    }

    public void testCreate(String resourceName) {
        resourceManager.create(resourceName);
    }

    @org.junit.Test
    public void test() {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("applicationContext.xml");
    	final TestResourceManager main = (TestResourceManager)appContext.getBean("main");

        //testCreate("registry-web3.0");

        testPrintAll();

        testQuery();

    }
}
