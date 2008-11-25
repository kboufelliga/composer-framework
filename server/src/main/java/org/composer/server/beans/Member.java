package org.composer.server.beans;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 11, 2008
 * Time: 5:35:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class Member {
    private String name;
    private String email;
    private String key;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
