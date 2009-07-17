package org.composer.engine.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.TransformerUtils;

/**
 *
 * @author kboufelliga
 */
public class SchemaValidator {
    private static Map<String,ArrayList> schemasMap;
    private static Transformer stringLowerFilter = TransformerUtils.chainedTransformer(TransformerUtils.stringValueTransformer(),TransformerUtils.invokerTransformer("toLowerCase"));

    public SchemaValidator() {
        schemasMap = new HashMap<String,ArrayList>();
    }

    public SchemaValidator(Map<String,ArrayList> schemas) {
        schemasMap = schemas;
    }

    public boolean validate(Class jsonClass, JSONObject jsonObj) {
        Collection classFields = CollectionUtils.collect(Arrays.asList(jsonClass.getFields()),stringLowerFilter);
        Collection jsonFields = CollectionUtils.collect(jsonObj.keys(), stringLowerFilter);

        return CollectionUtils.isSubCollection(classFields, jsonFields);
    }

    public boolean validate(JSONObject requiredObj, JSONObject jsonObj) throws Exception {

        return CollectionUtils.isSubCollection(flattenReq("",new ArrayList<String>(),requiredObj), flattenObj("",new ArrayList<String>(),jsonObj));
    }

    public boolean validate(JSONObject requiredFields, JSONObject requiredTypes, JSONObject jsonObj) throws Exception {
        if (CollectionUtils.isSubCollection(flattenReq("",new ArrayList<String>(),requiredFields), flattenObj("",new ArrayList<String>(),jsonObj)))
                return JavaTransform.checkType(requiredFields, requiredTypes, flattenObj("",new JSONObject(),jsonObj));

        return false;
    }


    public JSONObject validateWErrorMap(JSONObject schemaObj, JSONObject dataObj) throws Exception {
        return validateWErrorMap(schemaObj.getJSONObject("field_names"),schemaObj.getJSONObject("field_types"), dataObj);
    }

    public JSONObject validateWErrorMap(JSONObject requiredFields, JSONObject requiredTypes, JSONObject dataObj) throws Exception {
        Collection requirement = flattenReq("",new ArrayList<String>(),requiredFields);
        Collection data = flattenObj("",new ArrayList<String>(),dataObj);


        /*** NOTE THE SUB COLLECTION HERE IS DECEIVING BECAUSE THE TYPE CHECK
             REQUIRES A TYPE DEFINITION AND SO A CORRECTION IS NEEDED TO BE DONE
             AT A MINIMUM ALLOW THE TYPE CHECKING TO BE OPTIONAL WHEN NOT DEFINED
         ***/
        if (CollectionUtils.isSubCollection(requirement, data))
            return JavaTransform.checkTypeValue(requiredFields, requiredTypes, flattenObj("",new JSONObject(),dataObj));

        /*** SYMMETRIC DIFFERENCE A,B AND B.A ***/
        Collection difference = CollectionUtils.disjunction(requirement, data);

        /*** DOES NOT MEET REQUIREMENTS ***/
        JSONObject errorMap = new JSONObject();

        errorMap.put("error", "All fields are required");
        errorMap.put("error_description", difference.toString());

        return errorMap;
    }

    public JSONObject validateWError(JSONObject errorObj, JSONObject schemaObj, JSONObject dataObj) throws Exception {
        return validateWError(errorObj.getJSONObject("error_codes"),errorObj.getJSONObject("error_descs"),schemaObj.getJSONObject("field_names"),schemaObj.getJSONObject("field_types"), dataObj);
    }

    public JSONObject validateWError(JSONObject errorCodes, JSONObject errorDescs, JSONObject requiredFields, JSONObject requiredTypes, JSONObject dataObj) throws Exception {
        return JavaTransform.checkTypeValue(errorCodes, errorDescs, requiredFields, requiredTypes, flattenObj("",new JSONObject(),dataObj));
    }

    /*** ASSUMES ALL FIELDS ARE REQUIRED ***/
    public boolean validate(JSONArray requiredFields, JSONObject requiredTypes, JSONObject jsonObj) throws Exception {
        if (CollectionUtils.isSubCollection(flattenReq("",new ArrayList<String>(),requiredFields), flattenObj("",new ArrayList<String>(),jsonObj)))
                return JavaTransform.checkType(requiredTypes, flattenObj("",new JSONObject(),jsonObj));

        return false;
    }

    public JSONObject validateWErrorMap(JSONArray requiredFields, JSONObject requiredTypes, JSONObject jsonObj) throws Exception {
        if (CollectionUtils.isSubCollection(flattenReq("",new ArrayList<String>(),requiredFields), flattenObj("",new ArrayList<String>(),jsonObj)))
                return JavaTransform.checkTypeValue(requiredTypes, flattenObj("",new JSONObject(),jsonObj));

        /*** DOES NOT MEET REQUIREMENTS ***/
        JSONObject errorMap = new JSONObject();

        errorMap.put("error", "All fields are required");

        return errorMap;
    }
    private Collection flattenObj(String prefix, Collection flatList, JSONObject jsonObj) throws Exception {
        Iterator<String> keys = jsonObj.keys();
        StringBuffer field = new StringBuffer();

        while(keys.hasNext()) {
            String key = keys.next();

            if (jsonObj.optJSONObject(key) != null) {
                field = new StringBuffer();
                field.append(prefix);
                field.append("/");
                field.append(key.toLowerCase());

                flattenObj(field.toString(),flatList, jsonObj.getJSONObject(key));
            } else {
                field = new StringBuffer();
                field.append(prefix);
                field.append("/");
                field.append(key.toLowerCase());

                flatList.add(field.toString());
            }
        }

        return flatList;
    }

    private JSONObject flattenObj(String prefix, JSONObject flatObj, JSONObject jsonObj) throws Exception {
        Iterator<String> keys = jsonObj.keys();

        while(keys.hasNext()) {
            String key = keys.next();

            if (jsonObj.optJSONObject(key) != null) {
                String namePrefix = "".equals(prefix)? key : prefix+"/"+key;

                flattenObj(namePrefix, flatObj, jsonObj.getJSONObject(key));
            } else {
                String namePrefix = "".equals(prefix)? key : prefix+"/"+key;

                flatObj.put(namePrefix,jsonObj.get(key));
            }
        }

        return flatObj;
    }


    private Collection flattenReq(String prefix, Collection flatList, JSONObject jsonObj) throws Exception {
        Iterator<String> keys = jsonObj.keys();
        StringBuffer field = new StringBuffer();

        while(keys.hasNext()) {
            String key = keys.next();

            if (jsonObj.optJSONArray(key) != null) {
                JSONArray array = jsonObj.getJSONArray(key);
                StringBuffer pf = new StringBuffer();
                pf.append(prefix);
                pf.append("/");
                pf.append(key.toLowerCase());

                if (array.isEmpty() && schemasMap.containsKey(key)) {

                    Iterator<String> objKeys = schemasMap.get(key).iterator();

                    while (objKeys.hasNext()) {
                        String objkey = objKeys.next();

                        field = new StringBuffer();
                        field.append(pf.toString());
                        field.append("/");
                        field.append(objkey.toLowerCase());

                        flatList.add(field.toString());
                    }
                } else if (!array.isEmpty()) {
                    Iterator<String> objKeys = array.iterator();

                    while (objKeys.hasNext()) {
                        String objkey = objKeys.next();

                        field = new StringBuffer();
                        field.append(pf.toString());
                        field.append("/");
                        field.append(objkey.toLowerCase());

                        if (schemasMap.containsKey(objkey)) {
                            Iterator<String> okeys = schemasMap.get(objkey).iterator();

                            while (okeys.hasNext()) {
                                String okey = okeys.next();

                                StringBuffer pf2 = new StringBuffer();
                                pf2.append(field.toString());
                                pf2.append("/");
                                pf2.append(okey.toLowerCase());

                                flatList.add(pf2.toString());
                            }
                        } else {
                            flatList.add(field.toString());
                        }
                    }
                }
            } else if (jsonObj.optJSONObject(key) != null) {
                field = new StringBuffer();
                field.append(prefix);
                field.append("/");
                field.append(key.toLowerCase());

                flattenReq(field.toString(),flatList,jsonObj.getJSONObject(key));
            } else {
                field = new StringBuffer();
                field.append(prefix);
                field.append("/");
                field.append(key.toLowerCase());

                if (schemasMap.containsKey(key)) {
                    Iterator<String> objKeys = schemasMap.get(key).iterator();

                    while (objKeys.hasNext()) {
                        String objkey = objKeys.next();

                        StringBuffer pf = new StringBuffer();
                        pf.append(field.toString());
                        pf.append("/");
                        pf.append(objkey.toLowerCase());

                        flatList.add(pf.toString());
                    }
                } else {
                    flatList.add(field.toString());
                }
            }
        }

        return flatList;
    }


    private Collection flattenReq(String prefix, Collection flatList, JSONArray jsonObj) throws Exception {
        Iterator<String> keys = jsonObj.iterator();
        StringBuffer field = new StringBuffer();

        while(keys.hasNext()) {
            String objkey = keys.next();

            field = new StringBuffer();
            field.append(prefix);
            field.append("/");
            field.append(objkey.toLowerCase());

            if (schemasMap.containsKey(objkey)) {
                Iterator<String> okeys = schemasMap.get(objkey).iterator();

                while (okeys.hasNext()) {
                    String okey = okeys.next();

                    StringBuffer pf2 = new StringBuffer();
                    pf2.append(field.toString());
                    pf2.append("/");
                    pf2.append(okey.toLowerCase());

                    flatList.add(pf2.toString());
                }
            } else {
                flatList.add(field.toString());
            }
        }

        return flatList;
    }
}

