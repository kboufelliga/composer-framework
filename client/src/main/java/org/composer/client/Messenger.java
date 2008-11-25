package org.composer.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import net.sf.json.JSONArray;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 14, 2008
 * Time: 11:45:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class Messenger {
    private static Log log = LogFactory.getLog(Repository.class);
    private final String hostName;
    private final String exchangeName;
    private final String queueName;
    private final String memberId;
    private final String memberPassword;
    private final String applicationKey;
    private final String domain;
    private final String context;

    private Messenger(Builder builder) {
        hostName = builder.hostName;
        memberId = builder.memberId;
        memberPassword = builder.memberPassword;
        applicationKey = builder.applicationKey;
        exchangeName = builder.exchangeName;
        queueName = builder.queueName;
        domain = builder.domain;
        context = builder.context;
    }

    public static class Builder {
        private String hostName;
        private String memberId;
        private String memberPassword;
        private String exchangeName;
        private String queueName;
        private String applicationKey;
        private String domain = Properties.DEFAULT_DOMAIN_PREFIX.value();
        private String context = Properties.DEFAULT_CONTEXT_PREFIX.value();

        public Builder(String hostName, String exchangeName, String queueName, String memberId, String memberPassword) {
            this.hostName = hostName;
            this.exchangeName = exchangeName;
            this.queueName = queueName;
            this.memberId = memberId;
            this.memberPassword = memberPassword;
        }

        public Builder application(String key) {
            applicationKey = key;
            return this;
        }

        public Builder domain(String name) {
            domain = name;
            return this;
        }

        public Builder context(String name) {
            context = name;
            return this;
        }

        public Messenger build() {
            return new Messenger(this);
        }
    }

    public JSONArray send(String resourceName, String identifier, Collection<String> parms) throws Exception {
        JSONArray result = new JSONArray();
        HttpMethod put = new PutMethod(hostName+"/send/"+exchangeName+"/"+queueName+"/"+resourceName+"/"+identifier);

        HttpClient client = new HttpClient();
        client.getState().setCredentials(new AuthScope(null, 9111, "cafepress"), new UsernamePasswordCredentials(memberId, memberPassword));

        put.setDoAuthentication(true);

        Iterator<String> list = parms.iterator();
        JSONArray jsonList = JSONArray.fromCollection(parms);

        NameValuePair queryString = new NameValuePair("parms",jsonList.toString());
        put.setQueryString(new NameValuePair[]{queryString});

        int statusCode = client.executeMethod(put);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " +put.getStatusLine());
        }

        byte[] responseBody = put.getResponseBody();

        result = JSONArray.fromString(new String(responseBody));

        put.releaseConnection();

        return result;
    }

    public JSONArray receive(String routingKey) throws Exception {
        JSONArray result = new JSONArray();
        HttpMethod get = new GetMethod(hostName+"/receive/"+exchangeName+"/"+queueName+"/"+routingKey);

        HttpClient client = new HttpClient();
        client.getState().setCredentials(new AuthScope(null, 9111, "cafepress"), new UsernamePasswordCredentials(memberId, memberPassword));

        get.setDoAuthentication(true);

        int statusCode = client.executeMethod(get);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " +get.getStatusLine());
        } else {

            byte[] responseBody = get.getResponseBody();

            result = JSONArray.fromString(new String(responseBody));
        }

        get.releaseConnection();

        return result;
    }

}
