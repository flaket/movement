(ns movement.util
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [buddy.hashers :as hashers])
  (:import datomic.Util))

;; Create database and create a connection.
(def uri "datomic:dev://localhost:4334/movement9")
#_(d/delete-database uri)
(d/create-database uri)
(def conn (d/connect uri))

(let [schema-tx (first (Util/readAll (io/reader (io/resource "data/schema.edn"))))]
  (d/transact conn schema-tx))

(defn add-data-to-database
  "Read data sources and transact to database."
  []
  (let [pulling-tx (first (Util/readAll (io/reader (io/resource "data/movements/pulling.edn"))))
        pushing-tx (first (Util/readAll (io/reader (io/resource "data/movements/pushing.edn"))))
        conditioning-tx (first (Util/readAll (io/reader (io/resource "data/movements/endurance.edn"))))
        core-tx (first (Util/readAll (io/reader (io/resource "data/movements/core.edn"))))
        lowerbody-tx (first (Util/readAll (io/reader (io/resource "data/movements/lowerbody.edn"))))
        mobility-tx (first (Util/readAll (io/reader (io/resource "data/movements/mobility.edn"))))
        multiplane-tx (first (Util/readAll (io/reader (io/resource "data/movements/multiplane.edn"))))
        sass-tx (first (Util/readAll (io/reader (io/resource "data/movements/sass.edn"))))
        locomotion-tx (first (Util/readAll (io/reader (io/resource "data/movements/locomotion.edn"))))
        template-tx (first (Util/readAll (io/reader (io/resource "data/movements/template.edn"))))]
    (do
      (d/transact conn pulling-tx)
      (d/transact conn pushing-tx)
      (d/transact conn conditioning-tx)
      (d/transact conn core-tx)
      (d/transact conn lowerbody-tx)
      (d/transact conn mobility-tx)
      (d/transact conn multiplane-tx)
      (d/transact conn sass-tx)
      (d/transact conn locomotion-tx)
      (d/transact conn template-tx))))

#_(add-data-to-database)

;; Get the database value.
(def db (d/db conn))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn find-user [email]
  (let [db (d/db conn)]
    (d/pull db '[*] [:user/email email])))

(defn valid-user? [user password]
  (hashers/check password (:user/password user)))

(defn jws-login
  [email password]
  (let [user (find-user email)]
    (if-not (nil? (:db/id user))
      (let [valid? (valid-user? user password)]
        (if valid?
          (let []
            {:token "a" :user (dissoc user :user/password :db/id)})
          {:message "wrong password"}))
      {:message "unknown email"})))

(defn add-user [email password]
  (let [tx-user-data [{:db/id #db/id[:db.part/user]
                       :user/email email
                       :user/password (hashers/encrypt password)}]]
    (d/transact conn tx-user-data)))


(def tx-user-data [{:db/id #db/id[:db.part/user]
                    :user/email "admin@movementsession.com"
                    :user/name "Admin"
                    :user/password (hashers/encrypt "pw")}

                   {:db/id #db/id[:db.part/user]
                    :user/email "bob@bob.com"
                    :user/name "Bob"
                    :user/password (hashers/encrypt "bob")}

                   {:db/id #db/id[:db.part/user]
                    :user/email "alice@alice.com"
                    :user/name "Alice"
                    :user/password (hashers/encrypt "alice")}])

(d/transact conn tx-user-data)

;;;
(d/q '[:find ?email ?name ?pw
       :in $
       :where
       [?e :user/email ?email]
       [?e :user/password ?pw]
       [?e :user/name ?name]
       ]
     db)


(d/q '[:find (pull ?m [{:user/template
                        [:template/title]}])
       :in $ ?cat
       :where [?m :user/email ?cat]]
     db
     "admin")

(hashers/check "alice1" (:user/password (d/pull db '[*] [:user/email "alice@alice.com"])))

(let [users (d/q '[:find ?email ?pw ?r
                   :in $
                   :where
                   [?e :user/email ?email]
                   [?e :user/password ?pw]
                   [?e :user/role ?r]
                   ]
                 db)
      users (map #(zipmap [:username :password :roles] %) users)
      users (map #(assoc % :roles (read-string (:roles %))) users)
      users (zipmap (map #(:username %) users) users)]
  users)



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
  (d/q '[:find (pull ?e [*])
         :in $ ?category
         :where
         [?e :category/name ?category]]
       db
       name))
(get-category "Lower Body Strength")


(defn generate-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body    (pr-str data)})

; get n movements drawn randomly from categories
(defn get-movements [n categories]
  (let [movements (for [c categories]
                    (d/q '[:find (pull ?m [*]) :in $ ?cat
                           :where  [?c :category/name ?cat] [?m :movement/category ?c] ]
                         db c))
        m (->> movements flatten set shuffle (take n))]
    m))

(first (get-movements 1 ["Lower Body Strength"]))

(def title "Strength")
(def title-entity (d/pull db '[*] [:template/title title]))
title-entity
(def part-entities (map #(d/pull db '[*] %) (flatten (map vals (:template/part title-entity)))))
part-entities
(vec (for [p part-entities]
       (let [name (:part/name p)
             n (:part/number-of-movements p)
             c (flatten (map vals (:part/category p)))
             category-names (apply merge (flatten (map vals (map #(d/pull db '[:category/name] %) c))))
             movements (vec (get-movements n [category-names]))]
         {:title      name
          :parts [category-names]
          :movements  movements})))

(def category-entities (map #(d/pull db '[*] %) (vec (flatten (map vals (:part/category (first part-entities)))))))
category-entities

(defn create-session [title]
  (let [title-entity (d/pull db '[*] [:template/title title])
        description (:template/description title-entity)
        part-entities (map #(d/pull db '[*] %) (vec (flatten (map vals (:template/part title-entity)))))
        parts (vec (for [p part-entities]
                     (let [name (:part/name p)
                           n (:part/number-of-movements p)
                           c (flatten (map vals (:part/category p)))
                           category-names (vec (flatten (map vals (map #(d/pull db '[:category/name] %) c))))
                           #_(print (count category-names))
                           movements (vec (get-movements n category-names))]
                       {:title      name
                        :categories [category-names]
                        :movements  movements})))
        ]
    {:title title
     :description description
     :parts parts}))

(pp/pprint (create-session "Lower Body Strength"))

(def temp-seq '(17592186045481 17592186045482 17592186045483))
(def temp-seq1 '(17592186045481))

(vec (flatten (map vals (map #(d/pull db '[:category/name] %) temp-seq))))

(apply merge (flatten (map vals (map #(d/pull db '[:category/name] %) temp-seq1))))

(defn decorate
  "Map of entity attributes."
  [id]
  (let [e (d/entity db id)]
    (select-keys e (keys e))))

(defn decorate-results
  "Decorate a set of results."
  [r]
  (map #(decorate (first %)) r))


(d/pull db '[*] [:movement/name "Push Up"])

(for [p parts]
  (let [categories (:part/category p)
        x (for [c categories]
            (d/pull db '[:category/name] (:db/id c)))]
    (vec (map :category/name x))))

; query for all equipment names
(d/q '[:find ?name
       :where [?c :equipment/name ?name]]
     db)

; query for exercises using equipment
(d/q '[:find ?name
       :in $ ?equipment
       :where
       [?e :movement/name ?name]
       [?e :movement/equipment ?c]
       [?c :equipment/name ?equipment]]
     db
     "Rings")

; all exercises not using the input equipment parameter.
(d/q '[:find ?name
       :in $ ?equipment
       :where
       (not-join [?e]
                 [?e :movement/equipment ?c]
                 [?c :equipment/name ?equipment])
       [?e :movement/name ?name]]
     db
     "Rings")



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
