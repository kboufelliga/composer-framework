package org.composer.services;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.composer.engine.annotations.DomainPath;
import org.composer.engine.annotations.ContextPath;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import org.composer.engine.core.ResourceManager;
import org.composer.server.core.Application;
import org.composer.server.core.Membership;

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
public class  RESTManager {
    @Context UriInfo uriInfo;
    @Context SecurityContext security;
    
    private Log log = LogFactory.getLog(RESTManager.class);
    private ResourceManager resourceManager = ResourceManager.getInstance();
    private Application application = new Application(resourceManager);
    private Membership membership = new Membership(resourceManager);

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

    @GET
    @DomainPath("/cafepress")
    @ContextPath("/composer")
    @Produces({"application/xml","application/json"})
    @Path("/ask/{namespace}/{resourceName}/{propertyName}")
    public Response ask(@PathParam("namespace") String namespace,@PathParam("resourceName") String resourceName, @PathParam("propertyName") String propertyName,@QueryParam("parms") String propertyValueObject) {
        log.info(".............SECURITY AUTH SCHEME "+security.getAuthenticationScheme()+"............");
        log.info(".............SECURITY IS SECURE "+security.isSecure()+"............");
        log.info(".............SECURITY USER PRINCIPAL "+security.getUserPrincipal().toString()+"............");

        JSONObject responseObject = new JSONObject();
        URI uri =  uriInfo.getAbsolutePath();
        Response.created(uri).build();

        try {
            JSONObject propertyValue = JSONObject.fromString(propertyValueObject);
            boolean result = resourceManager.ask(namespace,resourceName,propertyName,propertyValue.get("value").toString());
            log.info("........................request: "+propertyValue.toString());
            log.info("........................response: "+result);
                        responseObject.put("response",result);

            return Response.ok(responseObject.toString(),"application/json").build();
        } catch (Exception e) {
            log.error("exception asking response - "+e);
        }

        return Response.serverError().build();
    }

    @GET
    @DomainPath("/cafepress")
    @ContextPath("/composer")
    @Produces({"application/xml", "application/json"})
    @Path("/read/{namespace}/{resourceName}")
    public Response read(@PathParam("namespace") String namespace,@PathParam("resourceName") String resourceName, @QueryParam("parms") String parmsList) {
        log.info(".............SECURITY AUTH SCHEME "+security.getAuthenticationScheme()+"............");
        log.info(".............SECURITY IS SECURE "+security.isSecure()+"............");
        log.info(".............SECURITY USER PRINCIPAL "+security.getUserPrincipal().toString()+"............");

        URI uri =  uriInfo.getAbsolutePath();
        Response.created(uri).build();

        try {
            Collection<String> parms = new ArrayList();
            JSONArray parmsArray;

            if (parmsList.length() > 0) {
                parmsArray = JSONArray.fromString(parmsList);

                for (int i=0; i < parmsArray.length(); i++) {
                    parms.add(parmsArray.getString(i));
                }
            }

            String result = resourceManager.read(namespace,resourceName, parms);

            return Response.ok(result,"application/json").build();
        } catch (Exception e) {
            log.error("exception reading response - "+e);
        }

        return Response.serverError().build();
    }


    @PUT
    @DomainPath("/cafepress")
    @ContextPath("/composer")
    @Produces({"application/xml", "application/json"})
    @Path("/write/{namespace}/{resourceName}")
    public Response write(@PathParam("namespace") String namespace,@PathParam("resourceName") String resourceName, @QueryParam("parms") String parmsList) {
        log.info(".............SECURITY AUTH SCHEME "+security.getAuthenticationScheme()+"............");
        log.info(".............SECURITY IS SECURE "+security.isSecure()+"............");
        log.info(".............SECURITY USER PRINCIPAL "+security.getUserPrincipal().toString()+"............");

        URI uri =  uriInfo.getAbsolutePath();
        Response.created(uri).build();

        try {
            Map<String,String> parms = new HashMap();
            JSONArray parmsArray;

            log.info("..................................... parmslist: "+parmsList);
            if (parmsList.length() > 0) {
                    parmsArray = JSONArray.fromString(parmsList);

                    for (int i=0; i < parmsArray.length(); i++) {
                        JSONObject property = JSONObject.fromString(parmsArray.getString(i));
                        Iterator<String> keys = property.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            parms.put(key,property.getString(key));
                        }
                    }
            }

            JSONArray result = resourceManager.write(namespace,resourceName, parms);

            log.info("response from resource manager : "+ result.toString());

            return Response.ok(result.toString(),"application/json").build();
         } catch (Exception e) {
                log.error("exception writing response > "+e);
         }

         return Response.serverError().build();
    }


    @PUT
    @Produces({"application/xml", "application/json"})
    @Path("/register/application/{namespace}/{appName}")
    @DomainPath("/cp/apps")
    public Response register(@PathParam("namespace") String namespace, @PathParam("appName") String appName) {
        log.info(".............SECURITY AUTH SCHEME "+security.getAuthenticationScheme()+"............");
        log.info(".............SECURITY IS SECURE "+security.isSecure()+"............");
        log.info(".............SECURITY USER PRINCIPAL "+security.getUserPrincipal().toString()+"............");


        log.info("processing request to create application \""+appName+"\"");
        URI uri =  uriInfo.getAbsolutePath();
        String username = security.getUserPrincipal().getName();
        log.info("........................username: "+username);

        String userEmail = "kboufelliga@composerlab.org";
        try {
            Collection<String> properties = new ArrayList(1);
            properties.add("email");
            JSONObject jsonobj =  JSONObject.fromString(resourceManager.read(namespace,username,properties));
            userEmail = (String)jsonobj.get("value");

        } catch (Exception e) {
            log.error("Email JSON Exception: "+e);
        }

        Response response = Response.created(uri).build();
        JSONObject jsonResp = new JSONObject();

        if (resourceManager.ask(namespace, appName,"appName",appName)) {
            try {
                jsonResp.put("appName",appName);
                jsonResp.put("info","");
                jsonResp.put("error","Application \""+appName+"\" already registered!");
            } catch (Exception e){
                log.error("JSON Exception: "+e);
            }

            return response.ok(jsonResp.toString(),"application/json").build();
        }

        String key = application.register(appName,security.getUserPrincipal().toString());

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

    @PUT
    @Produces("text/html")
    @Path("/register/user/{namespace}/{appName}/{userId}/{userEmail}")
    @DomainPath("/cp/apps")
    public Response register(@PathParam("namespace") String namespace, @PathParam("appName") String appName,@PathParam("userId") String userId,@PathParam("userEmail") String userEmail) {
        log.info("processing request to create user \""+userId+"\" application \""+appName+"\"");

        URI uri =  uriInfo.getAbsolutePath();
        Response response = Response.created(uri).build();
        JSONObject jsonResp = new JSONObject();

        if (!resourceManager.ask(namespace, appName,"appName",appName)) {
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


        if (resourceManager.ask(namespace, appName,"userId",userId)) {
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

        try {
            String key = application.registerUser(appName,userId,userEmail);

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
