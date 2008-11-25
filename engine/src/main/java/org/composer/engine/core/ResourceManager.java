package org.composer.engine.core;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.composer.engine.beans.RDFEntity;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.*;
import java.sql.Connection;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.shared.AlreadyExistsException;

import javax.sql.DataSource;


/**
 *
 * User:kboufelliga - Create Date: Sep 6, 2008 -
 */
public final class ResourceManager {
    private static Log log = LogFactory.getLog(ResourceManager.class);

    private static final ResourceManager INSTANCE = new ResourceManager();
    private static EntityStore entityStore;
    private static DataSource datasource;
    private SingleConnectionDataSource dataSource;
    private Model model;

    private static String databaseType = Properties.DEFAULT_DATABASE_TYPE.value();
    private static String hostName = Properties.DEFAULT_URL.value();
    private static String domainPath = Properties.DEFAULT_DOMAIN_PATH.value();
    private static String contextPath = Properties.DEFAULT_CONTEXT_PATH.value();
    private static String domainUrl = Properties.DEFAULT_URL.value()+domainPath;
    private static String contextUrl = Properties.DEFAULT_URL.value()+domainPath+contextPath;
    private static String identifierPrefix = Properties.PURL_IDENTIFIER_URI.value();
    private static String modelName = Properties.DEFAULT_MODEL_NAME.value();

    
    private ResourceManager() {
        ResourceManager.entityStore = EntityStore.INSTANCE;
        ResourceManager.entityStore.initialize();
    }

    public static ResourceManager getInstance() {
            return INSTANCE;
    }

    public static void setDataSource(DataSource datasource) {
        ResourceManager.datasource = datasource;
    }

    public void setDataSource(SingleConnectionDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SingleConnectionDataSource getDataSource() {
        return dataSource;
    }

    public static void setDatabaseType(String dbType) {
        ResourceManager.databaseType = dbType;
    }

    public static void setHost(String link) {
        ResourceManager.hostName = link;
    }

    public static void setDomainPath(String path) {
        ResourceManager.domainPath = path;
        ResourceManager.domainUrl = hostName +domainPath;
    }

    public static void setContextPath(String path) {
        ResourceManager.contextPath = path;
        ResourceManager.contextUrl = hostName+domainPath+contextPath;
    }

    public static void setDomainPrefix(String prefixName, String domainPath) {
        ResourceManager.domainUrl = hostName +domainPath;

        if (!entityStore.lookupKey(prefixName)) {
            entityStore.get(prefixName,domainUrl);
        } else {
            RDFEntity entity = (RDFEntity)entityStore.get(prefixName,domainUrl);
            if (!entity.getUri().equals(domainUrl)) {
                entityStore.update(prefixName,domainUrl);
            }
        }
    }

    public static void setContextPrefix(String prefixName, String contextPath) {
        ResourceManager.contextUrl = hostName+domainPath+contextPath;

        if (!entityStore.lookupKey(prefixName)) {
            entityStore.get(prefixName,contextUrl);
        }  else {
            RDFEntity entity = (RDFEntity)entityStore.get(prefixName,contextUrl);
            if (!entity.getUri().equals(contextUrl)) {
                entityStore.update(prefixName,contextUrl);
            }
        }
    }

    public static void setModelName(String modelName) {
        ResourceManager.modelName = modelName;
    }

    public boolean update(String name) {
        return false;
    }

    public boolean delete(String name) {
        return false;
    }

    public void printAll() {
        model = getModel(modelName);

        StmtIterator iterator = model.listStatements();
        while (iterator.hasNext()) {
            System.out.println(iterator.nextStatement().toString());
        }
    }

    // bind the context to the domain in a meaningfull way
    // right now there is no intelligent way to infere that a context belong to a similar domain and vice versa
    public void bind(String source, String bindingType, String target) {
       OntModel ontology = ModelFactory.createOntologyModel();
    }

    public String read(String namespace, String resourceName, Collection<String> properties) {
        StringBuffer selectString = new StringBuffer();
        StringBuffer statementString = new StringBuffer();
        Iterator<String> list = properties.iterator();

        while (list.hasNext()) {
            String name = list.next();
            String propertyName = resourceName+"."+name;
            selectString.append(" ?"+name);

            statementString.append(" OPTIONAL {");
            statementString.append(" site: relationship:"+propertyName+" ?"+name+" .");
            statementString.append(" FILTER(?"+name+"  != \""+propertyName+"\") ");
            statementString.append(" } . ");

        }

        String query =
                "PREFIX relationship: <"+identifierPrefix+"/> " +
                "PREFIX site: <"+contextUrl+"/"+namespace+"/"+resourceName+"> " +
                "SELECT"+selectString.toString()+" WHERE " +
                "{" + statementString.toString() + " }";

        log.info(".................................query: "+query);

        OutputStream output = new ByteArrayOutputStream();

        QueryExecution qe = QueryExecutionFactory.create(query, getModel(modelName));
        com.hp.hpl.jena.query.ResultSet results = qe.execSelect();

        ResultSetFormatter.outputAsJSON(output,results);

        qe.close();

        return output.toString();
    }

    public boolean ask(String namespace, String resourceName, String propertyName, String propertyValue) {
        boolean response;
        String property = resourceName+"."+propertyName;

        String askString =
        "PREFIX relationship: <"+identifierPrefix+"/"+property+"> " +
        "PREFIX site: <"+ contextUrl+"/"+namespace+"/"+resourceName+"> " +
        "ASK " +
        "{" +
        "     site: relationship: ?"+propertyName+" . " +
        "     FILTER(?"+propertyName+" != \""+property+"\") " +
        "     FILTER(?"+propertyName+" = \""+propertyValue+"\") " +
        "}";

        log.info(askString);

        com.hp.hpl.jena.query.Query query = QueryFactory.create(askString);

        QueryExecution qe = QueryExecutionFactory.create(query, getModel(modelName));

        response = qe.execAsk();

        qe.close();

        return response;
    }

    public String read(String namespace, String resourceName) {
        OutputStream output = new ByteArrayOutputStream();
        
        String queryString =
        "PREFIX relationship: <"+ identifierPrefix+"/> " +
        "PREFIX site: <"+ contextUrl+"/"+namespace+"/"+resourceName+"> " +
        "SELECT ?value " +
        "WHERE {" +
	    "           site: relationship: ?value . " +
        "      }";

        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, getModel(modelName));
        com.hp.hpl.jena.query.ResultSet results = qe.execSelect();
        ResultSetFormatter.outputAsJSON(output,results);

        qe.close();

        return output.toString();
    }

    public JSONArray write(String namespace, String resourceName) {
        return write(modelName,namespace, resourceName,new ArrayList());
    }

    public JSONArray write(String namespace, String resourceName, String propertyName, String propertyValue)   {
        Map<String,String> props = new HashMap<String,String>();
        props.put(propertyName,propertyValue);

        return write(modelName,namespace,resourceName,props);
    }

    public JSONArray write(String namespace, String resourceName, Map<String,String> properties)   {
        return write(modelName,namespace,resourceName, properties);
    }


    private JSONArray write(String modelName, String namespace, String resourceName, Map<String,String> properties) {
        JSONArray response = new JSONArray();
        JSONObject responseObject;

        Resource resource;
        Property property;
        Statement statement;

        try {
            model = getModel(modelName);
            model.begin();

            resource = model.getResource(contextUrl+"/"+namespace+"/"+resourceName);

            Iterator<String> it = properties.keySet().iterator () ;
            while (it.hasNext())   {
                String name =  it.next();
                String value = properties.get(name);
                String propertyName = resourceName+"."+name;

                property = model.createProperty(identifierPrefix+"/",propertyName);
                resource.addProperty(property,propertyName);

                statement = model.createStatement(resource,property,value);

                model.add(statement);
            }

            model.commit();

            responseObject = new JSONObject();
            responseObject.put("info",resourceName+" was written successfully in namespace "+namespace);

            response.put(responseObject);
        } catch (Exception e) {
            log.info("The following error happened while writing "+resourceName+" in namespace "+namespace+" >> "+e.toString());

            responseObject = new JSONObject();
            try {
                responseObject.put("error","the following error happened while writing "+resourceName+" in namespace "+namespace+" >> "+e.toString());

                response.put(responseObject);
            } catch (Exception je){
                 log.info("RESOURCE MANAGER EXCEPTION WRITING REQUEST: "+response.toString());
            }
        }

        log.info("RESOURCE MANAGER RESPONSE TO WRITE REQUEST: "+response.toString());
        return response;
    }


    private JSONArray write(String modelName, String namespace, String resourceName, Collection<String> properties) {
        JSONArray response = new JSONArray();
        JSONObject responseObject;

        Resource contextResource;
        Resource resource;
        Property property;
        Statement statement;

        try {
            model = getModel(modelName);
            model.begin();
            contextResource = model.getResource(contextUrl);
            property = model.createProperty(identifierPrefix+domainPath+contextPath,domainPath+contextPath);

            resource = model.createResource(contextUrl+"/"+namespace+"/"+resourceName);
            resource.addProperty(property,contextResource);
            statement = model.createStatement(contextResource,property,resource);
            model.add(statement);

            property = model.createProperty(identifierPrefix+"/"+namespace+"."+resourceName, namespace+"."+resourceName);
            resource.addProperty(property,resourceName);


            if (!properties.isEmpty()) {
                Iterator<String> list = properties.iterator();

                while (list.hasNext()) {
                    String name = list.next();
                    String propertyName = resourceName+"."+name;
                    property = model.createProperty(identifierPrefix+"/",propertyName);
                    resource.addProperty(property,name);
                    statement = model.createStatement(resource,property,name);
                }
            }

            model.commit();

            responseObject = new JSONObject();
            responseObject.put("info",resourceName+" was created successfully in namespace "+namespace);

            response.put(responseObject);
         } catch (Exception e) {
            responseObject = new JSONObject();
            try {
                responseObject.put("error","the following error happened while writing "+resourceName+" in namespace "+namespace+" >> "+e.toString());

                response.put(responseObject);
            } catch (Exception je){};
        }

        return response;
    }

    

    private Connection getConnection() {
        try {
           return datasource.getConnection();
        } catch (Exception e) {
            log.error("failed to get datasource connection "+ e);
        }

        return null ;
    }

    private Model getModel(String name) {
        ResourceManager.setModelName(name);

        if (model == null || !(name.equals(modelName))) {
            IDBConnection dbconnection = new DBConnection(getConnection(),databaseType);
            ModelMaker maker = ModelFactory.createModelRDBMaker(dbconnection);

            try {
                model = maker.createModel(name,true);
            } catch (AlreadyExistsException exits) {
                model = maker.openModel(name);
            }
        }

        return model;
    }
}