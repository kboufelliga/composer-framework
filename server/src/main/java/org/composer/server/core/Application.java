package org.composer.server.core;

import org.composer.engine.core.*;
import org.composer.server.utils.KeyGen;
import org.composer.server.utils.RegisterUtils;
import org.composer.server.utils.JenaUtils;
import org.composer.server.beans.Member;
import org.composer.server.beans.User;
import org.composer.exceptions.UserActivationException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 4, 2008
 * Time: 4:41:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class Application {
    private static Log log = LogFactory.getLog(Application.class);

    private ResourceManager resourceManager;
    private RegisterUtils registerUtils = RegisterUtils.getInstance();

    public Application(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public String register(String appName, String memberKey) {
        Member member = getMember(appName, memberKey);

        String key = KeyGen.generateKey();
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("key",key);
        properties.put("owner",memberKey);

        resourceManager.write("applications",appName,"name",appName);
        resourceManager.write("applications",appName,"key",key);
        resourceManager.write("applications",appName,"owner",memberKey);
        

        registerUtils.emailAppKey(member.getEmail(),appName,key);

        return key;
    }

    public void activate(String appName, String appKey) {
        if (resourceManager.ask("applications",appName,"key",appKey)) {
            Map<String,String> instanceValues = new HashMap<String,String>();
            instanceValues.put("status","active");
            resourceManager.write("applications",appName,"status","active");
        }

        //resourceManager.write("applications",appName,appName+".status","active");
    }

    public boolean exists(String appName, String appKey) {
        return resourceManager.ask("applications",appName,"key",appKey);
    }

    public String registerUser(String appName, String userId, String userEmail) throws Exception {
        if (resourceManager.ask(appName,userId,"userId",userId)) {
            throw new Exception("A user with user id '"+userId+"' is already registered!");
        }

        String key = KeyGen.generateShortKey();

        Map<String,String> properties = new HashMap<String,String>();
        properties.put("key",key);
        properties.put("email",userEmail);

        resourceManager.write(appName,userId,"userId",userId);
        resourceManager.write(appName,userId,"email",userEmail);
        resourceManager.write(appName,userId,"key",key);

        return key;
    }

    public void activateUser(String appName, String userId, String userKey) throws Exception {
        User user = getUser(appName,userId);

        if ("active".equals(user.getStatus())) {
            throw new UserActivationException("User '"+userId+"' already activated!");
        }

        if ("suspended".equals(user.getStatus())) {
            throw new UserActivationException("User '"+userId+"' suspended!");

        }

        if (user.getStatus() == null) {
            if (userKey.equals(user.getKey())) {
                resourceManager.write(appName,userId,"status","active");
            } else {
                throw new UserActivationException("User '"+userId+"' key is invalid!");
            }
        }
    }

    public JSONArray users(String appName) {
        Collection<String> properties = new ArrayList(1);
        properties.add("email");
        return JenaUtils.resultSet(resourceManager.read(appName,"userId",properties));

    }

    public Member getMember(String appName, String memberKey) {
        Member member = new Member();

        Collection<String> properties = new ArrayList(1);
        properties.add("email");

        resourceManager.read(appName,memberKey,properties);
        //TODO
        
        return member;
    }

    public User getUser(String appName, String userId) {
        User user = new User();
        user.setId(userId);

        JSONArray result;
        JSONObject list;

        Collection<String> properties = new ArrayList(2);
        properties.add("key");
        properties.add("email");

        result = JenaUtils.resultSet(resourceManager.read(appName, userId, properties));
        list = result.getJSONObject(0);
        JSONObject key = list.getJSONObject("key");
        JSONObject email = list.getJSONObject("email");
        JSONObject status = list.getJSONObject("status");

        log.info("fetch key result: "+result.toString());

        if (key.get("value") != null)
            user.setKey(key.get("value").toString());

        if (email.get("value") != null)
                    user.setEmail(email.get("value").toString());

        if (status.get("value") != null)
            user.setStatus(status.get("value").toString());

        return user;
    }

}
