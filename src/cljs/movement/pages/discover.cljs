(ns movement.pages.discover
  (:require [movement.menu :refer [menu-component]]))

(defn discover-page []
  (let []
    (fn []
      [:div#layout
       [menu-component]
       [:div.content
        [:h1 "DISCOVER"]]])))