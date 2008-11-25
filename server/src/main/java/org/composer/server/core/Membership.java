package org.composer.server.core;

import org.composer.engine.core.*;
import org.composer.server.utils.KeyGen;
import org.composer.server.utils.RegisterUtils;
import org.composer.server.utils.JenaUtils;
import org.composer.server.beans.Member;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 11, 2008
 * Time: 5:14:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class Membership {
    private static Log log = LogFactory.getLog(Membership.class);

    private ResourceManager resourceManager;
    
    public Membership(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public String register(String name, String email) {
        String key = KeyGen.generateKey();

        resourceManager.write("memberships",name,"id",name);
        resourceManager.write("memberships",name,"key",key);
        resourceManager.write("memberships",name,"email",email);

        RegisterUtils.emailAppKey(email,"memberships",key);

        return key;
    }

    public boolean exists(String memberName) {
        return resourceManager.ask("memberships",memberName,"id",memberName);
    }

    public Member getMember(String memberName) {
        Member member = new Member();
        member.setName(memberName);

        try {
            Collection<String> properties = new ArrayList(2);
            properties.add("key");
            properties.add("email");
            

            JSONArray result = JenaUtils.resultSet(resourceManager.read("memberships",memberName,properties));

            JSONObject list = result.getJSONObject(0);
            JSONObject key = list.getJSONObject("key");
            JSONObject email = list.getJSONObject("email");
            
            member.setKey(key.get("value").toString());
            member.setEmail(email.get("value").toString());

        } catch (Exception e) {
            log.error("JSON Error: "+ e);
        }

        return member;
    }
    
}
