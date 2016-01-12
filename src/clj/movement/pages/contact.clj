(ns movement.pages.contact
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [movement.pages.components :refer [header footer footer-2]]))

(defn contact-page []
  (html5
    [:head
     [:link {:rel "shortcut icon" :href "images/static-air-baby.png"}]
     [:title "Contact Movement Session"]
     (include-js "analytics.js")
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
       [:div.pure-u-1.pure-u-md-1-3]
       [:div.pure-u-1.pure-u-md-1-3
        [:div.l-box
         [:h3.information-head "Contact Us"]
         [:p "support@movementsession.com"]]]
       [:div.pure-u-1.pure-u-md-1-3]]
      [:div.information.pure-g
       [:div.pure-u-1.pure-u-md-1-2
        [:div.l-box
         [:h3.information-head "30 day free trial"]
         [:p "After registering your credit card you have access for 30 days without any fees."]]]
       [:div.pure-u-1.pure-u-md-1-2
        [:div.l-box
         [:h3.information-head "Cancel your plan anytime"]
         [:p "You are free to cancel your subscription at any time. If you cancel within the first 30 days,
         there will be no charges to your credit card."]]]]
      [:div.information.pure-g
       [:div.pure-u-1.pure-u-md-1-2
        [:div.l-box
         [:h3.information-head "30 day free trial"]
         [:p "After registering your credit card you have access for 30 days without any fees."]]]
       [:div.pure-u-1.pure-u-md-1-2
        [:div.l-box
         [:h3.information-head "Cancel your plan anytime"]
         [:p "You are free to cancel your subscription at any time. If you cancel within the first 30 days,
         there will be no charges to your credit card."]]]]]
     (footer-2)]))