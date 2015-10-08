(ns movement.components.footer
  (:require [reagent.core :refer [atom]]
            [secretary.core :as secretary
             :include-macros true :refer [dispatch!]]))

(defn footer []
  (let []
    (fn []
      [:nav.footer-nav
       [:div
        [:div
         [:a {:on-click #(dispatch! "/")} "MS"]]
        [:div
         [:ul
          [:li [:a {:on-click #(dispatch! "/")} "Home"]]
          [:li [:a {:on-click #(dispatch! "/about")} "About"]]
          [:li [:a {:on-click #(dispatch! "/blog")} "Blog"]]
          [:li [:a {:on-click #(dispatch! "/contact")} "Contact Us"]]]]
        [:div
         [:a.fa.fa-twitter
          {:title "Follow MovementSession on Twitter"
           :href ""
           :target "_blank"} "Tweet"]]]])))