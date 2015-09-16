(ns movement.handler
  (:import org.apache.commons.codec.binary.Base64
           java.nio.charset.Charset)
  (:require [clojure.java.io :as io]
            [compojure.core :refer [GET POST ANY defroutes]]
            [compojure.route :refer [not-found resources]]
            [compojure.response :refer [render]]
            [ring.util.response :refer [redirect]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.x-headers :refer [wrap-frame-options]]
            [selmer.parser :refer [render-file]]
            [prone.middleware :refer [wrap-exceptions]]
            [environ.core :refer [env]]
            [datomic.api :as d]
            [clojure.string :as str]

            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]

            [buddy.hashers :as hashers]))

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

(defn unauthorized-handler [request metadata]
  ;; If request is authenticated, raise 403 instead
  ;; of 401 (because user is authenticated but permission
  ;; denied is raised).
  ;; In other cases, redirect to user login.
  (cond
    (authenticated? request) (-> (render-file "index.html" {:dev (env :dev?)})
                                 (assoc :status 403))

    :else (let [current-url (:uri request)]
            (redirect (format "/landing?next=%s" current-url)))))

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))

(defn home [req]
  (when (not (authenticated? req))
    (throw-unauthorized {:message "Not authorized"}))
  (render-file "app.html" {:dev (env :dev?)}))

(defn login [req]
  (render-file "login.html" {:dev (env :dev?)}))

(defn logout [req]
  (assoc (redirect "/") :session nil))

(defn find-user [username password]
  (hashers/encrypt "password"))

(defn login-form-authenticate [req]
  (let [username (get-in req [:form-params "username"])
        password (get-in req [:form-params "password"])
        session (:session req)
        found-password (find-user username password)]
    (if (and found-password (hashers/check password found-password))
      (let [next-url (get-in req [:query-params :next] "/")
            updated-session (assoc session :identity (keyword username))]
        (-> (redirect next-url)
            (assoc :session updated-session)))
      (render-file "login.html" {:dev (env :dev?)}))))


;;;;;;;;;;;;;;

(defn auth [req]
  (get-in req [:headers "Authorization"]))

(defn decode-auth [encoded]
  (let [auth (second (.split encoded " "))]
    (-> (Base64/decodeBase64 auth)
        (String. (Charset/forName "UTF-8"))
        (.split ":"))))

(defn login! [req]
  (let [[username password] (decode-auth (auth req))]

    (if (and (= username "admin")
             (= password "pw"))
      ;todo: if creds authenticate, add user to backend session
      {:result "ok"}
      {:error "wrong username or password"})))

(defn logout! []
  ;todo: clear backend session
  {:result "ok"})


(defn handle-signup! [req])


;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes

           (GET "/" [] (render-file "app.html" {:dev (env :dev?)}))
           (GET "/landing" [] (render-file "index.html" {:dev (env :dev?)}))

           (GET "/login" [] login)
           (POST "/login" [] login-form-authenticate)
           (GET "/logout" [] (render-file "index.html" {:dev (env :dev?)}))



           (POST "/signup" [] handle-signup!)
           (POST "/change-password!" [] nil)

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
                    (wrap-authentication auth-backend)
                    (wrap-authorization auth-backend)
                    (wrap-params)
                    (wrap-session)
                    (wrap-defaults site-defaults)
                    (wrap-frame-options {:allow-from (or "http://movementsession.com"
                                                         "http://www.movementsession.com")}))]
    (if (env :dev?)
      (wrap-reload (wrap-exceptions handler))
      handler)))