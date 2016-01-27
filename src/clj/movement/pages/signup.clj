(ns movement.pages.signup
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [movement.pages.components :refer [header footer footer-2]]
            [movement.activation :refer [generate-activation-id send-activation-email]]))

(defn signup-form []
  [:form.pure-form.pure-form-stacked
   {:method "POST"
    :action "/signup"}
   [:fieldset
    [:input#email {:type        "email"
                   :name        "email"
                   :required    "required"
                   :placeholder "Your Email"}]
    [:input#password {:type        "password"
                      :name        "password"
                      :placeholder "Your Password"
                      :required    "required"}]
    [:input.button-primary {:type  "submit"
                            :value "Sign Up"}]
    (anti-forgery-field)]])

(defn fast-spring-store [ref]
  [:a.pure-u-1.button.button-primary
   {:href (str "http://sites.fastspring.com/roebucksoftware/product/movementsessionsubscription"
                  "?referrer="
                  ref)} "Purchase subscription"])

(defn payment-page [ref message]
  (html5
    [:head
     [:link {:rel "shortcut icon" :href "images/pull-up.png"}]
     [:title "Payment Movement Session"]
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
     #_[:div.content.is-center
      [:div.pure-g
       [:div.pure-u.pure-u-md-2-5]
       [:div.pure-u.pure-u-md-1-5 message]
       [:div.pure-u.pure-u-md-2-5]]
      [:div.pure-g
       [:div.pure-u.pure-u-md-2-5]
       [:div.pure-u.pure-u-md-1-5 (fast-spring-store ref)]
       [:div.pure-u.pure-u-md-2-5]]]
     [:div.l-content
      [:div.information.pure-g
       [:div.pure-u-1.pure-u-md-1-5]
       [:div.pure-u-1.pure-u-md-3-5
        [:div.l-box
         [:div.pure-g [:h3.pure-u-1.information-head message]]
         [:div.pure-g
          [:p.pure-u-1 "To complete the sign up process you will have to register your
         credit card with FastSpring."]]
         [:div.pure-g (fast-spring-store ref)]]]
       [:div.pure-u-1.pure-u-md-1-5]]
      [:div.information.pure-g
       [:div.pure-u-1.pure-u-md-1-3]
       [:div.pure-u-1.pure-u-md-1-3
        [:div.l-box
         ]]
       [:div.pure-u-1.pure-u-md-1-3]]]
     (footer)]))

(defn signup-page [& error-message]
  (html5
    [:head
     [:link {:rel "shortcut icon" :href "images/pull-up.png"}]
     [:title "Sign Up Movement Session"]
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
     (header)
     [:div.content.is-center
      (when error-message
        [:div
         [:div.pure-g
          [:div.pure-u.pure-u-md-2-5]
          [:div.pure-u.pure-u-md-1-5 error-message]
          [:div.pure-u.pure-u-md-2-5]]
         [:div.pure-g
          [:div.pure-u.pure-u-md-2-5]
          [:a.pure-u.pure-u-md-1-5.button.button-primary
           {:title  "Log in"
            :href   "/app"
            :target ""} "Log in"]
          [:div.pure-u.pure-u-md-2-5]]])
      [:div.pure-g
       [:div.pure-u.pure-u-md-2-5]
       [:div.pure-u.pure-u-md-1-5
        (signup-form)]
       [:div.pure-u.pure-u-md-2-5]]]
     (footer)]))

(defn activation-page [message]
  (html5
    [:head
     [:title "Activation Movement Session"]
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
       "/css/site.css"
       "/css/pricing.css")]
    [:body
     (header)
     [:div.l-content
      [:div.information.pure-g
       [:div.pure-u.pure-u-md-1-5]
       [:div.pure-u.pure-u-md-3-5
        [:div.l-box
         [:h3.information-head "Thanks!"]
         [:p message]]]
       [:div.pure-u.pure-u-md-1-5]]]
     (footer)]))