(ns movement.pages.contact
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [movement.pages.components :refer [header footer footer-2]]
            ))

(defn contact-page []
  (html5
    [:head
     [:link {:rel "shortcut icon" :href "images/static-air-baby.png"}]
     [:title "Contact Movement Session"]
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
       [:div.pure-u-1.is-center
        [:div.pure-g
         [:p.pure-u-1 "For support questions or feature suggestions, send an email to"]
         [:p.pure-u-1 "support (at) movementsession.com"]]]]]
     (footer)

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]

     ]))