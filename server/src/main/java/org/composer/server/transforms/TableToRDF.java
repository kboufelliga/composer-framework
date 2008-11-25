package org.composer.server.transforms;

import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.Column;
import org.composer.engine.core.ResourceManager;

import javax.sql.DataSource;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 1, 2008
 * Time: 1:11:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class TableToRDF {
    private ResourceManager resources = ResourceManager.getInstance() ;

    public TableToRDF(DataSource datasource) {
        ResourceManager.setDataSource(datasource);
    }

    public void properties(Table table) {
        String resourceName = table.getName();

        for (Column column: table.getColumns()) {
            resources.write("cafepress",resourceName, column.getName(), "property");
        }

    }
}
