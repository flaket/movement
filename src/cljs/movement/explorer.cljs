(ns movement.explorer
  (:require
    [reagent.session :as session]
    [movement.menu :refer [menu-component]]))

(defn explorer-component []
  (let []
    (fn []

      [:div#layout {:class (str "" (when (session/get :active?) "active"))}

       [menu-component]

       [:div#main
        [:div.header
         [:h1 "Movement Explorer"]]

        [:div.content
         "This section allow users to discover the movements in the database
         view the animations more clearly, add their own comments to the movements
         and mark the subjective difficulty level of the movements."]]])))