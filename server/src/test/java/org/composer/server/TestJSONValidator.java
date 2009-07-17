package org.composer.server;

import org.composer.engine.json.SchemaValidator;
import org.composer.server.core.JSONSchemaManager;
import junit.framework.TestCase;
import net.sf.json.JSONObject;


/**
 *
 * @author kboufelliga
 */
public class TestJSONValidator extends TestCase {
    private final String SERVICE_CCSTORE_JSONSCH_NEW = "service_ccstore_new";
    
    private JSONObject creditcard;
    private JSONObject ccStorePass;
    private JSONObject ccStoreFail;
    
    private JSONObject jsonschema;
    
    private SchemaValidator validator;



    @Override
    protected void setUp() {
        validator = new SchemaValidator();
        
        jsonschema = JSONSchemaManager.getSchema(SERVICE_CCSTORE_JSONSCH_NEW);
        
        creditcard = new JSONObject();
        creditcard.put("name","Test Fixture");
		creditcard.put("cc_number","41111111111112222");
		creditcard.put("cc_exp_month","01");
		creditcard.put("cc_exp_year","2012");
		creditcard.put("address1","950 Tower Lane");
		creditcard.put("address2","");
		creditcard.put("city","Foster City");
		creditcard.put("state","CA");
		creditcard.put("postal_code","94404");
		creditcard.put("country","US");

        ccStorePass = new JSONObject();
        ccStorePass.put("creditcard", creditcard);
        
        creditcard = new JSONObject();
        creditcard.put("name","Test Fixture");
		creditcard.put("number","41111111111112222");
		creditcard.put("expMonth","01");
		creditcard.put("expYear","2012");
		creditcard.put("secCode","");
		creditcard.put("address1","950 Tower Lane");
		creditcard.put("address2","");
		creditcard.put("city","Foster City");
		creditcard.put("state","CA");
		creditcard.put("postalCode","94404");
		creditcard.put("country","US");

        ccStoreFail = new JSONObject();
        ccStoreFail.put("creditcard", creditcard);
    }

    public void testValidateStorage() throws Exception {
        JSONObject errorMap = validator.validateWErrorMap(jsonschema, ccStorePass);

        assertTrue(errorMap.size() == 0);

        assertTrue(validator.validate(jsonschema.getJSONObject("field_names"), jsonschema.getJSONObject("field_types"), ccStorePass));

        errorMap = validator.validateWErrorMap(jsonschema, ccStoreFail);

        assertTrue(errorMap.has("error"));

        assertFalse(validator.validate(jsonschema.getJSONObject("field_names"), jsonschema.getJSONObject("field_types"), ccStoreFail));

    }

    @Override
    protected void tearDown() {
        ccStorePass = null;
        ccStoreFail = null;
    }
}
