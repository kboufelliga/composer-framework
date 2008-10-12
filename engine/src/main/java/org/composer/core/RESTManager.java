package org.composer.core;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.OutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 7, 2008
 * Time: 10:21:21 AM
 * To change this template use File | Settings | File Templates.
 */
@Path("/resources")
public class RESTManager {
    @Context UriInfo uriInfo;
    private Log log = LogFactory.getLog(RESTManager.class);
    private ResourceManager resourceManager = ResourceManager.getInstance();

    public RESTManager() {
        InitialContext initialContext;
        DataSource dataSource;
        try {
            initialContext = new InitialContext();
            dataSource = (DataSource)initialContext.lookup("java:comp/env/jdbc/database");
            resourceManager.setDataSource(dataSource);
        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    @GET
    @Produces({"application/xml", "application/json"})
    @Path("/read/{resourceName}")
    public Response read(@PathParam("resourceName") String resourceName) {
        log.info("processing request to retrieve resource name: "+resourceName);

        Response response;
        OutputStream output = new ByteArrayOutputStream();
        URI uri =  uriInfo.getAbsolutePath();
        response = Response.created(uri).build();

        try {
            resourceManager.query(output);

            return response.ok(output.toString()).build();

        } catch (Exception e) {
            log.error("exception writing response - "+e);

        }

        return response.serverError().build();
    }

    // Need To Add Authentication

    @GET
    @Produces("text/plain")
    @Path("/create/{resourceName}")
    public String create(@PathParam("resourceName") String resourceName) {
        log.info("processing request to add resource name: "+resourceName);

        URI uri =  uriInfo.getAbsolutePath();

        resourceManager.create(resourceName);

        return "Resource "+resourceName+" successfully created!";
     }

    public void delete() {}
    public void update() {}
    public void inport() {}
    public void export() {}
    public void publish() {}
    public void subscribe() {}
}
