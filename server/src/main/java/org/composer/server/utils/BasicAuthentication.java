package org.composer.server.utils;

import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientHandlerException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 3, 2008
 * Time: 12:58:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasicAuthentication extends ClientFilter {
    private String username;
    private String password;

    public BasicAuthentication(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public ClientResponse handle(ClientRequest clientRequest) throws
        ClientHandlerException {

        byte[] encoded = Base64.encodeBase64((username + ":" +password).getBytes());

        List<Object> headerValue = new ArrayList<Object>();
        headerValue.add("Basic " + new String(encoded));
        clientRequest.getMetadata().put("Authorization", headerValue);

        return getNext().handle(clientRequest);
    }
}
