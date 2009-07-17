package org.composer.services.exceptions;

import net.sf.json.JSONObject;

/**
 *
 * @author kboufelliga
 */
public class GeneralException extends Exception {
    private JSONObject exception = new JSONObject();

    public GeneralException(String msg) {
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
