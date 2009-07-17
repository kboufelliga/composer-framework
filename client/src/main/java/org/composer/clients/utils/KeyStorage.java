package org.composer.clients.utils;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;
import java.util.List;
import org.composer.beans.Store;

/**
 *
 * @author kboufelliga
 */
public enum KeyStorage {
    INSTANCE;

    private ObjectContainer storage;

    private KeyStorage() {
            storage = Db4o.openFile(".jsonhub.keys");
    }

    public void add(Store store) {
        storage.store(store);
        storage.commit();
    }

    public void deleteByKey(String key) {
        Store store = getByKey(key);

        if (store != null) {
            storage.delete(store);
            storage.commit();
        }
    }

    public void deleteByName(String name) {
        Store store = getByName(name);

        if (store != null) {
            storage.delete(store);
            storage.commit();
        }
    }

    public Store getByName(String name) {
        final String storeName = name;

        //if (storage.ext().isClosed()) {
        //    initialize();
        //}

        List<Store> list = storage.query(new Predicate<Store>() {
            public boolean match(Store store) {
                return store.getName().equals(storeName);
            }
        });

        if (list.size() > 0) {
            return list.get(0);
        }

        return null;
    }

    public Store getByKey(String key) {
        final String storeKey = key;

        List<Store> list = storage.query(new Predicate<Store>() {
            public boolean match(Store store) {
                return store.getKey().equals(storeKey);
            }
        });

        if (list.size() > 0) {
            return list.get(0);
        }

        return null;
    }
}
