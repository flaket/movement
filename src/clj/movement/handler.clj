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

(defn positions [pred coll]
  (keep-indexed (fn [idx x]
                  (when (pred x) idx)) coll))

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
        (do
          (when (:last-session? session)
            (db/progress-plan! (:plan-id session)))
          (db/transact-session! user session)
          (db/transact-new-movements! user (:parts session)))
        (catch Exception e
          (error e (str "error transacting session: user: " user " session: " session)))
        (finally (do (update-tx-db!)
                     (response {:message "Session stored successfully"})))))))

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
      (send-email "admin@movementsession.com" "A new user registered!" "")
      (update-tx-db!)
      (activation-page "To verify your email address we have sent you an activation email."))
    (pricing-page (str email " is already registered as a user."))))

(defn begin-plan! [user-id plan-id]
  (try
    (db/begin-plan! user-id plan-id)
    (catch Exception e
      (response "An error occured trying to begin this plan."))
    (finally (do (update-tx-db!)
                 (response "Plan started successfully!")))))

(defn progress-plan! [plan-id]
  (try
    (db/progress-plan! plan-id)
    (catch Exception e
      (response (str "Exception: " e)))
    (finally (do (update-tx-db!)
                 (response "Plan progressed successfully!")))))

(defn end-plan! [user-id plan-id]
  (try
    (db/end-plan! user-id plan-id)
    (catch Exception e
      (response (str "Exception: " e)))
    (finally (do (update-tx-db!)
                 (response "Plan ended successfully!")))))

(defn next-session-from-plan [req]
  (let [user-entity (db/find-user (:email (:params req)))
        plan (db/entity-by-id (:db/id (:user/ongoing-plan user-entity)))
        current-day (db/entity-by-id (:db/id (:plan/current-day plan)))
        templates (:day/template current-day)
        ;;todo: group (:day/group current-day)

        current (:plan/current-day plan)
        days (:plan/day plan)
        ]
    ; if finished (current is last day and day is completed): end-plan, return stats
    (if (and (= current (last days)) (:day/completed? current-day))
      (do
        (end-plan! (:db/id user-entity) (:db/id plan))
        {:title "Plan completed!"
         :plan-completed? true})
      (cond
        ; if no templates today; progress plan and return rest day
        (nil? templates) (do
                           (db/progress-plan! (:db/id plan))
                           (db/update-tx-conn!)
                           (db/update-tx-db!)
                           {:title       "A Rest Day"
                            :description "The plan calls for a rest day. How about playing some sports or going for a hike?"})
        ; if one template; return session with last-session? flag true
        (= 1 (count templates)) (let [template (db/entity-by-id (:db/id (first templates)))
                                      template (assoc template :plan-id (:db/id plan) :last-session? true)]
                                  (db/create-session (:user/email user-entity) template))
        ; else; it gets tricky..
        :else (let [last-session (atom false)
              last-planned-session (last (filter #(= (:db/id plan)
                                                     (:db/id (:session/plan (db/entity-by-id (:db/id %)))))
                                                 (:user/session user-entity)))
              template-id (if (nil? last-planned-session)
                            (first templates)
                            (let [s (:session/title (db/entity-by-id (:db/id last-planned-session)))
                                  pos (vec (positions #{s} (map (fn [t] (:template/title (db/entity-by-id (:db/id t)))) templates)))]
                              (if-not (empty? pos)
                                (do
                                  (when (= (get templates (first pos)) (last templates))
                                    (reset! last-session true))
                                  (get templates (inc (first pos))))
                                (first templates))))
              template (db/entity-by-id (:db/id template-id))
              template (assoc template :plan-id (:db/id plan)
                                       :last-session? @last-session)]
          (db/create-session (:user/email user-entity) template))))))

(defn activate-user! [id]
  (let [user (db/entity-by-lookup-ref :user/activation-id id)]
    (if-not (nil? (:db/id user))
      (let []
        (db/transact-activated-user! (:user/email user))
        (db/add-standard-templates-to-user! (:user/email user))
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
           (POST "/begin-plan" req (if-not (authenticated? req)
                                     (throw-unauthorized)
                                     (let [user-id (:db/id (db/find-user (:email (:params req))))
                                           plan-id (:id (:params req))]
                                       (begin-plan! user-id plan-id))))
           (POST "/progress-plan" req (if-not (authenticated? req)
                                     (throw-unauthorized)
                                     (progress-plan! (:id (:params req)))))
           (POST "/end-plan" req (if-not (authenticated? req)
                                     (throw-unauthorized)
                                     (let [user-id (:db/id (db/find-user (:email (:params req))))
                                           plan-id (:id (:params req))]
                                       (end-plan! user-id plan-id))))
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
                                  (let [id (:template-id (:params req))
                                        template (db/entity-by-id (if (string? id) (read-string id) id))
                                        email (:email (:params req))]
                                    (response (db/create-session email template)))))
           (GET "/next-session-from-plan" req (if-not (authenticated? req)
                                                (throw-unauthorized)
                                                (response (next-session-from-plan req))))
           (GET "/templates" req (if-not (authenticated? req)
                                   (throw-unauthorized)
                                   (response (db/all-templates (str (:user (:params req)))))))
           (GET "/group" req (if-not (authenticated? req)
                               (throw-unauthorized)
                               (let [group (:group (:params req))
                                     email (:email (:params req))]
                                 (response (db/create-session email
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
           (GET "/ongoing-plan" req (if-not (authenticated? req)
                                     (throw-unauthorized)
                                     (response (db/ongoing-plan (str (:email (:params req)))))))
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
                                  (response (db/movement
                                              (:email (:params req))
                                              :name
                                              (:name (:params req))
                                              (:part (:params req))))))
           (GET "/explore-movement" req (if-not (authenticated? req)
                                          (throw-unauthorized)
                                          (let [unique-name (:unique-name (:params req))
                                                email (:email (:params req))]
                                            (response (db/explore-movement email unique-name)))))
           (GET "/user-movements" req (if-not (authenticated? req)
                                          (throw-unauthorized)
                                          (let [email (:email (:params req))]
                                            (response (db/user-movements email)))))
           (GET "/movement-by-id" req (if-not (authenticated? req)
                                        (throw-unauthorized)
                                        (response (db/movement
                                                    (:email (:params req))
                                                    :id
                                                    (read-string (:id (:params req)))
                                                    (:part (:params req))))))
           (GET "/movements-by-category" req (if-not (authenticated? req)
                                               (throw-unauthorized)
                                               (response
                                                 (db/get-movements-from-category
                                                   (read-string (:n (:params req)))
                                                   (:category (:params req))))))
           (GET "/movements" req (if-not (authenticated? req)
                                   (throw-unauthorized)
                                   (response (db/all-movement-names))))
           (GET "/singlemovement" req (if-not (authenticated? req)
                                        (throw-unauthorized)
                                        (let [email (:email (:params req))
                                              part (:part (:params req))]
                                          (response (db/single-movement email part)))))
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