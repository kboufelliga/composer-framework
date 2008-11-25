package org.composer.exceptions;

import net.sf.json.JSONObject;


/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 13, 2008
 * Time: 4:22:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserActivationException extends Exception {
    private JSONObject exception = new JSONObject();

    public UserActivationException(String msg) {
        exception.put("error", msg);
    }

    public JSONObject toJSON() {
        return exception;
    }
    
    @Override
    public String toString() {
        return exception.toString();
    }
}
