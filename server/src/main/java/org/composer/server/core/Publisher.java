package org.composer.server.core;

import org.composer.engine.core.*;
import org.composer.interfaces.Publish;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;

import com.hp.hpl.jena.rdf.model.Resource;

import java.io.CharArrayWriter;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PutMethod;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 19, 2008
 * Time: 7:50:48 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Publisher implements Publish {
    private static Log log = LogFactory.getLog(ResourceManager.class);

    private static String applicationKey;
    private static String domainPrefix;
    private static String contextPrefix;
    private static String resourceType = Properties.TYPE_JENA_RESOURCE.value();
    private static String actionUri = Properties.PUBLISH_URI.value();
    private static String memberId;
    private static String memberPassword;
    protected final static int BUFFER_SIZE = 4096;


    public Publisher(String domainPrefix, String contextPrefix, String applicationKey, String memberId, String memberPassword) {
        Publisher.domainPrefix = domainPrefix;
        Publisher.contextPrefix = contextPrefix;
        Publisher.applicationKey = applicationKey;
        Publisher.memberId = memberId;
        Publisher.memberPassword = memberPassword;
    }

    public void publish(String baseUrl, Resource resource) {
        URL url = buildUrl(baseUrl);

        publish(url,resource);
    }

    public void publish(URL destinationUrl, Resource resource) {
    }


    private void post(URL url, JSONObject object) throws Exception {
        HttpMethod put = new PutMethod(url.toString());

        HttpClient client = new HttpClient();
        client.getState().setCredentials(new AuthScope(null, 9111, "cafepress"), new UsernamePasswordCredentials(memberId, memberPassword));
        put.setDoAuthentication(true);

        int statusCode = client.executeMethod(put);

        if (statusCode != HttpStatus.SC_OK) {
            log.error("Method failed: " +put.getStatusLine());
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(put.getResponseBodyAsStream()));
        CharArrayWriter cdata = new CharArrayWriter();
        char buf[] = new char[BUFFER_SIZE];
        int ret;
        while ((ret = buffer.read(buf, 0, BUFFER_SIZE)) != -1)
            cdata.write(buf, 0, ret);

        buffer.close();

        JSONObject jsonObject = JSONObject.fromString(cdata.toString());
 
        put.releaseConnection();
    }

    private URL buildUrl(String baseUrl) {
        URL url = null;
        try {
            url = new URL(baseUrl+"/"+domainPrefix+"/"+contextPrefix+actionUri+"/"+resourceType);
        } catch(IOException ioe) {
            log.error("IO Exception: "+ioe);
        }

        return url;
    }

}
