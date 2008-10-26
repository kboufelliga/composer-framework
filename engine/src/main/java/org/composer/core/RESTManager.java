package org.composer.core;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.OutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import java.net.URI;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.composer.annotations.Domain;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 7, 2008
 * Time: 10:21:21 AM
 * To change this template use File | Settings | File Templates.
 */

// NOTES: This class should be made more generic as it is turning out to be more of an application
// rather than an implementation of the framework.
// The goal to make data structures automatically accesible in a REST fashion through the RESTManager
// should be completly abstracted.

// Next to do is add authentication
    
@Path("/resources")
public class RESTManager {
    @Context UriInfo uriInfo;
    private Log log = LogFactory.getLog(RESTManager.class);
    private ResourceManager resourceManager = ResourceManager.getInstance();

    public RESTManager() {
         try {
            BasicDataSource ds = new BasicDataSource();
            Configuration config = new PropertiesConfiguration("datasource.properties");

            ds.setDriverClassName(config.getString("className"));
            ds.setUsername(config.getString("username"));
            ds.setPassword(config.getString("password"));
            ds.setUrl(config.getString("url"));

            ResourceManager.setDataSource(ds);

            if (config.getString("databaseType") != null) {
                ResourceManager.setDatabaseType(config.getString("databaseType"));
            }
        } catch (Exception e) {
            log.error("datasource set up failed "+e);
        }

    }

    @Domain(name="cafepress",uri="/cp/tests")
    @GET
    @Produces({"application/xml", "application/json"})
    @Path("/read/{resourceName}")
    public Response read(@PathParam("resourceName") String resourceName) {
        log.info("processing request to retrieve resource name: "+resourceName);
        System.out.println("........................................RESOURCE NAME: "+resourceName);
        Response response;
        OutputStream output = new ByteArrayOutputStream();
        URI uri =  uriInfo.getAbsolutePath();
        response = Response.created(uri).build();

        try {
            resourceManager.search(resourceName, output);

            return response.ok(output.toString(),"application/json").build();
        } catch (Exception e) {
            log.error("exception writing response - "+e);
        }

        return response.serverError().build();
    }

    @GET
    @Produces("text/plain")
    @Path("/create/{resourceName}")
    @Domain(name="cptests",uri="/cp/tests")
    public String create(@PathParam("resourceName") String resourceName) {
        log.info("processing request to add resource name: "+resourceName);

        URI uri =  uriInfo.getAbsolutePath();

        resourceManager.addResourceName(resourceName);

        return "Resource "+resourceName+" successfully created!";
     }

    @GET
    @Produces({"application/xml", "application/json"})
    @Path("/register/application/{appName}")
    @Domain(name="cpApps",uri="/cp/apps")
    public Response register(@PathParam("appName") String appName) {
        log.info("processing request to create application \""+appName+"\"");

        URI uri =  uriInfo.getAbsolutePath();
        Response response = Response.created(uri).build();
        JSONObject jsonResp = new JSONObject();

        if (resourceManager.ask(appName,"appName",appName)) {
            try {
                jsonResp.put("appName",appName);
                jsonResp.put("info","");
                jsonResp.put("error","Application \""+appName+"\" already registered!");
            } catch (Exception e){
                log.error("JSON Exception: "+e);
            }

            return response.ok(jsonResp.toString(),"application/json").build();
        }

            String key = resourceManager.register(appName,"appName");
        try {
            jsonResp.put("appName",appName);
            jsonResp.put("appKey",key);
            jsonResp.put("info","Application successfully registered !");
            jsonResp.put("error","");
        } catch (Exception e) {
            log.error("JSON Exception: "+e);
        }

        return response.ok(jsonResp.toString(),"application/json").build();
    }

    @GET
    @Produces("text/html")
    @Path("/register/user/{appName}/{userId}")
    @Domain(name="cpApps",uri="/cp/apps")
    public Response register(@PathParam("appName") String appName,@PathParam("userId") String userId) {
        log.info("processing request to create user \""+userId+"\" application \""+appName+"\"");

        URI uri =  uriInfo.getAbsolutePath();
        Response response = Response.created(uri).build();
        JSONObject jsonResp = new JSONObject();

        // check if application exists
        if (!resourceManager.ask(appName,"appName",appName)) {
            try {
                jsonResp.put("userId",userId);
                jsonResp.put("appName",appName);
                jsonResp.put("info","");
                jsonResp.put("error","Application \""+appName+"\" does not exist!");
            } catch (Exception e){
                log.error("JSON Exception: "+e);
            }

            return response.ok(jsonResp.toString(),"application/json").build();
        }


        if (resourceManager.ask(appName,"userId",userId)) {
            try {
                jsonResp.put("userId",userId);
                jsonResp.put("appName",appName);
                jsonResp.put("info","");
                jsonResp.put("error","User "+ userId+ " is already registerd with apllication \""+appName+"\"!");
            } catch (Exception e){
                log.error("JSON Exception: "+e);
            }

            return response.ok(jsonResp.toString(),"application/json").build();
        }

        String key = resourceManager.registerAppUser(appName,userId);
        try {
            jsonResp.put("appName",appName);
            jsonResp.put("userId",userId);
            jsonResp.put("userKey",key);
            jsonResp.put("info","User successfully registered with application \""+ appName+ "\"!");
            jsonResp.put("error","");
        } catch (Exception e) {
            log.error("JSON Exception: "+e);
        }

        return response.ok(jsonResp.toString(),"application/json").build();
    }

    public void remove() {}
    public void update() {}
    public void inport() {}
    public void export() {}

    public void publish() {}
    public void subscribe() {}
}
