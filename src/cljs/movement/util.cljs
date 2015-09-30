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
    [ajax.core :as cljs-ajax]
    [cljs.core.async :as async :refer [>! <! put! take! alts! chan sliding-buffer close!]]
    [dommy.core :as dommy :refer-macros [sel sel1]]))

(def csrf-token
  (dommy/attr (sel1 :#anti-forgery-token) "value"))

(defn GET [url & [opts]]
  (cljs-ajax/GET url opts #_(update-in opts [:params] assoc :timestamp (.getTime (js/Date.)))))

(defn POST [url & [opts]]
  (let [base-opts {:headers {:x-csrf-token csrf-token}}]
    (cljs-ajax/POST url (merge base-opts opts))))

(defn get-templates []
  (GET "templates" {:handler       #(session/put! :templates %)
                    :error-handler #(print "error retrieving templates.")}))

(defn get-all-categories []
  (GET "categories" {:handler       #(session/put! :all-categories %)
                     :error-handler #(print "error retrieving categories.")}))

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

#_(defn ajax-opts [{:keys [keywords? context headers format uri method]
                  :as opts}]
  (let [format-opts {:format          (cljs-ajax/edn-request-format)
                     :response-format (cljs-ajax/edn-response-format)
                     :headers         (merge {:x-csrf-token csrf-token}
                                             headers)}]
    (-> opts
        (merge format-opts)
        cljs-ajax/transform-opts)))

#_(defn ajax [method url & {:as opts}]
  (let [channel (chan)
        base-opts {:method method
                   :uri url
                   :handler #(put! channel %)
                   :error-handler #(put! channel %)
                   :finally #(close! channel)}]
    (-> base-opts
        (merge opts)
        ajax-opts
        cljs-ajax/ajax-request)
    channel))

(defn timeout [ms]
  (let [c (chan)]
    (js/setTimeout (fn [] (close! c)) ms)
    c))