(ns movement.styles
  (:require [garden.core :refer [css]]))

(defn insert-styles [styles]
  "Inserts Stylesheet into document head"
  (let [el (.createElement js/document "style")
        node (.createTextNode js/document styles)]
    (.appendChild el node)
    (.appendChild (.-head js/document) el)
    el))

(def test1
  (css [:body
        {:background-color "#fffff6"}]))

(insert-styles test1)