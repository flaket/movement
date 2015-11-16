(ns movement.components.footer
  (:require [reagent.core :refer [atom]]
            [secretary.core :as secretary
             :include-macros true :refer [dispatch!]]))

(defn footer []
  [:div.footer.l-box.is-center
   [:div.pure-g
    [:div.pure-u
     [:p "Movement Session"]]
    [:div.pure-u
     [:a {:on-click #(dispatch! "/")} "Home"]]
    [:div.pure-u
     [:a {:on-click #(dispatch! "/")} "About"]]
    [:div.pure-u
     [:a {:on-click #(dispatch! "/")} "Blog"]]
    [:div.pure-u
     [:a {:on-click #(dispatch! "/")} "Contact Us"]]

    [:div.pure-u
     [:a
      {:title  "Follow MovementSession on Twitter"
       :href   ""
       :target "_blank"} "Tweet"]]]])