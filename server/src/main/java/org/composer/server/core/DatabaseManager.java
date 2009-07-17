package org.composer.server.core;

import org.composer.engine.json.SchemaValidator;
import org.composer.engine.utils.KeyGen;
import net.sf.json.JSONObject;
import org.composer.exceptions.GeneralException;

/**
 *
 * @author kboufelliga
 */
public class DatabaseManager {
    private static DatabaseManager INSTANCE = new DatabaseManager();

    public static SchemaValidator schemaValidator;

    private DatabaseManager() {
        schemaValidator = new SchemaValidator();
    }

    public static DatabaseManager getInstance() {
        return INSTANCE;
    }

    public JSONObject registration(String id) {
            String accountId = KeyGen.generateKey();
                        
            try {
                JSONObject reply = new JSONObject();

                reply.put("account_id", accountId);
            
                return reply;
                
            } catch (Exception e) {

                return new GeneralException(e.toString()).toJSON();
            }
    }
}
