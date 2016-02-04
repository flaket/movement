(ns movement.pages.about
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [movement.pages.components :refer [html-head top-menu footer-always-bottom footer-after-content]]))

(defn about-page []
  (html5
    (html-head "About")
    [:body
     (top-menu)
     [:div.l-content
      [:div.information.pure-g
       [:div.pure-u.pure-u-md-1-5
        [:img {:width 150 :height 150 :src "images/static-air-baby.png"}]]
       [:div.pure-u.pure-u-md-4-5
        [:div.pure-g
         [:p.pure-u-1 "Movement Session was created by me, Andreas Flakstad. I'm a software engineer and small business owner from Trondheim, Norway."]]
        [:div.pure-g
         [:p.pure-u-1 "You can contact me at admin (at) movementsession.com, or reach me through Twitter @AndreasFlakstad."]]]]]
     (footer-always-bottom)

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]

     ]))