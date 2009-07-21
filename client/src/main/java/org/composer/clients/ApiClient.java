package org.composer.clients;

import java.io.CharArrayWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.RuntimeException;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import java.util.List;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.composer.beans.Store;
import org.composer.clients.utils.KeyStorage;
import org.composer.exceptions.GeneralException;


/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 14, 2008
 * Time: 11:45:52 PM
 * To change this template use File | Settings | File Templates.
 *
 * TODO ENABLE SECURITY
 * TODO GENERETICIZE COOKIE SETTINGS SO IT IS NOT RESTRICTED TO JSONHUB.ORG ONLY
 */
public class ApiClient {
    private static Log log = LogFactory.getLog(ApiClient.class);
    
    private static final String PUBLIC_STORE_TYPE = "public";
    private static final String PRIVATE_STORE_TYPE = "private";
    private static final String PUBLIC_GROUP_STORE_TYPE = "public-group";
    private static final String PRIVATE_GROUP_STORE_TYPE = "private-group";
    
    private String storeKey;
    private String storeType = PUBLIC_STORE_TYPE;
    private String repoHostname;
    private int repoHostPort;
    private String memberId;
    private String memberPassword;

    protected final static int BUFFER_SIZE = 4096;
    protected static HttpClient client;
    protected static Cookie storeCookie;

    private ApiClient(Builder builder) {
        /*** REPOSITORY HOST:PORT ***/
        repoHostname = builder.hostname;
        repoHostPort = builder.hostPort;
        
        /*** STORE INFO ***/
        storeKey = builder.storeKey;
        storeType = builder.storeType;

        /*** PRIVATE CREDENTIONALS - DEFAULT TO NONE ***/
        memberId = builder.memberId;
        memberPassword = builder.memberPassword;

        /*** THE "path" IS THE METADATA INFO
             FOR EXAMPLE:
         *   Let say you want to create a finance
         *   dev environment database data structure.
         *   The path should be "finance/dev/env/db"
         *   To add a value for example db named fndb1
         *   Submit a PUT request  to /data/finance/dev/env {db:"fndb1"}
         *
         ***/

        client = new HttpClient();

        List authPrefs = new ArrayList(2);
        authPrefs.add(AuthPolicy.BASIC);

        client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);

    }

    public static class Builder {
        private String hostname;
        private int hostPort;
        private String memberId;
        private String memberPassword;
        private String storeKey;
        private String storeType;

        public Builder(String hostname, int port) {
            this.hostname = hostname;
            this.hostPort = port;

            //by default open store for attached cooke
            try {
                open(hostname, port);
            } catch (Exception e) {
                //we should decide wether to throw a runtime exception or not;
                log.fatal(e.toString());
            }
        }

        public Builder store(String key, String type) {
            this.storeKey = key;
            this.storeType = type;

            return this;
        }

        public Builder credentials(String memberId, String memberPassword) {
            this.memberId = memberId;
            this.memberPassword = memberPassword;

            return this;
        }

        public ApiClient build() {
            return new ApiClient(this);
        }
    }

    public JSONObject post(String path) throws Exception {
        JSONObject result;
        PostMethod method = new PostMethod(repoHostname+":"+repoHostPort+"/api/meta/"+path);
        method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        method.setRequestHeader("Cookie", "jsonhub-store-key="+getStoreKey());

        //client.getState().setCredentials(new AuthScope(null, repoHostPort, "authentication"), new UsernamePasswordCredentials(memberId, memberPassword));

        //method.setDoAuthentication(true);

        method.setRequestBody(path);

        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " +method.getStatusLine());
        }

        Reader reader = new InputStreamReader(method.getResponseBodyAsStream());
        CharArrayWriter cdata = new CharArrayWriter();
        char buf[] = new char[BUFFER_SIZE];
        int ret;
        while ((ret = reader.read(buf, 0, BUFFER_SIZE)) != -1)
            cdata.write(buf, 0, ret);

        reader.close();

        System.out.println("PUT RESPONSE: "+cdata.toString());

        result = JSONObject.fromObject(cdata.toString());

        method.releaseConnection();

        System.out.println("PUT RESPONSE: "+result.getString(path));

        if ("error".equals(result.optString("error")))
            throw new GeneralException(result.getString("error"));

        return result;
    }

    public JSONObject delete(String path) throws Exception {
        JSONObject result = new JSONObject();
        DeleteMethod method = new DeleteMethod(repoHostname+":"+repoHostPort+"/api/meta/"+path);
        method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        method.setRequestHeader("Cookie", "jsonhub-store-key="+getStoreKey());

        //client.getState().setCredentials(new AuthScope(null, repoHostPort, "authentication"), new UsernamePasswordCredentials(memberId, memberPassword));

        //method.setDoAuthentication(true);

        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " +method.getStatusLine());
        }

        Reader reader = new InputStreamReader(method.getResponseBodyAsStream());
        CharArrayWriter cdata = new CharArrayWriter();
        char buf[] = new char[BUFFER_SIZE];
        int ret;
        while ((ret = reader.read(buf, 0, BUFFER_SIZE)) != -1)
            cdata.write(buf, 0, ret);

        reader.close();

        result = JSONObject.fromString(cdata.toString());

        method.releaseConnection();

        if ("error".equals(result.optString("error")))
            throw new GeneralException(result.getString("error"));

        return result;
    }

    public JSONObject get(String path) throws Exception {
        JSONObject result;
        HttpMethod method = new GetMethod(repoHostname+":"+repoHostPort+"/api/meta/"+path);
        method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        method.setRequestHeader("Cookie", "jsonhub-store-key="+getStoreKey());

        //client.getState().setCredentials(new AuthScope(null, repoHostPort, "authentication"), new UsernamePasswordCredentials(memberId, memberPassword));

        //method.setDoAuthentication(true);

        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " +method.getStatusLine());
            throw new GeneralException(method.getStatusLine().toString());
        } else {
            Reader reader = new InputStreamReader(method.getResponseBodyAsStream());
            CharArrayWriter cdata = new CharArrayWriter();
            char buf[] = new char[BUFFER_SIZE];
            int ret;
            while ((ret = reader.read(buf, 0, BUFFER_SIZE)) != -1)
                cdata.write(buf, 0, ret);

            reader.close();

            System.out.println("get response: "+cdata.toString());
            
            result = JSONObject.fromObject(cdata.toString());
        }

        method.releaseConnection();

        System.out.println("GET RESPONSE: "+result.getString(path));


        if ("error".equals(result.optString("error")))
            throw new GeneralException(result.getString("error"));

        return result;
    }

    public JSONObject register() throws Exception {
        JSONObject result;
        PostMethod method = new PostMethod(repoHostname+":"+repoHostPort+"/api/meta/store.registration");
        method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        method.setRequestHeader("Cookie", "jsonhub-store-key="+getStoreKey());

        //client.getState().setCredentials(new AuthScope(null, repoHostPort, "authentication"), new UsernamePasswordCredentials(memberId, memberPassword));

        //method.setDoAuthentication(true);

        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " +method.getStatusLine());
        }

        Reader reader = new InputStreamReader(method.getResponseBodyAsStream());
        CharArrayWriter cdata = new CharArrayWriter();
        char buf[] = new char[BUFFER_SIZE];
        int ret;
        while ((ret = reader.read(buf, 0, BUFFER_SIZE)) != -1)
            cdata.write(buf, 0, ret);

        reader.close();

        result = JSONObject.fromString(cdata.toString());

        method.releaseConnection();

        if ("error".equals(result.optString("error")))
            throw new GeneralException(result.getString("error"));

        return result;
    }

    private static JSONObject open(String host, int port) throws Exception {
        JSONObject result;
        PostMethod method = new PostMethod(host+":"+port+"/api/meta/store.open");

        //client.getState().setCredentials(new AuthScope(null, repoHostPort, "authentication"), new UsernamePasswordCredentials(memberId, memberPassword));

        //method.setDoAuthentication(true);

        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " +method.getStatusLine());
        }

        Reader reader = new InputStreamReader(method.getResponseBodyAsStream());
        CharArrayWriter cdata = new CharArrayWriter();
        char buf[] = new char[BUFFER_SIZE];
        int ret;
        while ((ret = reader.read(buf, 0, BUFFER_SIZE)) != -1)
            cdata.write(buf, 0, ret);

        reader.close();

        result = JSONObject.fromString(cdata.toString());

        method.releaseConnection();

        if ("error".equals(result.optString("error")))
            throw new GeneralException(result.getString("error"));

        return result;
    }

    private String getStoreKey() {
        try {
            System.out.println("CURRENT STORE KEY VALUE: "+storeKey);

            if (storeKey == null) {
                Store store = KeyStorage.INSTANCE.getByName("jsonhub-store-key");

                if (store != null) {
                        this.storeKey = store.getKey();
                        
                        System.out.println("OBJDB STORE KEY VALUE: "+storeKey);

                } else {
                        JSONObject jshubkey = register();

                        this.storeKey = jshubkey.getString("store-key");

                        System.out.println("REGISTRATION STORE KEY VALUE: "+storeKey);

                        store = new Store();
                        store.setName("jsonhub-store-key");
                        store.setKey(storeKey);
                        
                        KeyStorage.INSTANCE.add(store);
                    }
            }

            return storeKey;
        } catch (Exception e) {
            throw new RuntimeException("could not get jsonhub store key! \n Reason: \n"+e.getMessage());
        }
    }
}
