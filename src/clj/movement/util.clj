(ns movement.util
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [buddy.hashers :as hashers]
            [clojure.string :as str]
            [clojure.set :as set])
  (:import datomic.Util)
  (:import java.util.Date))

#_(def uri "datomic:dev://localhost:4334/testing6")

#_(def uri "datomic:ddb://us-east-1/movementsession/production?aws_access_key_id=AKIAJI5GV57L43PZ6MSA&aws_secret_key=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")

#_(d/delete-database uri)

#_(d/create-database uri)

#_(def conn (d/connect uri))

#_(let [schema-tx (first (Util/readAll (io/reader (io/resource "data/schema.edn"))))]
  (d/transact conn schema-tx))

#_(let [acrobatics-tx (first (Util/readAll (io/reader (io/resource "data/movements/acrobatics.edn"))))
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
    (d/transact conn walking-tx))

#_(let [templates-tx (first (Util/readAll (io/reader (io/resource "data/templates.edn"))))]
    (d/transact conn templates-tx))

#_(let [tx-user-data [{:db/id                    #db/id[:db.part/user]
                       :user/email               "andflak@gmail.com"
                       :user/valid-subscription? true}]]
    (d/transact conn tx-user-data))

;; Get the database value.
#_(def db (d/db conn))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#_(defn image-url [name]
  (str "public/images/" (str/replace (str/lower-case name) " " "-") ".png"))

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

#_(find-no-image-movements)

#_(defn find-no-data-images []
  (let [f (io/file "resources/public/images")
        images (for [file (file-seq f)] (.getName file))
        images (drop 2 images) ; remove leading junk files
        no-data-images (filter #(has-no-data? %) images)]
    {:#images          (count images)
     :#no-data-images (count no-data-images)
     :no-data-images  (vec no-data-images)}))

;;;;;;;;;;;;;; EXPERIMENTAL LAB ;;;;;;;;;;;;;;;;;;;;;;;

#_"Time to practice running fast. Warm up well by running, doing mobility work and/or practicing explosive jumps. Finish the warm up by running a 100m run at 80% of max speed.
Perform between four and ten 50-200 meter sprints at close to max effort. Rest between sets by walking back to the starting position slowly.",

#_(let [
      tx-user-data [{:db/id                    #db/id[:db.part/user]
                     :user/email               "chrhage@gmail.com"
                     :user/valid-subscription? true}]]
  (d/transact conn tx-user-data))

#_(let [
      tx-user-data [{:db/id                    #db/id[:db.part/user]
                     :user/email               "martinarnesen1@gmail.com"
                     :user/password (hashers/encrypt "Dg86AS721Gas1")}]]
  (d/transact conn tx-user-data))

#_(let []
  (empty? (d/q '[:find [?username ...]
                 :in $ ?username
                 :where
                 [?e :user/name ?username]]
               db
               "flaket")))

#_(d/q '[:find (pull ?s [*])
         :where
         [?u :user/email ?e]
         [?u :user/plan ?s]]
       db)

#_(d/q '[:find (pull ?u [*])
       :where [?u :user/email ?n]]
     db)

#_(d/q '[:find (pull ?u [*])
       :where [?u :category/name ?n]]
     db)

#_(d/q '[:find (pull ?u [*])
         :where [?u :movement/unique-name ?n]]
       db)

#_(let [templates ["Natural Movement" "Locomotion" "4x4 Interval Run"]
        template-ids (map (pull ))])

#_(def db (d/db conn))
#_(d/pull db '[*] 17592186045888)
#_(d/transact conn [[:db/retract 17592186045925
                     :movement/equipment 17592186045447]])
#_(d/transact conn [[:db.fn/retractEntity 17592186045782]])

#_(d/q '[:find (pull ?e [*])
         :in $ ?name
       :where
       [?e :movement/unique-name ?name]]
     db "One Leg Extended Side Lever")

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

#_(def email "admin@movementsession.com")

#_(def ex-plan [["Learning Handstand"] ["Natural Movement"]
                ["Learning Handstand" "Gymnastic Strength"] ["Natural Movement"]
                ["Learning Handstand"] ["Natural Movement"]
                [""]])

#_(defn transact-plan! [email {:keys [title created-by public? description plan]}]
    (let [description (if (nil? description) "" description)
          public? (if (nil? public?) true public?)
          tx-days (vec (for [day plan]
                         (let [template-ids (vec (map #(get-user-template-id email %) day))]
                           {:db/id          (d/tempid :db.part/user)
                            :day/completed? false
                            :day/template   (if (= [nil] template-ids)
                                              []
                                              template-ids)})))
          tx-data [{:db/id      #db/id[:db.part/user -99]
                    :user/email email
                    :user/plan [#db/id[:db.part/user -100]]}
                   {:db/id            #db/id[:db.part/user -100]
                    :plan/title       title
                    :plan/description description
                    :plan/public?     public?
                    :plan/completed?  false
                    :plan/day         (vec (map :db/id tx-days))
                    :plan/created-by  #db/id[:db.part/user -102]}
                   {:db/id     #db/id[:db.part/user -102]
                    :user/name created-by}]
          tx-data (concat tx-data tx-days)]
      (d/transact conn tx-data)))

#_(transact-plan! email {:title "My Plan" :public? true :created-by "movementsession"
                         :plan ex-plan})

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

#_(d/q '[:find (pull ?r [*])
            :in $
            :where
         [?u :user/routine ?r]]
          db)

#_(vec (map #(d/pull db '[*] (:db/id %)) (:part/specific-movement (d/pull db '[*] 17592186045859))))

#_(d/pull db '[*] 17592186045646)

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



#_(pp/pprint *1)

; Looking up unique value with the pull api
; The [:ns ""] vector is a "look-up ref". Anywhere in datomic where
; an entity is supposed to be provided, a look-up ref can be used instead.
; This let's us avoid dealing with entities. The attribute value must be unique.
#_(d/pull db '[] [:equipment/name "Rings"])

; There are three different ways of referring to an entity in datomic.
; By it's id
#_(d/pull db '[*] 17592186045430)
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
