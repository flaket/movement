(ns movement.util
  (:import goog.History)
  (:require
    [goog.events :as events]
    [goog.dom :as gdom]
    [goog.crypt.base64 :as b64]
    [goog.history.EventType :as EventType]
    [reagent.session :as session]
    [secretary.core :as secretary
     :include-macros true :refer [dispatch!]]
    [dommy.core :as dommy :refer-macros [sel1]]
    [ajax.core :as cljs-ajax :refer [to-interceptor]]
    [ajax.edn :refer [edn-request-format edn-response-format]]
    [clojure.string :as str]))

(def csrf-token
  (dommy/attr (sel1 :#anti-forgery-token) "value"))

(defn positions
  "Finds the integer positions of the elements in the collection, that matches the predicate."
  [pred coll]
  (keep-indexed (fn [idx x]
                  (when (pred x) idx)) coll))

(defn GET [url & [opts]]
  (let [token (str/trim (str "Token " (session/get :token)))
        base-opts {:format          (edn-request-format)
                   :response-format (edn-response-format)
                   ;:with-credentials true
                   :interceptors    [(to-interceptor {:name    "Token Interceptor"
                                                      :request #(assoc-in % [:headers "authorization"]
                                                                          token)})]}]
    (cljs-ajax/GET url (merge base-opts opts))))

(defn POST [url & [opts]]
  (let [token (str/trim (str "Token " (session/get :token)))
        base-opts {:format          (edn-request-format)
                   :response-format (edn-response-format)
                   ;:with-credentials true
                   :interceptors    [(to-interceptor {:name    "Token Interceptor"
                                                      :request #(assoc-in % [:headers "authorization"] token)})]
                   :headers         {:x-csrf-token csrf-token}}]
    (cljs-ajax/POST url (merge base-opts opts))))

(defn get-user-info []
  (if-let [email (session/get :user)]
    (GET "user" {:params        {:email email}
                 :handler       #(do
                                  (session/put! :username (:username %)))
                 :error-handler #(pr (str "error retrieving user information: " %))})
    (pr "no user in session.")))

(defn get-templates []
  (if-let [user (session/get :user)]
    (GET "templates" {:params        {:user user}
                      :handler       #(session/put! :templates %)
                      :error-handler #(pr (str "error retrieving templates: " %))})
    (pr "no user in session.")))

(defn get-groups []
  (if-let [email (session/get :email)]
    (GET "groups" {:params        {:email email}
                      :handler       #(session/put! :groups %)
                      :error-handler #(pr (str "error retrieving groups: " %))})
    (pr "no user in session.")))

(defn get-plans []
  (if-let [email (session/get :email)]
    (GET "plans" {:params        {:email email}
                   :handler       #(session/put! :plans %)
                   :error-handler #(pr (str "error retrieving plans: " %))})
    (pr "no user in session.")))

(defn get-routines []
  (if-let [email (session/get :email)]
    (GET "routines" {:params        {:email email}
                     :handler       #(session/put! :routines %)
                     :error-handler #(pr (str "error retrieving routines: " %))})
    (pr "no user in session.")))

(defn get-equipment []
  (if-let [user (session/get :user)]
    (GET "equipment" {:params        {:user user}
                      :handler       #(session/put! :equipment %)
                      :error-handler #(pr (str "error retrieving equipment: " %))})
    (pr "no user in session.")))

(defn get-all-categories []
  (GET "categories" {:handler       #(session/put! :all-categories %)
                     :error-handler #(pr (str "error retrieving categories: " %))}))

(defn get-all-movements []
  (GET "movements" {:handler       #(session/put! :all-movements %)
                    :error-handler #(pr (str "error retrieving movements: " %))}))

(defn get-stored-sessions []
  (if-let [user (session/get :user)]
    (GET "sessions" {:params        {:user user}
                     :handler       #(session/put! :stored-sessions %)
                     :error-handler #(pr (str "error retrieving stored sessions: " %))})
    (pr "no user in session.")))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn set-page! [page]
  (session/put! :current-page page))

(defn text-input [target & [opts]]
  [:input (merge
            {:type      "text"
             :on-change #(reset! target (-> % .-target .-value))
             :value     @target}
            opts)])