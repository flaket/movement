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
            [movement.db.db :as db]
            [movement.pages.landing :refer [landing-page]]
            [movement.pages.signup :refer [signup-page payment-page activation-page]]
            [movement.pages.contact :refer [contact-page]]
            [movement.pages.pricing :refer [pricing-page]]
            [movement.pages.about :refer [about-page]]
            [movement.pages.tour :refer [tour-page]]
            [movement.pages.session :refer [view-session-page view-sub-activated-page]]
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

(defn valid-user? [user password]
  (hashers/check password (:password user)))

(def secret "mysupersecret")

(def jws-auth-backend (jws-backend {:secret secret :options {:alg :hs512}}))

(defn jws-login
  [email password]
  (let [user (db/user-by-email email)]
    (cond
      (nil? user) (response {:message "Unknown user"} 400)
      (false? (:activated? user)) (response {:message "Email has not been activated. Check your inbox for an activation code."} 400)
      ;(false? (:valid-subscription? user)) (response {:message "This account does not have a valid subscription." :update-payment? true} 400)
      (valid-user? user password) (let [claims {:user (keyword (:user-id user))
                                                :exp  (-> 72 hours from-now)}
                                        token (jws/sign claims secret {:alg :hs512})]
                                    (response (-> user
                                                  (assoc :token token)
                                                  (dissoc :password :activated?))))
      :else (response {:message "Incorrect password"} 401))))

;;;;;; end login ;;;;;;

(defn store-session! [params]
  (try
    (response (db/add-session! params))
    (catch Exception e (error e "error storing session"))))

(defn add-user! [email password]
  #_(if (nil? (db/user-by-email email))
    (let [activation-id (str (UUID/randomUUID))]
      (db/add-user! email password activation-id)
      (send-activation-email email activation-id)
      (send-email "admin@movementsession.com" "A new user registered!" "")
      (activation-page "To verify your email address we have sent you an activation email."))
    (pricing-page (str email " is already registered as a user."))))

(defn set-zone! [email movement zone]
  (try
    (db/update-zone! email movement zone)
    (catch Exception e
      (response (str "Exception: " e)))))

(defn like [params]
  (try
    (db/like! params)
    (catch Exception e
      (response (str "Exception: " e)))))

(defn activate-user! [id]
  #_(let [user (old-db/entity-by-lookup-ref :user/activation-id id)]
    (if-not (nil? (:db/id user))
      (let []
        (old-db/transact-activated-user! (:user/email user))
        #_(old-db/add-standard-templates-to-user! (:user/email user))
        {:status  302
         :headers {"Location" (str "/activated/" (:user/email user))}
         :body    ""})
      "<h1>This activation-id is invalid.</h1>")))

(defn change-password! [{:keys [email password new-password]}]
  (if (valid-user? (db/user-by-email email) password)
    (try
      (response (db/update-password! email new-password))
      (catch Exception e
        (response "Error changing password" 500)))
    (response "Wrong old password" 400)))

(defn change-username! [{:keys [email username]}]
  (try
    (response {:message  (db/update-name! email username)
               :username username})
    (catch Exception e
      (response {:message "Error changing username"} 500))))

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
        (db/update-subscription! SubscriptionReferrer value)))))

;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
           (HEAD "/" [] "")
           (GET "/" [] (landing-page))
           (GET "/blog" [] (redirect "/blog/index.html"))
           (GET "/contact" [] (contact-page))
           (GET "/terms" [] (render-file "privacypolicy.htm" {}))
           (GET "/about" [] (about-page))
           (GET "/tour" [] (tour-page))
           (GET "/pricing" [] (pricing-page))
           (GET "/signup" [] (pricing-page))
           (GET "/activated/:user" [user] (payment-page user "Account successfully activated!"))
           (GET "/app" [] (render-file "app.html" {:dev (env :dev?) :csrf-token *anti-forgery-token*}))

           (GET "/movements" req (if (authenticated? req) (response (db/movements)) (throw-unauthorized)))
           (GET "/categories" req (if (authenticated? req) (response (db/categories)) (throw-unauthorized)))

           (GET "/activate/:id" [id] (db/activate-user! id))
           (GET "/subscription-activated" req (update-subscription-status! (:params req) true))
           (GET "/subscription-deactivated" req (update-subscription-status! (:params req) false))

           (POST "/signup" [email password] (add-user! email password))
           (POST "/login" [email password] (jws-login email password))

           (GET "/user" req (if (authenticated? req)
                              (let [{:keys [email user-id user-name]} (:params req)]
                                (cond
                                  email (response (dissoc (db/user-by-email email) :password :activation-id :activated? :valid-subscription?))
                                  user-id (response (dissoc (db/user user-id) :password :activation-id :activated? :valid-subscription?))
                                  user-name (response (dissoc (db/user-by-name user-name) :password :activation-id :activated? :valid-subscription?))
                                  :else "missing email, user-id or user-name"))
                              (throw-unauthorized)))

           (POST "/change-password" req (if (authenticated? req) (change-password! (:params req)) (throw-unauthorized)))
           (POST "/change-username" req (if (authenticated? req) (change-username! (:params req)) (throw-unauthorized)))

           (POST "/store-session" req (if (authenticated? req) (store-session! (:params req)) (throw-unauthorized)))
           (POST "/set-zone" req (if-not (authenticated? req)
                                   (throw-unauthorized)
                                   (let [email (:email (:params req))
                                         name (:name (:params req))
                                         zone (:zone (:params req))]
                                     (set-zone! email name zone))))

           (GET "/create-session" req (if (authenticated? req)
                                        (let [type (:type (:params req))
                                              user-id (:user-id (:params req))]
                                          (response (db/create-session user-id type))) (throw-unauthorized)))

           (GET "/feed" req (if (authenticated? req)
                               (let [user-id (:user-id (:params req))]
                                 (response (vec (db/create-feed user-id)))) (throw-unauthorized)))

           (GET "/user-only-feed" req (if (authenticated? req)
                                        (let [user-id (:user-id (:params req))]
                                          (response (vec (db/create-user-only-feed user-id)))) (throw-unauthorized)))

           (GET "/movement-from-category" req (if (authenticated? req)
                                                (response (db/movements-from-category 1 (:category (:params req)))) (throw-unauthorized)))
           (GET "/movement" req (if (authenticated? req)
                                  (let [name (:name (:params req))]
                                    (response (db/movement name))) (throw-unauthorized)))
           (POST "/like" req (if (authenticated? req) (db/like! (:params req)) (throw-unauthorized)))
           (POST "/comment" req (if (authenticated? req) (db/comment! (:params req)) (throw-unauthorized)))


           ;; --------------------------------------------------------

           #_(GET "/session/:url" [url] (view-session-page (old-db/get-session url)))

           (resources "/")
           (not-found "Not Found"))

(def app
  (let [handler (-> routes
                    (wrap-authentication jws-auth-backend)
                    (wrap-authorization jws-auth-backend)
                    (wrap-edn-params)
                    (wrap-params)
                    (wrap-session)
                    (wrap-defaults site-defaults)
                    (wrap-frame-options {:allow-from "http://mumrik.no"}))]
    (if (env :dev?)
      (wrap-reload (wrap-exceptions handler))
      handler)))