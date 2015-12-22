(ns movement.pages.pricing
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [movement.pages.components :refer [header footer-2]]))

(defn pricing-page []
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
       "/css/marketing.css"
       "/css/site.css"
       "/css/pricing.css")]
    [:body
     #_[:div
      (header)
      [:div.content.is-center
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-5]
        [:div.pure-u-1.pure-u-md-3-5
         [:div.pure-g
          [:h1.pure-u "Pricing"]]]
        [:div.pure-u.pure-u-md-1-5]]]
      (footer)]

     (header)
     #_[:div.pure-menu.pure-menu-horizontal
      [:a.pure-menu-heading {:href "#"} "Movement Session"]
      [:ul.pure-menu-list
       [:li.pure-menu-item [:a.pure-menu-link {:href "#"} "Home"]]
       [:li.pure-menu-item.pure-menu-selected [:a.pure-menu-link {:href "#"} "Pricing"]]
       [:li.pure-menu-item [:a.pure-menu-link {:href "#"} "Contact"]]]]

     [:div.l-content
      [:div.pricing-tables.pure-g
       [:div.pure-u-1.pure-u-md-1-3]
       [:div.pure-u-1.pure-u-md-1-3
        [:div.pricing-table.pricing-table-biz.pricing-table-selected
         [:div.pricing-table-header
          [:h2 ""]
          [:span.pricing-table-price "$9.95" [:span "per month"]]]
         [:button.button-choose.pure-button [:a {:href "/signup"} "Sign up"]]]]
       [:div.pure-u-1.pure-u-md-1-3]]
      [:div.information.pure-g
       [:div.pure-u-1.pure-u-md-1-3
        [:div.l-box
         [:h3.information-head "30 day free trial"]
         [:p "After registering your credit card you have access for 30 days without any fees."]]]
       [:div.pure-u-1.pure-u-md-1-3
        [:div.l-box
         [:h3.information-head "Cancel your plan anytime"]
         [:p "You are free to cancel your subscription at any time. If you cancel within the first 30 days,
         there will be no charges to your credit card."]]]
       [:div.pure-u-1.pure-u-md-1-3
        [:div.l-box
         [:h3.information-head "Customer support"]
         [:p "We will get back to you within 24 hours."]]]
       ]]
     (footer-2)]))