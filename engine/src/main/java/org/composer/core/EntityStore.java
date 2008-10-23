package org.composer.core;

import com.db4o.ObjectContainer;
import com.db4o.Db4o;
import com.db4o.query.Predicate;
import org.composer.beans.RDFEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.Transformer;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 20, 2008
 * Time: 4:08:07 PM
 * To change this template use File | Settings | File Templates.
 */
public enum EntityStore {
    INSTANCE;
    private Log log = LogFactory.getLog(EntityStore.class);
    private Map lookupCache;
    private final int LRU_SIZE = 250;
    private ObjectContainer database;
    private String defaultUri;

    public String getDefaultUri() {
        return defaultUri;
    }

    protected void initialize() {
        database = Db4o.openFile(Properties.DEFAULT_INTERNAL_DBNAME.value());
        lookupCache = new LRUMap(LRU_SIZE);
        lookupCache = LazyMap.decorate(lookupCache,getPrefixMapper());
    }
    
    private Transformer getPrefixMapper() {
        Transformer prefixMapper = new Transformer( ) {
                public Object transform(Object object) {
                    final String prefixName = (String)object;

                    RDFEntity entity = search(prefixName);

                    if (entity == null) {
                            entity = new RDFEntity();
                            entity.setName(prefixName);
                            entity.setUri(getDefaultUri());

                            store(entity);
                    }

                    return entity;
                }
        };

        return prefixMapper;
    }
    
    public boolean lookupKey(Object key) {
        return lookupCache.containsKey(key);
    }
    
    public Object get(Object key, String defaultUri) {
        this.defaultUri = defaultUri;
        return lookupCache.get(key);
    }
    
    protected void store(RDFEntity entity) {
        try {
            database.store(entity);
            database.commit();
        } catch (Exception e) {
            log.error("could not store prefix mapping pair: "+entity.getName()+"\n Error Description: ");
            e.printStackTrace();
        } finally {
            database.close();
        }
    }

    protected void delete(RDFEntity entity) {
        try {
            database.delete(entity);
            database.commit();
        } catch (Exception e) {
            log.error("could not delete prefix mapping pair: "+entity.getName()+"\n Error Description: ");
            e.printStackTrace();
        } finally {
            database.close();
        }
    }

     protected void update(String prefix, String uri) {
         this.defaultUri = uri;

         try {
            RDFEntity entity = search(prefix);
            if (entity == null) {
                entity = new RDFEntity();
                entity.setUri(uri);
                entity.setName(prefix);
            } else {
                entity.setUri(uri);

                if (lookupCache.containsKey(prefix)) {
                    lookupCache.remove(prefix);
                }
            }

            database.commit();
        } catch (Exception e) {
            log.error("could not store prefix mapping pair: "+prefix+"\n Error Description: ");
            e.printStackTrace();
        } finally {
            database.close();
        }
    }

    protected RDFEntity search(String prefixName) {
      final String prefix = prefixName;

        RDFEntity entity = null;

        if (database.ext().isClosed()) {
            database = Db4o.openFile(Properties.DEFAULT_INTERNAL_DBNAME.value());
        }

        List<RDFEntity> list = database.query(new Predicate<RDFEntity>() {
                                                    public boolean match(RDFEntity rdfEntity) {
                                                        return rdfEntity.getName().equals(prefix);
                                                    }
                                              });
        if (list.size() > 0) {
            entity = list.get(0);
        }

        return entity;
    }


}
