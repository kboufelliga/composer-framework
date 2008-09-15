package org.composer.core;

import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.composer.beans.RDFBean;

import java.util.Map;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Sep 6, 2008
 * Time: 12:54:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReaderManager extends SimpleJdbcDaoSupport implements org.composer.interfaces.Metadata {

    private Log log = LogFactory.getLog(ReaderManager.class);
    private static final ReaderManager INSTANCE = new ReaderManager();

    private ReaderManager() {
	}


    public static ReaderManager getInstance() {
        return INSTANCE;
    }

    public boolean add() {
        return false;
    }
    
    public boolean read() {
        return false;
    }

    public boolean read(RDFBean bean) {
            return false;
        }

    public boolean update() {
    return false;
    }

    public boolean update(String name) {
            return false;
    }

    public boolean delete() {
    return false;
    }

    public boolean delete(String name) {
            return false;
        }

    public List<RDFBean> getAll(String type) {
        return getSimpleJdbcTemplate().query(new StringBuilder().append("").append(" SELECT DISTINCT M.MEMBER_NO AS MBR_NO").append(" FROM STORE_PLUS_CHARGE SPC1 WITH (READUNCOMMITTED)").append(" INNER JOIN MEMBERS M WITH (READUNCOMMITTED) ").append(" ON M.MEMBER_NO = SPC1.MEMBER_NO").append(" INNER JOIN STORE S WITH (READUNCOMMITTED) ").append(" ON S.STORE_NO = SPC1.STORE_NO ").append(" WHERE SPC1.ORDER_NO = 0").append(" AND SPC1.START_DATE >= '1/1/2008'").append(" AND SPC1.AMOUNT > 0").append(" AND S.ACTIVE = 1").append(" AND S.PREMIUM = 1").append(" AND S.IS_SELFBUY = 0").append(" UNION ").append(" SELECT DISTINCT INV.MEMBER_NO AS MBR_NO").append(" FROM INVOICE_ITEM ITEM WITH (READUNCOMMITTED)").append(" INNER JOIN INVOICE INV WITH (READUNCOMMITTED)").append(" ON INV.INVOICE_NO = ITEM.INVOICE_NO").append(" INNER JOIN INVOICE_REFERENCE REF WITH (READUNCOMMITTED)").append(" ON REF.INVOICE_NO = ITEM.INVOICE_NO").append(" INNER JOIN STORE S WITH (READUNCOMMITTED)").append(" ON S.STORE_NO = REF.STORE_NO ").append(" WHERE INV.INVOICE_STATUS_NO IN (6,9) ").append(" AND GETDATE() >= DATEADD(M,1,ISNULL(INV.INVOICE_DATE,GETDATE()))").append(" AND REF.INVOICE_SEQ_NO = ITEM.INVOICE_SEQ_NO").append(" AND S.ACTIVE = 1").append(" AND S.PREMIUM = 1").toString(), new GetRDFBeanMapper());
    }


    private class GetStatusMapper implements ParameterizedRowMapper<Object> {
        public String mapRow(ResultSet rs, int rownb) throws SQLException {
	        System.out.println("status mapper: "+ rs.toString());
            return rs.toString();
        }
    }

    private class GetRDFBeanMapper implements ParameterizedRowMapper<RDFBean> {
        public RDFBean mapRow(ResultSet rs, int rownb) throws SQLException {
            RDFBean bean = new RDFBean();
            bean.setName(rs.getString("name"));

            return bean;
        }
    }
}