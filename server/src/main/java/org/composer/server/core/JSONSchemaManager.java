package org.composer.server.core;

import org.composer.engine.exceptions.GeneralException;
import org.composer.engine.utils.SqlMap;
import com.ibatis.sqlmap.client.SqlMapClient;
import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author kboufelliga
 */
public class JSONSchemaManager {
    static SqlMapClient sqlMap = SqlMap.getSqlMapInstance();

    /*** ASSUMES ALL FIELDS ARE REQUIRED ***/
    public static JSONObject newSchema(String name, JSONArray fieldList) throws Exception {
        JSONObject schemaFields = new JSONObject();

        Iterator fields = fieldList.iterator();

        while (fields.hasNext()) {
            Object fieldObj = fields.next();

            schemaFields.put(fieldObj, "required");
        }

        return newSchema(name,schemaFields,new JSONObject());
    }

    public static JSONObject newSchema(String name, JSONObject fields) throws Exception {
        return newSchema(name,fields,new JSONObject());
    }
    
    public static JSONObject newSchemaSubset(String sourceName, String name, JSONArray fieldList) {
        JSONArray sourceList = new JSONArray();

        sourceList.add(sourceName);

        return newSchemaSubset(sourceList, name, fieldList);
    }

    public static JSONObject newSchemaSubset(JSONArray sourceList, String name, JSONArray fieldList) {
        JSONObject reply;
       
        /*** BUILD THE SUBSET FROM THE UNION OF SETS IN THE SOURCELIST***/
        try {

            JSONObject schema = getSchemaUnion(sourceList);

            if (schema.has("error")) {

                reply = new JSONObject();
                reply.put("error", "failed");

            } else {

                reply = new JSONObject();
                reply.put("name", name);

                JSONObject newsetFields = new JSONObject();
                JSONObject newsetTypes = new JSONObject();

                JSONObject sourceFields = schema.getJSONObject("field_names");
                JSONObject sourceTypes = schema.getJSONObject("field_types");

                /*** BUILD SUBSET ***/
                StringBuffer missingFields = new StringBuffer();

                Iterator fields = fieldList.iterator();

                while (fields.hasNext()) {
                    String field = fields.next().toString();

                    if (sourceFields.has(field)) {

                        newsetFields.put(field, sourceFields.get(field));
                        newsetTypes.put(field, sourceTypes.get(field));
                    } else {

                        missingFields.append(field);
                        missingFields.append(",");
                    }
                }

                /*** MAKE SURE WE HAVE ALL THE REQUESTED FIELDS ***/
                if (fieldList.size() == newsetFields.size()) {
                    JSONObject saveStatus = newSchema(name, newsetFields, newsetTypes);

                    if (saveStatus.has("error")) {

                        reply = saveStatus;
                    } else {

                        reply.put("field_names", newsetFields);
                        reply.put("field_types", newsetTypes);
                    }
                } else {
                    reply.put("error", "failed");
                    reply.put("error_description","The following fields were not found in the source fields: "+missingFields.toString());
                }
            }
        } catch (Exception e) {
            reply = new JSONObject();
            reply.put("error", "failed");
            reply.put("error_description","reason: "+e.getMessage());
        }

        return reply;
    }

    public static JSONObject newSchemaSubset(String sourceName, String name, JSONObject objList) {
        JSONArray sourceList = new JSONArray();
        
        sourceList.add(sourceName);
        
        return newSchemaSubset(sourceList, name, objList);
    }

    public static JSONObject newSchemaSubset(JSONArray sourceList, String name, JSONObject objList) {
        JSONObject reply;

        /*** BUILD THE SUBSET FROM THE UNION OF SETS IN THE SOURCELIST***/
        try {

            JSONObject schema = getSchemaUnion(sourceList);

            if (schema.has("error")) {

                reply = new JSONObject();
                reply.put("error", "failed");
            } else {

                reply = new JSONObject();
                reply.put("name", name);

                JSONObject newsetFields = new JSONObject();
                JSONObject newsetTypes = new JSONObject();

                JSONObject sourceFields = schema.getJSONObject("field_names");
                JSONObject sourceTypes = schema.getJSONObject("field_types");

                StringBuffer missingFields = new StringBuffer();

                Iterator objs = objList.keys();
                
                while (objs.hasNext()) {

                    String key = objs.next().toString();

                    JSONArray list = objList.optJSONArray(key);

                    if (list != null) {

                        Iterator fields = list.iterator();

                        while (fields.hasNext()) {
                            String field = fields.next().toString();

                            if (sourceFields.has(field)) {

                                newsetFields.put(key+"/"+field, sourceFields.get(field));
                                newsetTypes.put(key+"/"+field, sourceTypes.get(field));
                            } else {

                                missingFields.append(key);
                                missingFields.append("/");
                                missingFields.append(field);
                                missingFields.append(",");
                            }
                        }
                    } else {
                        String field = objList.getString(key);

                        if (sourceFields.has(field)) {

                            newsetFields.put(key+"/"+field, sourceFields.get(field));
                            newsetTypes.put(key+"/"+field, sourceTypes.get(field));
                        } else {
                            missingFields.append(key);
                            missingFields.append("/");
                            missingFields.append(field);
                            missingFields.append(",");
                        }
                    }
                }

                if (missingFields.length() == 0) {

                    JSONObject saveStatus = newSchema(name, newsetFields, newsetTypes);

                    if (saveStatus.has("error")) {

                        reply = saveStatus;
                    } else {

                        reply.put("field_names", newsetFields);
                        reply.put("field_types", newsetTypes);
                    }
                } else {

                    reply.put("error", "failed");
                    reply.put("error_description","The following fields were not found in the source fields: "+missingFields.toString());
                }
            }
        } catch (Exception e) {

            reply = new JSONObject();

            reply.put("error", "failed");
            reply.put("error_description","reason: "+e.getMessage());
        }

        return reply;
    }

    private static JSONObject getSchemaUnion(JSONArray list) throws Exception {
        JSONObject union = new JSONObject();
        JSONObject fields = new JSONObject();
        JSONObject types = new JSONObject();

        //TODO MODIFY WITH A QUERY TO GET THE UNION OF FIELDS AND TYPES

        Iterator refs = list.iterator();

        while(refs.hasNext()) {

            String schemaName = refs.next().toString();

            JSONObject schema = getSchema(schemaName);

            JSONObject schemaFields = schema.getJSONObject("field_names");

            Iterator fieldKeys = schemaFields.keys();

            while (fieldKeys.hasNext()) {
                Object key = fieldKeys.next();

                fields.put(key, schemaFields.get(key));
            }

            JSONObject schemaTypes = schema.getJSONObject("field_types");

            Iterator typeKeys = schemaTypes.keys();

            while (typeKeys.hasNext()) {
                Object key = typeKeys.next();
                
                types.put(key, schemaTypes.get(key));
            }
       }

        union.put("name", "union");
        union.put("field_names", fields);
        union.put("field_types", types);

        return union;
    }

    public static JSONObject newSchema(String name, JSONObject fields, JSONObject types) {
        JSONObject reply = new JSONObject();

        try {

            /*JSONSchema schema = new JSONSchema();

            schema.setName(name);
            schema.setDataFields(fields.toString());
            schema.setDataTypes(types.toString());
            schema.setLastUpdateDate(new Date());

            sqlMap.insert("new_json_schema", schema);*/
            
            reply.put("status", "success");
        } catch (Exception e) {

            reply.put("error", "failed");
            reply.put("error_description","reason: "+e.getMessage());
        }

        return reply;
    }

    public static JSONObject getSchemaFields(String name) throws Exception {
        try {
            
            /*JSONSchema schema = (JSONSchema)sqlMap.queryForObject("get_json_schema_byname", name);

            String schemaString = schema.getDataFields();
            return JSONObject.fromObject(schemaString);*/

            return new JSONObject();
        
        } catch (Exception e) {
            throw new GeneralException(e.toString());
        }
    }

    public static JSONObject getSchemaTypes(String name) throws Exception {
        try {

            /*JSONSchema schema = (JSONSchema)sqlMap.queryForObject("get_json_schema_byname", name);

            return JSONObject.fromObject(schema.getDataTypes());*/

            return new JSONObject();

        } catch (Exception e) {
            throw new GeneralException(e.toString());
        }
    }

    public static JSONObject getSchema(String name) {
        try {
            JSONObject reply = new JSONObject();

            /*JSONSchema schemaBean = (JSONSchema)sqlMap.queryForObject("get_json_schema_byname", name);

            reply.put("name", name);
            reply.put("field_names", JSONObject.fromObject(schemaBean.getDataFields()));
            reply.put("field_types", JSONObject.fromObject(schemaBean.getDataTypes()));*/

            return reply;

        } catch (Exception e) {

            return new GeneralException(e.toString()).toJSON();
        }
    }

    public static JSONObject removeSchema(String name) {
        try {
            JSONObject reply = new JSONObject();

            int result = sqlMap.delete("delete_json_schema", name);

            if (result == 1)
                reply.put("status","success");
            else
                reply.put("status","failed");

            return reply;

        } catch (Exception e) {
            e.printStackTrace();
            return new GeneralException(e.toString()).toJSON();
        }
    }

    public static boolean schemaExits(String name) throws Exception {
        try {
            Object count = sqlMap.queryForObject("count_json_schema_name", name);

            if (Integer.valueOf(count.toString()) > 0)
                return true;

            return false;

        } catch (Exception e) {

            throw new GeneralException(e.toString());
        }
    }
}
