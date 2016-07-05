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

(defn vec-remove
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(def csrf-token
  (dommy/attr (sel1 :#anti-forgery-token) "value"))

(defn positions
  "Finds the integer positions of the elements in the collection, that matches the predicate."
  [pred coll]
  (keep-indexed (fn [idx x]
                  (when (pred x) idx)) coll))

(defn GET [url & [opts]]
  (let [token (str/trim (str "Token " (:token (session/get :user))))
        base-opts {:format          (edn-request-format)
                   :response-format (edn-response-format)
                   ;:with-credentials true
                   :interceptors    [(to-interceptor {:name    "Token Interceptor"
                                                      :request #(assoc-in % [:headers "authorization"] token)})]}]
    (cljs-ajax/GET url (merge base-opts opts))))

(defn POST [url & [opts]]
  (let [token (str/trim (str "Token " (:token (session/get :user))))
        base-opts {:format          (edn-request-format)
                   :response-format (edn-response-format)
                   ;:with-credentials true
                   :interceptors    [(to-interceptor {:name    "Token Interceptor"
                                                      :request #(assoc-in % [:headers "authorization"] token)})]
                   :headers         {:x-csrf-token csrf-token}}]
    (cljs-ajax/POST url (merge base-opts opts))))

(defn get-user-info []
  (GET "user" {:params        {:email (:email (session/get :user))}
               :handler       #(session/put! :username (:username %))
               :error-handler #(pr (str "error retrieving user information: " %))}))

(defn get-templates []
  (GET "templates" {:params        {:user (:email (session/get :user))}
                    :handler       #(session/put! :templates %)
                    :error-handler #(pr (str "error retrieving templates: " %))}))

(defn get-movements []
  (GET "movements" {:handler       #(session/put! :all-movements %)
                    :error-handler #(pr (str "error retrieving movements: " %))}))

(defn get-categories []
  (GET "categories" {:handler       #(session/put! :all-categories %)
                    :error-handler #(pr (str "error retrieving movements: " %))}))

(defn get-stored-sessions []
  (GET "sessions" {:params        {:user (session/get :user)}
                   :handler       #(session/put! :stored-sessions %)
                   :error-handler #(pr (str "error retrieving stored sessions: " %))}))

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

(defn handler-fn
  "Wrapper function to force component handler functions to return nil.
  This is a React requirement."
  [func]
  (fn [] func nil))

(def temp-state (atom {}))

