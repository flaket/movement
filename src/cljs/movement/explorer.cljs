(ns movement.explorer
  (:require [movement.nav :refer [nav-component]]))

(defn explorer-component []
  [:div
   [:div.container
    [nav-component]
    [:section
     [:div "This section allow users to discover the movements in the database,
    view the animations more clearly, add their own comments to the movements
    and mark the subjective difficulty level of the movements."]]]])