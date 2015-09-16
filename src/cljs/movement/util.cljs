(ns movement.util
  (:import goog.History)
  (:require
    [goog.events :as events]
    [goog.dom :as gdom]
    [goog.crypt.base64 :as b64]
    [goog.history.EventType :as EventType]
    [reagent.session :as session]
    [secretary.core :as secretary
     :include-macros true]
    [ajax.core :as ajax]))

(defn csrf-token []
  (aget js/window "CSRFToken"))

(defn GET [url & [opts]]
  (ajax/GET url opts #_(update-in opts [:params] assoc :timestamp (.getTime (js/Date.)))))

(defn POST [url & [opts]]
  (ajax/POST url {:headers {"X-CSRF-Token" (csrf-token)}}))

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
