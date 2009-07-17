package org.composer.engine;

import org.composer.engine.json.JavaTransform;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author kboufelliga
 */
public class TestCheckTypes extends TestCase {

    public void testTransform() throws Exception {
       List<String> list = new ArrayList<String>();
       list.add("field_a");
       list.add("field_b");
       list.add("field_c");
       list.add("name");
       list.add("cc_number");
       list.add("date");

       Map<String,Class> typeMap = new HashMap<String,Class>();
       typeMap.put("name", String.class);
       typeMap.put("cc_number", Integer.class);
       typeMap.put("date", Date.class);

       Map<String,Object> valueMap = new HashMap<String,Object>();
       valueMap.put("name", "Some Name");
       valueMap.put("cc_number", 1234111111);
       valueMap.put("date", new Date());

       assertTrue(JavaTransform.checkType(list, typeMap, valueMap));

       List<Object> objList = JavaTransform.convert(list, typeMap, valueMap);

       for (Object obj: objList) {
           System.out.println(obj.getClass().getName());
       }
    }

}
