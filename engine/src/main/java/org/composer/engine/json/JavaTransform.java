package org.composer.engine.json;

import org.composer.engine.exceptions.GeneralException;
import org.composer.engine.utils.JDBCMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jsr166y.forkjoin.ForkJoinExecutor;
import jsr166y.forkjoin.ForkJoinPool;
import jsr166y.forkjoin.Ops;
import jsr166y.forkjoin.ParallelArray;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

/**
 *
 * @author kboufelliga
 */
public class JavaTransform {
    private static BidiMap jsonNamesMap;

    public static JSONObject toJSON(JSONArray schema, Object obj) throws Exception {
        JSONObject jsonObj = new JSONObject();
        Map javaObjMap = PropertyUtils.describe(obj);

        jsonNamesMap = getJSONNamesMap(schema);

        Iterator keys = javaObjMap.keySet().iterator();

        while(keys.hasNext()) {
            Object key = keys.next();
            String name = key.toString().replaceAll("_", "").toLowerCase();

            if (jsonNamesMap.inverseBidiMap().containsKey(name)) {
                jsonObj.put(jsonNamesMap.inverseBidiMap().get(name),javaObjMap.get(key));
            }
        }

        return jsonObj;
    }

    private static BidiMap getJSONNamesMap(JSONArray jsonObj){
        Iterator keys = jsonObj.iterator();
        BidiMap map = new DualHashBidiMap();

        while (keys.hasNext()) {
            String key = keys.next().toString();
            map.put(key, key.replaceAll("_", "").toLowerCase());
        }

        return map;
    }

     public static List<Object> convert(List<String> nameList, final Map<String,Class> typeMap, final Map<String,Object> valueMap) throws Exception {
        ForkJoinExecutor forkExec = ParallelArray.defaultExecutor();

        final Ops.Mapper typeCheck = new Ops.Mapper() {

            public Object map(Object arg0) {
                try {
                    //if same return the original
                    if (typeMap.get(arg0).isInstance(valueMap.get(arg0))) {

                        return typeMap.get(arg0).cast(valueMap.get(arg0));
                    } else {
                        Class clazz = typeMap.get(arg0);
                        Object obj = valueMap.get(arg0);

                        return clazz.cast(obj);
                    }

                } catch (Exception e) {
                    return null;
                }
            }

        };

        String[] strResult=new String[nameList.size()];

        ParallelArray<String> dataList = ParallelArray.create(nameList.size(), String.class, forkExec);

        dataList.addAll(typeMap.keySet().toArray(strResult));
        dataList.removeNulls();

        ParallelArray<Object> dataCheck = dataList.withMapping(typeCheck).all();

        dataCheck.removeNulls();

        if (dataCheck.size() == dataList.size())
            return dataCheck.asList();
        else
            throw new GeneralException("Invalid Data Dype");

    }


     public static boolean checkType(final JSONObject fieldObj, final JSONObject typeObj, final JSONObject dataObj) {
        ForkJoinExecutor forkExec = ParallelArray.defaultExecutor();

        final Ops.Mapper typeCheck = new Ops.Mapper() {
            public Object map(Object arg0) {
                try {
                    Class clazz = JDBCMap.get(typeObj.getString(arg0.toString()));

                    /*** ONLY CHECK IF THE FIELD IS REQUIRED ***/
                    if ("required".equals(fieldObj.getString(arg0.toString()).trim().toLowerCase())) {

                        if (clazz.getSimpleName().equals("Short")) {
                            /*** TRY TO CAST THE dataObj VALUE ***/
                            return Short.valueOf(dataObj.get(arg0.toString()).toString());

                        } else if (clazz.getSimpleName().equals("String") && "".equals(dataObj.get(arg0.toString().trim()))) {

                            return null;

                        } else {
                            if (clazz.isInstance(dataObj.get(arg0.toString()))) {

                                return arg0;
                            } else {

                                return null;
                            }
                        }

                    } else {
                        return arg0;
                    }
                } catch (Exception e) {

                    return null;
                }
            }

        };

        String[] strResult=new String[typeObj.size()];

        ParallelArray<Object> dataList = ParallelArray.create(typeObj.size(), Object.class, forkExec);

        dataList.addAll(typeObj.keySet().toArray(strResult));
        dataList.removeNulls();

        ParallelArray<Object> dataCheck = dataList.withMapping(typeCheck).all();

        dataCheck.removeNulls();

        if (dataCheck.size() == dataList.size())
            return Boolean.TRUE;
        else
            return Boolean.FALSE;

     }

     /*** ASSUME ALL FIELDS ARE REQUIRED ***/
     public static boolean checkType(final JSONObject typeObj, final JSONObject dataObj) {
        ForkJoinExecutor forkExec = ParallelArray.defaultExecutor();

        final Ops.Mapper typeCheck = new Ops.Mapper() {
            public Object map(Object arg0) {
                try {
                    Class clazz = JDBCMap.get(typeObj.getString(arg0.toString()));

                    if (clazz.getSimpleName().equals("Short")) {
                            /*** TRY TO CAST THE dataObj VALUE ***/
                            return Short.valueOf(dataObj.get(arg0.toString()).toString());

                    } else if (clazz.getSimpleName().equals("String") && "".equals(dataObj.get(arg0.toString().trim()))) {

                            return null;

                    } else {

                        if (clazz.isInstance(dataObj.get(arg0.toString()))) {

                                return arg0;
                        } else {

                            return null;
                        }
                    }

                } catch (Exception e) {

                    return null;
                }
            }

        };

        String[] strResult=new String[typeObj.size()];

        ParallelArray<Object> dataList = ParallelArray.create(typeObj.size(), Object.class, forkExec);

        dataList.addAll(typeObj.keySet().toArray(strResult));
        dataList.removeNulls();

        ParallelArray<Object> dataCheck = dataList.withMapping(typeCheck).all();

        dataCheck.removeNulls();

        if (dataCheck.size() == dataList.size())
            return Boolean.TRUE;
        else
            return Boolean.FALSE;

     }

     /*** ASSUME ALL FIELDS ARE REQUIRED ***/
     public static JSONObject checkTypeValue(final JSONObject typeObj, final JSONObject dataObj) {
        ForkJoinExecutor forkExec = ParallelArray.defaultExecutor();

        final JSONObject errorMap = new JSONObject();

        final Ops.Mapper typeCheck = new Ops.Mapper() {
            public Object map(Object arg0) {
                try {
                    Class clazz = JDBCMap.get(typeObj.getString(arg0.toString()));

                    if (clazz.getSimpleName().equals("Short")) {
                            /*** TRY TO CAST THE dataObj VALUE ***/
                            return Short.valueOf(dataObj.get(arg0.toString()).toString());

                    } else if (clazz.getSimpleName().equals("String") && "".equals(dataObj.get(arg0.toString().trim()))) {

                            errorMap.put(arg0.toString(),"A data value is required");

                            return null;

                    } else {

                        if (clazz.isInstance(dataObj.get(arg0.toString()))) {

                                return arg0;
                        } else {

                            errorMap.put(arg0,"Expecting data of type: "+JDBCMap.get(typeObj.getString(arg0.toString())));

                            return null;
                        }
                    }

                } catch (Exception e) {

                    errorMap.put(arg0,"Expecting data of type: "+JDBCMap.get(typeObj.getString(arg0.toString())));

                    return null;
                }
            }

        };

        String[] strResult=new String[typeObj.size()];

        ParallelArray<Object> dataList = ParallelArray.create(typeObj.size(), Object.class, forkExec);

        dataList.addAll(typeObj.keySet().toArray(strResult));
        dataList.removeNulls();

        ParallelArray<Object> dataCheck = dataList.withMapping(typeCheck).all();

        dataCheck.removeNulls();

        return errorMap;

     }

     public static JSONObject checkTypeValue(final JSONObject fieldObj, final JSONObject typeObj, final JSONObject dataObj) {
        ForkJoinExecutor forkExec = ParallelArray.defaultExecutor();

        final JSONObject errorMap = new JSONObject();

        final Ops.Mapper typeCheck = new Ops.Mapper() {
            public Object map(Object arg0) {

                try {

                    Class clazz = JDBCMap.get(typeObj.getString(arg0.toString()));

                    /*** ONLY CHECK IF THE FIELD IS REQUIRED ***/
                    if ("required".equals(fieldObj.getString(arg0.toString()).trim().toLowerCase())) {

                        if (clazz.getSimpleName().equals("Short")) {
                            /*** TRY TO CAST THE dataObj VALUE ***/
                            return Short.valueOf(dataObj.get(arg0.toString()).toString());

                        } else if (clazz.getSimpleName().equals("String") && "".equals(dataObj.get(arg0.toString().trim()))) {

                            errorMap.put(arg0.toString(),"A data value is required");

                            return null;
                        } else {

                            if (clazz.isInstance(dataObj.get(arg0.toString()))) {
                                return arg0;

                            } else {

                                errorMap.put(arg0,"Expecting data of type: "+clazz.getSimpleName());

                                return null;
                            }
                        }
                    } else {

                        return arg0;
                    }
                } catch (Exception e) {

                    errorMap.put(arg0,"Expecting data of type: "+JDBCMap.get(typeObj.getString(arg0.toString())));

                    return null;
                }
            }

        };

        String[] strResult=new String[typeObj.size()];

        ParallelArray<Object> dataList = ParallelArray.create(typeObj.size(), Object.class, forkExec);

        dataList.addAll(typeObj.keySet().toArray(strResult));
        dataList.removeNulls();

        ParallelArray<Object> dataCheck = dataList.withMapping(typeCheck).all();

        dataCheck.removeNulls();

        return errorMap;

     }

     public static JSONObject checkTypeValue(final JSONObject errorCodeObj,final JSONObject errorDescObj , final JSONObject fieldObj, final JSONObject typeObj, final JSONObject dataObj) {
        ForkJoinExecutor forkExec = ParallelArray.defaultExecutor();

        final JSONObject errorMap = new JSONObject();
        final JSONArray errorCodeList = new JSONArray();
        

        final Ops.Mapper typeCheck = new Ops.Mapper() {
            public Object map(Object arg0) {

                try {

                    Class clazz = JDBCMap.get(typeObj.getString(arg0.toString()));

                    /*** ONLY CHECK IF THE FIELD IS REQUIRED ***/
                    if ("required".equals(fieldObj.getString(arg0.toString()).trim().toLowerCase())) {

                        if (clazz.getSimpleName().equals("Short")) {
                            /*** TRY TO CAST THE dataObj VALUE ***/
                            return Short.valueOf(dataObj.get(arg0.toString()).toString());

                        } else if (clazz.getSimpleName().equals("String") && "".equals(dataObj.get(arg0.toString().trim()))) {

                            errorMap.put(arg0.toString(),errorDescObj.optString(arg0.toString()));
                            errorCodeList.add(errorCodeObj.optString(arg0.toString()));

                            return null;
                        } else {

                            if (clazz.isInstance(dataObj.get(arg0.toString()))) {
                                return arg0;

                            } else {

                                errorMap.put(arg0,"Expecting data of type: "+clazz.getSimpleName());
                                errorCodeList.add(errorCodeObj.optString(arg0.toString()));

                                return null;
                            }
                        }
                    } else {

                        return arg0;
                    }
                } catch (Exception e) {

                    errorMap.put(arg0,"Expecting data of type: "+JDBCMap.get(typeObj.getString(arg0.toString())));

                    errorCodeList.add(errorCodeObj.optString(arg0.toString()));

                    return null;
                }
            }

        };

        String[] strResult=new String[typeObj.size()];

        ParallelArray<Object> dataList = ParallelArray.create(typeObj.size(), Object.class, forkExec);

        dataList.addAll(typeObj.keySet().toArray(strResult));
        dataList.removeNulls();

        ParallelArray<Object> dataCheck = dataList.withMapping(typeCheck).all();

        dataCheck.removeNulls();

        if (errorCodeList.isEmpty()) {

            return errorMap;
        } else {

            errorMap.put("error_codes", errorCodeList);
        
            return errorMap;
        }

     }

     public static boolean checkType(List<String> nameList, final Map<String,Class> typeMap, final Map<String,Object> valueMap) {
        final ForkJoinPool pool = new ForkJoinPool(2);
        ForkJoinExecutor forkExec = ParallelArray.defaultExecutor();

        final Ops.Mapper typeCheck = new Ops.Mapper() {

            public Object map(Object arg0) {
                try {

                    if (typeMap.get(arg0).isInstance(valueMap.get(arg0))) {

                        return arg0;
                    } else {
                        Class clazz = typeMap.get(arg0);
                        Object obj = valueMap.get(arg0);

                        return clazz.cast(obj);
                    }

                } catch (Exception e) {
                    return null;
                }
            }

        };

        String[] strResult=new String[nameList.size()];

        ParallelArray<String> dataList = ParallelArray.create(nameList.size(), String.class, forkExec);

        dataList.addAll(typeMap.keySet().toArray(strResult));

        dataList.removeNulls();

        ParallelArray<Object> dataCheck = dataList.withMapping(typeCheck).all();

        dataCheck.removeNulls();

        if (dataCheck.size() == dataList.size())
            return Boolean.TRUE;
        else
            return Boolean.FALSE;

    }

     public static JSONObject jsonJDBCTransform(JSONObject obj) throws Exception {
         JSONObject transform = new JSONObject();

         Iterator keys = obj.keys();

         while (keys.hasNext()) {
            String key = keys.next().toString();

            transform.put(key, JDBCMap.get(key));
         }

         return transform;
     }
}
