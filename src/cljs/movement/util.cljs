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
    [ajax.core :as cljs-ajax]
    [cljs.core.async :as async :refer [>! <! put! take! alts! chan sliding-buffer close!]]
    [dommy.core :as dommy :refer-macros [sel sel1]]))

(def csrf-token
  (dommy/attr (sel1 :#anti-forgery-token) "value"))

(defn GET [url & [opts]]
  (cljs-ajax/GET url opts))

(defn POST [url & [opts]]
  (let [base-opts {:headers {:x-csrf-token csrf-token
                             "authorization" (session/get :token)}}]
    (cljs-ajax/POST url (merge base-opts opts))))

(defn get-templates []
  (GET "templates" {:headers {"authorization" (session/get :token)}
                    :handler       #(session/put! :templates %)
                    :error-handler #(print "error retrieving templates.")}))

(defn get-templates-1 [t]
  (GET "templates" {:headers {"authorization" t}
                    :handler       #(session/put! :templates %)
                    :error-handler #(print "error retrieving templates.")}))

(defn get-all-categories []
  (GET "categories" {:headers {"authorization" (session/get :token)}
                     :handler       #(session/put! :all-categories %)
                     :error-handler #(print "error retrieving categories.")}))

(defn get-all-movements []
  (GET "movements" {:headers {"authorization" (session/get :token)}
                    :handler       #(session/put! :all-movements %)
                    :error-handler #(print "error retrieving movements.")}))

(defn launch-template-creator []
  (do (when (nil? (session/get :all-categories)) (get-all-categories))
      (when (nil? (session/get :templates)) (get-templates))
      (when (nil? (session/get :all-movements)) (get-all-movements))
      (dispatch! "/template")))

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
            {:type "text"
             :on-change #(reset! target (-> % .-target .-value))
             :value @target}
            opts)])

(defn timeout [ms]
  (let [c (chan)]
    (js/setTimeout (fn [] (close! c)) ms)
    c))