(ns movement.pages.components
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn html-head [title]
  [:head
   [:link {:rel "shortcut icon" :href "images/movements/static-air-baby.png"}]
   [:title title]
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
     "/css/pricing.css")
   [:meta {:name    "description"
           :content "Try Movement Session to learn functional and natural movement training through generated workouts."}]
   [:meta {:name    "google-site-verification"
           :content "4dEA4Y9dvxAtlRBwuG5bwlmo9fEKfI7TTX5wo4bdj_M"}]
   [:meta {:name    "msvalidate.01"
           :content "2B704D213DB4E877B4F06C35F4FADFC4"}]])

(defn top-menu []
  [:div.home-menu.pure-menu-horizontal
   [:a.pure-menu-heading.pure-u {:title  "Home"
                                 :href   "/"
                                 :target "_self"} "Movement Session " [:span "BETA"] ""]
   [:ul.pure-menu-list
    [:li.pure-menu-item
     [:a.pure-menu-link {:title  "Home"
                         :href   "/"
                         :target "_self"} "Home"]]
    [:li.pure-menu-item
     [:a.pure-menu-link {:title  "Tour"
                         :href   "/tour"
                         :target "_self"} "Tour"]]
    [:li.pure-menu-item
     [:a.pure-menu-link {:title  "Sign up"
                         :href   "/signup"
                         :target "_self"} "Sign up"]]
    [:li.pure-menu-item
     [:a.pure-menu-link {:title  "Log in"
                         :href   "/app"
                         :target "_blank"} "Log In"]]]])

(defn footer-after-content []
  [:div.footer.l-box.center
   [:div#footer-links.pure-g
    [:div.pure-u.pure-u-md-1-4]
    [:div.pure-u.pure-u-md-1-2
     [:div.pure-g
      [:div.pure-u.pure-u-md-1-5 [:a {:title "About Movement Session" :href "/about" :target "_top"} "About"]]
      [:div.pure-u.pure-u-md-1-5 [:a {:title "Contact Us" :href "/contact" :target "_top"} "Contact"]]
      [:div.pure-u.pure-u-md-1-5 [:a {:title "Read our Blog" :href "/blog" :target "_top"} "Blog"]]
      [:div.pure-u.pure-u-md-1-5 [:a {:title "Terms and agreement" :href "/terms" :target "_top"} "Terms/Privacy"]]
      [:div.pure-u.pure-u-md-1-5 [:a {:title  "Follow Movement Session on Twitter" :href "https://twitter.com/SessionMovement"
                                      :target "" :class "twitter-follow-button" :data-show-count "false"} "@SessionMovement"]]]]
    [:div.pure-u.pure-u-md-1-4]]
   [:div#footer-logo.pure-g
    [:div.pure-u.pure-u-md-1-3]
    [:div.pure-u.pure-u-md-1-3 [:img {:width 75 :height 75 :src "images/movements/static-air-baby.png"}]]
    [:div.pure-u.pure-u-md-1-3]]
   [:div#footer-copyright.pure-g
    [:div.pure-u.pure-u-md-1-3]
    [:div.pure-u.pure-u-md-1-3 [:i.fa.fa-copyright] "2016 Movement Session"]
    [:div.pure-u.pure-u-md-1-3]]])

(defn footer-always-bottom []
  [:div#footer.l-box.center
   [:div#footer-links.pure-g
    [:div.pure-u.pure-u-md-1-4]
    [:div.pure-u.pure-u-md-1-2
     [:div.pure-g
      [:div.pure-u.pure-u-md-1-5 [:a {:title "About Movement Session" :href "/about" :target "_top"} "About"]]
      [:div.pure-u.pure-u-md-1-5 [:a {:title "Contact Us" :href "/contact" :target "_top"} "Contact"]]
      [:div.pure-u.pure-u-md-1-5 [:a {:title "Read our Blog" :href "/blog" :target "_top"} "Blog"]]
      [:div.pure-u.pure-u-md-1-5 [:a {:title "Terms and agreement" :href "/terms" :target "_top"} "Terms/Privacy"]]
      [:div.pure-u.pure-u-md-1-5 [:a {:title  "Follow Movement Session on Twitter" :href "https://twitter.com/SessionMovement"
                                      :target "" :class "twitter-follow-button" :data-show-count "false"} "@SessionMovement"]]]]
    [:div.pure-u.pure-u-md-1-4]]
   [:div#footer-logo.pure-g
    [:div.pure-u-1.center [:img {:width 75 :height 75 :src "images/movements/static-air-baby.png"}]]]
   [:div#footer-copyright.pure-g
    [:div.pure-u-1.center [:i.fa.fa-copyright] "2016 Movement Session"]]])
