(ns movement.pages.signup
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [movement.pages.components :refer [header footer]]
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
    [:input {:type  "submit"
             :value "Sign Up Free"}]
    (anti-forgery-field)]])

(defn signup-page [& error-message]
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
          [:a.pure-u.pure-u-md-1-5.pure-button.pure-button-primary
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

#_[div
   [:span.pure-u [:i.fa.fa-envelope-o.fa-fw]]
   [:input {:type        "email"
            :name        "email"
            :placeholder "Your Email"}]
   [:span.pure-u [:i.fa.fa-key.fa-fw]]
   [:input {:type        "password"
            :name        "password"
            :placeholder "Your Password"}]
   [:button.pure-button {} "Sign Up Free"]]