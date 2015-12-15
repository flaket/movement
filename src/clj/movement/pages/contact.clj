(ns movement.pages.contact
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]))

(defn contact-page []
  (html5
    [:head
     [:title ""]
     (include-js "/js/analytics.js")
     (include-css
       "http://yui.yahooapis.com/pure/0.6.0/pure-min.css"
       "http://yui.yahooapis.com/pure/0.6.0/grids-responsive-min.css"
       "https://fonts.googleapis.com/css?family=Roboto"
       "https://fonts.googleapis.com/css?family=Raleway"
       "https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css"
       "/css/normalize.css"
       "/css/animate.min.css"
       "/css/marketing.css"
       "/css/side-menu.css"
       "/css/site.css")]
    [:body
     [:div.content
      [:div.pure-g
       [:div.pure-u
        "support@movementsession.com"]]]]))