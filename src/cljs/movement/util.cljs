(ns movement.util
  (:import goog.History)
  (:require
    [goog.events :as events]
    [goog.history.EventType :as EventType]
    [reagent.session :as session]
    [secretary.core :as secretary :include-macros true]))

(defn vec-remove
  [pos coll]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(defn positions
  "Finds the integer positions of the elements in the collection, that matches the predicate."
  [pred coll]
  (keep-indexed
    (fn [idx x]
      (when (pred x) idx))
    coll))

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
