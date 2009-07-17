(ns org.composer.db.mysql.DataManager
  (:gen-class
   :methods [
	    [insert [String String String String String] String]
	    [delete [String String String String String] String]
	    [read [String] clojure.lang.LazySeq]
	    [search [String String] clojure.lang.LazySeq]
	    ]
   :state state
   :constructors {[String int String String String] []}
   :init constructor)
  (:use clojure.set clojure.contrib.json.write clojure.contrib.sql)
 )

(clojure.core/refer 'clojure.core)

(def db nil)

(defn jsonMsg
  ([msg] (jsonMsg "message" msg))
  ([type msg] (str "{" (json-str type) ": " (json-str msg) "}")))

(defmacro db-parms [host port db-name user password]
  `(def db {:classname "com.mysql.jdbc.Driver"
	   :subprotocol "mysql"
	   :subname (str "//" ~host ":" ~port "/" ~db-name)
	   :user ~user
	   :password ~password}))

(defn -constructor [host port db-name user password]
  [[] (db-parms host port db-name user password)])

(defn table-name [#^String store-key]
  (str "DATA" store-key))

(defn insert-data [#^String store-key #^String nm #^String link #^String reference #^String id]
  (clojure.contrib.sql/insert-values
     (table-name store-key)
     [:name :link :reference :identity]
     [nm link reference id]))

(defn delete-data [#^String store-key #^String nm #^String link #^String reference #^String id]
  (clojure.contrib.sql/delete-rows
     (table-name store-key)
     ["name=? and link=? and reference=? and identity=?" nm link reference id]))

(defn -insert [this store-key nm link reference id]
  (try (jsonMsg "result" (first (clojure.contrib.sql/with-connection db
	       (clojure.contrib.sql/transaction (insert-data store-key nm link reference id)))))
  (catch Exception e ( jsonMsg "error" (str "failed to insert data for store with key " store-key)))))

(defn -delete [this store-key nm link reference id]
  (try (jsonMsg "result" (first (clojure.contrib.sql/with-connection db
	       (clojure.contrib.sql/transaction (delete-data store-key nm link reference id)))))
  (catch Exception e ( jsonMsg "error" (str "failed to delete data for store with key " store-key " ERROR: " (.toString e))))))

(defn #^{:tag clojure.lang.LazySeq} -read [this store-key]
  (let [query (str "SELECT * FROM " (table-name store-key))]
    (with-connection db
		     (with-query-results rs [query] (doall rs)))))

(defn #^{:tag clojure.lang.LazySeq} -search [this store-key where-clause]
  (let [query (str "SELECT * FROM " (table-name store-key) " WHERE " where-clause)]
    (with-connection db
		     (with-query-results rs [query] (doall rs)))))