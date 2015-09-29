(ns movement.handler
  (:import org.apache.commons.codec.binary.Base64
           java.nio.charset.Charset)
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
            [clj-time.core :as time]
            [buddy.sign.jws :as jws]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]))

(def uri "datomic:dev://localhost:4334/movement8")
(def conn (d/connect uri))
(def db (d/db conn))
(selmer.parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(selmer.parser/set-resource-path!  (clojure.java.io/resource "templates"))

(defn generate-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body    (pr-str data)})

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
        templates (d/q '[:find [?title ...]
                         :in $ ?email
                         :where
                         [?e :user/email ?email]
                         [?e :user/template ?t]
                         [?t :template/title ?title]]
                       db
                       "admin")]
    (generate-response templates)))

(defn create-session [title]
  (let [title-entity (ffirst (d/q '[:find (pull ?t [*])
                                    :in $ ?title ?email
                                    :where
                                    [?e :user/email ?email]
                                    [?e :user/template ?t]
                                    [?t :template/title ?title]]
                                  db
                                  title
                                  "admin"))
        description (:template/description title-entity)
        part-entities (map #(d/pull db '[*] %) (vec (flatten (map vals (:template/part title-entity)))))
        parts
        (vec (for [p part-entities]
               (let [name (:part/name p)
                     n (:part/number-of-movements p)
                     c (flatten (map vals (:part/category p)))
                     category-names (vec (flatten (map vals (map #(d/pull db '[:category/name] %) c))))
                     movements (vec (get-movements n category-names))]
                 {:title      name
                  :categories category-names
                  :movements  movements})))
        ]
    (generate-response {:title       title
                        :description description
                        :parts       parts})))


;;;;;;;;;;;;;;;;;;;;;;

#_(defn auth [req]
  (get-in req [:headers "Authorization"]))

#_(defn decode-auth [encoded]
  (let [auth (second (.split encoded " "))]
    (-> (Base64/decodeBase64 auth)
        (String. (Charset/forName "UTF-8"))
        (.split ":"))))

#_(defn login! [req]
  (let [[username password] (decode-auth (auth req))]

    (if (and (= username "admin")
             (= password "pw"))
      ;todo: if creds authenticate, add user to backend session
      {:result "ok"}
      {:error "wrong username or password"})))

#_(defn logout! []
  ;todo: clear backend session
  {:result "ok"})


;;;;
(def secret "mysupersecret")

(defn jws-home
  [request]
  (if-not (authenticated? request)
    (throw-unauthorized)
    (generate-response {:status "Logged"
                        :message (str "hello logged user " (:identity request))})))

(def authdata {"admin" "secret"})

(defn jws-login
  [request]
  (let [username (get-in request [:params :username])
        password (get-in request [:params :password])
        valid? (some-> authdata
                       (get (keyword username))
                       (= password))]
    (if valid?
      (let [claims {:user (keyword username)
                    :exp (time/plus (time/now) (time/seconds 3600))}
            token (jws/sign claims secret {:alg :hs512})]
        (generate-response {:token token}))
      (generate-response {:message "wrong auth data"} 400))))

(def jws-auth-backend (jws-backend {:secret secret :options {:alg :hs512}}))

;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes

           (GET "/" [] (render-file "app.html" {:dev (env :dev?)
                                                :csrf-token *anti-forgery-token*}))

           (POST "/login" [username password] (generate-response {:from "post"
                                                                  :usr username
                                                                  :pw password}))
           (GET "/login" [username password] (generate-response {:from "get"
                                                                 :usr username
                                                                  :pw password}))


           (GET "/movements" [] (all-movement-names))
           (GET "/movement/:name" [name] (movement name))
           (GET "/templates" [] (all-template-titles))
           (GET "/template/:title" [title] (create-session (str/replace title "-" " ")))
           (GET "/singlemovement" [categories]
             (generate-response (get-movements 1
                                               (if (not (vector? categories)) [categories]
                                                                              categories))))
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