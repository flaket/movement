(ns movement.pages.pricing
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [movement.pages.components :refer [header footer footer-2]]
            [movement.pages.signup :refer [signup-form]]))

(defn pricing-page [& error-message]
  (html5
    [:head
     [:link {:rel "shortcut icon" :href "images/pull-up.png"}]
     [:title "Pricing Movement Session"]
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
     [:div.content
      [:div.pricing-tables.pure-g
       [:div.pure-u-1-12.pure-u-md-1-3]
       [:div.pure-u-5-6.pure-u-md-1-3
        [:div.pricing-table.pricing-table-biz
         [:div.pricing-table-header
          [:h2 ""]
          [:span.pricing-table-price "$8" [:span "per month"]]]]]
       [:div.pure-u-1-12.pure-u-md-1-3]]
      [:div.information.pure-g
       [:div.pure-u.pure-u-md-1-3
        [:div.l-box
         [:h3.information-head "30 day free trial"]
         [:p "After registering your credit card you have access for 30 days without any fees."]]]
       [:div.pure-u.pure-u-md-1-3
        [:div.l-box
         [:h3.information-head "Cancel your plan anytime"]
         [:p "If you cancel within the first 30 days there will be no charges to your credit card."]]]
       [:div.pure-u.pure-u-md-1-3
        [:div.l-box
         [:h3.information-head "Customer support"]
         [:p "We will get back to you within 24 hours."]]]]
      [:div.information.pure-g
       [:div.pure-u-1-12.pure-u-md-1-5]
       [:div.pure-u-5-6.pure-u-md-3-5
        [:div
         (when error-message
           [:div
            [:div.pure-g
             [:div.pure-u-1 error-message]]
            [:div.pure-g
             [:a.pure-u-1.button.button-secondary
              {:title  "Launch app"
               :href   "/app"
               :target ""} "Launch app & Log in"]]])]
        (signup-form)]
       [:div.pure-u-1-12.pure-u-md-1-5]]]
     (footer-2)

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]

     ]))