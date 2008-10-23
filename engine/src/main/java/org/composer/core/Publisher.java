package org.composer.core;

import org.composer.interfaces.Publish;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.net.URLConnection;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;

import com.hp.hpl.jena.rdf.model.Resource;
import sun.misc.BASE64Encoder;

import org.json.JSONObject;
import org.json.JSONException;
import org.composer.transforms.JSONSerializer;
import org.composer.core.Properties;

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

    public Publisher(String domainPrefix, String contextPrefix, String applicationKey) {
        this.domainPrefix = domainPrefix;
        this.contextPrefix = contextPrefix;
        this.applicationKey = applicationKey;
    }

    private static String getCredentials() {
      return new BASE64Encoder().encode((domainPrefix+":"+contextPrefix+ ":" + applicationKey).getBytes());
    }

    public void publish(String baseUrl, Resource resource) {
        URL url = buildUrl(baseUrl);

        publish(url,resource);
    }

    public void publish(URL destinationUrl, Resource resource) {
        JSONSerializer serializer = new JSONSerializer(false);

        try {
			JSONObject jsonObject = (JSONObject) serializer.objectifyResource(resource);

            post(destinationUrl,jsonObject);

		} catch (JSONException je) {
			log.error("JSON Exception: "+je.toString());
		} catch (IOException ioe) {
            log.error("IO Exception: "+ioe.toString());
        } catch (Exception e) {
            log.error("Exception: "+e.toString());
        }
    }


    private void post(URL url, JSONObject object) throws Exception {
        URLConnection connect = url.openConnection();
        connect.setDoOutput(true);

        connect.setRequestProperty ("Authorization", "Basic " + getCredentials());

        OutputStreamWriter wr = new OutputStreamWriter(connect.getOutputStream());
        wr.write(object.toString());
        wr.flush();

        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(connect.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            log.debug(" return code: "+line);
        }
        wr.close();
        rd.close();
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
