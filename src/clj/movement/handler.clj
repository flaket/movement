(ns movement.handler
  (:require [compojure.core :refer [GET POST HEAD ANY defroutes]]
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
            [clojure.set :refer [rename-keys]]
            [clj-time.core :refer [from-now hours]]
            [buddy.sign.jws :as jws]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.hashers :as hashers]
            [datomic.api :as d]
            [hiccup.core :refer [html]]
            [taoensso.timbre :refer [info error]]
            [movement.db :as db]
            [movement.webpages :refer [web-page]]
            [movement.activation :refer [generate-activation-id send-email send-activation-email]])
  (:import java.security.MessageDigest
           java.math.BigInteger
           (java.util UUID)))

(selmer.parser/set-resource-path! (clojure.java.io/resource "templates"))

(defn response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body    (pr-str data)})

(defn positions [pred coll]
  (keep-indexed (fn [idx x]
                  (when (pred x) idx)) coll))

;;;;;; login ;;;;;;

#_(
  (defn valid-user? [user password]
    (hashers/check password (:password user)))

  (def secret "mysupersecret")

  (def jws-auth-backend (jws-backend {:secret secret :options {:alg :hs512}}))

  (defn jws-login
    [email password]
    (let [user (db/user-by-email email)]
      (cond
        (nil? user) (response {:message "Ukjent epost"} 400)
        (false? (:activated? user)) (response {:message "Du er registrert, men eposten har ikke blitt bekreftet. Sjekk om du har fått en bekreftelseslenke på epost."} 400)
        ;(false? (:valid-subscription? user)) (response {:message "This account does not have a valid subscription." :update-payment? true} 400)
        (valid-user? user password) (let [claims {:user (keyword (:user-id user))
                                                  :exp  (-> 72 hours from-now)}
                                          token (jws/sign claims secret {:alg :hs512})]
                                      (response (-> user
                                                    (assoc :token token)
                                                    (dissoc :password :activated?))))
        :else (response {:message "Passordet stemte ikke"} 401))))

  ;;;;;; end login ;;;;;;

  (defn store-session! [params]
    (try
      (response (db/add-session! params))
      (catch Exception e (error e "error storing session"))))

  (defn add-user! [email username password]
    (if (nil? (db/user-by-email email))
      (let [activation-id (str (UUID/randomUUID))]
        (db/add-user! email username password activation-id)
        (send-activation-email email username activation-id)
        (send-email "andreas@mumrik.no" "En ny bruker registrerte" (str "Epost: " email "\nNavn: " username))
        (web-page :account-created {:email email}))
      (web-page :account-exists {:email email})))

  (defn like [params]
    (try
      (db/like! params)
      (catch Exception e
        (response (str "Exception: " e)))))

  (defn change-password! [{:keys [user-id password new-password]}]
    (if (valid-user? (db/user user-id) password)
      (try
        (response (db/update-password! user-id new-password))
        (catch Exception e
          (response "Noe gikk galt under passordbyttet" 500)))
      (response "Det nåværende passordet var galt" 400)))

  (defn change-profile! [{:keys [user-id profile]}]
    (try
      (response (db/update-profile! user-id profile))
      (catch Exception e
        (response {:message "Noe gikk galt under profiloppdateringen"} 500))))

  (defn md5 [s]
    (let [algorithm (MessageDigest/getInstance "MD5")
          size (* 2 (.getDigestLength algorithm))
          raw (.digest algorithm (.getBytes s))
          sig (.toString (BigInteger. 1 raw) 16)
          padding (apply str (repeat (- size (count sig)) "0"))]
      (str padding sig)))

  (defn update-subscription-status! [{:keys [security_data security_hash
                                             SubscriptionReferrer SubscriptionIsTest
                                             SubscriptionEndDate SubscriptionCustomerUrl
                                             SubscriptionQuantity SubscriptionReference]} value]
    (let [private-key (if value "20e964736aa0570a261d44b8b4a5b6eb" "c503849c6b5f2783bb88b33cac3533ea")]
      (when (= (md5 (str security_data private-key)) security_hash)
        (send-email "admin@movementsession.com"
                    "Automatic Subscription Update"
                    (str "SubscriptionReferrer: " SubscriptionReferrer "\n"
                         "SubscriptionIsTest: " SubscriptionIsTest "\n"
                         "SubscriptionEndDate:" SubscriptionEndDate
                         "SubscriptionReference: " SubscriptionReference "\n"
                         "SubscriptionQuantity: " SubscriptionQuantity "\n"
                         "SubscriptionCustomerUrl: " SubscriptionCustomerUrl))
        (when value
          (db/update-subscription! SubscriptionReferrer value))))))

;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes

           (GET "/app" [] (render-file "app.html" {:dev (env :dev?) :csrf-token *anti-forgery-token*}))

           #_(HEAD "/" [] "")
           #_(GET "/" [] (web-page :landing))
           #_(GET "/blog" [] (redirect "/blog/index.html"))
           #_(GET "/contact" [] (contact-page))
           #_(GET "/about" [] (about-page))
           #_(GET "/tour" [] (tour-page))
           #_(GET "/pricing" [] (pricing-page))
           #_(GET "/signup" [] (web-page :signup))

           #_(GET "/subscription-activated" req (update-subscription-status! (:params req) true))
           #_(GET "/subscription-deactivated" req (update-subscription-status! (:params req) false))

           #_(POST "/signup" [email username password] (add-user! email username password))
           #_(GET "/activated/:user" [user] (web-page :account-activated))
           #_(GET "/activate/:id" [id] (do (db/activate-user! id)
                                         (web-page :account-activated)))


           #_(POST "/login" [email password] (jws-login email password))

           #_(GET "/movements" req (if (authenticated? req) (response (db/movements)) (throw-unauthorized)))
           #_(GET "/categories" req (if (authenticated? req) (response (db/categories)) (throw-unauthorized)))
           #_(GET "/users" req (if (authenticated? req)
                               (response (vec (db/users))) (throw-unauthorized)))
           #_(GET "/user" req (if (authenticated? req)
                              (let [{:keys [email user-id user-name]} (:params req)]
                                (cond
                                  email (response (dissoc (db/user-by-email email) :password :activation-id :activated? :valid-subscription?))
                                  user-id (response (dissoc (db/user user-id) :password :activation-id :activated? :valid-subscription?))
                                  user-name (response (dissoc (db/user-by-name user-name) :password :activation-id :activated? :valid-subscription?))
                                  :else "missing email, user-id or user-name"))
                              (throw-unauthorized)))

           #_(POST "/change-password" req (if (authenticated? req) (change-password! (:params req)) (throw-unauthorized)))
           #_(POST "/change-profile" req (if (authenticated? req) (change-profile! (:params req)) (throw-unauthorized)))

           #_(POST "/store-session" req (if (authenticated? req) (store-session! (:params req)) (throw-unauthorized)))
           #_(GET "/create-session" req (if (authenticated? req)
                                        (let [type (:type (:params req))
                                              user-id (:user-id (:params req))]
                                          (response (db/create-session user-id type))) (throw-unauthorized)))

           #_(GET "/feed" req (if (authenticated? req)
                               (let [user-id (:user-id (:params req))]
                                 (response (vec (db/create-feed user-id)))) (throw-unauthorized)))

           #_(GET "/user-only-feed" req (if (authenticated? req)
                                        (let [user-id (:user-id (:params req))]
                                          (response (vec (db/create-user-only-feed user-id)))) (throw-unauthorized)))

           #_(GET "/movement-from-category" req (if (authenticated? req)
                                                (let [{:keys [category user-id]} (:params req)
                                                      user-id (:user-id (:params req))
                                                      movement (first (db/movements-from-category 1 category))
                                                      user-movement (db/user-movement user-id (:name movement))
                                                      movement (if-let [zone (:zone user-movement)]
                                                                 (assoc movement :zone zone)
                                                                 (assoc movement :zone 0))]
                                                  (response [movement])) (throw-unauthorized)))
           #_(GET "/movement" req (if (authenticated? req)
                                  (let [name (:name (:params req))
                                        user-id (:user-id (:params req))
                                        movement (db/movement name)
                                        user-movement (db/user-movement user-id name)
                                        movement (if-let [zone (:zone user-movement)]
                                                   (assoc movement :zone zone)
                                                   (assoc movement :zone 0))]
                                    (response movement)) (throw-unauthorized)))
           #_(POST "/like" req (if (authenticated? req) (response (db/like! (:params req))) (throw-unauthorized)))
           #_(POST "/comment" req (if (authenticated? req) (response (db/comment! (:params req))) (throw-unauthorized)))

           #_(POST "/follow" req (if (authenticated? req) (response (db/follow-user! (:params req))) (throw-unauthorized)))
           #_(POST "/unfollow" req (if (authenticated? req) (response (db/unfollow-user! (:params req))) (throw-unauthorized)))

           ;; --------------------------------------------------------

           #_(GET "/session/:url" [url] (view-session-page (old-db/get-session url)))

           (resources "/")
           (not-found "Not Found"))

(def app
  (let [handler (-> routes
                    #_(wrap-authentication jws-auth-backend)
                    #_(wrap-authorization jws-auth-backend)
                    (wrap-edn-params)
                    (wrap-params)
                    (wrap-session)
                    (wrap-defaults site-defaults)
                    (wrap-frame-options {:allow-from "http://mumrik.no"}))]
    (if (env :dev?)
      (wrap-reload (wrap-exceptions handler))
      handler)))