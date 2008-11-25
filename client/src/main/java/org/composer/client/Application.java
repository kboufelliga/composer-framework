package org.composer.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.composer.client.utils.RegisterUtils;
import org.composer.client.utils.KeyGen;
import org.composer.client.utils.ResultSet;
import org.composer.client.beans.Member;
import org.composer.client.beans.User;
import org.composer.exceptions.UserActivationException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 21, 2008
 * Time: 6:22:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class Application {
    private Repository repository;
    
    public Application(Repository repository) {
        this.repository = repository;
    }

    public String register(String appName, String memberKey) throws Exception {
        Member member = getMember(appName, memberKey);

        String key = KeyGen.generateKey();
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("key",key);
        properties.put("owner",memberKey);

        repository.write("applications",appName,"name",appName);
        repository.write("applications",appName,"key",key);
        repository.write("applications",appName,"owner",memberKey);


        RegisterUtils.emailAppKey(member.getEmail(),appName,key);

        return key;
    }

    public void activate(String appName, String appKey) throws Exception {
        if (repository.ask("applications",appName,"key",appKey)) {
            Map<String,String> instanceValues = new HashMap<String,String>();
            instanceValues.put("status","active");
            repository.write("applications",appName,"status","active");
        }
    }

    public boolean exists(String appName, String appKey) throws Exception {
        return repository.ask("applications",appName,"key",appKey);
    }

    public String registerUser(String appName, String userId, String userEmail) throws Exception {
        if (repository.ask(appName,userId,"userId",userId)) {
            throw new Exception("A user with user id '"+userId+"' is already registered!");
        }

        String key = KeyGen.generateShortKey();

        Map<String,String> properties = new HashMap<String,String>();
        properties.put("key",key);
        properties.put("email",userEmail);

        repository.write(appName,userId,"userId",userId);
        repository.write(appName,userId,"email",userEmail);
        repository.write(appName,userId,"key",key);

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
                repository.write(appName,userId,"status","active");
            } else {
                throw new UserActivationException("User '"+userId+"' key is invalid!");
            }
        }
    }

    public JSONArray users(String appName) throws Exception {
        Collection<String> properties = new ArrayList(1);
        properties.add("email");
        return repository.read(appName,"userId",properties);

    }

    public Member getMember(String appName, String memberKey) throws Exception{
        Member member = new Member();

        Collection<String> properties = new ArrayList(1);
        properties.add("email");

        repository.read(appName,memberKey,properties);
        //TODO

        return member;
    }

    public User getUser(String appName, String userId) throws Exception {
        User user = new User();
        user.setId(userId);

        Collection<String> properties = new ArrayList(3);
        properties.add("key");
        properties.add("email");
        properties.add("status");

        JSONObject response = ResultSet.fromJenaResult(repository.read(appName, userId, properties));

        if (response.has("status")) {
            JSONObject status = response.getJSONObject("status");
            if (status.get("value") != null)
                user.setStatus(status.get("value").toString());
        }

        if (response.has("key")) {
            JSONObject key = response.getJSONObject("key");
            if (key.get("value") != null)
                user.setKey(key.get("value").toString());
        }

        if (response.has("email")) {
            JSONObject email = response.getJSONObject("email");
            if (email.get("value") != null)
                user.setEmail(email.get("value").toString());
        }

        return user;
    }

}
