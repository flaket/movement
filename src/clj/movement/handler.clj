(ns movement.handler
  (:require [compojure.core :refer [GET ANY defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.util.response :refer [redirect]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.x-headers :refer [wrap-frame-options]]
            [selmer.parser :refer [render-file]]
            [prone.middleware :refer [wrap-exceptions]]
            [environ.core :refer [env]]
            [datomic.api :as d]
            [clojure.string :as str]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

;; temp in-memory user "database"
(def users1
  {"admin" {:username "admin"
           :password (creds/hash-bcrypt "adminpassword")
           :roles #{::admin}}
   "jane" {:username "jane"
           :password (creds/hash-bcrypt "pw")
           :roles #{::user}}})

(derive ::admin ::user)

(def uri "datomic:dev://localhost:4334/movement7")
(def conn (d/connect uri))
(def db (d/db conn))

(defn get-user-creds
  "Queries the database for user info."
  [creds]
  (let [users (d/q '[:find ?email ?pw ?r
                     :in $
                     :where
                     [?e :user/email ?email]
                     [?e :user/password ?pw]
                     [?e :user/role ?r]]
                   db)
        users (map #(zipmap [:username :password :roles] %) users)
        users (map #(assoc % :roles (read-string (:roles %))) users)
        users (zipmap (map #(:username %) users) users)]
    users))

(defn verify-credentials
  [creds]
  (let [valid-user (creds/bcrypt-credential-fn get-user-creds creds)]
    valid-user))

(selmer.parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))

(defn generate-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body    (pr-str data)})

(defn all-movement-names []
  "Returns the names of all movements in the database."
  (let [movements (d/q '[:find ?n
                         :where
                         [_ :movement/name ?n]]
                       db)]
    (generate-response movements)))

(defn movement [name]
  "Returns the whole entity of a named movement."
  (let [movement (d/pull db '[*] [:movement/name name])]
    (generate-response movement)))

(defn get-movements [n categories]
  "Get n random movement entities drawn from param list of categories."
  (let [movements (for [c categories]
                    (d/q '[:find (pull ?m [*]) :in $ ?cat
                           :where [?c :category/name ?cat] [?m :movement/category ?c]]
                         db c))
        m (->> movements flatten set shuffle (take n))]
    m))

(defn all-template-titles []
  (let [db (d/db conn)
        templates (d/q '[:find [?t ...]
                         :where
                         [_ :template/title ?t]]
                       db)]
    (generate-response templates)))

(defn create-session [title]
  (let [title-entity (d/pull db '[*] [:template/title title])
        description (:template/description title-entity)
        part-entities (map #(d/pull db '[*] %) (vec (flatten (map vals (:template/part title-entity)))))
        parts (vec (for [p part-entities]
                     (let [name (:part/name p)
                           n (:part/number-of-movements p)
                           c (flatten (map vals (:part/category p)))
                           category-names (vec (flatten (map vals (map #(d/pull db '[:category/name] %) c))))
                           movements (vec (get-movements n category-names))]
                       {:title      name
                        :categories category-names
                        :movements movements})))
        ]
    (generate-response {:title       title
                        :description description
                        :parts       parts})))

(defroutes routes
           (GET "/" []
             (friend/authorize #{::user} (render-file "templates/index.html" {:dev (env :dev?)})))
           (GET "/raw" [] (render-file "templates/indexraw.html" {:dev (env :dev?)}))

           (GET "/admin" request
             (friend/authorize #{::admin} "This page can only be seen by administrators."))
           (GET "/authorized" request
             (friend/authorize #{::user} "This page can only be seen by authenticated users."))
           (GET "/login" [] (render-file "templates/login.html" {:dev (env :dev?)}))

           (GET "/movements" [] (all-movement-names))
           (GET "/movement/:name" [name] (movement name))
           (GET "/templates" [] (all-template-titles))
           (GET "/template/:title" [title] (create-session (str/replace title "-" " ")))
           (GET "/singlemovement/:category" [category] (generate-response (get-movements 1 [(str/replace category "-" " ")])))

           (GET "/user/:id" [id] (generate-response (str "Hello user " id)))

           (friend/logout (ANY "/logout" request (redirect "/")))
           (resources "/")
           (not-found "Not Found"))

(def app
  (let [handler (wrap-frame-options
                  (wrap-defaults
                    (friend/authenticate routes {:credential-fn #_(partial creds/bcrypt-credential-fn get-user-creds)
                                                 #_(verify-credentials)
                                                 (partial creds/bcrypt-credential-fn users1)
                                                 :workflows     [(workflows/interactive-form)]})
                    site-defaults)
                  {:allow-from (or "http://movementsession.com"
                                   "http://www.movementsession.com")})]
    (if (env :dev?) (wrap-reload (wrap-exceptions handler)) handler)))