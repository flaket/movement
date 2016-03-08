(ns movement.util
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [buddy.hashers :as hashers]
            [clojure.string :as str]
            [clojure.set :as set :refer [rename-keys]])
  (:import datomic.Util)
  (:import java.util.Date))

#_(def uri "datomic:dev://localhost:4334/test-db")

#_(def uri "datomic:ddb://eu-west-1/movementsession/prod-db?aws_access_key_id=AKIAJI5GV57L43PZ6MSA&aws_secret_key=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")

#_(d/delete-database uri)

#_(d/create-database uri)

#_(def conn (d/connect uri))

#_(let [schema-tx (first (Util/readAll (io/reader (io/resource "data/schema.edn"))))]
    (d/transact conn schema-tx))

#_(let [balancing-tx (first (Util/readAll (io/reader (io/resource "data/movements/balancing.edn"))))
        climbing-tx (first (Util/readAll (io/reader (io/resource "data/movements/climbing.edn"))))
        crawling-tx (first (Util/readAll (io/reader (io/resource "data/movements/crawling.edn"))))
        jumping-tx (first (Util/readAll (io/reader (io/resource "data/movements/jumping.edn"))))
        lifting-tx (first (Util/readAll (io/reader (io/resource "data/movements/lifting.edn"))))
        rolling-tx (first (Util/readAll (io/reader (io/resource "data/movements/rolling.edn"))))
        hanging-tx (first (Util/readAll (io/reader (io/resource "data/movements/hanging.edn"))))
        running-tx (first (Util/readAll (io/reader (io/resource "data/movements/running.edn"))))
        swimming-tx (first (Util/readAll (io/reader (io/resource "data/movements/swimming.edn"))))
        throwing-tx (first (Util/readAll (io/reader (io/resource "data/movements/throwing-catching.edn"))))
        walking-tx (first (Util/readAll (io/reader (io/resource "data/movements/walking.edn"))))
        mobility-tx (first (Util/readAll (io/reader (io/resource "data/movements/mobility/mobility.edn"))))
        hand-balance-tx (first (Util/readAll (io/reader (io/resource "data/movements/other/hand-balance.edn"))))
        ring-tx (first (Util/readAll (io/reader (io/resource "data/movements/other/ring.edn"))))
        pushing-tx (first (Util/readAll (io/reader (io/resource "data/movements/other/pushing.edn"))))
        pulling-tx (first (Util/readAll (io/reader (io/resource "data/movements/other/pulling.edn"))))
        planche-lever-tx (first (Util/readAll (io/reader (io/resource "data/movements/other/planche-lever.edn"))))
        leg-strength-tx (first (Util/readAll (io/reader (io/resource "data/movements/other/leg-strength.edn"))))
        core-tx (first (Util/readAll (io/reader (io/resource "data/movements/other/core.edn"))))
        footwork-tx (first (Util/readAll (io/reader (io/resource "data/movements/other/footwork.edn"))))]
    (d/transact conn throwing-tx)
    (d/transact conn balancing-tx)
    (d/transact conn climbing-tx)
    (d/transact conn crawling-tx)
    (d/transact conn hanging-tx)
    (d/transact conn jumping-tx)
    (d/transact conn lifting-tx)
    (d/transact conn rolling-tx)
    (d/transact conn running-tx)
    (d/transact conn walking-tx)
    (d/transact conn swimming-tx)
    (d/transact conn mobility-tx)
    (d/transact conn hand-balance-tx)
    (d/transact conn ring-tx)
    (d/transact conn pushing-tx)
    (d/transact conn pulling-tx)
    (d/transact conn planche-lever-tx)
    (d/transact conn leg-strength-tx)
    (d/transact conn core-tx)
    (d/transact conn footwork-tx)
    )

#_(let [tx-user-data [{:db/id                    #db/id[:db.part/user -1]
                       :user/email               "andflak@gmail.com"
                       :user/password            (hashers/encrypt "pw")
                       :user/activated?          true
                       :user/valid-subscription? true}]]
    (d/transact conn tx-user-data))

#_(let [d [[:db.fn/cas 17592186045926 :part/title "Shoulder routine" "Elastic shoulders"]]]
  (d/transact conn d))

#_(let [d [{:db/id 17592186045926
            :part/category [#db/id[:db.part/user -2]]}
           {:db/id #db/id[:db.part/user -2]
            :category/name "Elastic Band"}]]
  (d/transact conn d))

#_(d/transact conn [[:db/retract 17592186045926 :part/category 17592186045493]])

;; Get the database value.
#_(def db (d/db conn))

;; Number of users
#_(count (d/q '[:find [?u ...]
                :where
                [?u :user/email ?e]]
              db))

#_(d/q '[:find [(pull ?u [:user/email]) ...]
       :where
       [?u :user/email ?e]]
     db)

#_(d/q '[:find [(pull ?t [*]) ...]
       :where
         [?u :user/template ?t]
         [?t :template/created-by ?x]
         [?x :user/name "movementsession"]]
     db)

#_(-> (d/q '[:find [(pull ?u [:user/movements]) ...]
             :where
             [?u :user/email ?e]]
           db)
      first
      :user/movements)

#_(d/q '[:find [?u ...]
       :where
       [?u :session/url _]]
     db)

#_(d/transact conn [[:db.fn/retractEntity 17592186045892]])

#_(d/pull db '[*] 17592186045892)

#_(defn image-url [name]
    (str "public/images/movements/" (str/replace (str/lower-case name) " " "-") ".png"))

#_(defn has-no-image? [m]
    (if-not (io/resource (image-url m)) true false))

#_(defn url->name [url]
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

#_(defn has-no-data? [url]
    (let [name (url->name url)
          x (d/q '[:find ?e
                   :in $ ?name
                   :where
                   [?e :movement/unique-name ?name]]
                 db
                 name)]
      (empty? x)))

#_(defn find-no-image-movements []
    (let [movements (flatten (seq (d/q '[:find ?name
                                         :where
                                         [_ :movement/unique-name ?name]]
                                       db)))
          no-image-movements (filter #(has-no-image? %) movements)]
      {:#                  (count no-image-movements)
       :no-image-movements (vec no-image-movements)}))

#_(defn find-no-data-images []
    (let [f (io/file "resources/public/images/movements")
          images (for [file (file-seq f)] (.getName file))
          images (drop 2 images)                            ; remove leading junk files
          no-data-images (filter #(has-no-data? %) images)]
      {:#images         (count images)
       :#no-data-images (count no-data-images)
       :no-data-images  (vec no-data-images)}))

#_(find-no-image-movements)
#_(find-no-data-images)

;;;;;;;;;;;;;; EXPERIMENTAL LAB ;;;;;;;;;;;;;;;;;;;;;;;

 ;; progress-plan!
#_(let [;conn (:conn @tx)
        user-id 17592186045808
        plan-id 17592186046127
        plan (d/pull db '[*] plan-id)
        days (:plan/day plan)
        current-day (:plan/current-day plan)
        day-id (:db/id current-day)
        current-day-pos (first (positions #{current-day} days))
        new-current-day (:db/id (get days (inc current-day-pos)))
        tx-data [[:db/add day-id :day/completed? true]
                 [:db/add plan-id :plan/current-day new-current-day]]]
    tx-data
    #_(d/transact conn tx-data))

;; end-plan!
#_(let [user-id 17592186045808
        plan-id 17592186046127
        plan (d/pull db '[*] plan-id)
        days (map #(d/pull db '[*] (:db/id %)) (:plan/day plan))
        all-completed? (every? #(true? (:day/completed? %)) days)
        tx-data [[:db/add plan-id :plan/ended (Date.)]
                 [:db/add plan-id :plan/completed? all-completed?]
                 [:db/retract user-id :user/ongoing-plan plan-id]]]
    (d/transact conn tx-data))

#_(empty? (d/q '[:find [?u ...]
                 :in $
                 :where
                 [?u :user/email "admin@movementsession.com"]
                 [?u :user/template 17592186045829]]
               db))

#_(d/q '[:find (pull ?e [*])
         :in $ ?name
         :where
         [?e :template/title ?name]]
       db "Locomotion")

#_(defn all-movements []
    (d/q '[:find [?name ...]
           :in $
           :where
           [?e :movement/unique-name ?name]]
         db))

#_(defn movements-from-category [category]
    (d/q '[:find [?name ...]
           :in $ ?cname
           :where
           [?e :movement/unique-name ?name]
           [?e :movement/category ?c]
           [?c :category/name ?cname]]
         db category))

#_(count (all-movements))
#_(count (movements-from-category "Pushing"))

#_(let [pushing (set (movements-from-category "Pushing"))
        pulling (set (movements-from-category "Pulling"))
        bas (set (movements-from-category "Bent Arm Strength"))]
    (set/difference bas (set/union pushing pulling)))

#_(first (d/q '[:find [?id ...]
                :in $ ?email ?name
                :where
                [?u :user/email ?email]
                [?u :user/template ?id]
                [?id :template/title ?name]]
              db "admin@movementsession.com" "Locomotion"))

#_(defn get-user-template-id [email template-title]
    (first (d/q '[:find [?id ...]
                  :in $ ?email ?name
                  :where
                  [?u :user/email ?email]
                  [?u :user/template ?id]
                  [?id :template/title ?name]]
                db email template-title)))

#_(defn entity-by-template-title
    "Returns the whole entity of a named template."
    [email title]
    (d/q '[:find (pull ?t [*])
           :in $ ?email ?title
           :where
           [?u :user/email ?email]
           [?u :user/template ?t]
           [?t :template/title ?title]]
         db email title))

#_(d/q '[:find (pull ?t [*])
         :in $ ?email ?title
         :where
         [?u :user/email ?email]
         [?u :user/group ?group]
         [?group :group/title ?title]
         [?group :group/template ?t]]
       db
       "admin@movementsession.com"
       "Standard Templates 1")

#_(d/q '[:find (pull ?u [*])
         :in $
         :where
         [?u :user/name _]]
       db)

#_(d/q '[:find (pull ?t [*])
         :in $ ?username ?item
         :where
         [?e :user/name ?username]
         [?e ?item ?t]
         [?t ?created-by ?e]]
       db "movementsession" :user/plan)

#_(vec (map #(d/pull db '[*] (:db/id %)) (:part/specific-movement (d/pull db '[*] 17592186045859))))

#_(d/pull db '[*] 17592186045808)

#_(d/q '[:find ?e
         :in $ ?id
         :where
         [_ :db/id ?id]
         #_[?m :movement/harder ?e]]
       db
       17592186045637)

; all exercises not using the input equipment parameter.
#_(d/q '[:find ?name
         :in $ ?equipment
         :where
         (not-join [?e]
                   [?e :movement/equipment ?c]
                   [?c :equipment/name ?equipment])
         [?e :movement/name ?name]]
       db
       "Rings")

#_(d/q '[:find [?e ...]
         :in $ ?cat
         :where
         [?e :template/part ?p]
         [?p :part/category ?c]
         [?c :category/name ?cat]]
       db
       "Hip Mobility")

#_(flatten (d/q '[:find (pull ?t [*])
                  :in $ ?kw ?username
                  :where
                  [?e :user/name ?username]
                  [?e ?kw ?t]
                  [?t :plan/created-by ?e]]
                db :user/plan "movementsession"))

#_(d/q '[:find [?t ...]
         :in $ ?username
         :where
         [(fulltext $ :user/name ?username) [[?e ?n]]]
         [?e :user/template ?t]
         [?t :template/created-by ?e]]
       db "movementsession")

#_(->> (d/q '[:find (pull ?t [*])
              :in $ ?username ?item ?created-by
              :where
              [?e :user/name ?username]
              [?e ?item ?t]
              [?t ?created-by ?e]]
            db "movementsession" :user/template :template/created-by)
       flatten)

#_(->> (map #(flatten
              (d/q '[:find (pull ?t [*])
                     :in $ ?category
                     :where
                     [?e :user/template ?t]
                     [?t :template/created-by ?e]
                     [?t :template/part ?p]
                     [?p :part/category ?c]
                     [?c :category/name ?category]]
                   db %)) ["Crawling" "Rolling"])
       flatten
       set
       seq)

#_(defn items-by-category
    ""
    [type category]
    (flatten
      (case type
        :template (d/q '[:find (pull ?t [*])
                         :in $ ?category
                         :where
                         [?e :user/template ?t]
                         [?t :template/created-by ?e]
                         [?t :template/part ?p]
                         [?p :part/category ?c]
                         [?c :category/name ?category]]
                       db category)
        :group (d/q '[:find (pull ?g [*])
                      :in $ ?category
                      :where
                      [?e :user/group ?g]
                      [?g :group/created-by ?e]
                      [?g :group/template ?t]
                      [?t :template/part ?p]
                      [?p :part/category ?c]
                      [?c :category/name ?category]]
                    db category)
        :plan (d/q '[:find (pull ?plan [*])
                     :in $ ?category
                     :where
                     [?e :user/plan ?plan]
                     [?plan :plan/created-by ?e]
                     [?plan :plan/day ?d]
                     [?d :day/template ?t]
                     [?t :template/part ?p]
                     [?p :part/category ?c]
                     [?c :category/name ?category]]
                   db category)
        nil)))

#_(flatten (d/q '[:find (pull ?t [:db/id :template/title])
                  :in $ ?email
                  :where
                  [?e :user/email ?email]
                  [?e :user/template ?t]]
                db
                "admin@movementsession.com"))

#_(d/pull db '[*] 17592186045838)

#_(flatten
    (d/q '[:find (pull ?t [*])
           :in $ ?title
           :where
           [(fulltext $ :template/title ?title) [[?t ?n]]]
           [?e :user/template ?t]
           [?t :template/created-by ?e]]
         db "5x5"))

#_(flatten (map #(flatten
                  (d/q '[:find (pull ?t [*])
                         :in $ ?title
                         :where
                         [(fulltext $ :template/title ?title) [[?t ?n]]]
                         [?e :user/template ?t]
                         [?t :template/created-by ?e]]
                       db %)) (str/split "5x5 locomotion" #" ")))

#_(flatten (map #(flatten (d/q '[:find (pull ?t [*])
                                 :in $ ?description
                                 :where
                                 [(fulltext $ :template/description ?description) [[?t ?n]]]
                                 [?e :user/template ?t]
                                 [?t :template/created-by ?e]]
                               db %)) (str/split "practice" #" ")))

#_(pp/pprint *1)

; Looking up unique value with the pull api
; The [:ns ""] vector is a "look-up ref". Anywhere in datomic where
; an entity is supposed to be provided, a look-up ref can be used instead.
; This let's us avoid dealing with entities. The attribute value must be unique.
#_(d/pull db '[] [:category/name "Handstand"])

; There are three different ways of referring to an entity in datomic.
; By it's id
#_(d/pull db '[*] 17592186045811)
; By look-up ref
#_(d/pull db '[*] [:category/name "Pushing"])
; Directly by it's programmtic name (if it has one, this does not.)
#_(d/pull db '[*] :category/pushing)

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
#_(d/q '[:find ?category-name
         :in $ ?movement-name
         :where
         [?e :movement/name ?movement-name]
         [?e :movement/category ?category]
         [?category :category/name ?category-name]]
       db
       "Russian Dip")

; find all movement names
#_(d/q '[:find ?name
         :where
         [_ :movement/name ?name]]
       db)

; Find binding
; [?t ...] says "I want to get back the result ?t unwrapped.
; ?t . says "Give only one, unwrapped result".

; find all category names, return unwrapped collection.
#_(d/q '[:find [?name ...]
         :where
         [_ :category/name ?name]]
       db)

; pull syntax '[*]: getting everything about an entity
#_(d/pull db '[*] 17592186045430)
; pull syntax '[attribute-1 attribute-2 attribute-3] gets specific entity attribute(s)
#_(d/pull db '[:category/name] 17592186045430)

; The query api is a logic api that's primarily about locating entities.
; The pull api is a declarative api and it's primarily about navigating from entities
; to specific information that can be reached by navigation, as opposed to information
; that can be reached by logic and by joins. These things are pretty much chocolate and
; peanut butter, and the mixing of the two is the way to go!

#_(d/q '[:find (pull ?p [:part/name])
         :in $ ?template-name
         :where
         [?e :template/title ?template-name]
         [?e :template/part ?p]]
       db
       "Strength")

; exercises that use "Rings" equipment.
#_(d/q '[:find (pull ?m [:movement/name])
         :in $ ?equipment-name
         :where
         [?e :equipment/name ?equipment-name]
         [?m :movement/equipment ?e]]
       db
       "Rings")

; using lookup-ref to simplify adding data to an entity
#_(d/transact conn [[:db/add
                     [:user/email "jdoe@example.com"]
                     :db/doc
                     "doc about John"]])

; most restrictive clauses should come first in a query
; :where [?e :only/matches ?few]
;        [?e :will/match ?many]

; Collection Binding
; pass collections as inputs
; This behaves as a logical or, that is, it returns a union of the results for each item in the collection. When you want all results that match any of a given collection of entities or values, you should prefer a collection binding in a parameterized query over running multiple queries.
; The following query uses a collection binding to return all names for artists based in Japan or Canada:
#_(d/q '[:find (pull ?a [:artist/name])
         :in $ [?c ...]
         :where [?a :artist/country ?country]
         [?country :country/name ?c]]
       db ["Canada" "Japan"])

; Relation Binding
; A relation binding is fully general, binding multiple variables positionally to a relation (collection of tuples) passed in. This can be used to ask "or" questions involving multiple variables. For example, what releases are associated with either John Lennon's Mind Games or Paul McCartney's Ram?
#_[:find ?release
   :in $ [[?artist-name ?release-name]]
   :where [?artist :artist/name ?artist-name]
   [?release :release/artists ?artist]
   [?release :release/name ?release-name]]
;; inputs:
; db, [["John Lennon" "Mind Games"] ["Paul McCartney" "Ram"]]

