package org.composer.engine.utils;

import org.composer.interfaces.Import;
import org.composer.engine.transforms.TableToRDF;
import com.hp.hpl.jena.vocabulary.RDF;

import java.io.InputStream;
import java.io.FileInputStream;

import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;

import javax.sql.DataSource;

import sun.tools.javap.Tables;


/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 29, 2008
 * Time: 12:38:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class ImportImpl implements Import {
    private DataSource composerDataSource;
    private DataSource importDataSource;

    public ImportImpl(DataSource composerDataSource, DataSource importDataSource) {
        this.composerDataSource = composerDataSource;
        this.importDataSource = importDataSource;
    }

    public void database(String databaseName, String databaseType) {
        TableToRDF transform = new TableToRDF(composerDataSource);
        Platform platform = PlatformFactory.createNewPlatformInstance(importDataSource);

        if ("mssql".equalsIgnoreCase(databaseType)) {
            platform.setDelimitedIdentifierModeOn(true);
        }

        Database database = platform.readModelFromDatabase(databaseName);

        for (Table table: database.getTables()) {
            transform.properties(table);
        }
    }
}
