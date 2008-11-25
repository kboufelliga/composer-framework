package org.composer.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import com.sun.grizzly.http.SelectorThread;

public class Main {
    private Log log = LogFactory.getLog(Main.class);

    public static void main(String[] args) throws Exception {
        final String baseUri = "http://localhost:9998/";
        final Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.packages","org.composer.core.resources");

        System.out.println("Starting grizzly...");

        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);

        System.out.println(String.format("Jersey app started with WADL available at %sapplication.wadl\n” + “Try out %shelloworld\nHit enter to stop it...", baseUri, baseUri));

        System.in.read();

        threadSelector.stopEndpoint();

        System.exit(0);
     }
}
