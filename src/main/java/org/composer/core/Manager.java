package org.composer.core;

import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.composer.beans.RDFBean;
import org.composer.beans.RDFStore;
import org.composer.beans.DomainEntity;
import org.composer.beans.ContextEntity;
import org.composer.interfaces.Entity;

import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import com.db4o.query.Query;
import com.db4o.query.Evaluation;
import com.db4o.query.Candidate;
import com.db4o.config.annotations.Indexed;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Sep 6, 2008
 * Time: 12:54:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class Manager extends SimpleJdbcDaoSupport implements org.composer.interfaces.Metadata {

    private Log log = LogFactory.getLog(Manager.class);
    private static final Manager INSTANCE = new Manager();
    private ObjectContainer database;

    private DomainEntity domain = new DomainEntity("semantic-framework");
    private ContextEntity context = new ContextEntity("ComposerLab");

    private Manager() {
	}


    public static Manager getInstance() {
        return INSTANCE;
    }

    public void setDomain(DomainEntity domain) {
        log.info("setting domain to "+domain.name()+" from "+this.domain.name());
        this.domain = domain;
    }

    public void setContext(ContextEntity context) {
            this.context = context;
    }


    public void setDatabase(ObjectContainer container) {
        this.database = container;
    }

    public boolean add(RDFBean bean) {
        try {
            RDFStore store = new RDFStore();
            log.info("add bean to domain "+domain.name());
            store.setContext(context);
            store.setDomain(domain);
            store.setBean(bean);

            database.store(store);
            database.commit();
        
            return true;
        } catch (Exception e) {
            log.error("could not store binding: "+e);

            return false;
        } finally {
            database.close();
        }
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
        List<RDFBean> beanList = new ArrayList();

        log.info("reading from domain: "+domain.name());

        /*** SIMPLER ***
        List<RDFStore> storeList = database.query(new Predicate<RDFStore>() {
                            public boolean match(RDFStore store) {
                                return store.getContext().name().equals(context.name());
                            }
        });
        *** SIMPLER ***/

        Query query = database.query();
        query.constrain(RDFStore.class);
        query.constrain(new Evaluation() {
            public void evaluate(Candidate candidate) {
                RDFStore store = (RDFStore)candidate.getObject();
                candidate.include(store.getContext().name().equals(context.name()));
                candidate.include(store.getDomain().name().equals(domain.name()));
            }
        });

        ObjectSet<RDFStore> stores = query.execute();


        while (stores.hasNext()) {
            RDFStore store = stores.next();
            beanList.add(store.getBean());
        }

        return beanList;

        //return getSimpleJdbcTemplate().query(new StringBuilder().append("").append(" SELECT DISTINCT M.MEMBER_NO AS MBR_NO").append(" FROM STORE_PLUS_CHARGE SPC1 WITH (READUNCOMMITTED)").append(" INNER JOIN MEMBERS M WITH (READUNCOMMITTED) ").append(" ON M.MEMBER_NO = SPC1.MEMBER_NO").append(" INNER JOIN STORE S WITH (READUNCOMMITTED) ").append(" ON S.STORE_NO = SPC1.STORE_NO ").append(" WHERE SPC1.ORDER_NO = 0").append(" AND SPC1.START_DATE >= '1/1/2008'").append(" AND SPC1.AMOUNT > 0").append(" AND S.ACTIVE = 1").append(" AND S.PREMIUM = 1").append(" AND S.IS_SELFBUY = 0").append(" UNION ").append(" SELECT DISTINCT INV.MEMBER_NO AS MBR_NO").append(" FROM INVOICE_ITEM ITEM WITH (READUNCOMMITTED)").append(" INNER JOIN INVOICE INV WITH (READUNCOMMITTED)").append(" ON INV.INVOICE_NO = ITEM.INVOICE_NO").append(" INNER JOIN INVOICE_REFERENCE REF WITH (READUNCOMMITTED)").append(" ON REF.INVOICE_NO = ITEM.INVOICE_NO").append(" INNER JOIN STORE S WITH (READUNCOMMITTED)").append(" ON S.STORE_NO = REF.STORE_NO ").append(" WHERE INV.INVOICE_STATUS_NO IN (6,9) ").append(" AND GETDATE() >= DATEADD(M,1,ISNULL(INV.INVOICE_DATE,GETDATE()))").append(" AND REF.INVOICE_SEQ_NO = ITEM.INVOICE_SEQ_NO").append(" AND S.ACTIVE = 1").append(" AND S.PREMIUM = 1").toString(), new GetRDFBeanMapper());
    }


    private class GetStatusMapper implements ParameterizedRowMapper<Object> {
        public String mapRow(ResultSet rs, int rownb) throws SQLException {
	        System.out.println("status mapper: "+ rs.toString());
            return rs.toString();
        }
    }

    private class GetRDFBeanMapper implements ParameterizedRowMapper<RDFBean> {
        public RDFBean mapRow(ResultSet rs, int rownb) throws SQLException {
            RDFBean bean = new RDFBean(rs.getString("name"));

            return bean;
        }
    }
}