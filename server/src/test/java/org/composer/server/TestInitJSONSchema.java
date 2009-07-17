package org.composer.server;

import org.composer.engine.json.DBTransform;
import org.composer.server.core.JSONSchemaManager;
import java.util.Iterator;
import junit.framework.TestCase;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author kboufelliga
 */
public class TestInitJSONSchema extends TestCase {
    private final String SERVICE_CCSTORE_JSONSCH_CC = "service_ccstore_cc";
    private final String SERVICE_CCSTORE_JSONSCH_NEW = "service_ccstore_new";
    private final String SERVICE_CCSTORE_JSONSCH_DEL = "service_ccstore_del";
    private final String ERROR_CODES = "service_ccstore_error_codes";
    private final String ERROR_CODES_DESCS = "service_ccstore_error_codes_descs";

    private final String DB_CC_ACCOUNT_ENCRYPTED_JSON = "cpf_cc_account_encrypted_json";

    private JSONObject serviceSchemaCCStoreNew;
    private JSONArray serviceSchemaCCStoreCC;
    private JSONArray serviceSchemaCCStoreDel;
    private JSONObject errorCodesSchema;
    private JSONObject errorCodesDescsSchema;

    private JSONArray serviceSchemaCCStoreDelRefs;

    private JSONObject ccFields;
    private JSONObject ccTypes;
    private JSONObject ccErrorCodes;
    private JSONObject ccErrorDescs;

    private DBTransform dbTransform;

    private JSONArray ccSchemaSubsetReqPass;
    private JSONArray ccSchemaSubsetReqFail;
    private JSONObject ccSchemaSubsetP;
    private String subsetSchemaName;




    @Override
    protected void setUp() {

        serviceSchemaCCStoreCC = new JSONArray();
        serviceSchemaCCStoreCC.add("name");
        serviceSchemaCCStoreCC.add("address1");
        serviceSchemaCCStoreCC.add("address2");
        serviceSchemaCCStoreCC.add("city");
        serviceSchemaCCStoreCC.add("state");
        serviceSchemaCCStoreCC.add("postal_code");
        serviceSchemaCCStoreCC.add("country");
        serviceSchemaCCStoreCC.add("cc_number");
        serviceSchemaCCStoreCC.add("cc_exp_month");
        serviceSchemaCCStoreCC.add("cc_exp_year");
        
        serviceSchemaCCStoreNew = new JSONObject();
        serviceSchemaCCStoreNew.put("creditcard",serviceSchemaCCStoreCC);
        

        serviceSchemaCCStoreDel = new JSONArray();
        serviceSchemaCCStoreDel.add("account_id");

        serviceSchemaCCStoreDelRefs = new JSONArray();
        serviceSchemaCCStoreDelRefs.add(DB_CC_ACCOUNT_ENCRYPTED_JSON);


        ccFields = new JSONObject();
        ccFields.put("account_id", "required");
        ccFields.put("name","required");
        ccFields.put("address1","required");
        ccFields.put("address2","optional");
        ccFields.put("city","required");
        ccFields.put("state","required");
        ccFields.put("postal_code","required");
        ccFields.put("country","required");
        ccFields.put("cc_number","required");
        ccFields.put("cc_exp_month","required");
        ccFields.put("cc_exp_year","required");
        ccFields.put("cc_last_4","optional");
        
        ccTypes = new JSONObject();
        ccTypes.put("account_id", "VARCHAR");
        ccTypes.put("name","VARCHAR");
        ccTypes.put("address1","VARCHAR");
        ccTypes.put("address2","VARCHAR");
        ccTypes.put("city","VARCHAR");
        ccTypes.put("state","VARCHAR");
        ccTypes.put("postal_code","VARCHAR");
        ccTypes.put("country","VARCHAR");
        ccTypes.put("cc_number","VARCHAR");
        ccTypes.put("cc_exp_month","VARCHAR");
        ccTypes.put("cc_exp_year","VARCHAR");
        ccTypes.put("cc_last_4","VARCHAR");

        ccErrorCodes = new JSONObject();
        ccErrorCodes.put("address1","1");
        ccErrorCodes.put("address2","1");
        ccErrorCodes.put("city","2");
        ccErrorCodes.put("postal_code","3");
        ccErrorCodes.put("state","4");
        ccErrorCodes.put("country","5");
        ccErrorCodes.put("cc_number","6");
        ccErrorCodes.put("cc_expired", "7");
        ccErrorCodes.put("cc_exp_month","8");
        ccErrorCodes.put("cc_exp_year","9");
        ccErrorCodes.put("name","10");

        ccErrorDescs = new JSONObject();
        ccErrorDescs.put("address1","No address specified");
        ccErrorDescs.put("address2","No address specified");
        ccErrorDescs.put("city","No city specified");
        ccErrorDescs.put("postal_code","Postal code has invalid format or does not exist");
        ccErrorDescs.put("state","State has invalid format or does not exist");
        ccErrorDescs.put("country","Country code has invalid format or does not exist");
        ccErrorDescs.put("cc_number","Credit card number is invalid");
        ccErrorDescs.put("cc_expired","Credit card is expired");
        ccErrorDescs.put("cc_exp_month","Credit card month is invalid");
        ccErrorDescs.put("cc_exp_year","Credit card year is invalid");
        ccErrorDescs.put("name","No credit card holder name specified");

        subsetSchemaName = "fgs_creditcard";

        ccSchemaSubsetReqPass = new JSONArray();
        ccSchemaSubsetReqPass.add("name");
        ccSchemaSubsetReqPass.add("address1");
        ccSchemaSubsetReqPass.add("address2");
        ccSchemaSubsetReqPass.add("city");
        ccSchemaSubsetReqPass.add("state");
        ccSchemaSubsetReqPass.add("postal_code");
        ccSchemaSubsetReqPass.add("country");
        ccSchemaSubsetReqPass.add("cc_number");
        ccSchemaSubsetReqPass.add("cc_exp_month");
        ccSchemaSubsetReqPass.add("cc_exp_year");

        ccSchemaSubsetReqFail = new JSONArray();
        ccSchemaSubsetReqFail.add("phone");


        dbTransform = new DBTransform();

    }

    public void testDBJSONInitialize() {
        try {

           JSONArray list = dbTransform.getAllObj();

           Iterator<JSONObject> objList = list.iterator();

           while (objList.hasNext()) {
                JSONObject obj = objList.next();

                if (JSONSchemaManager.schemaExits(obj.getString("name"))) {
                    JSONObject response = JSONSchemaManager.removeSchema(obj.getString("name"));

                    assertTrue(response.has("status"));
                    assertEquals("success",response.get("status"));
                }

                JSONObject response = JSONSchemaManager.newSchema(obj.getString("name"), obj.getJSONObject("field_names"), obj.getJSONObject("field_types"));

                assertTrue(response.has("status"));
                assertEquals("success",response.get("status"));

           }

        } catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testSubsetSchemaInit() {
        try {
            
            //CCStore Credit Card
            if (JSONSchemaManager.schemaExits("creditcard")) {
                JSONSchemaManager.removeSchema("creditcard");
            }

            JSONObject ccSchema = JSONSchemaManager.newSchema("creditcard", ccFields, ccTypes);

            assertFalse(ccSchema.has("error"));

            //CCStore Credit Card Error Codes
            if (JSONSchemaManager.schemaExits("creditcard_error_codes")) {
                JSONSchemaManager.removeSchema("creditcard_error_codes");
            }

            JSONObject ccSchemaError = JSONSchemaManager.newSchema("creditcard_error_codes", ccErrorCodes, new JSONObject());

            assertFalse(ccSchemaError.has("error"));

            //CCStore Credit Card Error Codes
            if (JSONSchemaManager.schemaExits("creditcard_error_descs")) {
                JSONSchemaManager.removeSchema("creditcard_error_descs");
            }

            JSONObject ccSchemaEDescs = JSONSchemaManager.newSchema("creditcard_error_descs", ccErrorDescs, new JSONObject());

            assertFalse(ccSchemaEDescs.has("error"));

            
            JSONArray ccRefs = new JSONArray();
            ccRefs.add("creditcard");

            JSONArray ccErrorRefs = new JSONArray();
            ccErrorRefs.add("creditcard_error_codes");

            JSONArray ccEDescsRefs = new JSONArray();
            ccEDescsRefs.add("creditcard_error_descs");

            //CCStore Credit Card
            if (JSONSchemaManager.schemaExits(SERVICE_CCSTORE_JSONSCH_CC)) {
                JSONSchemaManager.removeSchema(SERVICE_CCSTORE_JSONSCH_CC);
            }

            JSONObject ccSchemaSubset = JSONSchemaManager.newSchemaSubset(ccRefs,SERVICE_CCSTORE_JSONSCH_CC , serviceSchemaCCStoreCC);

            assertFalse(ccSchemaSubset.has("error"));

            
            //CCStore New
            if (JSONSchemaManager.schemaExits(SERVICE_CCSTORE_JSONSCH_NEW)) {
                JSONSchemaManager.removeSchema(SERVICE_CCSTORE_JSONSCH_NEW);
            }

            ccSchemaSubset = JSONSchemaManager.newSchemaSubset(ccRefs,SERVICE_CCSTORE_JSONSCH_NEW , serviceSchemaCCStoreNew);

            assertFalse(ccSchemaSubset.has("error"));

            //CCStore New Error Codes
            if (JSONSchemaManager.schemaExits("service_ccstore_error")) {
                JSONSchemaManager.removeSchema("service_ccstore_error");
            }

            ccSchemaSubset = JSONSchemaManager.newSchemaSubset(ccErrorRefs,"service_ccstore_error" , serviceSchemaCCStoreNew);

            assertFalse(ccSchemaSubset.has("error"));

            //CCStore New Error Descriptions
            if (JSONSchemaManager.schemaExits("service_ccstore_error_desc")) {
                JSONSchemaManager.removeSchema("service_ccstore_error_desc");
            }

            ccSchemaSubset = JSONSchemaManager.newSchemaSubset(ccEDescsRefs,"service_ccstore_error_desc" , serviceSchemaCCStoreNew);

            assertFalse(ccSchemaSubset.has("error"));

            //CCStore Delete
            if (JSONSchemaManager.schemaExits(SERVICE_CCSTORE_JSONSCH_DEL)) {
                JSONSchemaManager.removeSchema(SERVICE_CCSTORE_JSONSCH_DEL);
            }

            ccSchemaSubset = JSONSchemaManager.newSchemaSubset(serviceSchemaCCStoreDelRefs,SERVICE_CCSTORE_JSONSCH_DEL , serviceSchemaCCStoreDel);

            assertFalse(ccSchemaSubset.has("error"));


        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    public void testSubsetSchema() {
        try {
            
            if (JSONSchemaManager.schemaExits(subsetSchemaName)) {
                JSONSchemaManager.removeSchema(subsetSchemaName);
            }

            ccSchemaSubsetP = JSONSchemaManager.newSchemaSubset("creditcard",subsetSchemaName , ccSchemaSubsetReqPass);

            assertEquals(subsetSchemaName,ccSchemaSubsetP.getString("name"));

            ccSchemaSubsetP = JSONSchemaManager.newSchemaSubset("creditcard", subsetSchemaName, ccSchemaSubsetReqFail);

            assertTrue(ccSchemaSubsetP.has("error"));

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
    
    @Override
    protected void tearDown() {
    }
}
