(ns movement.db
  (:import java.util.Date)
  (:require [buddy.hashers :as hashers]
            [datomic.api :as d]
            [clojure.string :as str]
            [clojure.set :refer [rename-keys]]
            [taoensso.timbre :refer [info error]]
            [movement.activation :refer [generate-activation-id send-activation-email]]))

(def uri "datomic:dev://localhost:4334/testing6")
#_(def uri "datomic:ddb://us-east-1/movementsession/production?aws_access_key_id=AKIAJI5GV57L43PZ6MSA&aws_secret_key=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")

(def tx (atom {}))

(defn update-tx-conn! [] (swap! tx assoc :conn (d/connect uri)))

(defn update-tx-db! [] (swap! tx assoc :db (d/db (:conn @tx))))

(defn all-movement-names
  "Returns the names of all movements in the database."
  []
  (d/q '[:find [?n ...] :where [_ :movement/unique-name ?n]] (:db @tx)))

(defn all-equipment-names
  "Returns the all equipment names in the database."
  []
  (d/q '[:find [?n ...] :where [_ :equipment/name ?n]] (:db @tx)))

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

(defn get-movement-from-equipment [e]
  (let [r (d/q '[:find (pull ?m [*])
                 :in $ ?name
                 :where
                 [?e :equipment/name ?name]
                 [?m :movement/equipment ?e]]
               (:db @tx)
               e)
        m (->> r flatten set shuffle (take 1))]
    m))

(defn get-n-movements-from-categories
  "Get n random movement entities drawn from param list of categories."
  [n categories d]
  (let [db (:db @tx)
        movements (for [c categories]
                    (d/q '[:find (pull ?m [*]) :in $ ?cat
                           :where [?c :category/name ?cat] [?m :movement/category ?c]]
                         db c))
        m (->> movements flatten set shuffle (take n))
        m (map #(assoc % :rep (:rep d) :set (:set d) :distance (:distance d) :duration (:duration d)) m)]
    m))

(defn all-template-titles [email]
  (d/q '[:find [?title ...]
         :in $ ?email
         :where
         [?e :user/email ?email]
         [?e :user/template ?t]
         [?t :template/title ?title]]
       (:db @tx)
       email))

(defn all-group-titles [email]
  (d/q '[:find [?title ...]
         :in $ ?email
         :where
         [?e :user/email ?email]
         [?e :user/group ?g]
         [?g :group/title ?title]]
       (:db @tx)
       email))

(defn all-routine-titles [email]
  (d/q '[:find [?name ...]
         :in $ ?email
         :where
         [?e :user/email ?email]
         [?e :user/routine ?r]
         [?r :routine/name ?name]]
       (:db @tx)
       email))

(defn create-session [title user]
  ;todo: refactor, smaller functions
  (let [db (:db @tx)
        title-entity (ffirst (d/q '[:find (pull ?t [*])
                                    :in $ ?title ?email
                                    :where
                                    [?e :user/email ?email]
                                    [?e :user/template ?t]
                                    [?t :template/title ?title]]
                                  db
                                  title
                                  user))
        part-entities (map #(d/pull db '[*] %) (vec (flatten (map vals (:template/part title-entity)))))
        parts (vec (for [p part-entities]
                     (let [name (:part/title p)
                           n (:part/number-of-movements p)
                           c (flatten (map vals (:part/category p)))
                           category-names (vec (flatten (map vals (map #(d/pull db '[:category/name] %) c))))
                           movements (if (or (nil? n) (zero? n))
                                       []
                                       (vec (get-n-movements-from-categories n category-names {:rep      (:part/rep p)
                                                                                               :set      (:part/set p)
                                                                                               :distance (:part/distance p)
                                                                                               :duration (:part/duration p)})))]
                       {:title      name
                        :categories category-names
                        :movements  (if-let [specific-movements (vec (map #(d/pull db '[*] (:db/id %)) (:part/specific-movement p)))]
                                      (let [specific-movements (map #(assoc % :rep (:part/rep p)
                                                                              :set (:part/set p)
                                                                              :distance (:part/distance p)
                                                                              :duration (:part/duration p)) specific-movements)]
                                        (concat specific-movements movements))
                                      movements)})))]
    {:title       title
     :description (:template/description title-entity)
     :background  (:template/background title-entity)
     :parts       parts}))

(defn pick-template-title-from-group [email group]
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
                       group)
        template-title (:template/title (ffirst (shuffle templates)))]
    template-title))

(defn get-routine [email routine]
  (let [db (:db @tx)
        ]))

(defn create-equipment-session [name n]
  (let [db (:db @tx)
        r (d/q '[:find (pull ?m [*])
                 :in $ ?name
                 :where
                 [?e :equipment/name ?name]
                 [?m :movement/equipment ?e]]
               db
               name)
        m (->> r flatten set shuffle (take n) vec)]
    {:title "Let's play with.."
     :parts [{:title      (str/capitalize name)
              :categories []
              :equipment  name
              :movements  m}]}))

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
     :time        (:session/time session)}))

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

;;;;;;;;;;; TRANSACTIONS ;;;;;;;;;;;;;

(defn transact-template! [user {:keys [title description parts]}]
  (let [conn (:conn @tx)
        description (if (nil? description) "" description)
        parts (map #(assoc % :db/id (d/tempid :db.part/user)) parts)
        user-template-data [{:db/id         #db/id[:db.part/user -99]
                             :user/email    user
                             :user/template [#db/id[:db.part/user -100]]}
                            {:db/id                #db/id[:db.part/user -100]
                             :template/title       title
                             :template/part        (vec (for [p parts] (:db/id p)))
                             :template/description description}]
        user-template-data (remove nil? user-template-data)
        parts (map #(rename-keys % {:n                  :part/number-of-movements
                                    :categories         :part/category
                                    :specific-movements :part/specific-movement
                                    :title              :part/title
                                    :rep                :part/rep
                                    :set                :part/set
                                    :distance           :part/distance
                                    :duration           :part/duration}) parts)
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
        tx-data (concat user-template-data part-data category-data specific-movement-data)]
    (d/transact conn tx-data)))

(defn transact-group! [email {:keys [title created-by public? description templates]}]
  (let [description (if (nil? description) "" description)
        template-ids (vec (map #(get-user-template-id email %) templates))
        tx-data [{:db/id      #db/id[:db.part/user -99]
                  :user/email email
                  :user/group [#db/id[:db.part/user -100]]}
                 {:db/id             #db/id[:db.part/user -100]
                  :group/title       title
                  :group/description description
                  :group/template    template-ids
                  :group/public?     public?
                  :group/created-by  #db/id[:db.part/user -101]}
                 {:db/id     #db/id[:db.part/user -101]
                  :user/name created-by}]]
    (d/transact (:conn @tx) tx-data)))

(defn transact-routine! [email {:keys [name created-by public? description movements]}]
  (let [description (if (nil? description) "" description)
        movement-ids (vec (map #(:db/id (entity-by-movement-name %)) movements))
        tx-data [{:db/id        #db/id[:db.part/user -99]
                  :user/email   email
                  :user/routine [#db/id[:db.part/user -100]]}
                 {:db/id               #db/id[:db.part/user -100]
                  :routine/name        name
                  :routine/description description
                  :routine/movement    movement-ids
                  :routine/public?     public?
                  :routine/created-by  #db/id[:db.part/user -101]}
                 {:db/id     #db/id[:db.part/user -101]
                  :user/name created-by}]]
    (d/transact (:conn @tx) tx-data)))

(defn transact-session! [user {:keys [title description parts comment time]}]
  (let [conn (:conn @tx)
        parts (map #(assoc % :movements (vals (:movements %))
                             :db/id (d/tempid :db.part/user)) parts)
        parts (map
                #(assoc %
                  :movements (map
                               (fn [e] (assoc e :db/id (d/tempid :db.part/user))) (:movements %))) parts)
        session-data [{:db/id        #db/id[:db.part/user -99]
                       :user/email   user
                       :user/session [#db/id[:db.part/user -100]]}
                      {:db/id               #db/id[:db.part/user -100]
                       :session/url         (str (java.util.UUID/randomUUID))
                       :session/title       title
                       :session/description description
                       :session/comment     comment
                       :session/timestamp   (Date.)
                       :session/part        (vec (map :db/id parts))
                       :session/time        time}]
        part-data (vec (for [p parts]
                         {:db/id                 (:db/id p)
                          :part/title            (:title p)
                          :part/session-movement (vec (map :db/id (:movements p)))}))
        movement-data (vec (flatten (for [p parts]
                                      (for [m (:movements p)]
                                        (apply dissoc m (for [[k v] m :when (nil? v)] k))))))
        movement-data (map #(rename-keys % {:movement/unique-name :movement/name
                                            :rep                  :movement/rep
                                            :set                  :movement/set
                                            :distance             :movement/distance
                                            :duration             :movement/duration
                                            :id                   :movement/position})
                           movement-data)
        tx-data (concat session-data part-data movement-data)]
    (d/transact conn tx-data)))

(defn transact-new-user! [email password activation-id]
  (let [conn (:conn @tx)
        tx-user-data [{:db/id              #db/id[:db.part/user]
                       :user/email         email
                       :user/password      (hashers/encrypt password)
                       :user/activation-id activation-id
                       :user/activated?    false}]]
    (d/transact conn tx-user-data)))

(defn transact-activated-user! [email]
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

(defn transact-subscription-status! [email value]
  (let [conn (:conn @tx)
        tx-user-data [{:db/id                    #db/id[:db.part/user]
                       :user/email               email
                       :user/valid-subscription? value}]]
    (d/transact conn tx-user-data)))

(defn transact-new-password! [email password]
  (let [conn (:conn @tx)
        tx-user-data [{:db/id         #db/id[:db.part/user]
                       :user/email    (:user/email email)
                       :user/password (hashers/encrypt password)}]]
    (d/transact conn tx-user-data)
    "Password changed successfully!"))

(defn transact-username! [email username]
  (let [conn (:conn @tx)
        tx-user-data [{:db/id      #db/id[:db.part/user]
                       :user/email email
                       :user/name  username}]]
    (d/transact conn tx-user-data)
    "Username changed successfully!"))

