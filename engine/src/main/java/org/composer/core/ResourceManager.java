package org.composer.core;

import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.composer.beans.*;
import org.composer.core.Properties;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.Db4o;
import com.db4o.query.Query;
import com.db4o.query.Evaluation;
import com.db4o.query.Candidate;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.AlreadyExistsException;

/**
 *
 * @User:kboufelliga <Create Date: Sep 6, 2008>
 */
public class ResourceManager extends SimpleJdbcDaoSupport {
    private static Log log = LogFactory.getLog(ResourceManager.class);

    private static final ResourceManager INSTANCE = new ResourceManager();

    private static ObjectContainer database;
    private static Map<Object,Pair> prefixLookup = new HashMap<Object,Pair>();
    private Model model;


    private static String domainPrefix = Properties.DEFAULT_DOMAIN_PREFIX.value();
    private static String contextPrefix = Properties.DEFAULT_CONTEXT_PREFIX.value();
    private static String domainUri = Properties.DEFAULT_DOMAIN_URI.value();
    private static String contextUri = Properties.DEFAULT_CONTEXT_URI.value();
    private static String defaultSite = Properties.SITE.value();
    private static String publishSite = Properties.PUBLISH.value();
    private static String importSite = Properties.IMPORT.value();
    private static String exportSite = Properties.EXPORT.value();
    private static String subscribeSite = Properties.SUBSCRIBE.value();
    private static String transformSite = Properties.TRANSFORM.value();
    private static String relationshipUri = Properties.RELATIONSHIP_URI.value();
    private static String modelName = Properties.DEFAULT_MODEL_NAME.value();

    private ResourceManager() {
        this.database = Db4o.openFile(Properties.DEFAULT_INTERNAL_DBNAME.value());
        loadPrefixLookup();
    }

    public static ResourceManager getInstance() {
        return INSTANCE;
    }

    private void loadPrefixLookup() {
        Query query = database.query();
        query.constrain(PrefixMapping.class);

        ObjectSet<Pair> pairSet = query.execute();


        while (pairSet.hasNext()) {
            Pair pair = pairSet.next();
            prefixLookup.put(pair.getLeft(),pair);
        }
    }

    public static void setDomain(String domainPrefix) {
        setDomain(domainPrefix,domainUri);
    }

    public static void setDomain(String domainPrefix, String domainUrl) {
        ResourceManager.domainPrefix = domainPrefix;
        
        if (!prefixLookup.containsKey(domainPrefix)) {
            storePrefixMapping(domainPrefix,domainUrl);
        }
    }

    public static void setContext(String contextPrefix) {
        setContext(contextPrefix,contextUri);
    }

    public static void setContext(String contextPrefix, String contextUrl) {
        ResourceManager.contextPrefix = contextPrefix;
        
        if (!prefixLookup.containsKey(contextPrefix)) {
            storePrefixMapping(contextPrefix,contextUrl);
        }
    }

    public static void setPublishSite(String publishSite) {
        ResourceManager.publishSite = publishSite;
    }

    public static void setImportSite(String importSite) {
        ResourceManager.importSite = importSite;
    }

    public static void setExportSite(String exportSite) {
        ResourceManager.exportSite = exportSite;
    }

    public static void setSubscribeSite(String subscribeSite) {
        ResourceManager.subscribeSite = subscribeSite;
    }

    public static void setTransformSite(String transformSite) {
        ResourceManager.transformSite = transformSite;
    }

    public static void setModelName(String modelName) {
        ResourceManager.modelName = modelName;
    }

    
    private static void storePrefixMapping(String name, String value) {
        Pair pair = Pair.create(name,value);

        //store in local db
        dbStorePrefixMapping(pair);

        //store in memory
        prefixLookup.put(domainPrefix,pair);

    }

    private static void dbStorePrefixMapping(Pair pair) {
        try {
            database.store(pair);
            database.commit();
        } catch (Exception e) {
            log.error("could not store prefix mapping pair: "+e);
        } finally {
            database.close();
        }
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
        model = getModel("prototypeRdf");

        StmtIterator iterator = model.listStatements();
        while (iterator.hasNext()) {
            System.out.println(iterator.nextStatement().toString());
        }
    }

    private static String getDomainUri(String prefix) {
        log.info("getting entity for prefix "+prefix);

        if (prefixLookup.containsKey(prefix)) {
            Pair<String,String> pair = prefixLookup.get(prefix);

            return pair.getLeft();
        }

        return domainUri;
    }

    // bind the context to the domain in a meaningfull way
    // right now there is no intelligent way to infere that a context belong to a similar domain and vice versa
    public void bind(Resource from, Resource to) {
       OntModel ontology = ModelFactory.createOntologyModel();
    }

    public void query(OutputStream out) {
        String queryString =
        "PREFIX relationship: <"+ relationshipUri +"> " +
        "PREFIX site: <"+defaultSite+"> " +
        "SELECT ?context " +
        "WHERE {" +
	    "      ?context relationship:domain site:cafepress . " +
	    "      }";

        IDBConnection dbconnection = new DBConnection(super.getConnection(),"PostgreSQL");
        ModelMaker modelMaker = ModelFactory.createModelRDBMaker(dbconnection);
        Model model = modelMaker.openModel("prototypeRdf");
        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        com.hp.hpl.jena.query.ResultSet results = qe.execSelect();

        ResultSetFormatter.outputAsJSON(out,results);

        System.out.println("out :"+out.toString());

        qe.close();

    }

    private Model getModel() {
        if (model == null) {
            model = ModelFactory.createDefaultModel();
        }

        return model;
    }

    private Model getModel(String name) {
        ResourceManager.setModelName(name);

        if (model == null || !(name.equals(modelName))) {
            IDBConnection dbconnection = new DBConnection(super.getConnection(),"PostgreSQL");
            ModelMaker maker = ModelFactory.createModelRDBMaker(dbconnection);

            try {
                model = maker.createModel(name,true);
            } catch (AlreadyExistsException exits) {
                model = maker.openModel(name);
            }
        }

        return model;
    }

    public void create(String modelName, String resourceName)   {
        model = getModel(modelName);

        model.begin();
        Resource dbresourceDomain = model.createResource(domainUri);
        Resource dbresourceContext = model.createResource(contextUri);

        Resource dbresource = model.createResource(relationshipUri+resourceName);
        Property dbcontextProperty = model.createProperty(relationshipUri,"context");
        Property dbdomainProperty = model.createProperty(relationshipUri,"domain");

        dbresource.addProperty(dbcontextProperty,dbresourceContext);

        Statement dbstatement = model.createStatement(dbresourceContext,dbdomainProperty,dbresourceDomain);

        model.add(dbstatement);

        model.commit();
    }

    public void create(String resourceName) {
        create(modelName,resourceName);
    }
}