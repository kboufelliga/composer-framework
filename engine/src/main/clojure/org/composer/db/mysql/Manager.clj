(ns org.composer.db.mysql.Manager
  (:gen-class
   :methods [
	    [addMetaStore [String String String] String]
	    [addDataStore [String String String] String]
	    [deleteMetaStore [String] String]
	    [deleteDataStore [String] String]
	    [getStoresMap [] clojure.lang.LazySeq]
	    [getPublicStores [] clojure.lang.LazySeq]
	    ]
   :state state
   :init constructor
   :constructors {[String int String String String] []})
  (:use clojure.set clojure.contrib.json.write clojure.contrib.sql))

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

(defn table-exists? [table]
   (let [query (str "SELECT COUNT(*) AS count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" table "'")]
     (with-connection db
     (with-query-results rs [query]
			 (if (== 1 (:count (first rs))) true false)))))

(defn store-type? [#^String store-key]
  (let [meta-table (str "META" store-key)
	data-table (str "DATA" store-key)
	metastore? (table-exists? meta-table)
	datastore? (table-exists? data-table)]
    (if metastore?
      (if datastore?
	"datastore"
	"metastore")
      (if datastore?
	"orphan"
	"does not exits"))))

(defn storesmap-table []
	  (clojure.contrib.sql/create-table
	   :storesmap
	   [:owner "varchar(255)"]
	   [:store_key "varchar(255)"]
	   [:store_type "varchar(255)"]
	   [:meta_store "boolean"]
	   [:data_store "boolean"]
	   ["CONSTRAINT uqid_storemap" "UNIQUE (store_key)"]))

(defn add-storesmap-table []
  (if-not (table-exists? "storesmap")
	  (clojure.contrib.sql/with-connection db
	  (clojure.contrib.sql/transaction (storesmap-table)))))

(defn -constructor [host port db-name user password]
  [[] (db-parms host port db-name user password)
  ;;(add-storesmap-table)
   ])

(defn store-table [#^String table-name]
	  (clojure.contrib.sql/create-table
	   table-name
	   [:name "varchar(255)"]
	   [:link "varchar(255)"]
	   [:reference "varchar(255)"]
	   [:identity "varchar(1000)"]
	   ["CONSTRAINT " (str "uqid_" table-name) "UNIQUE (identity)"]))

(defn add-store-table [#^String store-key]
  (let [table-name (str "META" store-key)]
    (if-not (table-exists? table-name)
	  (clojure.contrib.sql/with-connection db
	  (clojure.contrib.sql/transaction
	   (store-table table-name))))))

(defn datastore-table [#^String table-name]
  (clojure.contrib.sql/create-table
	   table-name
	   [:name "varchar(255)"]
	   [:link "varchar(255)"]
	   [:reference "varchar(255)"]
	   [:identity "varchar(1000)"]
	   ["CONSTRAINT " (str "uqid_" table-name) "UNIQUE (identity)"]))

(defn add-datastore-table [#^String store-key]
  (let [table-name (str "DATA" store-key)]
    (if-not (table-exists? table-name)
	  (clojure.contrib.sql/with-connection db
	  (clojure.contrib.sql/transaction
	   (datastore-table table-name))))))

(defn add-storesmap-key [#^String owner #^String store-key #^String store-type #^int meta_store #^int data_store]
  (let [lowner (.toLowerCase owner)
	ltype (.toLowerCase store-type)]
    (clojure.contrib.sql/insert-values
     :storesmap
     [:owner :store_key :store_type :meta_store :data_store]
     [lowner store-key ltype meta_store data_store])))

(defn provision-datastore [#^String owner #^String store-key #^String store-type]
  (let [lowner (.toLowerCase owner)
	ltype (.toLowerCase store-type)
	meta-table (str "META" store-key)
	data-table (str "DATA" store-key)
	store-type (store-type? store-key)]
    (if-not (.equals "datastore" store-type)
	  (try (jsonMsg "result" (first (clojure.contrib.sql/with-connection db
	       (clojure.contrib.sql/transaction (store-table meta-table) (datastore-table data-table) (add-storesmap-key lowner store-key ltype 1 1)))))
	  (catch Exception e ( jsonMsg "error" (str "failed to provision store for " owner " with store key " store-key))))
	  (jsonMsg "error" (str store-key " already provisioned!")))))

(defn provision-metastore [#^String owner #^String store-key #^String store-type]
  (let [lowner (.toLowerCase owner)
	ltype (.toLowerCase store-type)
	meta-table (str "META" store-key)]
    (if-not (table-exists? meta-table)
	  (try (jsonMsg "result" (first (clojure.contrib.sql/with-connection db
	       (clojure.contrib.sql/transaction (store-table meta-table) (add-storesmap-key lowner store-key ltype 1 0)))))
	  (catch Exception e ( jsonMsg "error" (str "failed to provision store for " owner " with store key " store-key))))
	  (jsonMsg "error" (str store-key " already provisioned!")))))

(defmacro add-store
  ([owner store-key store-type]
   `(provision-metastore ~owner ~store-key ~store-type))
  ([option owner store-key store-type]
     (let [option-wi-data# 'with-data]
	 `(if (= '~option '~option-wi-data#)
	    (provision-datastore ~owner ~store-key ~store-type)
	    (jsonMsg "error" (str '~option " is an invalid option! [valid options: with-data]"))))))

(defn -addMetaStore [this owner store-key store-type]
  (add-store owner store-key store-type))

(defn -addDataStore [this owner store-key store-type]
  (add-store with-data owner store-key store-type))

(defn delete-storesmap-key [#^String store-key]
  (clojure.contrib.sql/delete-rows
     :storesmap
     ["store_key=?" store-key]))

(defn terminate-datastore [#^String store-key]
  (let [meta-table (str "META" store-key)
	data-table (str "DATA" store-key)
	store-type (store-type? store-key)]
    (if (.equals "datastore" store-type)
	  (try (jsonMsg "result" (first (clojure.contrib.sql/with-connection db
	       (clojure.contrib.sql/transaction (clojure.contrib.sql/drop-table meta-table) (clojure.contrib.sql/drop-table data-table) (delete-storesmap-key store-key)))))
	  (catch Exception e ( jsonMsg "error" (str "failed to terminate store with key " store-key))))
	  (jsonMsg "error" (str store-key " is not a datastore but of type: " store-type)))))

(defn terminate-metastore [#^String store-key]
  (let [meta-table (str "META" store-key)
	store-type (store-type? store-key)]
    (if (.equals "metastore" store-type)
	  (try (jsonMsg "result" (first (clojure.contrib.sql/with-connection db
	       (clojure.contrib.sql/transaction (clojure.contrib.sql/drop-table meta-table) (delete-storesmap-key store-key)))))
	  (catch Exception e ( jsonMsg "error" (str "failed toterminate store with key " store-key))))
	  (jsonMsg "error" (str store-key " is not a metastore but of type: " store-type)))))

(defmacro delete-store
  ([store-key]
   `(terminate-metastore ~store-key))
  ([option store-key]
     (let [option-wi-data# 'with-data]
	 `(if (= '~option '~option-wi-data#)
	    (terminate-datastore ~store-key)
	    (jsonMsg "error" (str '~option " is an invalid option! [valid options: with-data]"))))))

(defn -deleteMetaStore [this store-key]
  (delete-store store-key))

(defn -deleteDataStore [this store-key]
  (delete-store with-data store-key))

(defn #^{:tag clojure.lang.LazySeq} -getStoresMap [this]
  (let [query "SELECT * FROM storesmap"]
    (with-connection db
		     (with-query-results rs [query] (doall rs)))))

(defn #^{:tag clojure.lang.LazySeq} -getPublicStores [this]
  (let [query "SELECT * FROM storesmap WHERE store_type = 'public'"]
    (with-connection db
		     (with-query-results rs [query] (doall rs)))))