(ns movement.components.footer
  (:require [reagent.core :refer [atom]]))

(defn footer []
  (let []
    (fn []
      [:nav.footer-nav
       [:div
        [:div
         [:ul
          [:li.header "MovementSession"]
          [:li [:a {:href "/about"} "About"]]
          [:li [:a {:href "/blog"} "Blog"]]]]
        [:div
         [:ul
          [:li.header "Help"]
          [:li [:a {:href "/security"} "Security"]]
          [:li [:a {:href "/privacy"} "Privacy"]]
          [:li [:a {:href "/contact"} "Contact Us"]]]]
        [:div
         [:a.fa.fa-twitter
          {:title "Follow MovementSession on Twitter"
           :href ""
           :target "_blank"}]]]])))