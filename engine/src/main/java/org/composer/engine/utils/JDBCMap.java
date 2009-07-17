package org.composer.engine.utils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kboufelliga
 */
public class JDBCMap {

    private final static Map<String,Class> map;

    static {
        map = new HashMap<String,Class>();
        map.put("VARCHAR",String.class);
        map.put("LONGVARCHAR",String.class);
        map.put("CLOB",java.sql.Clob.class);
        map.put("BIT",Boolean.class);
        map.put("BINARY",Byte.class);
        map.put("LONGVARBINARY",Byte.class);
        map.put("BLOB",java.sql.Blob.class);
        map.put("TINYINT",Short.class);
        map.put("SMALLINT",Short.class);
        map.put("INTEGER",Integer.class);
        map.put("BIGINT",Long.class);
        map.put("REAL",Float.class);
        map.put("DOUBLE",Double.class);
        map.put("DECIMAL",java.math.BigDecimal.class);
        map.put("DATE",java.sql.Date.class);
        map.put("TIME",java.sql.Time.class);
        map.put("TIMESTAMP",java.sql.Timestamp.class);
    }

    public static Class get(String jdbcType) {
        if (map.containsKey(jdbcType)) {
            return map.get(jdbcType);
        }

        return null;
    }
}
