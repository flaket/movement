(ns movement.text
  (:require [reagent.core :refer [dom-node atom]]))

#_(defn auto-complete-did-mount
  "Attaches the jQuery autocomplete functionality to DOM elements."
  []
  (js/$ (fn []
          (let [available-tags []]
            (.autocomplete (js/$ "#tags")
                           (clj->js {:source available-tags}))))))

(defn text-input-component [{:keys [title on-save on-stop]}]
  (let [val (atom title)
        stop #(do (reset! val "")
                  (if on-stop (on-stop)))
        save #(let [v (-> @val str clojure.string/trim)]
               (if-not (empty? v) (on-save v))
               (stop))]
    (fn [props]
      [:input.pure-u (merge props
                          {:type "text"
                           :value @val
                           :on-blur #(do (reset! val (-> % .-target .-value))
                                         (save))
                           :on-change #(reset! val (-> % .-target .-value))
                           :on-key-down #(case (.-which %)
                                          13 (save)
                                          27 (stop)
                                          nil)})])))

(def text-edit-component
  (with-meta text-input-component {:component-did-mount #(do (.focus (dom-node %))
                                                             #_(auto-complete-did-mount))}))