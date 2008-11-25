package org.composer.server;

import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 31, 2008
 * Time: 5:09:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestImport extends TestCase {
    private static Log log = LogFactory.getLog(TestImport.class);
    private BasicDataSource composerDataSource;
    private BasicDataSource importDataSource;
    private String importDatabaseType;
        
    public TestImport() {
        
        try {
            composerDataSource = new BasicDataSource();
            Configuration config = new PropertiesConfiguration("datasource.properties");

            composerDataSource.setDriverClassName(config.getString("className"));
            composerDataSource.setUsername(config.getString("username"));
            composerDataSource.setPassword(config.getString("password"));
            composerDataSource.setUrl(config.getString("url"));

            // quick test to actually represent the jena tables in our model
            importDataSource = composerDataSource;

        } catch (Exception e) {
            e.printStackTrace();
            //log.error("................ datasource set up failed "+e);
        }
    }

    /*

    public void testDumpDB() {
        try {
            importDataSource = new BasicDataSource();
            Configuration config = new PropertiesConfiguration("larrydb.properties");
            importDatabaseType = config.getString("databaseType");
            importDataSource.setDriverClassName(config.getString("className"));
            importDataSource.setUsername(config.getString("username"));
            importDataSource.setPassword(config.getString("password"));
            importDataSource.setUrl(config.getString("url"));

            DatabaseToXML dump = new DatabaseToXML(importDataSource, importDatabaseType);
            dump.write("larry_db","larrydb.schema");
         } catch (Exception e) {
            e.printStackTrace();
            log.error("datasource set up failed "+e);
         }
    }


    public void testImportLarryDB() {
        try {
            importDataSource = new BasicDataSource();
            Configuration config = new PropertiesConfiguration("larrydb.properties");

            importDatabaseType = config.getString("databaseType");

            importDataSource.setDriverClassName(config.getString("className"));
            importDataSource.setUsername(config.getString("username"));
            importDataSource.setPassword(config.getString("password"));
            importDataSource.setUrl(config.getString("url"));

            ImportImpl imports = new ImportImpl(composerDataSource, importDataSource);

            imports.database("larry_db",importDatabaseType);
        } catch (Exception e) {
            log.error("datasource set up failed "+e);
        }
    }
*/
    

    /*
    public void testJenaDB() {
        try {
            composerDataSource = new BasicDataSource();
            Configuration config = new PropertiesConfiguration("datasource.properties");

            composerDataSource.setDriverClassName(config.getString("className"));
            composerDataSource.setUsername(config.getString("username"));
            composerDataSource.setPassword(config.getString("password"));
            composerDataSource.setUrl(config.getString("url"));

            // quick test to actually represent the jena tables in our model
            importDataSource = composerDataSource;

            ImportImpl imports = new ImportImpl(composerDataSource, importDataSource);

            imports.database("sandbox");
        } catch (Exception e) {
            log.error("datasource set up failed "+e);
        }
    }
      */
    @org.junit.Test
    public void test() {
    }
}
