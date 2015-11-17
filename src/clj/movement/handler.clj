(ns movement.handler
  (:import java.util.Date)
  (:require [clojure.java.io :as io]
            [compojure.core :refer [GET POST ANY defroutes]]
            [compojure.route :refer [not-found resources]]
            [compojure.response :refer [render]]
            [ring.util.response :refer [redirect]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.x-headers :refer [wrap-frame-options]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [selmer.parser :refer [render-file]]
            [prone.middleware :refer [wrap-exceptions]]
            [environ.core :refer [env]]
            [datomic.api :as d]
            [clojure.string :as str]
            [clojure.set :refer [rename-keys]]
            [clj-time.core :as time :refer [from-now hours]]
            [buddy.sign.jws :as jws]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.hashers :as hashers]
            [hiccup.core :refer [html]]

            [movement.db]
            [movement.pages.landing :refer [landing]]
            ))

(def uri "datomic:dev://localhost:4334/movement14")
(def conn (d/connect uri))
(def db (d/db conn))
(selmer.parser/set-resource-path!  (clojure.java.io/resource "templates"))

(defn generate-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body    (pr-str data)})

(defn all-movement-names []
  "Returns the names of all movements in the database."
  (let [movements (d/q '[:find [?n ...]
                         :where
                         [_ :movement/name ?n]]
                       db)]
    (generate-response movements)))

(defn all-equipment-names []
  "Returns the all equipment names in the database."
  (let [equipment (d/q '[:find [?n ...]
                         :where
                         [_ :equipment/name ?n]]
                       db)]
    (generate-response equipment)))

(defn all-category-names []
  "Returns the names of all categories in the database."
  (let [categories (d/q '[:find [?n ...]
                          :where
                          [_ :category/name ?n]]
                        db)]
    (generate-response categories)))

(defn get-movement-entity [name]
  "Returns the whole entity of a named movement."
  (let [movement (d/pull db '[*] [:movement/name name])]
    movement))

(defn get-movement-from-equipment [e]
  (let [r (d/q '[:find (pull ?m [*])
                 :in $ ?name
                 :where
                 [?e :equipment/name ?name]
                 [?m :movement/equipment ?e]]
               db
               e)
        m (->> r flatten set shuffle (take 1))]
    (generate-response m)))

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

(defn get-new-difficulty-movement [movement difficulty]
  (case difficulty
    :easier (let [r (d/q '[:find (pull ?e [*])
                           :in $ ?id
                           :where
                           [?m :db/id ?id]
                           [?m :movement/harder ?e]]
                         db
                         movement)
                  m (->> r flatten set shuffle (take 1))]
              (generate-response m))
    :harder (let [r (d/q '[:find (pull ?e [*])
                           :in $ ?id
                           :where
                           [?m :db/id ?id]
                           [?m :movement/easier ?e]]
                         db
                         movement)
                  m (->> r flatten set shuffle (take 1))]
              (generate-response m))
    nil))

(defn all-template-titles [email]
  (let [db (d/db conn)
        templates (d/q '[:find [?title ...]
                         :in $ ?email
                         :where
                         [?e :user/email ?email]
                         [?e :user/template ?t]
                         [?t :template/title ?title]]
                       db
                       email)]
    (generate-response templates)))

(defn create-session [title user]
  (let [title-entity (ffirst (d/q '[:find (pull ?t [*])
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
                           movements (if (nil? n) [] (vec (get-n-movements-from-categories n category-names
                                                                                           {:rep      (:part/rep p)
                                                                                           :set      (:part/set p)
                                                                                           :distance (:part/distance p)
                                                                                           :duration (:part/duration p)})))]
                       {:title      name
                        :categories category-names
                        :movements  (if-let [regular-movements (vec (map #(d/pull db '[*] (:db/id %)) (:part/specific-movement p)))]
                                      (concat regular-movements movements)
                                      movements)})))]

    (generate-response {:title       title
                        :description (:template/description title-entity)
                        :background (:template/background title-entity)
                        :parts       parts})))

(defn create-equipment-session [name n]
  (let [r (d/q '[:find (pull ?m [*])
                 :in $ ?name
                 :where
                 [?e :equipment/name ?name]
                 [?m :movement/equipment ?e]]
               db
               name)
        m (->> r flatten set shuffle (take n) vec)]
    (generate-response {:title "Let's play with.."
                        :parts [{:title      (str/capitalize name)
                                 :categories []
                                 :equipment  name
                                 :movements  m}]})))

(defn new-unique-template? [user template-title]
  (empty? (d/q '[:find [?user ...]
                 :in $ ?user ?template-title
                 :where
                 [?e :user/email ?user]
                 [?e :user/template ?template]
                 [?template :template/title ?template-title]]
               db
               user
               template-title)))

(defn transact-template! [user {:keys [title description background parts] :as template}]
  (let [parts (map #(assoc % :db/id (d/tempid :db.part/user)) parts)
        user-template-data [{:db/id         #db/id[:db.part/user]
                             :user/email    user
                             :user/template [#db/id[:db.part/user -100]]}
                            {:db/id                #db/id[:db.part/user -100]
                             :template/title       title
                             :template/part        (vec (for [p parts] (:db/id p)))}
                            (when description
                              {:db/id #db/id[:db.part/user -100]
                               :template/description description})
                            (when background
                              {:db/id #db/id[:db.part/user -100]
                               :template/background background})]
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

(defn transact-session! [user {:keys [title description parts comment]}]
  (let [parts (map #(assoc % :movements (vals (:movements %))
                             :db/id (d/tempid :db.part/user)) parts)
        parts (map
                #(assoc %
                  :movements (map
                               (fn [e] (assoc e :db/id (d/tempid :db.part/user))) (:movements %))) parts)
        session-data [{:db/id        #db/id[:db.part/user]
                       :user/email   user
                       :user/session [#db/id[:db.part/user -100]]}
                      {:db/id               #db/id[:db.part/user -100]
                       :session/name        title
                       :session/description description
                       :session/comment     (str/join (interpose " " comment))
                       :session/timestamp   (Date.)
                       :session/part (vec (map :db/id parts))}]
        part-data (vec (for [p parts]
                         {:db/id                 (:db/id p)
                          :part/title            (:title p)
                          :part/session-movement (vec (map :db/id (:movements p)))}))
        movement-data (vec (flatten (for [p parts]
                                      (for [m (:movements p)]
                                        (apply dissoc m (for [[k v] m :when (nil? v)] k))))))
        movement-data (map #(rename-keys % {:rep :movement/rep :set :movement/set
                                            :distance :movement/distance :duration :movement/duration
                                            :id :movement/position-in-part})
                           movement-data)
        tx-data (concat session-data part-data movement-data)]
    (d/transact conn tx-data)))

(defn add-session! [req]
  (let [session (:session (:params req))
        user (:user (:params req))]
    (if (nil? user)
      (generate-response {:message "User email lacking from client data. User not logged in?"
                          :session session} 400)
      (try
        (transact-session! user session)
        (catch Exception e
          (generate-response {:message (str "Exception: " e)}))
        (finally (generate-response {:session session :message "Session stored successfully."}))))))

(defn add-template! [req]
  (let [template (:params req)
        user (:user template)]
    (if (nil? user)
      (generate-response (str "User email lacking from client data. User not logged in?" " template: " template) 400)
      (if (new-unique-template? user (:title template))
        (try
          (transact-template! user template)
          (catch Exception e
            (generate-response {:message (str "Exception: " e)}))
          (finally (generate-response {:template template :message "New template stored successfully."})))
        (generate-response {:message "You already have a template with this title. Choose a unique title for your template."} 400)))))

(defn retrieve-sessions [req]
  (let [db (d/db conn)
        user (:user (:params req))
        sessions (vec (flatten
                        (d/q '[:find (pull ?s [*])
                               :in $ ?mail
                               :where
                               [?m :user/email ?mail]
                               [?m :user/session ?s]]
                             db
                             user)))]
    (generate-response sessions)))

(defn find-user [email]
  (let [db (d/db conn)]
    (d/pull db '[*] [:user/email email])))

(defn valid-user? [user password]
  (hashers/check password (:user/password user)))

(def secret "mysupersecret")

(defn jws-login
  [email password]
  (let [user (find-user email)]
    (if-not (nil? (:db/id user))
      (let [valid? (valid-user? user password)]
        (if valid?
          (let [claims {:user (keyword email)
                        :exp  (-> 3 hours from-now)}
                token (jws/sign claims secret {:alg :hs512})]
            (generate-response {:token token :user (:user/email (dissoc user :user/password :db/id))}))
          (generate-response {:message "Wrong email/password combination"} 401)))
      (generate-response {:message "Unknown email"} 400))))

(def jws-auth-backend (jws-backend {:secret secret :options {:alg :hs512}}))
;;;;;;

(defn add-user [email password]
  (let [tx-user-data [{:db/id #db/id[:db.part/user]
                       :user/email email
                       :user/password (hashers/encrypt password)}]]
    (d/transact conn tx-user-data)))

(defn add-user! [email password]
  (if (nil? (:db/id (find-user email)))
    (do
      (add-user email password)
      (generate-response {:message "New user created successfully"}))
    (generate-response {:message "This email is already registered as a user"} 400)))

;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
           (GET "/" [] (landing))

           (GET "/app" [] (render-file "app.html" {:dev        (env :dev?)
                                                   :csrf-token *anti-forgery-token*}))

           (POST "/login" [username password] (jws-login username password))

           (POST "/signup" [username password] (add-user! username password))

           (POST "/store-session" req (if-not (authenticated? req)
                                        (throw-unauthorized)
                                        (add-session! req)))

           (POST "/template" req (if-not (authenticated? req)
                                   (throw-unauthorized)
                                   (add-template! req)))

           (GET "/sessions" req (if-not (authenticated? req)
                                  (throw-unauthorized)
                                  (retrieve-sessions req)))

           (GET "/template" req (if-not (authenticated? req)
                                  (throw-unauthorized)
                                  (let [template-name (:template-name (:params req))
                                        user (:user (:params req))]
                                    (create-session template-name user))))

           (GET "/templates" req (if-not (authenticated? req)
                                   (throw-unauthorized)
                                   (all-template-titles (str (:user (:params req))))))

           (GET "/movement" req (if-not (authenticated? req)
                                  (throw-unauthorized)
                                  (get-movement-entity (:name (:params req)))))

           (GET "/movements" req (if-not (authenticated? req)
                                   (throw-unauthorized)
                                   (all-movement-names)))

           (GET "/equipment" req (if-not (authenticated? req)
                                   (throw-unauthorized)
                                   (all-equipment-names)))

           (GET "/equipment-session" req (if-not (authenticated? req)
                                           (throw-unauthorized)
                                           (let [equipment (:equipment (:params req))]
                                             (create-equipment-session equipment 5))))

           (GET "/movement-from-equipment" req (let [e (:equipment (:params req))
                                                     difficulty (:difficulty (:params req))]
                                                 (if-not (authenticated? req)
                                                   (throw-unauthorized)
                                                   (get-movement-from-equipment e))))

           (GET "/singlemovement" req
             (let [categories (vec (vals (:categories (:params req))))
                   difficulty (:difficulty (:params req))]
               (if-not (authenticated? req)
                 (throw-unauthorized)
                 (generate-response (get-n-movements-from-categories 1 categories {})))))

           (GET "/movement-from-difficulty" req (let [movement (:movement (:params req))
                                                      difficulty (:difficulty (:params req))]
                                                 (if-not (authenticated? req)
                                                   (throw-unauthorized)
                                                   (get-new-difficulty-movement movement difficulty))))

           (GET "/categories" req (if-not (authenticated? req)
                                    (throw-unauthorized)
                                    (all-category-names)))

           (resources "/")

           (not-found "Not Found")

           (GET "/raw" [] (render-file "indexraw.html" {:dev (env :dev?)}))

           )

(def app
  (let [handler (-> routes
                    (wrap-authentication jws-auth-backend)
                    (wrap-authorization jws-auth-backend)
                    (wrap-edn-params)
                    (wrap-params)
                    (wrap-session)
                    (wrap-defaults site-defaults)
                    (wrap-frame-options {:allow-from (or "http://movementsession.com"
                                                         "http://www.movementsession.com")}))]
    (if (env :dev?)
      (wrap-reload (wrap-exceptions handler))
      handler)))