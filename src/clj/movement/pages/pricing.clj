(ns movement.pages.pricing
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [movement.pages.components :refer [html-head top-menu footer-always-bottom footer-after-content]]
            [movement.pages.signup :refer [signup-form]]))

(defn pricing-page [& error-message]
  (html5
    (html-head "Sign Up | Movement Session")
    [:body
     (top-menu)
     [:div.content
      #_[:div.pure-g
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
         [:p "I will always get back to you within 24 hours."]]]]

      [:div.information.pure-g
       [:div.pure-u-1.is-center
        (when error-message
          [:div.pure-g
           [:div.pure-u.pure-u-md-1-3]
           [:div.pure-u-1.pure-u-md-1-3
            [:div.pure-g
             [:div.pure-u-1.center error-message]]
            [:div.pure-g
             [:a.pure-u-1.button.center
              {:title  "Launch app"
               :href   "/app"
               :target ""} "Launch app & Log in"]]]
           [:div.pure-u.pure-u-md-1-3]])]]
      [:div.information.pure-g
       [:div.pure-u.pure-u-lg-1-3]
       [:div.pure-u-1.pure-u-lg-1-3
        (signup-form)]]

      #_[:div.pure-g
       [:div.pure-u-1
         [:div.pure-u.pure-u-md-1-3]
         [:div.pure-u-1.pure-u-md-1-3
          [:div.pricing-table.pricing-table-biz
           [:div.pricing-table-header.center
            [:h2 ""]
            [:span.pricing-table-price "$8" [:span "per month"]]]]]
         [:div.pure-u.pure-u-md-1-3]]

        [:div.pure-g
         [:div.pure-u.pure-u-md-1-3]
         [:div.pure-u-1.pure-u-md-1-3
          ]
         [:div.pure-u.pure-u-md-1-3]]]]]
     (footer-always-bottom)

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]))