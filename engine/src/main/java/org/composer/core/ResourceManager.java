package org.composer.core;

import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.composer.beans.RDFEntity;
import org.composer.utils.KeyGen;

import java.io.*;
import java.sql.Connection;

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

    private Model model;
    private static String domainUrl = Properties.DEFAULT_URL.value()+Properties.DEFAULT_DOMAIN_URI.value();
    private static String contextUrl = Properties.DEFAULT_URL.value()+Properties.DEFAULT_CONTEXT_URI.value();

    private static String domainPrefix = Properties.DEFAULT_DOMAIN_PREFIX.value();
    private static String contextPrefix = Properties.DEFAULT_CONTEXT_PREFIX.value();
    private static String domainUri = Properties.DEFAULT_DOMAIN_URI.value();
    private static String contextUri = Properties.DEFAULT_CONTEXT_URI.value();
    private static String defaultUrl = Properties.DEFAULT_URL.value();
    private static String relationshipUrl = Properties.RELATIONSHIP_URL.value();
    private static String relationshipUri = Properties.RELATIONSHIP_URI.value();
    private static String modelName = Properties.DEFAULT_MODEL_NAME.value();

    private static Publisher publisher;
    private SingleConnectionDataSource dataSource;

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

    public static void setDomain(String domainPrefix, String domainUri) {
        ResourceManager.domainPrefix = domainPrefix;
        ResourceManager.domainUri = domainUri;
        ResourceManager.domainUrl = defaultUrl+domainUri;

        if (!entityStore.lookupKey(domainPrefix)) {
            entityStore.get(domainPrefix,domainUri);
        } else {
            RDFEntity entity = (RDFEntity)entityStore.get(domainPrefix,domainUri);
            if (!entity.getUri().equals(domainUri)) {
                entityStore.update(domainPrefix,domainUri);
            }
        }
    }

    public static void setDomain(String domainPrefix) {
            setDomain(domainPrefix,domainUri);
    }

    public static void setContext(String contextPrefix, String contextUri) {
        ResourceManager.contextPrefix = contextPrefix;
        ResourceManager.contextUri = contextUri;
        ResourceManager.contextUrl = defaultUrl+contextUri;

        if (!entityStore.lookupKey(contextPrefix)) {
            entityStore.get(contextPrefix,contextUri);
        }  else {
            RDFEntity entity = (RDFEntity)entityStore.get(contextPrefix,contextUri);
            if (!entity.getUri().equals(contextUri)) {
                entityStore.update(contextPrefix,contextUri);
            }
        }
    }

    public static void setContext(String contextPrefix) {
        setContext(contextPrefix,contextUri);
    }

    public static void setPublisher(Publisher publisher) {
        ResourceManager.publisher = publisher;
    }

    public static void setModelName(String modelName) {
        ResourceManager.modelName = modelName;
    }

    public boolean read(Resource resource) {
        return false;
    }

    public boolean update(String name) {
        return false;
    }

    public boolean delete(String name) {
        return false;
    }

    public void printAll() {
        log.info("reading from domain: "+domainPrefix);
        model = getModel(modelName);

        StmtIterator iterator = model.listStatements();
        while (iterator.hasNext()) {
            System.out.println(iterator.nextStatement().toString());
        }
    }

    // bind the context to the domain in a meaningfull way
    // right now there is no intelligent way to infere that a context belong to a similar domain and vice versa
    public void bind(Resource from, Resource to) {
       OntModel ontology = ModelFactory.createOntologyModel();
    }

    public boolean ask(String resourceName, String propertyName, String propertyValue) {
        boolean response = false;
        String askString =
        "PREFIX relationship: <"+relationshipUrl+relationshipUri+"/property#"+propertyName+"> " +
        "PREFIX site: <"+defaultUrl+domainUri+contextUri+"/"+resourceName+"> " +
        "ASK " +
        "{" +
        "     site: relationship: ?"+propertyName+" " +
        "     FILTER(?"+propertyName+" = \""+propertyValue+"\") " +
        "}";

        com.hp.hpl.jena.query.Query query = QueryFactory.create(askString);

        QueryExecution qe = QueryExecutionFactory.create(query, getModel(modelName));

        response = qe.execAsk();

        qe.close();

        return response;
    }

    public void search(String resourceName, String relationship, OutputStream out) {
        String queryString =
        "PREFIX relationship: <"+ relationship +"> " +
        "PREFIX site: <"+ defaultUrl+domainUri+contextUri+"> " +
        "SELECT ?name " +
        "WHERE {" +
        "           ?name relationship: \"userId\" . " +
        "      }";

        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, getModel(modelName));
        com.hp.hpl.jena.query.ResultSet results = qe.execSelect();

        ResultSetFormatter.outputAsJSON(out,results);

        System.out.println("properties: "+out.toString());

        qe.close();

    }

    public void search(String resourceName, OutputStream out) {
        String queryString =
        "PREFIX relationship: <"+ relationshipUrl+relationshipUri+"/domain"+domainUri+"> " +
        "PREFIX site: <"+ defaultUrl+domainUri+"/> " +
        "SELECT ?"+resourceName+" ?context " +
        "WHERE {" +
	    "           ?"+resourceName+" relationship: ?context . " +
	    "      }";

        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, getModel(modelName));
        com.hp.hpl.jena.query.ResultSet results = qe.execSelect();

        ResultSetFormatter.outputAsJSON(out,results);

        System.out.println(out.toString());

        qe.close();

    }

    public void addPropertyValue(String modelName, String resourceName, String propertyName, String propertyValue)   {
        model = getModel(modelName);

        model.begin();

        Resource resource = model.getResource(defaultUrl+domainUri+contextUri+"/"+resourceName);
        Property resourceProperty = model.createProperty(relationshipUrl+relationshipUri+"/property#",propertyName);

        resource.addProperty(resourceProperty,propertyName);

        Statement statement = model.createStatement(resource,resourceProperty,propertyValue);
        log.info("........................ create property statement: "+resource.toString());

        model.add(statement);

        model.commit();
    }

    public void addPropertyName(String modelName, String resourceName, String propertyName)   {
        model = getModel(modelName);

        model.begin();

        Resource resource = model.getResource(defaultUrl+domainUri+contextUri+"/"+resourceName);
        Property resourceProperty = model.createProperty(relationshipUrl+relationshipUri+"/property#",propertyName);

        resource.addProperty(resourceProperty,propertyName);

        Statement statement = model.createStatement(resource,resourceProperty,propertyName);
        log.info("........................ create property statement: "+resource.toString());

        model.add(statement);

        model.commit();
    }

    public void addResourceName(String modelName, String resourceName)   {
        model = getModel(modelName);

        model.begin();
        log.info(".............. DEFAULT URL: "+defaultUrl);
        log.info("............... DOMAIN URL: "+domainUrl);
        log.info("............... DOMAIN URI: "+domainUri);
        log.info("...............CONTEXT URI: "+contextUri);
                                                
        Resource resourceDomain = model.createResource(defaultUrl+domainUri);
        Resource resourceContext = model.createResource(defaultUrl+domainUri+contextUri);

        Resource resource = model.createResource(domainUrl+"/"+resourceName);
        Property contextProperty = model.createProperty(relationshipUrl+relationshipUri+"/context",contextUri);
        Property domainProperty = model.createProperty(relationshipUrl+relationshipUri+"/domain",domainUri);

        resource.addProperty(domainProperty,resourceContext);

        Statement statement = model.createStatement(resourceContext,domainProperty,resourceDomain);

        model.add(statement);

        model.commit();
    }

    public void addResourceName(String resourceName) {
        addResourceName(modelName,resourceName);
    }

    public void addProperty(String resourceName, String propertyName) {
        addPropertyName(modelName,resourceName,"property#"+propertyName);

    }

    public void getProperty(String resourceName, String propertyName) {

    }

    public String register(String resourceName, String registrationType) {
        String key = "";
        if (registrationType.equals("appName")) {
            //generate a unique key
            key = KeyGen.generateKey();
            addPropertyValue(modelName,resourceName,registrationType,resourceName);
            addPropertyValue(modelName,resourceName,resourceName+".appKey",key);
        }

        return key;
    }

    public String registerAppUser(String appName, String userId) {
        String key = "";

            key = KeyGen.generateShortKey();
            addPropertyValue(modelName,appName,"userId",userId);
            addPropertyValue(modelName,appName,userId+".userKey",key);

        return key;
    }
    
    public OutputStream properties(String resourceName) {
        OutputStream output = new ByteArrayOutputStream();
        search(resourceName,relationshipUrl+relationshipUri+"/property#userId",output);
        return output;
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
            IDBConnection dbconnection = new DBConnection(getConnection(),"PostgreSQL");
            ModelMaker maker = ModelFactory.createModelRDBMaker(dbconnection);

            try {
                model = maker.createModel(name,true);
            } catch (AlreadyExistsException exits) {
                model = maker.openModel(name);
            }
        }

        return model;
    }

    public void setDataSource(SingleConnectionDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SingleConnectionDataSource getDataSource() {
        return dataSource;
    }
}