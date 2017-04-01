(ns movement.util
  (:require [reagent.session :as session]))

(defn vec-remove
  "Removes element from within a vector."
  [pos coll]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(defn positions
  "Finds the integer positions of the elements in the collectios that matches the predicate."
  [pred coll]
  (keep-indexed
    (fn [idx x](when (pred x) idx))
    coll))

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
