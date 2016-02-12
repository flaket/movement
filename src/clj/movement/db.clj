(ns movement.db
  (:import java.util.Date)
  (:require [buddy.hashers :as hashers]
            [datomic.api :as d]
            [clojure.string :as str]
            [clojure.set :refer [rename-keys]]
            [taoensso.timbre :refer [info error]]
            [movement.activation :refer [generate-activation-id send-activation-email]]
            [clojure.set :as set]))

(def uri "datomic:dev://localhost:4334/testing16")
#_(def uri "datomic:ddb://us-east-1/movementsession/real-production?aws_access_key_id=AKIAJI5GV57L43PZ6MSA&aws_secret_key=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")

(def tx (atom {}))

(defn update-tx-conn! [] (swap! tx assoc :conn (d/connect uri)))

(defn update-tx-db! [] (swap! tx assoc :db (d/db (:conn @tx))))

(defn positions [pred coll]
  (keep-indexed (fn [idx x]
                  (when (pred x) idx)) coll))

(defn all-movement-names
  "Returns the names of all movements in the database."
  []
  (d/q '[:find [?n ...] :where [_ :movement/unique-name ?n]] (:db @tx)))

(defn all-category-names
  "Returns the names of all categories in the database."
  []
  (d/q '[:find [?n ...] :where [_ :category/name ?n]] (:db @tx)))

(defn entity-by-movement-name
  "Returns the whole entity of a named movement."
  [name]
  (d/pull (:db @tx) '[*] [:movement/unique-name name]))

(defn entity-by-id
  "Returns the whole entity of some id."
  [id]
  (d/pull (:db @tx) '[*] id))

(defn entity-by-lookup-ref
  "Returns the whole entity matching a keyword-value pair."
  [kw-ref id]
  (d/pull (:db @tx) '[*] [kw-ref id]))

(defn template-entity-by-title [email template-title]
  (d/q '[:find (pull ?t [*])
         :in $ ?email ?template-title
         :where
         [?t :template/title ?template-title]
         [?t :template/created-by ?u]
         [?u :user/template ?t]
         [?u :user/email ?email]]
       (:db @tx) email template-title))



(defn get-n-movements-from-categories
  "Get n random movement entities drawn from param list of categories."
  [n categories]
  (let [db (:db @tx)
        movement-ids (d/q '[:find [?m ...]
                            :in $ [?cname ...]
                            :where
                            [?c :category/name ?cname]
                            [?m :movement/category ?c]
                            [?m :movement/unique-name _]]
                          db categories)
        movements (->> movement-ids
                       shuffle
                       (take n)
                       (map #(d/pull db '[*] %)))]
    movements))

(defn single-movement [email part]
  (let [db (:db @tx)
        ; convert string values to ints. Why is this needed? A mistake in the client?
        part (into {} (for [[k v] part]
                        (if (and (string? v)
                                 (or (= k :rep) (= k :set) (= k :distance)
                                     (= k :duration) (= k :weight) (= k :rest)))
                          [k (read-string v)]
                          [k v])))
        movement (first (get-n-movements-from-categories 1 (vals (:categories part))))
        user-movements (d/q '[:find [(pull ?m [*]) ...]
                              :in $ ?email
                              :where
                              [?u :user/email ?email]
                              [?u :user/movements ?m]]
                            db
                            email)
        movement (if-let [user-movement (some #(when
                                                (= (:movement/unique-name movement) (:movement/name %))
                                                %)
                                              user-movements)]
                   (merge movement (dissoc user-movement :db/id))
                   (loop [m movement]
                     (let [easier (:movement/easier m)]
                       (if (nil? easier)
                         m
                         (let [new (d/pull db '[*] (:db/id (first (shuffle easier))))
                               new-has-been-done? (some #(= (:movement/unique-name new) (:movement/name %)) user-movements)]
                           (if new-has-been-done?
                             new
                             (recur new)))))))
        movement (merge movement (dissoc part :categories))
        movement (apply dissoc movement (for [[k v] movement :when (nil? v)] k))
        movement (rename-keys movement {:movement/measurement :measurement
                                        :movement/category    :category
                                        :movement/unique-name :unique
                                        :movement/easier      :easier
                                        :movement/harder      :harder
                                        :movement/description :description
                                        :movement/zone        :zone
                                        :movement/practical   :practical})]
    movement))

(defn movement [type id part]
  (let [; convert string values to ints. Why is this needed? A mistake in the client?
        part (into {} (for [[k v] part]
                        (if (and (string? v)
                                 (or (= k :rep) (= k :set) (= k :distance)
                                     (= k :duration) (= k :weight) (= k :rest)))
                          [k (read-string v)]
                          [k v])))
        movement (cond (= type :name) (entity-by-movement-name id)
                       (= type :id) (entity-by-id id)
                       (= type :category) (first (get-n-movements-from-categories 1 (vals (:categories part)))))
        movement (merge movement (dissoc part :categories))
        movement (apply dissoc movement (for [[k v] movement :when (nil? v)] k))
        movement (rename-keys movement {:movement/measurement :measurement
                                        :movement/category    :category
                                        :movement/unique-name :unique
                                        :movement/easier      :easier
                                        :movement/harder      :harder
                                        :movement/description :description
                                        :movement/zone        :zone
                                        :movement/practical   :practical})]
    (.println System/out (str "New movement: " movement))
    movement))

(defn get-movements-from-category
  "Get n movement entities of the category."
  [n category]
  (let [movements (d/q '[:find (pull ?m [*])
                         :in $ ?cat
                         :where
                         [?c :category/name ?cat]
                         [?m :movement/category ?c]
                         [?m :movement/unique-name _]]
                       (:db @tx) category)
        m (->> movements flatten (sort-by :movement/unique-name) (take n))]
    m))

;; refactor the following four functions "all-x"

(defn all-templates [email]
  (flatten (d/q '[:find (pull ?t [*])
                  :in $ ?email
                  :where
                  [?e :user/email ?email]
                  [?e :user/template ?t]]
                (:db @tx)
                email)))

(defn all-groups [email]
  (flatten (d/q '[:find (pull ?g [*])
                  :in $ ?email
                  :where
                  [?e :user/email ?email]
                  [?e :user/group ?g]]
                (:db @tx)
                email)))

(defn all-plans [email]
  (flatten (d/q '[:find (pull ?p [*])
                  :in $ ?email
                  :where
                  [?e :user/email ?email]
                  [?e :user/plan ?p]]
                (:db @tx)
                email)))

(defn ongoing-plan [email]
  (ffirst (d/q '[:find (pull ?p [*])
                 :in $ ?email
                 :where
                 [?e :user/email ?email]
                 [?e :user/ongoing-plan ?p]]
               (:db @tx)
               email)))

(defn all-routines [email]
  (flatten (d/q '[:find (pull ?r [*])
                  :in $ ?email
                  :where
                  [?e :user/email ?email]
                  [?e :user/routine ?r]]
                (:db @tx)
                email)))

(defn prep-new-movement [movement part]
  {:unique      (:movement/unique-name movement)
   :name        (:movement/name movement)
   :category    (:movement/category movement)
   :measurement (:movement/measurement movement)
   :easier      (:movement/easier movement)
   :harder      (:movement/harder movement)
   :description (:movement/description movement)
   :zone        (:movement/zone movement)
   :rep         (:part/rep part)
   :set         (:part/set part)
   :distance    (:part/distance part)
   :duration    (:part/duration part)
   :weight      (:part/weight part)
   :rest        (:part/rest part)
   :practical   (:part/practical part)})

(defn create-session [email template]
  (let [db (:db @tx)
        parts (vec
                (for [part (:template/part template)]
                  (let
                    [n (:part/number-of-movements part)
                     specific-movements (:part/specific-movement part)
                     category-names (vec (map :category/name (:part/category part)))
                     generated-movements (if (nil? n)
                                           []
                                           (get-n-movements-from-categories n category-names))
                     user-movements (d/q '[:find [(pull ?m [*]) ...]
                                           :in $ ?email
                                           :where
                                           [?u :user/email ?email]
                                           [?u :user/movements ?m]]
                                         db
                                         email)
                     generated-movements (for [movement generated-movements]
                                           ; if the user has done the movement before
                                           (if-let [user-movement (some #(when
                                                                          (= (:movement/unique-name movement) (:movement/name %))
                                                                          %)
                                                                        user-movements)]
                                             ; assoc :zone data
                                             (merge movement (dissoc user-movement :db/id))
                                             ; else: movement has not been performed, swap recursively to the easiest variationwhen generated has easier: swap
                                             (loop [m movement]
                                               (let [easier (:movement/easier m)]
                                                 (if (nil? easier)
                                                   m
                                                   (let [new (d/pull db '[*] (:db/id (first (shuffle easier))))
                                                         new-has-been-done? (some #(= (:movement/unique-name new) (:movement/name %)) user-movements)]
                                                     (if new-has-been-done?
                                                       new
                                                       (recur new))))))))
                     movements (concat specific-movements generated-movements)
                     movements (vec (for [m movements] (prep-new-movement m part)))]
                    {:title      (:part/title part)
                     :categories category-names
                     :movements  movements
                     :rep        (:part/rep part)
                     :set        (:part/set part)
                     :distance   (:part/distance part)
                     :duration   (:part/duration part)
                     :weight     (:part/weight part)
                     :rest       (:part/rest part)})))
        session (assoc template :parts parts)]
    {:title         (:template/title session)
     :description   (:template/description session)
     :template-id   (:db/id session)
     :plan-id       (:plan-id session)
     :last-session? (:last-session? session)
     :parts         (:parts session)}))

(defn random-template-from-group [email group]
  (let [db (:db @tx)
        templates (d/q '[:find (pull ?t [*])
                         :in $ ?email ?title
                         :where
                         [?u :user/email ?email]
                         [?u :user/group ?group]
                         [?group :group/title ?title]
                         [?group :group/template ?t]]
                       db
                       email
                       group)]
    (ffirst (shuffle templates))))

(defn get-routine [email routine]
  (let [db (:db @tx)
        ]))

(defn retrieve-sessions [req]
  (let [db (:db @tx)
        user (:user (:params req))
        sessions (vec (flatten
                        (d/q '[:find (pull ?s [*])
                               :in $ ?mail
                               :where
                               [?m :user/email ?mail]
                               [?m :user/session ?s]]
                             db
                             user)))]
    sessions))

(defn get-session [url]
  (let [db (:db @tx)
        session (ffirst (d/q '[:find (pull ?e [*])
                               :in $ ?url
                               :where
                               [?e :session/url ?url]]
                             db
                             url))
        parts (vec (map #(d/pull db '[*] (:db/id %)) (:session/part session)))
        parts (vec (for [p parts]
                     {:title     (:part/title p)
                      :movements (vec (map #(d/pull db '[*] (:db/id %)) (:part/session-movement p)))}))]
    {:title       (:session/title session)
     :description (:session/description session)
     :parts       parts
     :comment     (:session/comment session)
     :time        (:session/time session)
     :template    (:session/template session)
     :plan        (:session/plan session)
     :heart-rate  (:session/heart-rate session)}))

; refactor following five functions "new-unique-x?", (defn unique-title? [email type title])

(defn new-unique-template? [user template-title]
  (let [db (:db @tx)]
    (empty? (d/q '[:find [?user ...]
                   :in $ ?user ?template-title
                   :where
                   [?e :user/email ?user]
                   [?e :user/template ?template]
                   [?template :template/title ?template-title]]
                 db user template-title))))

(defn new-unique-group? [email title]
  (let [db (:db @tx)]
    (empty? (d/q '[:find [?email ...]
                   :in $ ?email ?title
                   :where
                   [?e :user/email ?email]
                   [?e :user/group ?group]
                   [?group :group/title ?title]]
                 db email title))))

(defn new-unique-plan? [email title]
  (let [db (:db @tx)]
    (empty? (d/q '[:find [?email ...]
                   :in $ ?email ?title
                   :where
                   [?e :user/email ?email]
                   [?e :user/plan ?plan]
                   [?plan :plan/title ?title]]
                 db email title))))

(defn new-unique-routine? [email name]
  (let [db (:db @tx)]
    (empty? (d/q '[:find [?email ...]
                   :in $ ?email ?name
                   :where
                   [?e :user/email ?email]
                   [?e :user/routine ?routine]
                   [?routine :routine/name ?name]]
                 db email name))))

(defn new-unique-username? [username]
  (let [db (:db @tx)]
    (empty? (d/q '[:find [?username ...]
                   :in $ ?username
                   :where
                   [?e :user/name ?username]]
                 db
                 username))))

(defn find-user [email]
  (d/pull (:db @tx) '[*] [:user/email email]))

(defn get-user-template-id [email template-title]
  (first (d/q '[:find [?id ...]
                :in $ ?email ?name
                :where
                [?u :user/email ?email]
                [?u :user/template ?id]
                [?id :template/title ?name]]
              (:db @tx) email template-title)))

(defn user-has-template? [email template-id]
  (let [template (entity-by-id template-id)]
    (first
      (flatten
        (d/q '[:find (pull ?t [:db/id])
               :in $ ?email ?created-by ?part ?desc
               :where
               [?u :user/email ?email]
               [?u :user/template ?t]
               [?t :template/created-by ?created-by]
               [?t :template/part ?part]
               [?t :template/description ?desc]]
             (:db @tx)
             email
             (:db/id (:template/created-by template))
             (:template/part template)
             (:template/description template))))))

(defn items-by-category
  "Find all original templates, groups or plans that uses a category in their template parts.
  Input variable type must be one of the keywords :template, :group, :plan."
  [type category]
  (flatten
    (case type
      :template (d/q '[:find (pull ?t [*])
                       :in $ ?category
                       :where
                       #_[?e :user/template ?t]
                       [?t :template/created-by ?e]
                       [?t :template/part ?p]
                       [?p :part/category ?c]
                       [?c :category/name ?category]]
                     (:db @tx) category)
      :group (d/q '[:find (pull ?g [*])
                    :in $ ?category
                    :where
                    #_[?e :user/group ?g]
                    [?g :group/created-by ?e]
                    [?g :group/template ?t]
                    [?t :template/part ?p]
                    [?p :part/category ?c]
                    [?c :category/name ?category]]
                  (:db @tx) category)
      :plan (d/q '[:find (pull ?plan [*])
                   :in $ ?category
                   :where
                   #_[?e :user/plan ?plan]
                   [?plan :plan/created-by ?e]
                   [?plan :plan/day ?d]
                   [?d :day/template ?t]
                   [?t :template/part ?p]
                   [?p :part/category ?c]
                   [?c :category/name ?category]]
                 (:db @tx) category)
      nil)))

(defn items-by-title
  "Finds all original templates, groups or plans by searching for a word in the title.
  Input variable type must be one of the keywords :template, :group, :plan."
  [type title]
  (let [title-relation (case type :template :template/title :group :group/title :plan :plan/title nil)
        user-relation (case type :template :user/template :group :user/group :plan :user/plan nil)
        created-by-relation (case type :template :template/created-by :group :group/created-by :plan :plan/created-by nil)]
    (flatten
      (d/q '[:find (pull ?t [*])
             :in $ ?title-relation ?title ?user-relation ?created-by-relation
             :where
             [(fulltext $ ?title-relation ?title) [[?t ?n]]]
             #_[?e ?user-relation ?t]
             [?t ?created-by-relation ?e]]
           (:db @tx) title-relation title user-relation created-by-relation))))

(defn items-by-description
  "Finds all original templates, groups or plans by searching for a word in the description.
  Input variable type must be one of the keywords :template, :group, :plan."
  [type description]
  (let [desc-item (case type :template :template/description :group :group/description :plan :plan/description nil)
        item (case type :template :user/template :group :user/group :plan :user/plan nil)
        created-by (case type :template :template/created-by :group :group/created-by :plan :plan/created-by nil)]
    (flatten
      (d/q '[:find (pull ?t [*])
             :in $ ?desc-item ?description ?item ?created-by
             :where
             [(fulltext $ ?desc-item ?description) [[?t ?n]]]
             #_[?e ?item ?t]
             [?t ?created-by ?e]]
           (:db @tx) desc-item description item created-by))))

(defn items-by-username
  "Finds all templates, groups or plans a user has created.
  Input variable type must be one of the keywords :template, :group, :plan."
  [type username]
  (let [item (case type :template :user/template :group :user/group :plan :user/plan nil)
        created-by (case type :template :template/created-by :group :group/created-by :plan :plan/created-by nil)]
    (flatten
      (d/q '[:find (pull ?t [*])
             :in $ ?username ?item ?created-by
             :where
             [?e :user/name ?username]
             #_[?e ?item ?t]
             [?t ?created-by ?e]]
           (:db @tx) username item created-by))))

(defn search
  "Search for templates, groups or plans. The first input variable type must be one of the keywords :template, :group or :plan.
  The second input variable is a map of search parameters."
  [type {:keys [n categories title description username]}]
  (let [user-templates (if-not (nil? username) (items-by-username type username))
        category-templates (if-not (nil? categories) (flatten (map #(items-by-category type %) (str/split categories #" "))))
        title-templates (if-not (nil? title) (flatten (map #(items-by-title type %) (str/split title #" "))))
        description-templates (if-not (nil? description) (flatten (map #(items-by-description type %) (str/split description #" "))))
        templates (take (read-string n) (seq (set (concat user-templates title-templates description-templates category-templates))))
        templates (for [t templates]
                    (assoc t :template/created-by (:user/name (d/pull (:db @tx)
                                                                      '[:user/name] (:db/id (:template/created-by t))))
                             :template/part (map #(:part/title
                                                   (d/pull (:db @tx) '[:part/title] (:db/id %)))
                                                 (:template/part t))))]
    templates))

;;;;;;;;;;; TRANSACTIONS ;;;;;;;;;;;;;

(defn add-standard-templates-to-user!
  ""
  [email]
  (let [db (:db @tx)
        conn (:conn @tx)
        templates (flatten (d/q '[:find (pull ?t [*])
                                  :in $ ?name
                                  :where
                                  [?t :template/created-by ?u]
                                  [?u :user/name ?name]]
                                db
                                "movementsession"))
        tx-data [{:db/id         #db/id[:db.part/user -99]
                  :user/email    email
                  :user/template (vec (map :db/id templates))}]]
    (d/transact conn (concat tx-data templates))))

(defn retract-entity! [id]
  (d/transact (:conn @tx) [[:db.fn/retractEntity id]]))

(defn dissoc-template! [email id]
  (let [user-entity-id (:db/id (entity-by-lookup-ref :user/email email))]
    (d/transact (:conn @tx)
                [[:db/retract user-entity-id :user/template id]])))

(defn dissoc-group! [email id]
  (let [user-entity-id (:db/id (entity-by-lookup-ref :user/email email))]
    (d/transact (:conn @tx)
                [[:db/retract user-entity-id :user/group id]])))

(defn assoc-template!
  "Links a template to a user entity."
  [email template]
  (let [conn (:conn @tx)
        tx-data [{:db/id         #db/id[:db.part/user -99]
                  :user/email    email
                  :user/template (:db/id template)}
                 template]]
    (d/transact conn tx-data)))

(defn assoc-group!
  "Links a group to a user entity."
  [email group]
  (let [conn (:conn @tx)
        tx-data [{:db/id      #db/id[:db.part/user -99]
                  :user/email email
                  :user/group (:db/id group)}
                 group]]
    (d/transact conn tx-data)))

(defn assoc-plan!
  "Links a plan by a new id to a user entity. This is done because the plan will be
  updated as the user progresses."
  [email plan]
  (let [conn (:conn @tx)
        plan (assoc plan :db/id (d/tempid :db.part/user)
                         :plan/public? false)
        tx-data [{:db/id      #db/id[:db.part/user -99]
                  :user/email email
                  :user/plan  (:db/id plan)}
                 plan]]
    (d/transact conn tx-data)))

(defn begin-plan!
  "Sets a given plan as the currently ongoing plan, logs the start date and stores which day is the next one."
  [user-id plan-id]
  (let [conn (:conn @tx)
        plan (entity-by-id plan-id)
        current-day (:db/id (first (:plan/day plan)))
        tx-data [[:db/add plan-id :plan/started (Date.)]
                 [:db/add plan-id :plan/current-day current-day]
                 [:db/add user-id :user/ongoing-plan plan-id]]]
    (d/transact conn tx-data)))

(defn progress-plan!
  "Completes a day of a plan and sets the current day to the next day."
  [plan-id]
  (let [conn (:conn @tx)
        plan (entity-by-id plan-id)
        days (:plan/day plan)
        current-day (:plan/current-day plan)
        day-id (:db/id current-day)
        current-day-pos (first (positions #{current-day} days))
        new-current-day (:db/id (get days (inc current-day-pos)))
        tx-data (if (nil? new-current-day)
                  [[:db/add day-id :day/completed? true]]
                  [[:db/add day-id :day/completed? true]
                   [:db/add plan-id :plan/current-day new-current-day]])]
    (d/transact conn tx-data)))

(defn end-plan!
  "Removes the plan as the users ongoing plan. Logs the date and sets the plan as completed if all the plan days where completed."
  [user-id plan-id]
  (let [conn (:conn @tx)
        db (:db @tx)
        plan (d/pull db '[*] plan-id)
        days (map #(d/pull db '[*] (:db/id %)) (:plan/day plan))
        all-completed? (every? #(true? (:day/completed? %)) days)
        tx-data [[:db/add plan-id :plan/ended (Date.)]
                 [:db/add plan-id :plan/completed? all-completed?]
                 [:db/retract user-id :user/ongoing-plan plan-id]]]
    (d/transact conn tx-data)))

(defn transact-template!
  "Adds a template to the database."
  [{:keys [title description parts public? created-by]}]
  (let [conn (:conn @tx)
        description (if (nil? description) "" description)
        public? (if (nil? public?) true public?)
        parts (map #(assoc % :db/id (d/tempid :db.part/user)) parts)
        template-data [{:db/id                #db/id[:db.part/user -100]
                        :template/title       title
                        :template/part        (vec (for [p parts] (:db/id p)))
                        :template/description description
                        :template/public?     public?
                        :template/created-by  #db/id[:db.part/user -101]}
                       {:db/id     #db/id[:db.part/user -101]
                        :user/name created-by}]
        template-data (remove nil? template-data)
        parts (map #(rename-keys % {:n                  :part/number-of-movements
                                    :categories         :part/category
                                    :specific-movements :part/specific-movement
                                    :title              :part/title
                                    :rep                :part/rep
                                    :set                :part/set
                                    :distance           :part/distance
                                    :duration           :part/duration
                                    :weight             :part/weight
                                    :rest               :part/rest
                                    }) parts)
        parts (map #(assoc %
                     :part/category (vec (for [c (:part/category %)] {:db/id         (d/tempid :db.part/user)
                                                                      :category/name c}))
                     :part/specific-movement (vec (for [m (:part/specific-movement %)] {:db/id         (d/tempid :db.part/user)
                                                                                        :movement/name m}))) parts)
        part-data (map #(assoc %
                         :part/category (vec (for [c (:part/category %)] (:db/id c)))
                         :part/specific-movement (vec (for [m (:part/specific-movement %)] (:db/id m)))) parts)
        part-data (for [p part-data] (if (empty? (:part/specific-movement p)) (dissoc p :part/specific-movement) p))
        part-data (for [p part-data] (if (empty? (:part/category p)) (dissoc p :part/category) p))
        category-data (flatten (map #(:part/category %) parts))
        specific-movement-data (flatten (map #(:part/specific-movement %) parts))
        tx-data (concat template-data part-data category-data specific-movement-data)]
    (d/transact conn tx-data)))

(defn transact-group!
  "Adds a group to the database."
  [{:keys [title created-by public? description templates]}]
  (let [description (if (nil? description) "" description)
        public? (if (nil? public?) true public?)
        template-ids (map :db/id templates)
        tx-data [{:db/id             #db/id[:db.part/user -100]
                  :group/title       title
                  :group/description description
                  :group/template    template-ids
                  :group/public?     public?
                  :group/created-by  #db/id[:db.part/user -101]}
                 {:db/id     #db/id[:db.part/user -101]
                  :user/name created-by}]]
    (d/transact (:conn @tx) tx-data)))

(defn transact-plan!
  "Adds a plan to the database."
  [{:keys [title created-by public? description plan]}]
  (let [description (if (nil? description) "" description)
        public? (if (nil? public?) true public?)
        tx-days (vec (for [day plan]
                       (let [template-ids (vec (map :db/id day))]
                         {:db/id          (d/tempid :db.part/user)
                          :day/completed? false
                          :day/template   (if (= [nil] template-ids)
                                            []
                                            template-ids)})))
        tx-data [{:db/id            #db/id[:db.part/user -100]
                  :plan/title       title
                  :plan/description description
                  :plan/public?     public?
                  :plan/completed?  false
                  :plan/day         (vec (map :db/id tx-days))
                  :plan/created-by  #db/id[:db.part/user -102]}
                 {:db/id     #db/id[:db.part/user -102]
                  :user/name created-by}]
        tx-data (concat tx-data tx-days)]
    (d/transact (:conn @tx) tx-data)))

(defn transact-routine!
  "Adds a routine to the database."
  [{:keys [name created-by public? description movements]}]
  (let [description (if (nil? description) "" description)
        public? (if (nil? public?) true public?)
        movement-ids (vec (map #(:db/id (entity-by-movement-name %)) movements))
        tx-data [{:db/id               #db/id[:db.part/user -100]
                  :routine/name        name
                  :routine/description description
                  :routine/movement    movement-ids
                  :routine/public?     public?
                  :routine/created-by  #db/id[:db.part/user -101]}
                 {:db/id     #db/id[:db.part/user -101]
                  :user/name created-by}]]
    (d/transact (:conn @tx) tx-data)))

(defn transact-new-movements!
  "All movements from a session that the user has never done before are linked to the user."
  [user parts]
  (let [session-movements (into #{} (flatten (for [part parts] (for [m (vals (:movements part))] (:unique m)))))
        user-movements (into #{} (map :movement/name (d/q '[:find [(pull ?m [:movement/name]) ...]
                                                            :in $ ?email
                                                            :where
                                                            [?u :user/email ?email]
                                                            [?u :user/movements ?m]]
                                                          (:db @tx)
                                                          user)))
        new-movements (set/difference session-movements user-movements)
        new-movements (for [m new-movements] {:db/id         (d/tempid :db.part/user)
                                              :movement/name m
                                              :movement/zone {:db/id    (d/tempid :db.part/user)
                                                              :db/ident :zone/one}})
        user-movement-data [{:db/id          #db/id[:db.part/user -100]
                             :user/email     user
                             :user/movements (map :db/id new-movements)}]]
    (d/transact (:conn @tx) (concat user-movement-data new-movements))))

(defn transact-session!
  "Adds a completed session to the database."
  [user {:keys [title description parts comment time plan-id template-id]}]
  (let [; convert sorted-map to list and create part temp-id's
        parts (map (fn [part] (assoc part :movements (vals (:movements part)) :db/id (d/tempid :db.part/user))) parts)
        ; remove nil values from part and movement data. Create movement temp-id's.
        parts (for [part parts]
                (let [part (apply dissoc part (for [[k v] part :when (nil? v)] k))
                      movements (for [m (:movements part)] (apply dissoc m (for [[k v] m :when (nil? v)] k)))
                      movements (for [m movements] (assoc m :db/id (d/tempid :db.part/user)))]
                  (assoc part :movements movements)))
        ; create user and session data
        user-session-data [{:db/id        #db/id[:db.part/user -100]
                            :user/email   user
                            :user/session [#db/id[:db.part/user -101]]}
                           {:db/id               #db/id[:db.part/user -101]
                            :session/url         (str (java.util.UUID/randomUUID))
                            :session/title       title
                            :session/description description
                            :session/comment     comment
                            :session/timestamp   (Date.)
                            :session/part        (vec (map :db/id parts))
                            :session/time        time
                            :session/plan        plan-id
                            :session/template    template-id}]
        ; remove nil values
        user-session-data (for [s user-session-data] (apply dissoc s (for [[k v] s :when (nil? v)] k)))
        ; create part data
        part-data (for [part parts] {:db/id                 (:db/id part)
                                     :part/title            (:title part)
                                     :part/session-movement (vec (map :db/id (:movements part)))})
        ; extract movements from parts
        movement-data (flatten (for [part parts] (for [m (:movements part)] m)))
        ; remove unnecessary data
        movement-data (map #(dissoc % :zone :category :practical :measurement :harder :easier :id) movement-data)
        ; rename data to match schema
        movement-data (map #(rename-keys % {:unique   :movement/name
                                            :rep      :movement/rep
                                            :set      :movement/set
                                            :distance :movement/distance
                                            :duration :movement/duration
                                            :weight   :movement/weight
                                            :rest     :movement/rest}) movement-data)]
    ; transact all datoms
    (d/transact (:conn @tx) (concat user-session-data part-data movement-data))))

(defn transact-new-user!
  "Adds a new user to the database."
  [email password activation-id]
  (let [conn (:conn @tx)
        tx-user-data [{:db/id              #db/id[:db.part/user]
                       :user/email         email
                       :user/password      (hashers/encrypt password)
                       :user/activation-id activation-id
                       :user/activated?    false}]]
    (d/transact conn tx-user-data)))

(defn transact-activated-user!
  "Updates a user to activated status."
  [email]
  (let [conn (:conn @tx)
        tx-user-data [{:db/id                    #db/id[:db.part/user -99]
                       :user/email               email
                       :user/activated?          true
                       :user/activation-id       (generate-activation-id)
                       :user/sign-up-timestamp   (Date.)
                       :user/valid-subscription? false
                       :user/setting             [#db/id[:db.part/user -100]]}
                      {:db/id                  #db/id[:db.part/user -100]
                       :setting/view           "Standard"
                       :setting/receive-email? true}]]
    (d/transact conn tx-user-data)))

(defn transact-subscription-status!
  "Updates a user subscription status. Sets the valid-subscription? value to true or false."
  [email value]
  (let [conn (:conn @tx)
        tx-user-data [{:db/id                    #db/id[:db.part/user]
                       :user/email               email
                       :user/valid-subscription? value}]]
    (d/transact conn tx-user-data)))

(defn transact-new-password!
  "Change user password."
  [email password]
  (let [conn (:conn @tx)
        tx-user-data [{:db/id         #db/id[:db.part/user]
                       :user/email    (:user/email email)
                       :user/password (hashers/encrypt password)}]]
    (d/transact conn tx-user-data)
    "Password changed successfully!"))

(defn transact-username!
  "Change user username."
  [email username]
  (let [conn (:conn @tx)
        tx-user-data [{:db/id      #db/id[:db.part/user]
                       :user/email email
                       :user/name  username}]]
    (d/transact conn tx-user-data)
    "Username changed successfully!"))

