(ns movement.util
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :as pp])
  (:import datomic.Util))

;; Create database and create a connection.
(def uri "datomic:dev://localhost:4334/movement2")
(d/create-database uri)
(def conn (d/connect uri))

;; Locate data sources.
(def schema-tx (first (Util/readAll (io/reader (io/resource "data/schema.edn")))))
(def pulling-tx (first (Util/readAll (io/reader (io/resource "data/movements/pulling.edn")))))
(def pushing-tx (first (Util/readAll (io/reader (io/resource "data/movements/pushing.edn")))))
(def conditioning-tx (first (Util/readAll (io/reader (io/resource "data/movements/conditioning.edn")))))
(def core-tx (first (Util/readAll (io/reader (io/resource "data/movements/core.edn")))))
(def lowerbody-tx (first (Util/readAll (io/reader (io/resource "data/movements/lowerbody.edn")))))
(def mobility-tx (first (Util/readAll (io/reader (io/resource "data/movements/mobility.edn")))))
(def multiplane-tx (first (Util/readAll (io/reader (io/resource "data/movements/multiplane.edn")))))
(def sass-tx (first (Util/readAll (io/reader (io/resource "data/movements/sass.edn")))))
(def template-tx (first (Util/readAll (io/reader (io/resource "data/movements/template.edn")))))

;; Transact data to the database.
(d/transact conn schema-tx)
(d/transact conn pulling-tx)
(d/transact conn pushing-tx)
(d/transact conn conditioning-tx)
(d/transact conn core-tx)
(d/transact conn lowerbody-tx)
(d/transact conn mobility-tx)
(d/transact conn multiplane-tx)
(d/transact conn sass-tx)
(d/transact conn template-tx)

;; Get the database value.
(def db (d/db conn))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; get all movements names
(defn all-movement-names []
  (d/q '[:find [?name ...]
         :where [_ :movement/name ?name]]
       db))
(count (all-movement-names))

; get all category names + number of movements in category
(defn all-categories-sorted []
  (reverse
    (sort
      (d/q '[:find (count ?m) ?name
             :where
             [?cat :category/name ?name]
             [?m :movement/category ?cat]]
           db))))
(all-categories-sorted)

; get specific category
(defn get-category [name]
  (d/q '[:find ?e
         :in $ ?category
         :where
         [?e :category/name ?category]]
       db
       name))
(get-category "Mobility")

; get movements from specific category
(defn get-movements [category]
  (d/q '[:find [?name ...]
         :in $ ?category-name
         :where
         [?c :category/name ?category-name]
         [?e :movement/category ?c]
         [?e :movement/name ?name]]
       db
       category))
(get-movements "Muscle Up")

; get all template titles
(defn all-template-titles []
  (d/q '[:find [?t ...]
         :where [_ :template/title ?t]]
       db))

(all-template-titles)

; get specific template
(defn get-template [title]
  (d/q '[:find (pull ?e [*])
         :in $ ?title
         :where
         [?e :template/title ?title]]
       db
       title))
(:template/description (ffirst (get-template "Strength")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; query for all equipment names
(d/q '[:find ?name
       :where [?c :equipment/name ?name]]
     db)

#_(pp/pprint *1)

; Looking up unique value with the pull api
; The [:ns ""] vector is a "look-up ref". Anywhere in datomic where
; an entity is supposed to be provided, a look-up ref can be used instead.
; This let's us avoid dealing with entities. The attribute value must be unique.
(d/pull db '[] [:equipment/name "Rings"])

; There are three different ways of referring to an entity in datomic.
; By it's id
(d/pull db '[*] 17592186045430)
; By look-up ref
(d/pull db '[*] [:category/name "Pushing"])
; Directly by it's programmtic name (if it has one, this does not.)
(d/pull db '[*] :category/pushing)

; Four ways of getting data from Datomic:
; datalog (declarative)
; pull (declarative)
; entities (lazy navigation)
; raw indexes

; Datalog is a cousin of Prolog. Uses positional pattern matching.
; Data in Datomic is always stored as datoms; a five-tuple. This
; fits positional pattern matching and makes it usable and powerful.

; A data pattern (list) both constrains results and binds variables:
; [?id :movement/name ?name]
; entitiy(variable) attribute value(variable)

; Find a single entitiy attribute value, using a constant for the entity:
; [17592186045430 :movement/name ?name]

; Find attributes of entity 42, using truncated data pattern.
; [42 ?attribute]
; The data pattern is positional, so trailing parts can be left off.
; So far, transaction and operation has been left off from the full data pattern: [e a v t op].

; Find attributes and values of entity 42 (everything we know about 42):
; [42 ?attribute ?value]

; Data patterns are used in a :where clause. The :find clause says which variables to return.
; Any time a variable appears in more than one data pattern, it causes a join.

; The :in clause names inputs so they can be referred to elsewhere in the query.
; Used in parameterized queries: "Find a customer by email":
; q([:find ?customer
;    :in $database ?email
;    :where [$database ?customer :email ?email]],
;    db,
;    "joe@example.com");

; It's idiomatic to make the database name really short.
; q([:find ?customer
;    :in $ ?email
;    :where [$ ?customer :email ?email]],
;    db,
;    "joe@example.com");

; In fact, it's idiomatic to leave it out of data patterns.
; q([:find ?customer
;    :in $ ?email
;    :where [?customer :email ?email]],
;    db,
;    "joe@example.com");

; Predicates are functional constraints that appear in the :where clause.
; [:find ?item
;  :where [?item :item/price ?price]
;         [(< 50 ?price)]]

; You can also call arbitrary functions. Functions take bound variables as inputs,
; and bind variables with output.
;[(shipping ?zip ?weight) ?cost]

;;;;; REPL safety and convenience ;;;;;;
#_(set! *print-length* 250)

;;;;; data queries ;;;;;;

; find all categories a movement belongs to
(d/q '[:find ?category-name
       :in $ ?movement-name
       :where
       [?e :movement/name ?movement-name]
       [?e :movement/category ?category]
       [?category :category/name ?category-name]]
     db
     "Russian Dip")

; find all movement names
(d/q '[:find ?name
       :where
       [_ :movement/name ?name]]
     db)

; Find binding
; [?t ...] says "I want to get back the result ?t unwrapped.
; ?t . says "Give only one, unwrapped result".

; find all category names, return unwrapped collection.
(d/q '[:find [?name ...]
       :where
       [_ :category/name ?name]]
     db)

; pull syntax '[*]: getting everything about an entity
(d/pull db '[*] 17592186045430)
; pull syntax '[attribute-1 attribute-2 attribute-3] gets specific entity attribute(s)
(d/pull db '[:category/name] 17592186045430)

; The query api is a logic api that's primarily about locating entities.
; The pull api is a declarative api and it's primarily about navigating from entities
; to specific information that can be reached by navigation, as opposed to information
; that can be reached by logic and by joins. These things are pretty much chocolate and
; peanut butter, and the mixing of the two is the way to go!

(d/q '[:find (pull ?p [:part/name])
       :in $ ?template-name
       :where
       [?e :template/title ?template-name]
       [?e :template/part ?p]]
     db
     "Strength")

; exercises that use "Rings" equipment.
(d/q '[:find (pull ?m [:movement/name])
       :in $ ?equipment-name
       :where
       [?e :equipment/name ?equipment-name]
       [?m :movement/equipment ?e]]
     db
     "Rings")