(ns movement.pages.about
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [movement.pages.components :refer [header footer footer-2]]))

(defn about-page []
  (html5
    [:head
     [:link {:rel "shortcut icon" :href "images/static-air-baby.png"}]
     [:title "About Movement Session"]
     [:script {:src "analytics.js" :type "text/javascript"}]
     (include-css
       "https://fonts.googleapis.com/css?family=Roboto"
       "https://fonts.googleapis.com/css?family=Raleway"
       "https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css"
       "/css/pure-min.css"
       "/css/grids-responsive-min.css"
       "/css/normalize.css"
       "/css/marketing.css"
       "/css/site.css"
       "/css/pricing.css")]
    [:body
     (header)
     [:div.l-content
      [:div.information.pure-g
       [:div.pure-u-1
        [:div.l-box
         [:h3.information-head "About"]
         [:p "Movement Session is an online tool for generating brand, spanking new workouts and logging your progress."]]]]
      [:div.information.pure-g
       [:div.pure-u.pure-u-md-1-5
        [:div.l-box
         [:img {:width 150 :height 150 :src "images/static-air-baby.png"}]]]
       [:div.pure-u.pure-u-md-4-5
        [:div.l-box
         [:p "Movement Session was created by me, Andreas Flakstad. I'm a software engineer and small business owner from Trondheim, Norway."]
         [:p "You can contact me at admin (at) movementsession.com, or reach me through Twitter @AndreasFlakstad."]]]]]
     (footer-2)

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]

     ]))