(ns movement.pages.landing
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [movement.pages.signup :refer [signup-form]]
            [movement.pages.components :refer [header footer]]
            [movement.activation :refer [generate-activation-id send-activation-email]]))

(defn landing-header []
  [:div.header
   [:div.home-menu.pure-menu.pure-menu-horizontal.pure-menu-fixed
    [:a.pure-menu-heading {:title  "Home"
                           :href   "/"
                           :target ""} "Movement Session"]
    [:ul.pure-menu-list
     [:li.pure-menu-item
      [:a.pure-menu-link {:title  "Blog"
                          :href   "/blog"
                          :target ""} "Blog"]]
     [:li.pure-menu-item
      [:a.pure-menu-link {:title  "Log in"
                          :href   "/app"
                          :target ""} "Log in"]]]]])

(defn prolog []
  [:div.splash-container
   [:div.splash
    [:h1.splash-head.animated.fadeInDown "Plan less, move more"]
    [:p.splash-subhead.animated.fadeInDown "Stop searching for and creating static training programs.
      Let Movement Session generate your workout sessions and be inspired to learn new and challenging ways of moving your body."]
    [:p.animated.fadeInDown
     [:a.pure-button.pure-button-primary {:title  "Sign Up"
                                          :href   "/signup"
                                          :target ""} "Sign Up"]]]])

(defn sell []
  [:div.content
   [:h2.content-head.is-center "Movement Session"]
   [:div.pure-g
    [:div.pure-u.pure-u-lg-1-2
     [:p "Movement Session is a workout generator and a logging tool designed to help you move more,
     learn new skills, be healthier and have fun in the process."]]
    [:div.pure-u.pure-u-lg-1-2
     [:img.pure-u {:src "movement-cards.png" :height "500" :width "500"}]]]])

(defn benefits []
  [:div.content
   [:h2.content-head.is-center "Benefits"]
   [:div.pure-g
    [:div.l-box.pure-u.pure-u-md-1-2.pure-u-lg-1-2
     [:h3.content-subhead [:i.fa.fa-exclamation] "Learn new movements"]
     [:p "Sessions are generated from a growing database of 300+ movements."]]
    [:div.l-box.pure-u.pure-u-md-1-2.pure-u-lg-1-2
     [:h3.content-subhead [:i.fa.fa-diamond] "Unique sessions"]
     [:p "Create countless unique training sessions, either fully planned,
           randomly generated or a suitable combination of the two."]]]
   [:div.pure-g
    [:div.l-box.pure-u.pure-u-md-1-2.pure-u-lg-1-2
     [:h3.content-subhead [:i.fa.fa-random] "Refresh movements"]
     [:p "Your workout specifies that you perform an seemingly impossible exercise?
       No problem, refresh the movement with an easier variation or a totally different exercise."]]
    [:div.l-box.pure-u.pure-u-md-1-2.pure-u-lg-1-2
     [:h3.content-subhead [:i.fa.fa-share-alt] "Log & Share"]
     [:p "Log your sessions with repetitions, sets, time taken and your own comments.
       View logged sessions later and share them with others."]]]])

(defn pricing []
  [:div.ribbon
   [:div.pure-g.content-head-ribbon
    [:div.l-box.pure-u.pure-u-md-1-4]
    [:div.l-box.pure-u.pure-u-md-1-4
     [:h3 "21 day free trial, cancel payment at any time"]]
    [:div.l-box.pure-u.pure-u-md-1-4
     [:h3 "$10 monthly subscription"]]
    [:div.l-box.pure-u.pure-u-md-1-4]]
   [:div.pure-g.content-head-ribbon
    [:div.l-box.pure-u.pure-u-md-1-4]
    [:div.l-box.pure-u.pure-u-md-1-4
     [:h3 "30 day refund guarantee"]]
    [:div.l-box.pure-u.pure-u-md-1-4]
    [:div.l-box.pure-u.pure-u-md-1-4]]])

(defn epilog []
  [:div#epilog.content
   [:h2.content-head.is-center "So, are you ready to start moving more?"]
   [:div.pure-g
    [:div.pure-u.pure-u-md-2-5]
    [:a.pure-u-1.pure-u-md-1-5.pure-button.pure-button-primary
     {:title  "Sign Up"
      :href   "/signup"
      :target ""} "Sign Up"]
    [:div.pure-u.pure-u-md-2-5]]])

(defn landing []
  (html5
    [:head
     [:meta {:http-equiv "cache-control"
             :content "no-cache"}]
     [:meta {:http-equiv "expires"
             :content "0"}]
     [:meta {:http-equiv "pragma"
             :content "no-cache"}]
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
      (landing-header)
      (prolog)
      [:div.content-wrapper
       (sell)
       (benefits)
       (pricing)
       (epilog)
       (footer)]]]))

