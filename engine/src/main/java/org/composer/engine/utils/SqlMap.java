package org.composer.engine.utils;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import java.io.Reader;

/**
 *
 * @author kboufelliga
 */
public class SqlMap {
    private static final SqlMapClient sqlMap;

    static {
        try {
             String resource = "ibatis.xml";
             Reader reader = Resources.getResourceAsReader (resource);
             sqlMap =  SqlMapClientBuilder.buildSqlMapClient(reader);
         } catch (Exception e) {
             // If you get an error at this point, it doesn’t matter what it was.  It is going to be
             // unrecoverable and we will want the app to blow up hard so we are aware of the
             // problem.  You should always log such errors and re-throw them in such a way that
             // you can be made immediately aware of the problem.
             e.printStackTrace();
             throw new RuntimeException("error initializing sqlmap"+e);
         }
    }

    public static SqlMapClient getSqlMapInstance () {
        return sqlMap;
    }


}
