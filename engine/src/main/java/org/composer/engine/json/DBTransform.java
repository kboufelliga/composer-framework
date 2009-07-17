package org.composer.engine.json;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.platform.mysql.MySql50Platform;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 *
 * @author kboufelliga
 */
public class DBTransform {
    private static Log log = LogFactory.getLog(DBTransform.class);
    private BasicDataSource dataSource;
    private String databaseType;
    private String databaseName;
    private Platform platform;
    private Database database;


    public DBTransform() {
        try {
            Configuration config = new PropertiesConfiguration("datasource.properties");

            dataSource = new BasicDataSource();

            databaseType = config.getString("database.type");
            databaseName = config.getString("database.name");

            dataSource.setDriverClassName(config.getString("class.name"));
            dataSource.setUsername(config.getString("user.name"));
            dataSource.setPassword(config.getString("user.password"));
            dataSource.setUrl(config.getString("url"));

            if ("mysql".equalsIgnoreCase(databaseType)) {
                platform = new MySql50Platform();
                platform.setDataSource(dataSource);
            } else {
                platform = PlatformFactory.createNewPlatformInstance(dataSource);
            }

            if ("mssql".equalsIgnoreCase(databaseType)) {
                platform.setDelimitedIdentifierModeOn(true);
            }

            database = platform.readModelFromDatabase(databaseName);

            if (platform.getDataSource().getConnection() == null )
                    System.out.println("we do not have a connection");

        } catch(Exception e) {
          log.error("could not initialize ..."+e);
        }
    }

    public JSONArray getAllObj() {
        JSONArray list = new JSONArray();

        for (Table table: database.getTables()) {
            list.add(getObj(table));
        }

        return list;
    }

    public JSONObject getObj(String tableName) {
        Table table = getTable(tableName);

        return getObj(table);
    }

    public JSONObject getObj(Table table) {
        JSONObject obj = new JSONObject();

        obj.put("name", table.getName());
        obj.put("field_names", getFields(table));
        obj.put("field_types", getTypes(table));

        return obj;
    }


    public JSONObject getFields(String tableName) {
        Table table = getTable(tableName);


        return getFields(table);
    }

    public JSONObject getFields(Table table) {
        JSONObject objFields = new JSONObject();

        for (Column column: table.getColumns()) {
            if (column.isRequired())
                objFields.put(column.getName(),"required");
            else
                objFields.put(column.getName(),"optional");

        }

        return objFields;
    }

    public JSONObject getTypes(String tableName) {
        Table table = getTable(tableName);

        return getTypes(table);
    }

    public JSONObject getTypes(Table table) {
        JSONObject objTypes = new JSONObject();

        for (Column column: table.getColumns()) {
            objTypes.put(column.getName(), column.getType());
        }

        return objTypes;
    }

    private Table getTable(String tableName) {
        Table table = null;

        for (Table t: database.getTables()) {
            if (t.getName().trim().equals(tableName)) {
                table = t;

                break;
            }
        }

        return table;
    }
}
