(ns movement.pages.contact
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [movement.pages.components :refer [html-head top-menu footer-always-bottom footer-after-content]]
            ))

(defn contact-content []
  [:div
   (top-menu)
   [:div.l-content
    [:div.information.pure-g
     [:div.pure-u-1.is-center
      [:div.pure-g
       [:p.pure-u-1 "For support questions or feature suggestions, send an email to"]
       [:p.pure-u-1 "support (at) movementsession.com"]]]]]
   (footer-always-bottom)])

(defn contact-page []
  (html5
    (html-head "Contact")
    [:body
     (contact-content)
     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]]))