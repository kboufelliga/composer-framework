package org.composer.services.core;

import org.composer.engine.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;

import net.sf.json.JSONObject;
import com.sun.jersey.spi.resource.Singleton;

/**
 *
 * @author kboufelliga
 */
@Path("/public/stores")
@Singleton
public final class Manager {
    @Context UriInfo uriInfo;
    @Context SecurityContext security;

    private static Api composer;

    public Manager() {
        composer = new Api("localhost", 3306, "composer", "composer", "cp1111");
        composer.loadStores();
    }
    @GET
    @Produces({"application/json"})
    public Response publicStores() {
        URI uri =  uriInfo.getAbsolutePath();
        Response.created(uri).build();

        try {
            String publicStores = composer.getPublicStores();

            System.out.println("PUBLIC STORES: " +publicStores);

            JSONObject response = JSONObject.fromObject(composer.getPublicStores());

	        return Response.ok(response.toString(),"application/json").build();

        } catch (Exception e) {
            System.out.println("ERROR: " +e.getMessage());
        }

        return Response.serverError().build();
    }

}
