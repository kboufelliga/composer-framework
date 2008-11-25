package org.composer.client;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 15, 2008
 * Time: 7:02:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class Repository {
    private static Log log = LogFactory.getLog(Repository.class);
    private final String hostName;
    private final String memberId;
    private final String memberPassword;
    private final String domain;
    private final String context;
    protected final static int BUFFER_SIZE = 4096;
    
    private Repository(Builder builder) {
        hostName = builder.hostName;
        memberId = builder.memberId;
        memberPassword = builder.memberPassword;
        domain = builder.domain;
        context = builder.context;
    }

    public static class Builder {
        private String hostName;
        private String memberId;
        private String memberPassword;
        private String domain = Properties.DEFAULT_DOMAIN_PREFIX.value();
        private String context = Properties.DEFAULT_CONTEXT_PREFIX.value();

        public Builder(String hostName, String memberId, String memberPassword) {
            this.hostName = hostName;
            this.memberId = memberId;
            this.memberPassword = memberPassword;
        }

        public Builder domain(String name) {
            domain = name;
            return this;
        }

        public Builder context(String name) {
            context = name;
            return this;
        }

        public Repository build() {
            return new Repository(this);
        }
    }

    public JSONArray read(String namespace, String resourceName) throws Exception {
        JSONArray result = new JSONArray();
        HttpMethod put = new PutMethod(hostName+"/read/"+namespace+"/"+resourceName);

        HttpClient client = new HttpClient();
        client.getState().setCredentials(new AuthScope(null, 9111, "authentication"), new UsernamePasswordCredentials(memberId, memberPassword));
        put.setDoAuthentication(true);

        int statusCode = client.executeMethod(put);

        if (statusCode != HttpStatus.SC_OK) {
            log.error("Method failed: " +put.getStatusLine());

            return result;
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(put.getResponseBodyAsStream()));
        CharArrayWriter cdata = new CharArrayWriter();
        char buf[] = new char[BUFFER_SIZE];
        int ret;
        while ((ret = buffer.read(buf, 0, BUFFER_SIZE)) != -1)
            cdata.write(buf, 0, ret);

        buffer.close();

        JSONObject jsonObject = JSONObject.fromString(cdata.toString());
        result.put(jsonObject);

        put.releaseConnection();

        return result;
    }


    public boolean ask(String namespace, String resourceName, String propertyName, String propertyValue) throws Exception {
        HttpMethod get = new GetMethod(hostName+"/ask/"+namespace+"/"+resourceName+"/"+propertyName);

        HttpClient client = new HttpClient();
        client.getState().setCredentials(new AuthScope(null, 9111, "authentication"), new UsernamePasswordCredentials(memberId, memberPassword));
        get.setDoAuthentication(true);

        JSONObject parmsList = new JSONObject();
        parmsList.put("value",propertyValue);

        NameValuePair queryString = new NameValuePair("parms",parmsList.toString());
        get.setQueryString(new NameValuePair[]{queryString});

        int statusCode = client.executeMethod(get);

        if (statusCode != HttpStatus.SC_OK) {
            log.error("Method failed: " +get.getStatusLine());

            throw new Exception("server error "+get.getStatusLine());
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
        CharArrayWriter cdata = new CharArrayWriter();
        char buf[] = new char[BUFFER_SIZE];
        int ret;
        while ((ret = buffer.read(buf, 0, BUFFER_SIZE)) != -1)
            cdata.write(buf, 0, ret);

        buffer.close();

        JSONObject answer = JSONObject.fromString(cdata.toString());

        get.releaseConnection();
        log.info("................................. ask < result: "+answer.toString());
        return answer.getBoolean("response");
    }

    public JSONArray write(String namespace, String resourceName, Collection<String> resourceProperties) throws Exception {
        Map<String,String> request = new HashMap();
        Iterator<String> keys = resourceProperties.iterator();
        while(keys.hasNext()) {
            request.put(keys.next(),"property");
        }

        return write(namespace, resourceName, request);
    }

    public JSONArray read(String namespace, String resourceName, Collection<String> parms) throws Exception {
        JSONArray result = new JSONArray();
        HttpMethod get = new GetMethod(hostName+"/read/"+namespace+"/"+resourceName);

        HttpClient client = new HttpClient();
        client.getState().setCredentials(new AuthScope(null, 9111, "authentication"), new UsernamePasswordCredentials(memberId, memberPassword));

        get.setDoAuthentication(true);

        JSONArray parmsList = JSONArray.fromCollection(parms);

        NameValuePair queryString = new NameValuePair("parms",parmsList.toString());
        get.setQueryString(new NameValuePair[]{queryString});

        int statusCode = client.executeMethod(get);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " +get.getStatusLine());

            return result;
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
        CharArrayWriter cdata = new CharArrayWriter();
        char buf[] = new char[BUFFER_SIZE];
        int ret;
        while ((ret = buffer.read(buf, 0, BUFFER_SIZE)) != -1)
            cdata.write(buf, 0, ret);

        buffer.close();

        log.info("................................. read < result: "+cdata.toString());

        JSONObject jsonObject = JSONObject.fromString(cdata.toString());
        result.put(jsonObject);

        get.releaseConnection();

        return result;
    }

    public JSONArray write(String namespace, String resourceName) throws Exception {
        Map<String,String> properties = new HashMap();

        return write(namespace, resourceName, properties);
    }

    public JSONArray write(String namespace, String resourceName, String propertyName, String propertyValue) throws Exception  {
        Map<String,String> properties = new HashMap();
        properties.put(propertyName,propertyValue);

        return write(namespace, resourceName, properties);
    }

    public JSONArray write(String namespace, String resourceName, Map<String,String> properties)  throws Exception {
        HttpMethod put = new PutMethod(hostName+"/write/"+namespace+"/"+resourceName);

        HttpClient client = new HttpClient();
        client.getState().setCredentials(new AuthScope(null, 9111, "authentication"), new UsernamePasswordCredentials(memberId, memberPassword));
        put.setDoAuthentication(true);

        JSONObject propsMap = JSONObject.fromMap(properties);
        JSONArray propsList = new JSONArray();
        propsList.put(propsMap);
        NameValuePair queryString = new NameValuePair("parms",propsList.toString());
        put.setQueryString(new NameValuePair[]{queryString});

        int statusCode = client.executeMethod(put);

        if (statusCode != HttpStatus.SC_OK) {
            log.error("Method failed: " +put.getStatusLine());

            throw new Exception("server error");
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(put.getResponseBodyAsStream()));
        CharArrayWriter cdata = new CharArrayWriter();
        char buf[] = new char[BUFFER_SIZE];
        int ret;
        while ((ret = buffer.read(buf, 0, BUFFER_SIZE)) != -1)
            cdata.write(buf, 0, ret);

        buffer.close();

        put.releaseConnection();

        return JSONArray.fromString(cdata.toString());
    }

    public JSONObject update(String namespace, String resourceName, Map<String,String> parms) {
        JSONObject result = new JSONObject();
        return result;
    }

    public JSONObject delete(String resourceName, Map<String,String> parms) {
        JSONObject result = new JSONObject();
        return result;
    }
}
