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
    [cljs.core.async :as async :refer [>! <! put! take! alts! chan sliding-buffer close!]]))

(defn get-csrf-token []
  (aget js/window "CSRFToken"))

(defn GET [url & [opts]]
  (cljs-ajax/GET url opts #_(update-in opts [:params] assoc :timestamp (.getTime (js/Date.)))))

(defn POST [url & [opts]]
  (cljs-ajax/POST url {:headers {"X-CSRF-Token" (get-csrf-token)}}))

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

(defn ajax-opts [{:keys [keywords? context headers format csrf-token uri method]
                  :or {keywords? true format :json csrf-token true}
                  :as opts}]
  (let [csrf-header (when (and csrf-token (re-find #"^/" uri))
                      {:X-CSRFToken (get-csrf-token)})
        format-opts {:format          (cljs-ajax/edn-request-format)
                     :response-format (cljs-ajax/edn-response-format {:keywords? keywords? :url uri :method method})
                     :keywords?       keywords?
                     :headers         (merge {:Accept "application/edn"}
                                             csrf-header
                                             headers)}]
    (-> opts
        (merge format-opts)
        cljs-ajax/transform-opts)))

(defn ajax [method url & {:as opts}]
  (let [channel (chan)
        base-opts {:method method
                   :uri url
                   :handler #(put! channel %)
                   :error-handler #(put! channel %)
                   :finally #(close! channel)}]
    (-> base-opts
        (merge opts)
        ;ajax-opts
        cljs-ajax/ajax-request)
    channel))
