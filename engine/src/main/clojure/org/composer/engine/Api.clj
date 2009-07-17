(ns org.composer.engine.Api
(:gen-class
  :methods [
  #^{:static true} [addMetaStore [String] void]
  #^{:static true} [addMetaStore [String String] void]
  #^{:static true} [addDataStore [String] void]
  #^{:static true} [addDataStore [String String] void]
  #^{:static true} [linkName [String String] void]
  #^{:static true} [linkName [String String String] void]
  #^{:static true} [addLink [String String] String]
  #^{:static true} [addLink [String String String] String]
  #^{:static true} [getUplinks [String] String]
  #^{:static true} [getUplinks [String String] String]
  #^{:static true} [addRef [String String String String] void]
  #^{:static true} [addRef [String String String] void]

;;-------------------------------------------------------------------------------------------------------------------------------------
;; DATA
;;-------------------------------------------------------------------------------------------------------------------------------------
  #^{:static true} [dataByLink [String] String]
  #^{:static true} [dataByLink [String String] String]
  #^{:static true} [dataByRef [String String] String]
  #^{:static true} [dataByRef [String String String] String]
  #^{:static true} [addData [String String String] String]
  #^{:static true} [addData [String String String String] String]
  #^{:static true} [getData [String] String]
  #^{:static true} [getData [String String] String]
  #^{:static true} [removeData [String String] void]
  #^{:static true} [removeData [String String String] void]
   ]
  :state state
  :constructors {[String int String String String] []}
  :init constructor)
  (:use clojure.set clojure.contrib.json.write)
  (:import (java.util.regex.Matcher)
           (java.util.regex.Pattern)
           (org.composer.db.mysql.Manager)
           (org.composer.db.mysql.MetaManager)
           (org.composer.db.mysql.DataManager))
  )

(clojure.core/refer 'clojure.core)

(def compositions (ref #{}))
(def metastore-map (ref #{}))
(def datastore-map (ref #{}))

(defstruct store-def :key :type :store)
(defstruct owner-def :key :store)
(defstruct composition-def :name :link :reference :identity)
(defstruct composition-owner-def :key :name :link :reference :identity)

(defn jsonMsg
  ([msg] (jsonMsg "message" msg))
  ([type msg] (str "{" (json-str type) ": " (json-str msg) "}")))

(defmacro get-agent [agent-name & parms]
  (let [dbm# 'dbm
         metam# 'metam
         datam# 'datam]
     `(if (= '~agent-name '~dbm#)
        (new org.composer.db.mysql.Manager ~@parms)
        (if (= '~agent-name '~metam#)
            (new org.composer.db.mysql.MetaManager ~@parms)
          (if (= '~agent-name '~datam#)
                (new org.composer.db.mysql.DataManager ~@parms))))))

(def db-manager (new org.composer.db.mysql.Manager "localhost" 3306 "composer" "composer" "cp1111"))
(def meta-manager (new org.composer.db.mysql.MetaManager "localhost" 3306 "composer" "composer" "cp1111"))
(def data-manager (new org.composer.db.mysql.DataManager "localhost" 3306 "composer" "composer" "cp1111"))

(def db-agent (agent db-manager))
(def meta-agent (agent meta-manager))
(def data-agent (agent data-manager))

(defmacro load-db-agents [& args]
  `(do (def db-manager (new org.composer.db.mysql.Manager ~@args))
       (def meta-manager (new org.composer.db.mysql.MetaManager ~@args))
       (def data-manager (new org.composer.db.mysql.DataManager ~@args))
       (def db-agent (agent db-manager))
       (def meta-agent (agent meta-manager))
       (def data-agent (agent data-manager))))

(defn add-metastore
  "create store for member with key k and mode m (m can be 'public' 'private' or 'group')"
  ([#^String store-key]
     (add-metastore store-key "private"))
  ([#^String store-key #^String store-type]
     (let [stl (.toLowerCase store-type)
	   store (select #(= (:key %) store-key) @metastore-map)]
       (if (empty? store)
	 (dosync (alter metastore-map conj (struct store-def store-key stl (agent #{}))))))))

(defn add-datastore
  "create store for member key k and mode m (m can be 'public' 'private' or 'custom')"
  ([#^String store-key]
     (add-datastore store-key "private"))
  ([#^String store-key #^String store-type]
     (let [stl (.toLowerCase store-type)
	   datastore (select #(= (:type %) stl) (select #(= (:key %) store-key) @datastore-map))]
       (if (empty? datastore)
	 (dosync
	  (alter datastore-map conj (struct store-def store-key stl (agent #{})))
	  (alter metastore-map conj (struct store-def store-key stl (agent #{}))))))))

(defn add-metastore-db [store-key store-type]
  (let [stl (.toLowerCase store-type)]
    (send-off db-agent (fn [dbmanager] (.addMetaStore dbmanager store-key store-key stl) dbmanager))
    (if (agent-errors db-agent) (do (clear-agent-errors db-agent) false) true)))

(defn add-datastore-db [store-key store-type]
  (let [stl (.toLowerCase store-type)]
    (send-off db-agent (fn [dbmanager] (.addDataStore dbmanager store-key store-key stl) dbmanager))
    (if (agent-errors db-agent) (do (clear-agent-errors db-agent) false) true)))

(defmacro add-store [option persists & args]
  (let [option-meta# 'meta
	option-data# 'data
	persists-wi-db# 'with-db
	persists-wo-db# 'without-db]
	 `(if (= '~option '~option-meta#)
	    (if (= '~persists '~persists-wo-db#)
	      (add-metastore ~@args)
	      (if (= '~persists '~persists-wi-db#)
		(dosync (add-metastore-db ~@args) (add-metastore ~@args))
		(jsonMsg "error" (str '~persists " is an invalid persistence type! [valid options: with-db, without-db]"))))
	    (if (= '~option '~option-data#)
	      (if (= '~persists '~persists-wo-db#)
		(add-datastore ~@args)
		(if (= '~persists '~persists-wi-db#)
		  (dosync (add-datastore-db ~@args) (add-datastore ~@args))
		  (jsonMsg "error" (str '~persists " is an invalid persistence type! [valid options: with-db, without-db]"))))
	      (jsonMsg "error" (str '~option " is an invalid option! [valid options: meta, data]"))))))

(defn -addMetaStore
  "create store for member with key k and mode m (m can be 'public' 'private' or 'group')"
  ([#^String store-key]
     (-addMetaStore store-key "private"))
  ([#^String store-key #^String store-type]
     (add-store meta with-db store-key store-type)))

(defn -addDataStore
  "create store for member key k and mode m (m can be 'public' 'private' or 'custom')"
  ([#^String store-key]
     (-addDataStore store-key "private"))
  ([#^String store-key #^String store-type]
     (add-store data with-db store-key store-type)))

;; create or load the general public repository
;;(-addMetaStore "general-public" "public")

;; not sure about the creation a general-private repository
;;(-addMetaStore "general-private" "private")

;; create owner store
;;(-addMetaStore "owner" "private")

(defn get-metastore
  "get member store... if not found add it ... not a good thing to add a side effect like this"
  ([#^String store-key]
     (let [store (select #(= (:key %) store-key) @metastore-map)]
       (if (empty? store)
	 (do (-addMetaStore store-key "private") (get-metastore store-key))
	 (:store (first store))))))

(defn get-datastore
  "get member store ... if not found add it ... warning of side effect that i can do without"
  ([#^String store-key]
     (let [store (select #(= (:key %) store-key) @datastore-map)]
       (if (empty? store)
	 (do (-addDataStore store-key) (get-datastore store-key))
	 (:store (first store))))))

(defn compose-metadata
  "compose link in the general public and/or public groups repositories"
  ([#^String definition]
     (compose-metadata "general-private" definition))
  ([#^String store-key #^String definition]
     (compose-metadata store-key definition "=" "." definition))
  ([#^String store-key #^String name #^String link #^String reference #^String identity]
     (let [nml (.toLowerCase name)
	   lkl (.toLowerCase link)
	   rfl (.toLowerCase reference)
	   idl (.toLowerCase identity)
	   store (get-metastore store-key)
	   duplicate (@store (struct composition-def nml lkl rfl idl))]
       (when (empty? duplicate)
	 (dosync (send store conj (struct composition-def nml lkl rfl idl)))))))

(defn add-metadata-db [store-key name link reference identity]
  (send-off meta-agent (fn [metamanager] (.insert metamanager store-key name link reference identity) metamanager))
  (if (agent-errors meta-agent) (do (clear-agent-errors meta-agent) false) true))

(defn compose-metadata-db
  "compose link in the general public and/or public groups repositories"
  ([#^String definition]
     (compose-metadata-db "general-private" definition))
  ([#^String store-key #^String definition]
     (compose-metadata-db store-key definition "=" "." definition))
  ([#^String store-key #^String name #^String link #^String reference #^String identity]
     (let [nml (.toLowerCase name)
	   lkl (.toLowerCase link)
	   rfl (.toLowerCase reference)
	   idl (.toLowerCase identity)
	   store (get-metastore store-key)
	   duplicate (@store (struct composition-def nml lkl rfl idl))]
       (when (empty? duplicate)
	 (dosync
	  (send store conj (struct composition-def nml lkl rfl idl))
	  (add-metadata-db store-key nml lkl rfl idl))))))

(defmacro compose-meta [persists & args]
  (let [persists-wi-db# 'with-db
	persists-wo-db# 'without-db]
	 `(if (= '~persists '~persists-wo-db#)
	      (compose-metadata ~@args)
	      (if (= '~persists '~persists-wi-db#)
		(compose-metadata-db ~@args)
		(jsonMsg "error" (str '~persists " is an invalid persistence type! [valid options: with-db, without-db]"))))))

(defn compose-link
  ([#^String name #^String link]
     (compose-link "general-public" name link))
  ([#^String store-key #^String name #^String link]
     (compose-link store-key name "." link))
  ([#^String store-key #^String name #^String linkname #^String link]
     (let [store (get-metastore store-key)]
       ;;make sure a ref does not already exists
       (when (empty? (select #(= (:name %) name) (select #(= (:link %) linkname) (select #(= (:reference %) "@") @store))))
	 (compose-meta with-db store-key name linkname link (str link "." name))))))

(defn compose-ref
  ([#^String name #^String ref]
     (compose-ref "general-public" name ref))
  ([#^String store-key #^String name #^String ref]
     (compose-ref store-key name "." ref))
  ([#^String store-key #^String name #^String linkname #^String ref]
     (let [store (get-metastore store-key)]
       ;; make sure a link does not already exists
       (when (empty? (select #(= (:name %) name) (select #(= (:link %) linkname) (select #(= (:reference %) ref) @store))))
	 (compose-meta with-db store-key name ref "@" (str name "@" ref))))))

(defn create-link
  ([#^String name #^String link]
     (create-link "general-public" name link))
  ([#^String store-key #^String name #^String link]
     (compose-meta with-db store-key name "." link (str link "." name))
     (.toLowerCase (str link "." name))))

(defn add-link
  "create a link from a set of words"
  ([#^String pathlist]
     (add-link "general-public" pathlist))
  ([#^String store-key #^String pathlist]
     ;;should add validation to only allow certain delimiters
     (let [pl (.toLowerCase pathlist)
	   lst (re-seq #"\w+" pl)]
       (letfn [(linkl [nm lt]
		 (if (empty? lt)
		   nm
		   (linkl (create-link store-key (first lt) nm) (rest lt))))]
	 (do (compose-meta with-db store-key (first lst) "=" "." (first lst)) (linkl (first lst) (rest lst)))))))

(defn -addLink
    ([#^String link]
        (add-link link))
    ([#^String store-key #^String link]
        (add-link store-key link)))

(defn -linkName
  ([#^String link #^String name]
     (add-link (str link "." name)))
  ([#^String store-key #^String link #^String name]
     (add-link store-key (str link "." name))))

;;TODO
;;(defn remove-link
;;  ([#^String link]
;;     (remove-link "general-public" link))
;;  ([#^String store-key #^String link]
;;     (if-not (.equals (-getUplink store-key link) "[]")
;;	     (if-not (-getData store-key link)
;;		     (let [store (get-store store-key)
;;			   lnks (select #(= (:link %) "=") (select #(= (:reference %) lnkl) @store))
;;			   lnks-refs (select #(= (:reference %) (str "@" lnkl)) (select #(= (:identity %) data) @store))
;;	   refs-data (select #(.equals (str (first (:reference %))) "@") (select #(= (:identity %) data ) (select #(= (:link %) lnkl) @store)))]
;;       (if (and (not (empty? lnks-data)) (not (empty? lnks-refs)))
	       ;; we need to check if there are references we need to delete as well
;;	        (doseq [item (union lnks-data lnks-refs)] (dosync (send store disj item)))
;;          (;; TODO

(defn get-dblinks+
  ([#^String prefix #^String link]
     (get-dblinks+ "general-public" prefix link))
  ([#^String store-key #^String prefix #^String link]
     (let [pfxl (.toLowerCase prefix)
	   lnkl (.toLowerCase link)
	   store (get-metastore store-key)
	   where-clause (str "link = '.' AND reference = '" lnkl "'")
	   rs (.search meta-manager store-key where-clause)]
       (if (= 1 (:count (first rs)))
	 (do
	  (map #(compose-meta without-db (:name %) (:link %) (:reference %) (:identity %)) rs)
	  (map #(conj {:prefix pfxl} %) rs))
	 nil))))

(defn get-links+
  ([#^String prefix #^String link]
     (get-links+ "general-public" prefix link))
  ([#^String store-key #^String prefix #^String link]
     (let [pfxl (.toLowerCase prefix)
	   lnkl (.toLowerCase link)
	   store (get-metastore store-key)
	   rs (select #(= (:link %) ".") (select #(= (:reference %) lnkl) @store))]
       (if (empty? rs)
            (get-dblinks+ store-key prefix link)
            (map #(conj {:prefix pfxl} %) rs)))))

(defn composition-get
  ([#^String id]
     (composition-get "general-public" id))
  ([#^String store-key #^String id]
     (let [idl (.toLowerCase id)
	   store (get-metastore store-key)]
       (select #(= (:identity %) idl) @store)))
  ([#^String store-key #^String link #^String property]
     (composition-get store-key (str link "." property))))

(defn get-uplinks
  ([#^String link]
     (get-uplinks "general-publlic" link))
  ([#^String store-key #^String link]
     (let [lnkl (.toLowerCase link)]
       (letfn [(acc_lnks [curr_lst next_lst]
		  (if (nil? next_lst)
		      curr_lst
		      (acc_lnks (cons (map #(str lnkl (if (= "" (:prefix %)) "." (str (:prefix %) ".")) (:name %)) next_lst) curr_lst) (reduce union (map #(get-links+ store-key (str (:prefix %) "." (:name %)) (:identity %)) next_lst)))))]
	      (trampoline acc_lnks () (get-links+ store-key "" lnkl))))))

(defn get-uplinks*
  ([#^String link]
     (get-uplinks* "general-public" link))
  ([#^String store-key #^String link]
     (let [lnkl (.toLowerCase link)
	   store (get-metastore store-key)]
       (union (reduce union (conj (get-uplinks store-key lnkl) (map #(:identity %) (composition-get store-key lnkl))))
	      (reduce union (map #(get-uplinks store-key (reduce str (rest (:link %)))) (select #(= (str (first (:link %))) "@") (select #(= (:reference %) lnkl) @store))))))))


(defn -getUplinks
  ([#^String link]
     (-getUplinks "general-public" link))
  ([#^String store-key #^String link]
     (str "[" (reduce str (interpose "," (map #(json-str %) (get-uplinks* store-key link)))) "]")))

(defn down-link
([#^String link]
   (let [lst (re-seq #"\w+" link)
	 dlst (drop-last lst)]
     (reduce str (interpose "." dlst)))))

(defn compositions-by-id
  ([#^String id]
     (compositions-by-id "general-public" id))
  ([#^String store-key #^String id]
     (let [idl (.toLowerCase id)
	   store (get-metastore store-key)]
       (select #(= (:identity %) idl) @store)))
  ([#^String store-key #^String link #^String property]
     (compositions-by-id store-key (str link "." property))))

 (defn compositions-by-link
  ([#^String ref]
     (compositions-by-link "general-public" ref))
  ([#^String store-key #^String ref]
     (let [refl (.toLowerCase ref)
	   store (get-metastore store-key)
	   lnks (select #(= (:link %) ".") (select #(= (:reference %) refl) @store))
	   root (if (.equals #^{:tag String} (down-link refl) "") (compositions-by-id store-key refl) nil)]
       (if root (union root lnks) lnks)))
  ([#^String store-key #^String ref #^String filter-id]
     (let [refl (.toLowerCase ref)
	   store (get-metastore store-key)]
	   (select #(= (:link %) ".") (select #(= (:reference %) refl) (select #(= (:identity %) filter-id) @store))))))

(defn compositions-by-ref
  ([#^String ref]
     (compositions-by-ref "general-public" ref))
  ([#^String store-key #^String ref]
     (let [refl (.toLowerCase ref)
	   link (str "@" refl)
	   store (get-metastore store-key)
	   refs (select #(and (not (= (:link %) "@schema")) (.equals (str (first (:link %))) "@")) (select #(= (:reference %) refl) @store))]
       refs))
  ([#^String store-key #^String ref #^String filter-id]
     (let [refl (.toLowerCase ref)
	   store (get-metastore store-key)
	   refs (select #(and (not (= (:link %) "@schema")) (.equals (str (first (:link %))) "@")) (select #(= (:reference %) refl) (select #(= (:identity %) filter-id) @store)))]
       refs)))

(defn add-ref
  ([#^String store-key #^String ref #^String target-ref #^String property]
     ;;assume the name of the ref is to be used
     (let [recd (compositions-by-id store-key ref)
	   trecd (compositions-by-id store-key target-ref)]
       (if (empty? recd)
	  (jsonMsg "error" (str ref "does not exists"))
	 (if (empty? trecd)
	   (add-link store-key target-ref)
	   ;; check that we don't have an existing target id
	   (if (empty? (compositions-by-id store-key (str target-ref "." property)))
	     (compose-meta with-db store-key property (str "@" ref)  target-ref (str target-ref "." property))
	     (jsonMsg "warning" "identity already exists")
	     )))))
  ([#^String store-key #^String ref #^String target-ref]
     (let [recd (compositions-by-id store-key ref)
	   trecd (compositions-by-id store-key target-ref)]
       (if (empty? recd)
	 (jsonMsg "error" (str ref "does not exists"))
	 (do
	   (if (empty? trecd)
	     (add-link store-key target-ref))
	   ;; check that we don't have an existing target
	   (let [property (:name (first recd))]
	     (if (empty? (compositions-by-id store-key (str target-ref "." property)))
	       (compose-meta with-db store-key property (str "@" ref)  target-ref (str target-ref "." property))
	       (jsonMsg "warning" "identity already exists")
	       )))))))

(defn schema-set
  ([#^String link #^String property #^String requirement]
     (schema-set "general-public" link property requirement))
  ([#^String store-key #^String link #^String property #^String requirement]
     (let [lnkl (.toLowerCase link)
	   propl (.toLowerCase property)
	   reql (.toLowerCase requirement)
	   store (get-metastore store-key)
	   idl (str lnkl "." propl)
	   rs (select #(= (:identity %) idl) (compositions-by-link store-key lnkl))]
     (if (= 1 (count rs))
	 (compose-meta with-db store-key propl "@schema" idl reql)
       (str " could not find field " link "." property)))))

(defn schema-get
  ([#^String id]
     (schema-get "general-public" id))
  ([#^String store-key #^String id]
     (let [idl (.toLowerCase id)
	   store (get-metastore store-key)]
       (if-not (empty? (composition-get store-key idl))
	       (let [recd (select #(= (:link %) "@schema") (select #(= (:reference %) idl) @store))]
		 (if-not (empty? recd)
			 ((first recd) :identity)
			 (str "optional")))
	(str "property does not exists!"))))
  ([#^String store-key #^String link #^String property]
     (schema-get store-key (str link "." property))))

(defn schema-del
  ([#^String link #^String property]
     (schema-del "general-public" link property))
  ([#^String store-key #^String link #^String property]
     (let [lnkl (.toLowerCase link)
	   propl (.toLowerCase property)
	   store (get-metastore store-key)
	   refl (str lnkl "." propl)
	   recs (select #(= (:link %) "@schema") (select #(= (:reference %) propl) @store))]
       (if-not (empty? recs)
	       (dosync (send store disj (struct composition-def propl "@schema" refl (:identity (first recs)))))
	       "not found"))))

(defn schema-get*
  ([#^String link]
     (schema-get* "general-public" link))
  ([#^String store-key #^String link]
     (let [lnkl (.toLowerCase link)
	   recds (compositions-by-link store-key lnkl)]
       (if-not (empty? recds)
	       (let [refs (compositions-by-ref store-key lnkl)
		     wi-schema (select #(= (:link %) "@schema") refs)
		     wo-schema (select #(= (:link %) ".") refs)]
		 (union (map #(str (:name %) ":" (:identity %)) wi-schema) (map #(str (:name %) ":optional") wo-schema)))
	       (str "not found")))))

;;-------------------------------------------------------------------------------------------------------------------------------------
;; DATA
;;-------------------------------------------------------------------------------------------------------------------------------------

(defn add-storedata [store-key name link reference identity]
  (let [nml (.toLowerCase name)
	lkl (.toLowerCase link)
	rfl (.toLowerCase reference)
	idl (.toLowerCase identity)
	store (get-datastore store-key)
	duplicate (@store (struct composition-def nml lkl rfl idl))]
    (when  (empty? duplicate)
      (dosync (send store conj (struct composition-def nml lkl rfl idl))))))

(defn add-storedata-db [store-key name link reference identity]
  (send-off data-agent (fn [datamngr] (.insert datamngr store-key name link reference identity) datamngr))
  (if (agent-errors data-agent) (do (clear-agent-errors data-agent) false) true))

(defn compose-data
  "compose link in the general public and/or public groups repositories"
  ([#^String definition]
     (compose-data "general-private" definition))
  ([#^String store-key #^String definition]
     (compose-data store-key definition "=" "." definition))
  ([#^String store-key #^String name #^String link #^String reference #^String identity]
     (add-storedata store-key name link reference identity)))

(defn compose-data-db
  "compose link in the general public and/or public groups repositories"
  ([#^String definition]
     (compose-data-db "general-private" definition))
  ([#^String store-key #^String definition]
     (compose-data-db store-key definition "=" "." definition))
  ([#^String store-key #^String name #^String link #^String reference #^String identity]
     (let [nml (.toLowerCase name)
	   lkl (.toLowerCase link)
	   rfl (.toLowerCase reference)
	   idl (.toLowerCase identity)
	   store (get-datastore store-key)
	   duplicate (@store (struct composition-def nml lkl rfl idl))]
       (when (empty? duplicate)
	 (dosync
	  (send store conj (struct composition-def nml lkl rfl idl))
	  (add-storedata-db store-key nml lkl rfl idl))))))

(defmacro compose-storedata [persists & args]
  (let [persists-wi-db# 'with-db
	persists-wo-db# 'without-db]
	 `(if (= '~persists '~persists-wo-db#)
	      (compose-data ~@args)
	      (if (= '~persists '~persists-wi-db#)
		(compose-data-db ~@args)
		(jsonMsg "error" (str '~persists " is an invalid persistence type! [valid options: with-db, without-db]"))))))

(defn data-by-link
  ([#^String link]
     (data-by-link "general-public" link))
  ([#^String store-key #^String link]
     (let [lnkl (.toLowerCase link)
	   store (get-datastore store-key)
	   data (select #(= (:reference %) "=") (select #(= (:link %) lnkl) @store))]
       (str lnkl ":[" (:identity (first data)) (reduce str (map #(str "," (:identity %)) (rest data))) "]"))))

(defn data-by-ref
  ([#^String link #^String reference]
     (data-by-ref "general-public" link reference))
  ([#^String store-key #^String link #^String reference]
     (let [lnkl (.toLowerCase link)
	   refl (.toLowerCase reference)
	   store (get-datastore store-key)
	   data (select #(= (:reference %) lnkl) (select #(= (:link %) refl) @store))]
       (str refl ":[" (:identity (first data)) (reduce str (map #(str "," (:identity %)) (rest data))) "]"))))

(defn add-data
  ([#^String store-key #^String link #^String data]
     (let [lnklst (re-seq #"\w+" link)
	   property (last lnklst)]
       (if (= (count lnklst) 1)
	 (add-data store-key "=" property data)
	 (add-data store-key (reduce str (interpose "."(drop-last lnklst))) property data))))
  ([#^String store-key #^String link #^String property #^String data]
     ;; check the lnk has one value
     (let [lnkl (.toLowerCase link)
	   propl (.toLowerCase property)
	   refl (if (.equals lnkl "=") "." (str lnkl "." propl))
	   store (get-metastore store-key)
	   metadata (select #(= (:name %) propl) (select #(= (:reference %) lnkl) @store))
	   link-type (first (:link (first metadata)))]

       ;; make sure the meta-data is not a reference
       (if (.equals (str link-type) "@")
		(dosync (compose-storedata with-db store-key propl (:identity (first metadata)) (:link (first metadata)) data)
			(compose-storedata with-db store-key propl (apply str (rest (:link (first metadata)))) "=" data))
		(if (.equals (str link-type) ".")
		  (compose-storedata with-db store-key (:name (first metadata)) (:identity (first metadata)) "=" data)
		  (if  (.equals (str link-type) "=")
		    (compose-storedata with-db store-key propl propl link-type data)
		    (jsonMsg "error" (json-str (str lnkl " does not exists!")))))))))

(defn -addData
  ([#^String store-key #^String link #^String data]
     (add-data store-key link data))
  ([#^String store-key #^String link #^String property #^String data]
     (add-data store-key link property data)))

(defn -getData
  ([#^String link]
     (-getData "general-public" link))
  ([#^String store-key #^String link]
     (let [lnkl (.toLowerCase link)
	   store (get-datastore store-key)
	   #^{:tag String} dlnk (down-link lnkl)
	   lnks (union (compositions-by-link store-key lnkl) (if (not (.equals dlnk "")) (compositions-by-link store-key dlnk lnkl) nil))
	   refs (union (compositions-by-ref store-key lnkl) (if (not (.equals dlnk "")) (compositions-by-ref store-key dlnk lnkl) nil))]
       (if (and (not (empty? refs)) (not (empty? lnks)))
	       (jsonMsg "object" (reduce str (interpose "," (union
		                  (map #(data-by-link store-key (:identity %)) lnks)
				  (map #(data-by-ref store-key (:link %) (:identity %)) refs)))))
	       (if-not (empty? refs)
		       (jsonMsg "object" (reduce str (interpose "," (map #(data-by-ref store-key (:link %) (:identity %)) refs))))
		       (if-not (empty? lnks)
			       (jsonMsg "object" (reduce str (interpose "," (map #(data-by-link store-key (:identity %)) lnks))))))))))

(defn remove-data [store-key link data]
  ;; if link has refs then remove applies to the refs as well
  (let [lnkl (.toLowerCase link)
	   store (get-datastore store-key)
	   lnks-data (select #(= (:reference %) "=") (select #(= (:identity %) data) (select #(= (:link %) lnkl) @store)))
	   refs-data (select #(.equals (str (first (:reference %))) "@") (select #(= (:identity %) data ) (select #(= (:link %) lnkl) @store)))
	   lnks-refs (select #(= (:reference %) (str "@" lnkl)) (select #(= (:identity %) data) @store))]
       (if (and (not (empty? lnks-data)) (not (empty? lnks-refs)))
	       ;; we need to check if there are references we need to delete as well
	 (doseq [item (union lnks-data lnks-refs)] (dosync (send store disj item)))
	 (if-not (empty? lnks-data)
		 (doseq [item lnks-data] (dosync (send store disj item)))
		 (if-not (empty? refs-data)
			 (doseq [item refs-data] (dosync (send store disj item)))
			 (jsonMsg "error" (str lnkl "/" data " ... not found!")))))))

(defn delete-data-db [store-key name link reference identity]
  (send-off data-agent (fn [datamngr] (.delete datamngr store-key name link reference identity) datamngr))
  (if (agent-errors data-agent) (do (clear-agent-errors data-agent) false) true))

(defn remove-data-db [store-key link data]
  ;; if link has refs then remove applies to the refs as well
  (let [lnkl (.toLowerCase link)
	   store (get-datastore store-key)
	   lnks-data (select #(= (:reference %) "=") (select #(= (:identity %) data) (select #(= (:link %) lnkl) @store)))
	   refs-data (select #(.equals (str (first (:reference %))) "@") (select #(= (:identity %) data ) (select #(= (:link %) lnkl) @store)))
	   lnks-refs (select #(= (:reference %) (str "@" lnkl)) (select #(= (:identity %) data) @store))]
       (if (and (not (empty? lnks-data)) (not (empty? lnks-refs)))
	       ;; we need to check if there are references we need to delete as well
	 (doseq [item (union lnks-data lnks-refs)] (dosync (send store disj item) (delete-data-db store-key (:name item) (:link item) (:reference item) (:identity item))))
	 (if-not (empty? lnks-data)
		 (doseq [item lnks-data] (dosync (send store disj item) (delete-data-db store-key (:name item) (:link item) (:reference item) (:identity item))))
		 (if-not (empty? refs-data)
			 (doseq [item refs-data] (dosync (send store disj item) (delete-data-db store-key (:name item) (:link item) (:reference item) (:identity item))))
			 (jsonMsg "error" (str lnkl "/" data " ... not found!")))))))

(defmacro remove-storedata [option & args]
  (let [option-wi-db# 'with-db
	option-wo-db# 'without-db]
	 `(if (= '~option '~option-wo-db#)
	      (remove-data ~@args)
	      (if (= '~option '~option-wi-db#)
		(remove-data-db ~@args)
		(jsonMsg "error" (str '~option " is an invalid option! [valid options: with-db, without-db]"))))))

(defn -removeData
  ([#^String link #^String data]
     (-removeData "general-public" link data))
  ([#^String store-key #^String link #^String data]
     (remove-storedata with-db store-key link data)))

(defn init-store-db [store]
  (let [datastore? (:data_store store)
	store-key (:store_key store)
	store-type (:store_type store)]
    (if (= datastore? true)
      (add-store data without-db store-key store-type)
      (add-store meta without-db store-key store-type))))

(defn init-store-data [store-key]
  (let [data-set (.read db-manager store-key)]
    (when-not (empty? data-set)
      (doseq [recd data-set]
	(compose-storedata without-db store-key (:name recd) (:link recd) (:reference recd) (:identity recd))))))

;;(defn load-stores [] (send-off db-agent (fn [dbmngr] (map #(init-store-db dbmngr %) (.getStoresMap dbmngr)) dbmngr)))
(defn load-stores [] (map #(init-store-db %) (.getStoresMap db-manager)))
(defn load-stores-data [] (map #(init-store-data (:key %)) @datastore-map))

(defn -constructor [host port dbname user password]
  [ []
    ;;(load-db-agents host port dbname user password)
    (load-stores)
    (load-stores-data)
   ])

(defn -addRef
  ([#^String store-key #^String reference #^String target-ref #^String property]
    (add-ref store-key reference target-ref property))
  ([#^String store-key #^String reference #^String target-ref]
    (add-ref store-key reference target-ref)))

(defn -dataByLink
  ([#^String link]
     (data-by-link link))
  ([#^String store-key #^String link]
     (data-by-link store-key link)))

(defn -dataByRef
  ([#^String link #^String reference]
     (data-by-ref link reference))
  ([#^String store-key #^String link #^String reference]
     (data-by-ref store-key link reference)))
