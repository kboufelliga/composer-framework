package org.composer.engine;

import org.composer.engine.json.SchemaValidator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 *
 * @author kboufelliga
 */
public class TestSchema extends TestCase {
    private JSONObject creditcard;
    private JSONObject creditcardEmpty;
    private JSONObject ccStore;
    private JSONObject requiredCustomSchema;
    private JSONObject requiredCCSchema;
    private JSONObject requiredCCStoreSchema;
    private JSONObject ccOnlyRequest;
    private JSONObject ccStoreRequest;
    private ArrayList<String> ccSchema;
    private ArrayList<String> ccStoreSchema;
    private Map<String,ArrayList> schemasMap;
    private SchemaValidator jsonValidator;
    private JSONObject errorCodes;
    private JSONObject errorDescs;
    private JSONObject requiredFields;
    private JSONObject requiredTypes;
    private JSONObject typesFixturePass;
    private JSONObject typesFixtureFail;
    private JSONObject typesFixtureEmpty;


    @Override
    protected void setUp() {
        schemasMap = new HashMap<String,ArrayList>();

        ccSchema = new ArrayList<String>();
        ccSchema.add("name");
        ccSchema.add("number");
        ccSchema.add("country");

        schemasMap.put("creditcard", ccSchema);

        ccStoreSchema = new ArrayList<String>();
        ccStoreSchema.add("member_no");
        ccStoreSchema.add("creditcard");

        schemasMap.put("cc_store", ccStoreSchema);

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

        creditcardEmpty = new JSONObject();
        creditcardEmpty.put("name","");
		creditcardEmpty.put("number","");
		creditcardEmpty.put("expMonth","");
		creditcardEmpty.put("expYear","");
		creditcardEmpty.put("secCode","");
		creditcardEmpty.put("address1","");
		creditcardEmpty.put("address2","");
		creditcardEmpty.put("city","");
		creditcardEmpty.put("state","");
		creditcardEmpty.put("postalCode","");
		creditcardEmpty.put("country","");

        ccStore = new JSONObject();
        ccStore.put("member_no", "M1111");
        ccStore.put("creditcard", creditcard);
        
        jsonValidator = new SchemaValidator(schemasMap);

        errorCodes = new JSONObject();
        errorCodes.put("account_name", "1");
        errorCodes.put("account_id", "2");
        errorCodes.put("is_active", "3");

        errorDescs = new JSONObject();
        errorDescs.put("account_name", "account name is required");
        errorDescs.put("account_id", "account id is required");
        errorDescs.put("is_active", "is active field is required");

        requiredFields = new JSONObject();
        requiredFields.put("account_name", "required");
        requiredFields.put("account_id", "required");
        requiredFields.put("is_active", "required");
        requiredFields.put("status", "optional");

        requiredTypes = new JSONObject();
        requiredTypes.put("account_name", "VARCHAR");
        requiredTypes.put("account_id", "INTEGER");
        requiredTypes.put("is_active", "BIT");
        requiredTypes.put("status", "VARCHAR");

        typesFixturePass = new JSONObject();
        typesFixturePass.put("account_name", "Test Fixture Pass");
        typesFixturePass.put("account_id", 12345);
        typesFixturePass.put("is_active", Boolean.TRUE);

        typesFixtureFail = new JSONObject();
        typesFixtureFail.put("account_name", "Test Fixture Pass");
        typesFixtureFail.put("account_id", 12345);
        typesFixtureFail.put("is_active", "true");

        typesFixtureEmpty = new JSONObject();
        typesFixtureEmpty.put("account_name", "");
        typesFixtureEmpty.put("account_id", 12345);
        typesFixtureEmpty.put("is_active", "");

    }

    public void testValidate() throws Exception {
        Collection cccustom = new ArrayList<String>();
        cccustom.add("Name");
        cccustom.add("Number");
        cccustom.add("expMonth");
        cccustom.add("expYear");

        Collection address = new ArrayList<String>();
        address.add("street");
        address.add("city");
        address.add("state");
        address.add("zip");
        address.add("country");

        JSONObject requiredInvalid = new JSONObject();
        requiredInvalid.put("creditcard",JSONArray.fromObject(ccSchema));
        requiredInvalid.put("address", JSONArray.fromObject(address));
        requiredInvalid.put("cc_store", new JSONArray());

        requiredCCSchema = new JSONObject();
        requiredCCSchema.put("creditcard",JSONArray.fromObject(ccSchema));
        
        requiredCustomSchema = new JSONObject();
        requiredCustomSchema.put("creditcard",JSONArray.fromObject(cccustom));

        requiredCCStoreSchema = new JSONObject();
        requiredCCStoreSchema.put("cc_store", JSONArray.fromObject(ccStoreSchema));

        ccOnlyRequest = new JSONObject();
        ccOnlyRequest.put("creditcard", creditcard);

        JSONObject ccstore = new JSONObject();
        ccstore.put("member_no", "1111");
        ccstore.put("creditcard", creditcard);

        ccStoreRequest = new JSONObject();
        ccStoreRequest.put("cc_store", ccstore);

        assertFalse(jsonValidator.validate(requiredInvalid, creditcard));

        assertFalse(jsonValidator.validate(requiredInvalid, ccOnlyRequest));

        assertTrue(jsonValidator.validate(requiredCCSchema, ccOnlyRequest));

        assertTrue(jsonValidator.validate(requiredCustomSchema, ccOnlyRequest));

        assertFalse(jsonValidator.validate(requiredCCStoreSchema, ccOnlyRequest));

        assertTrue(jsonValidator.validate(requiredCCStoreSchema, ccStoreRequest));


        JSONObject errors = jsonValidator.validateWError(errorCodes, errorDescs, requiredFields, requiredTypes, typesFixtureEmpty);

        assertFalse(errors.isEmpty());

        errors = jsonValidator.validateWError(errorCodes, errorDescs, requiredFields, requiredTypes, typesFixturePass);

        assertTrue(errors.isEmpty());

    }

    @Override
    protected void tearDown() {
        creditcard = null;
        ccStore = null;
        jsonValidator = null;
        requiredCustomSchema = null;
        requiredCCSchema = null;
        requiredCCStoreSchema = null;
        ccOnlyRequest = null;
        ccStoreRequest = null;
    }
}
