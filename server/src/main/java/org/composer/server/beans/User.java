package org.composer.server.beans;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 13, 2008
 * Time: 4:13:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class User {
    private String id;
    private String email;
    private String key;
    private String status;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
