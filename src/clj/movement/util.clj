(ns movement.util
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [buddy.hashers :as hashers]
            [clojure.string :as str]
            [clojure.set :refer [rename-keys]])
  (:import datomic.Util)
  (:import java.util.Date))
;; Create database and create a connection.
(def uri "datomic:dev://localhost:4334/movement14")
#_(d/delete-database uri)
(d/create-database uri)
(def conn (d/connect uri))

(let [schema-tx (first (Util/readAll (io/reader (io/resource "data/schema.edn"))))]
  (d/transact conn schema-tx))

(let [templates-tx (first (Util/readAll (io/reader (io/resource "data/templates.edn"))))]
  (d/transact conn templates-tx))

(let [acrobatics-tx (first (Util/readAll (io/reader (io/resource "data/movements/acrobatics.edn"))))
      balancing-tx (first (Util/readAll (io/reader (io/resource "data/movements/balancing.edn"))))
      climbing-tx (first (Util/readAll (io/reader (io/resource "data/movements/climbing.edn"))))
      core-tx (first (Util/readAll (io/reader (io/resource "data/movements/core.edn"))))
      crawling-tx (first (Util/readAll (io/reader (io/resource "data/movements/crawling.edn"))))
      e-tx (first (Util/readAll (io/reader (io/resource "data/movements/e.edn"))))
      endurance-tx (first (Util/readAll (io/reader (io/resource "data/movements/endurance.edn"))))
      jumping-tx (first (Util/readAll (io/reader (io/resource "data/movements/jumping.edn"))))
      lifting-tx (first (Util/readAll (io/reader (io/resource "data/movements/lifting.edn"))))
      lowerbody-tx (first (Util/readAll (io/reader (io/resource "data/movements/lowerbody.edn"))))
      mobility-tx (first (Util/readAll (io/reader (io/resource "data/movements/mobility.edn"))))
      pulling-tx (first (Util/readAll (io/reader (io/resource "data/movements/pulling.edn"))))
      pushing-tx (first (Util/readAll (io/reader (io/resource "data/movements/pushing.edn"))))
      rolling-tx (first (Util/readAll (io/reader (io/resource "data/movements/rolling.edn"))))
      sass-tx (first (Util/readAll (io/reader (io/resource "data/movements/sass.edn"))))
      throwing-catching-tx (first (Util/readAll (io/reader (io/resource "data/movements/throwing-catching.edn"))))
      walking-tx (first (Util/readAll (io/reader (io/resource "data/movements/walking.edn"))))]
  (do
    (d/transact conn acrobatics-tx)
    (d/transact conn balancing-tx)
    (d/transact conn climbing-tx)
    (d/transact conn core-tx)
    (d/transact conn crawling-tx)
    (d/transact conn e-tx)
    (d/transact conn endurance-tx)
    (d/transact conn jumping-tx)
    (d/transact conn lifting-tx)
    (d/transact conn lowerbody-tx)
    (d/transact conn mobility-tx)
    (d/transact conn pulling-tx)
    (d/transact conn pushing-tx)
    (d/transact conn rolling-tx)
    (d/transact conn sass-tx)
    (d/transact conn throwing-catching-tx)
    (d/transact conn walking-tx)))

;; Get the database value.
(def db (d/db conn))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn image-url [name]
  (str "public/images/" (str/replace (str/lower-case name) " " "-") ".png"))

(defn has-no-image? [m]
  (if-not (io/resource (image-url m))
    true
    false))

(defn url->name [url]
  (let [name (-> url
                 (str/split (re-pattern ".png"))
                 (first)
                 (str/replace "-" " ")
                 (str/split (re-pattern " ")))
        name (map #(str/capitalize %) name)
        name (-> name
                 (interleave (cycle " "))
                 (drop-last)
                 (str/join))]
    name))

(defn has-no-data? [url]
  (let [name (url->name url)
        x (d/q '[:find ?e
                 :in $ ?name
                 :where
                 [?e :movement/name ?name]]
               db
               name)]
    (empty? x)))

(defn find-no-image-movements []
  (let [movements (flatten (seq (d/q '[:find ?name
                                       :where
                                       [_ :movement/name ?name]]
                                     db)))
        no-image-movements (filter #(has-no-image? %) movements)]
    [(count no-image-movements) no-image-movements]))

(defn find-no-data-images []
  (let [f (io/file "resources/public/images")
        images (for [file (file-seq f)] (.getName file))
        images (drop 2 images)
        no-data-images (filter #(has-no-data? %) images)]
    [(count no-data-images) no-data-images]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn create-equipment-session [name n]
  (let [r (d/q '[:find (pull ?m [*])
                 :in $ ?name
                 :where
                 [?e :equipment/name ?name]
                 [?m :movement/equipment ?e]]
               db
               name)
        m (->> r flatten set shuffle (take n) vec)]
    {:title  "Let's play with.."
     :parts [{:title      (str/capitalize name)
              :categories []
              :equipment  name
              :movements  m}]}))

(defn get-n-movements-from-categories
  [n categories d]
  "Get n random movement entities drawn from param list of categories."
  (let [movements (for [c categories]
                    (d/q '[:find (pull ?m [*]) :in $ ?cat
                           :where [?c :category/name ?cat] [?m :movement/category ?c]]
                         db c))
        m (->> movements flatten set shuffle (take n))
        m (map #(assoc % :rep (:rep d) :set (:set d) :distance (:distance d) :duration (:duration d)) m)]
    m))


(defn pull-on-id [id]
  (d/pull db '[*] id))

(defn number-of-movements-in-db []
  (count (d/q '[:find [?n ...]
                :where
                [_ :movement/name ?n]]
              db)))

(defn find-user [email]
  (let [db (d/db conn)]
    (d/pull db '[*] [:user/email email])))

(defn valid-user? [user password]
  (hashers/check password (:user/password user)))

(defn add-user [email password]
  (let [tx-user-data [{:db/id         #db/id[:db.part/user]
                       :user/email    email
                       :user/password (hashers/encrypt password)}]]
    (d/transact conn tx-user-data)))


(def tx-user-data [{:db/id         #db/id[:db.part/user]
                    :user/email    "admin@movementsession.com"
                    :user/name     "Admin"
                    :user/password (hashers/encrypt "pw")}])

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


(d/q '[:find (pull ?p [*])
       :in $ ?mail
       :where [?m :user/email ?mail] [?m :user/template ?t] [?t :template/part ?p]]
     db
     "admin@movementsession.com")

(d/q '[:find (pull ?m [{:user/template
                        [:template/title]}])
       :in $
       :where [?m :user/email ?cat]]
     db)

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

(into {}
      (d/q '[:find ?name (count ?m)
             :where
             [?cat :category/name ?name]
             [?m :movement/category ?cat]]
           db))

; get specific category
(defn get-category [name]
  (d/q '[:find (pull ?e [*])
         :in $ ?category
         :where
         [?e :category/name ?category]]
       db
       name))
(get-category "Lower Body Strength")


; get n movements drawn randomly from categories
(defn get-movements [n categories]
  (let [movements (for [c categories]
                    (d/q '[:find (pull ?m [*]) :in $ ?cat
                           :where [?c :category/name ?cat] [?m :movement/category ?c]]
                         db c))
        m (->> movements flatten set shuffle (take n))]
    m))

(first (get-movements 1 ["Climbing"]))

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
         {:title     name
          :parts     [category-names]
          :movements movements})))

(def category-entities (map #(d/pull db '[*] %) (vec (flatten (map vals (:part/category (first part-entities)))))))
category-entities

(defn movement [name]
  "Returns the whole entity of a named movement."
  (let [movement (d/pull db '[*] [:movement/name name])]
    movement))

(defn get-movements [n categories]
  "Get n random movement entities drawn from param list of categories."
  (let [movements (for [c categories]
                    (d/q '[:find (pull ?m [*]) :in $ ?cat
                           :where [?c :category/name ?cat] [?m :movement/category ?c]]
                         db c))
        m (->> movements flatten set shuffle (take n))]
    m))

(defn create-session [title]
  (let [title-entity (ffirst (d/q '[:find (pull ?t [*])
                                    :in $ ?title ?email
                                    :where
                                    [?e :user/email ?email]
                                    [?e :user/template ?t]
                                    [?t :template/title ?title]]
                                  db
                                  title
                                  "bob@bob.com"))
        description (:template/description title-entity)
        part-entities (map #(d/pull db '[*] %) (vec (flatten (map vals (:template/part title-entity)))))
        parts
        (vec (for [p part-entities]
               (let [name (:part/name p)
                     n (:part/number-of-movements p)
                     c (flatten (map vals (:part/category p)))
                     category-names (vec (flatten (map vals (map #(d/pull db '[:category/name] %) c))))
                     movements (vec (get-movements n category-names))]
                 (if-let [regular-movements (vec (map #(d/pull db '[*] (:db/id %)) (:part/regular-movement p)))]
                   {:title      name
                    :categories category-names
                    :movements  (concat regular-movements movements)}
                   {:title      name
                    :categories category-names
                    :movements  movements}))))
        ]
    {:title       title
        :description description
        :parts       parts}))

(create-session "Straight Arm Strength")


(defn decorate
  "Map of entity attributes."
  [id]
  (let [e (d/entity db id)]
    (select-keys e (keys e))))

(defn decorate-results
  "Decorate a set of results."
  [r]
  (map #(decorate (first %)) r))


;;;;;;;;;;;;


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
