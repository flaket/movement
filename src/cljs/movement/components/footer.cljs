(ns movement.components.footer
  (:require [reagent.core :refer [atom]]))

(defn footer []
  (let []
    (fn []
      [:nav.footer-nav
       [:div
        [:div
         [:a
          {:title "MovementSession"
           :href ""
           :target "_blank"} "MS"]]
        [:div
         [:ul
          [:li [:a {:href "/home"} "Home"]]
          [:li [:a {:href "/about"} "About"]]
          [:li [:a {:href "/blog"} "Blog"]]
          [:li [:a {:href "/contact"} "Contact Us"]]]]
        [:div
         [:a.fa.fa-twitter
          {:title "Follow MovementSession on Twitter"
           :href ""
           :target "_blank"} "Tweet"]]]])))