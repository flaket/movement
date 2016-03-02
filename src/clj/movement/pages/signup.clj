(ns movement.pages.signup
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [movement.pages.components :refer [html-head top-menu footer-always-bottom footer-after-content]]
            [movement.activation :refer [generate-activation-id send-activation-email]]))

(defn signup-form []
  [:form.pure-form.pure-form-stacked
   {:method "POST"
    :action "/signup"}
   [:fieldset
    [:input#email.pure-input-1
     {:type        "email"
      :name        "email"
      :required    "required"
      :placeholder "Your Email"}]
    [:input#password.pure-input-1
     {:type        "password"
      :name        "password"
      :placeholder "Your Password"
      :required    "required"}]
    [:input.button.pure-input-1
     {:type  "submit"
      :value "Sign up"}]
    (anti-forgery-field)]])

(defn fast-spring-store [ref]
  [:a.pure-u-1.button.button-primary
   {:href (str "http://sites.fastspring.com/roebucksoftware/product/movementsessionsubscription"
                  "?referrer="
                  ref)} "Purchase subscription"])

(defn payment-page [ref message]
  (html5
    (html-head "Payment | Movement Session")
    [:body
     (top-menu)
     [:div.l-content
      [:div.information.pure-g
       [:div.pure-u-1.pure-u-md-1-5]
       [:div.pure-u-1.pure-u-md-3-5
        [:div.l-box
         [:div.pure-g [:h3.pure-u-1.information-head message]]
         [:div.pure-g
          [:p.pure-u-1 "Congratulations! You can now log in to the app and start creating workouts."]]
         [:div.pure-g
          [:div.pure-u-1.center
           [:a.button.button-primary
            {:title  "Launch app"
             :href   "/app"
             :target ""} "Launch app"]]]
         #_[:div.pure-g
          [:p.pure-u-1 "Register a credit card with payment provider FastSpring to complete the sign up process."]]
         #_[:div.pure-g (fast-spring-store ref)]]]
       [:div.pure-u-1.pure-u-md-1-5]]
      [:div.information.pure-g
       [:div.pure-u-1.pure-u-md-1-5]
       [:div.pure-u-1.pure-u-md-3-5
        [:div.l-box
         ]]
       [:div.pure-u-1.pure-u-md-1-5]]]
     (footer-always-bottom)

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]]))

(defn signup-page [& error-message]
  (html5
    (html-head "Sign Up | Movement Session")
    [:body
     (top-menu)
     [:div.content.is-center
      (when error-message
        [:div
         [:div.pure-g
          [:div.pure-u.pure-u-md-1-3]
          [:div.pure-u-1.pure-u-md-1-3 error-message]
          [:div.pure-u.pure-u-md-1-3]]
         [:div.pure-g
          [:div.pure-u.pure-u-md-1-3]
          [:a.pure-u-1.pure-u-md-1-3.button.button-primary
           {:title  "Log in"
            :href   "/app"
            :target ""} "Log in"]
          [:div.pure-u.pure-u-md-1-3]]])
      [:div.pure-g
       [:div.pure-u.pure-u-md-1-5]
       [:div.pure-u-1.pure-u-md-3-5
        (signup-form)]
       [:div.pure-u.pure-u-md-1-5]]]
     (footer-always-bottom)]))

(defn activation-page [message]
  (html5
    (html-head "Activation | Movement Session")
    [:body
     (top-menu)
     [:div.l-content
      [:div.information.pure-g
       [:div.pure-u.pure-u-md-1-5]
       [:div.pure-u.pure-u-md-3-5
        [:div.l-box
         [:h3.information-head "Thanks!"]
         [:p message]]]
       [:div.pure-u.pure-u-md-1-5]]]
     (footer-always-bottom)

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]

     ]))