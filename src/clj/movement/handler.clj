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
            [movement.db :refer [tx update-tx-conn! update-tx-db!] :as db]
            [movement.pages.landing :refer [landing-page]]
            [movement.pages.signup :refer [signup-page payment-page activation-page]]
            [movement.pages.contact :refer [contact-page]]
            [movement.pages.pricing :refer [pricing-page]]
            [movement.pages.about :refer [about-page]]
            [movement.pages.tour :refer [tour-page]]
            [movement.pages.session :refer [view-session-page view-sub-activated-page]]
            [movement.activation :refer [generate-activation-id send-email send-activation-email]])
  (:import java.security.MessageDigest
           java.math.BigInteger))

(selmer.parser/set-resource-path! (clojure.java.io/resource "templates"))

(defn response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body    (pr-str data)})

;;;;;; login ;;;;;;

(defn md5 [s]
  (let [algorithm (MessageDigest/getInstance "MD5")
        size (* 2 (.getDigestLength algorithm))
        raw (.digest algorithm (.getBytes s))
        sig (.toString (BigInteger. 1 raw) 16)
        padding (apply str (repeat (- size (count sig)) "0"))]
    (str padding sig)))

(defn valid-user? [user password]
  (hashers/check password (:user/password user)))

(def secret "mysupersecret")

(def jws-auth-backend (jws-backend {:secret secret :options {:alg :hs512}}))

(defn jws-login
  [email password]
  (let [user (db/find-user email)]
    (cond
      (nil? (:db/id user)) (response {:message "Unknown user"} 400)
      (false? (:user/activated? user)) (response {:message "Email has not been activated. Check your inbox for an activation code."} 400)
      (false? (:user/valid-subscription? user)) (response {:message "This account does not have a valid subscription." :update-payment? true} 400)
      (valid-user? user password) (let [claims {:user (keyword email)
                                                :exp  (-> 3 hours from-now)}
                                        token (jws/sign claims secret {:alg :hs512})]
                                    (response {:token    token
                                               :user     (:user/email user)
                                               :email    (:user/email user)
                                               :username (:user/name user)}))
      :else (response {:message "Incorrect password"} 401))))

;;;;;;;;;;;

(defn add-session! [req]
  (let [session (:session (:params req))
        user (:user (:params req))]
    (if (nil? user)
      (response {:message "User email lacking from client data" :session session} 400)
      (try
        (db/transact-session! user session)
        (catch Exception e
          (error e (str "error transacting session: user: " user " session: " session)))
        (finally (do (update-tx-db!)
                     (response {:session session :message "Session stored successfully"})))))))

(defn try-assoc-template! [email template]
  (try
    (db/assoc-template! email template)
    (catch Exception e
      (response (str "Exception: " e)))
    (finally (do (update-tx-db!)
                 (response "Template added successfully.")))))

(defn assoc-template! [req]
  (let [email (:email (:params req))
        id (:id (:params req))
        template (db/entity-by-id id)]
    (if (nil? email)
      (response "User email lacking from client data" 400)
      (try-assoc-template! email template))))

(defn add-template!
  "Adds the new template to the database."
  [req]
  (let [email (:email (:params req))
        template (:template (:params req))]
    (if (nil? email)
      (response "User email lacking from client data" 400)
      (if (db/new-unique-template? email (:title template))
        (try
          (db/transact-template! template)
          (catch Exception e
            (response (str "Exception transact-template!: " e)))
          (finally (do (update-tx-db!)
                       (response "Template added successfully."))))
        (response "You already have a template with this title. Please choose a unique title for your template." 400)))))

(defn dissoc-template! [req]
  (let [email (:email (:params req))
        id (:id (:params req))]
    (if (nil? email)
      (response "User email lacking from client data" 400)
      (try
        (db/dissoc-template! email id)
        (catch Exception e
          (response (str "Exception: " e)))
        (finally (update-tx-db!))))))

(defn assoc-group! [req]
  (let [email (:email (:params req))
        group (db/entity-by-id (:id (:params req)))]
    (if (nil? email)
      (response "User email lacking from client data" 400)
      (try
        (db/assoc-group! email group)
        (catch Exception e
          (response (str "Exception: " e)))
        (finally (update-tx-db!))))))

(defn dissoc-group! [req]
  (let [email (:email (:params req))
        id (:id (:params req))]
    (if (nil? email)
      (response "User email lacking from client data" 400)
      (try
        (db/dissoc-group! email id)
        (catch Exception e
          (response (str "Exception: " e)))
        (finally (update-tx-db!))))))

(defn assoc-plan! [req]
  (let [email (:email (:params req))
        plan (db/entity-by-id (:id (:params req)))]
    (if (nil? email)
      (response "User email lacking from client data" 400)
      (try
        (db/assoc-plan! email plan)
        (catch Exception e
          (response (str "Exception: " e)))
        (finally (update-tx-db!))))))

(defn add-group! [req]
  (let [email (:email (:params req))
        group (:group (:params req))]
    (if (nil? email)
      (response "User email lacking from client data" 400)
      (if (db/new-unique-group? email (:title group))
        (try
          (db/transact-group! group)
          (catch Exception e
            (response (str "Exception: " e)))
          (finally (do (update-tx-db!)
                       (response "New group stored successfully."))))
        (response "You already have a group with this title. Please choose a unique title for your group." 400)))))

(defn add-plan! [req]
  (let [email (:email (:params req))
        plan (:plan (:params req))]
    (if (nil? email)
      (response "User email lacking from client data" 400)
      (if (db/new-unique-plan? email (:title plan))
        (try
          (db/transact-plan! plan)
          (catch Exception e
            (response (str "Exception: " e)))
          (finally (do (update-tx-db!)
                       (response "New plan stored successfully."))))
        (response "You already have a plan with this title. Please choose a unique title for your plan." 400)))))

(defn add-routine! [req]
  (let [email (:email (:params req))
        routine (:routine (:params req))]
    (if (nil? email)
      (response "User email lacking from client data" 400)
      (if (db/new-unique-routine? email (:name routine))
        (try
          (db/transact-routine! routine)
          (catch Exception e
            (response (str "Exception: " e)))
          (finally (do (update-tx-db!)
                       (response "New routine stored successfully."))))
        (response "You already have a routine with this title. Please choose a unique title for your routine." 400)))))

(defn add-user! [email password]
  (if (nil? (:db/id (db/find-user email)))
    (let [activation-id (generate-activation-id)]
      (db/transact-new-user! email password activation-id)
      (send-activation-email email activation-id)
      (update-tx-db!)
      (activation-page "To verify your email address we have sent you an activation email."))
    (pricing-page (str email " is already registered as a user."))))

(defn activate-user! [id]
  (let [user (db/entity-by-lookup-ref :user/activation-id id)]
    (if-not (nil? (:db/id user))
      (let []
        (db/transact-activated-user! (:user/email user))
        #_(add-standard-templates-to-user (:user/email user))
        {:status  302
         :headers {"Location" (str "/activated/" (:user/email user))}
         :body    ""})
      "<h1>This activation-id is invalid.</h1>")))

(defn change-password! [req]
  (let [email (:username (:params req))
        password (:password (:params req))
        new-password (:new-password (:params req))
        user (db/find-user email)]
    (if (valid-user? user password)
      (try
        (response (db/transact-new-password! user new-password))
        (catch Exception e
          (error e "error changing password: ")
          (response "Error changing password" 500))
        (finally (update-tx-db!)))
      (response "Wrong old password" 400))))

(defn change-username! [req]
  (let [email (:email (:params req))
        username (:username (:params req))]
    (if (db/new-unique-username? username)
      (try
        (response {:message  (db/transact-username! email username)
                   :username username})
        (catch Exception e
          (error e "error changing username: ")
          (response "Error changing username" 500))
        (finally (update-tx-db!)))
      (response "This username is already taken" 400))))

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
        (db/transact-subscription-status! SubscriptionReferrer value)))))

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
           (GET "/signup" [] (signup-page))
           (GET "/activate/:id" [id] (do
                                       (when (nil? (:conn @tx))
                                         (update-tx-conn!))
                                       (when (nil? (:db @tx))
                                         (update-tx-db!))
                                       (activate-user! id)))
           (GET "/activated/:user" [user] (payment-page user "Account successfully activated!"))
           (GET "/subscription-activated" req (update-subscription-status! (:params req) true))
           (GET "/subscription-deactivated" req (update-subscription-status! (:params req) false))
           (GET "/app" [] (render-file "app.html" {:dev (env :dev?) :csrf-token *anti-forgery-token*}))
           (POST "/signup" [email password] (do
                                              (when (nil? (:conn @tx))
                                                (update-tx-conn!))
                                              (update-tx-db!)
                                              (add-user! email password)))
           (POST "/login" [username password] (do
                                                (when (nil? (:conn @tx))
                                                  (update-tx-conn!))
                                                (update-tx-db!)
                                                (jws-login username password)))
           (POST "/store-session" req (if-not (authenticated? req)
                                        (throw-unauthorized)
                                        (add-session! req)))
           (POST "/template" req (if-not (authenticated? req)
                                   (throw-unauthorized)
                                   (add-template! req)))
           (POST "/group" req (if-not (authenticated? req)
                                (throw-unauthorized)
                                (add-group! req)))
           (POST "/routine" req (if-not (authenticated? req)
                                  (throw-unauthorized)
                                  (add-routine! req)))
           (POST "/plan" req (if-not (authenticated? req)
                               (throw-unauthorized)
                               (add-plan! req)))
           (POST "/change-password" req (if-not (authenticated? req)
                                          (throw-unauthorized)
                                          (change-password! req)))
           (POST "/change-username" req (if-not (authenticated? req)
                                          (throw-unauthorized)
                                          (change-username! req)))
           (POST "/assoc/template" req (if-not (authenticated? req)
                                         (throw-unauthorized)
                                         (assoc-template! req)))
           (POST "/dissoc/template" req (if-not (authenticated? req)
                                          (throw-unauthorized)
                                          (dissoc-template! req)))
           (POST "/assoc/group" req (if-not (authenticated? req)
                                      (throw-unauthorized)
                                      (assoc-group! req)))
           (POST "/dissoc/group" req (if-not (authenticated? req)
                                       (throw-unauthorized)
                                       (dissoc-group! req)))
           (POST "/assoc/plan" req (if-not (authenticated? req)
                                     (throw-unauthorized)
                                     (assoc-plan! req)))
           (GET "/user" req (if-not (authenticated? req)
                              (throw-unauthorized)
                              (let [email (:email (:params req))]
                                (response (dissoc (db/find-user email)
                                                  :user/password
                                                  :user/activation-id
                                                  :user/activated?
                                                  :user/valid-subscription?)))))
           (GET "/sessions" req (if-not (authenticated? req)
                                  (throw-unauthorized)
                                  (response (db/retrieve-sessions req))))
           (GET "/session/:url" [url] (view-session-page (db/get-session url)))
           (GET "/template" req (if-not (authenticated? req)
                                  (throw-unauthorized)
                                  (let [template (db/entity-by-id (read-string (:template-id (:params req))))]
                                    (response (db/create-session template)))))
           (GET "/templates" req (if-not (authenticated? req)
                                   (throw-unauthorized)
                                   (response (db/all-templates (str (:user (:params req)))))))
           (GET "/group" req (if-not (authenticated? req)
                               (throw-unauthorized)
                               (let [group (:group (:params req))
                                     email (:email (:params req))]
                                 (response (db/create-session
                                             (db/random-template-from-group email group))))))
           (GET "/groups" req (if-not (authenticated? req)
                                (throw-unauthorized)
                                (response (db/all-groups (str (:email (:params req)))))))
           (GET "/plan" req (if-not (authenticated? req)
                              (throw-unauthorized)
                              (let [plan (:plan (:params req))
                                    email (:email (:params req))]
                                (response ""))))
           (GET "/plans" req (if-not (authenticated? req)
                               (throw-unauthorized)
                               (response (db/all-plans (str (:email (:params req)))))))
           (GET "/routine" req (if-not (authenticated? req)
                                 (throw-unauthorized)
                                 (let [routine (:routine (:params req))
                                       email (:email (:params req))]
                                   (response (db/get-routine email routine)))))
           (GET "/routines" req (if-not (authenticated? req)
                                  (throw-unauthorized)
                                  (response (db/all-routines (str (:email (:params req)))))))
           (GET "/movement" req (if-not (authenticated? req)
                                  (throw-unauthorized)
                                  (response (db/entity-by-movement-name (:name (:params req))))))
           (GET "/movement-by-id" req (if-not (authenticated? req)
                                        (throw-unauthorized)
                                        (response (db/entity-by-id (read-string (:entity (:params req)))))))
           (GET "/movements-by-category" req (if-not (authenticated? req)
                                               (throw-unauthorized)
                                               (response
                                                 (db/get-movements-from-category (read-string (:n (:params req)))
                                                                                 (:category (:params req))))))
           (GET "/movements" req (if-not (authenticated? req)
                                   (throw-unauthorized)
                                   (response (db/all-movement-names))))
           (GET "/equipment" req (if-not (authenticated? req)
                                   (throw-unauthorized)
                                   (response (db/all-equipment-names))))
           (GET "/equipment-session" req (if-not (authenticated? req)
                                           (throw-unauthorized)
                                           (let [equipment (:equipment (:params req))]
                                             (response (db/create-equipment-session equipment 5)))))
           (GET "/movement-from-equipment" req (let [e (:equipment (:params req))]
                                                 (if-not (authenticated? req)
                                                   (throw-unauthorized)
                                                   (response (db/get-movement-from-equipment e)))))
           (GET "/singlemovement" req
             (let [categories (vec (vals (:categories (:params req))))]
               (if-not (authenticated? req)
                 (throw-unauthorized)
                 (response (db/get-n-movements-from-categories 1 categories {})))))
           (GET "/categories" req (if-not (authenticated? req)
                                    (throw-unauthorized)
                                    (response (db/all-category-names))))
           (GET "/search/template" req (if-not (authenticated? req)
                                         (throw-unauthorized)
                                         (response (db/search :template (:template (:params req))))))
           (GET "/search/group" req (if-not (authenticated? req)
                                      (throw-unauthorized)
                                      (response (db/search :group (:group (:params req))))))
           (GET "/search/plan" req (if-not (authenticated? req)
                                     (throw-unauthorized)
                                     (response (db/search :plan (:plan (:params req))))))
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
                    (wrap-frame-options {:allow-from "http://www.movementsession.com"}))]
    (if (env :dev?)
      (wrap-reload (wrap-exceptions handler))
      handler)))