(ns movement.pages.contact
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [movement.pages.components :refer [header footer]]))

(defn contact-page []
  (html5
    [:head
     [:title ""]
     (include-js "analytics.js")
     (include-css
       "https://fonts.googleapis.com/css?family=Roboto"
       "https://fonts.googleapis.com/css?family=Raleway"
       "https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css"

       "/css/pure-min.css"
       "/css/grids-responsive-min.css"
       "/css/normalize.css"
       "/css/animate.min.css"
       "/css/marketing.css"
       "/css/side-menu.css"
       "/css/site.css")]
    [:body
     [:div
      (header)
      [:div.content.is-center
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-5]
        [:div.pure-u-1.pure-u-md-2-5
         [:div.pure-g
          [:h1.pure-u "Contact Us"]]
         [:div.pure-g
          [:p.pure-u "support@movementsession.com"]]]
        [:div.pure-u.pure-u-md-1-5]]]
      (footer)]]))