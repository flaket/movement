(ns movement.pages.components
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn header []
  [:div.home-menu.pure-menu-horizontal
   [:a.pure-menu-heading.pure-u {:title  "Home"
                                 :href   "/"
                                 :target ""} "Movement Session"]
   [:ul.pure-menu-list
    [:li.pure-menu-item
     [:a.pure-menu-link {:title  "Tour"
                         :href   "/tour"
                         :target ""} "Tour"]]
    [:li.pure-menu-item
     [:a.pure-menu-link {:title  "Pricing"
                         :href   "/pricing"
                         :target ""} "Pricing"]]
    [:li.pure-menu-item
     [:a.pure-menu-link {:title  "Log in"
                         :href   "/app"
                         :target ""} "Log in"]]]])

(defn footer-2 []
  [:div.footer.l-box.is-center
   [:div#footer-links.pure-g
    [:div.pure-u.pure-u-md-1-3]
    [:div.pure-u.pure-u-md-1-12
     [:a {:title  "About Movement Session"
          :href   "/about"
          :target ""} "About"]]
    [:div.pure-u.pure-u-md-1-12
     [:a {:title  "Contact Us"
          :href   "/contact"
          :target ""} "Contact"]]
    [:div.pure-u.pure-u-md-1-12
     [:a {:title  "Read our Blog"
          :href   "/blog"
          :target ""} "Blog"]]
    [:div.pure-u.pure-u-md-1-12
     [:a {:title  "Terms and agreement"
          :href   "/terms"
          :target ""} "Terms/Privacy"]]
    [:div.pure-u.pure-u-md-1-3]]
   [:div#footer-logo.pure-g
    [:div.pure-u.pure-u-md-1-3]
    [:div.pure-u.pure-u-md-1-3 [:img {:width 75 :height 75 :src "images/static-air-baby.png"}]]
    [:div.pure-u.pure-u-md-1-3]]
   [:div#footer-copyright.pure-g
    [:div.pure-u.pure-u-md-1-3]
    [:div.pure-u.pure-u-md-1-3 [:i.fa.fa-copyright] "2015 Movement Session"]
    [:div.pure-u.pure-u-md-1-3]]])

(defn footer []
  [:div#footer.l-box.is-center
   [:div#footer-links.pure-g
    [:div.pure-u.pure-u-md-1-3]
    [:div.pure-u.pure-u-md-1-12
     [:a {:title  "About Movement Session"
          :href   "/about"
          :target ""} "About"]]
    [:div.pure-u.pure-u-md-1-12
     [:a {:title  "Contact Us"
          :href   "/contact"
          :target ""} "Contact"]]
    [:div.pure-u.pure-u-md-1-12
     [:a {:title  "Read our Blog"
          :href   "/blog"
          :target ""} "Blog"]]
    [:div.pure-u.pure-u-md-1-12
     [:a {:title  "Terms and agreement"
          :href   "/terms"
          :target ""} "Terms/Privacy"]]
    [:div.pure-u.pure-u-md-1-3]]
   [:div#footer-logo.pure-g
    [:div.pure-u.pure-u-md-1-3]
    [:div.pure-u.pure-u-md-1-3 [:img {:width 75 :height 75 :src "images/static-air-baby.png"}]]
    [:div.pure-u.pure-u-md-1-3]]
   [:div#footer-copyright.pure-g
    [:div.pure-u.pure-u-md-1-3]
    [:div.pure-u.pure-u-md-1-3 [:i.fa.fa-copyright] "2015 Movement Session"]
    [:div.pure-u.pure-u-md-1-3]]])
