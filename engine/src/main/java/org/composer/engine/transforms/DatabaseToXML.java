package org.composer.engine.transforms;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 4, 2008
 * Time: 12:53:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseToXML {
    private static Log log = LogFactory.getLog(DatabaseToXML.class);

    private DataSource dataSource;
    private String dbType;
    
    public DatabaseToXML(DataSource dataSource, String databaseType) {
        this.dataSource = dataSource;
        this.dbType = databaseType;
    }

    public void write(String databaseName, String fileName) throws Exception {
        Platform platform = PlatformFactory.createNewPlatformInstance(dataSource);

        if ("mssql".equalsIgnoreCase(dbType)) {
            platform.setDelimitedIdentifierModeOn(true);
        }

        Database database = platform.readModelFromDatabase(databaseName);

        new DatabaseIO().write(database, fileName);

    }
}
