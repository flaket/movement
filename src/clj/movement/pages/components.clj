(ns movement.pages.components
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn header []
  [:div
   [:div.pure-g
    [:div.pure-u-1
     [:div.home-menu.pure-menu-horizontal
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
                            :target ""} "Log in"]]]]]]])

(defn footer []
  [:div.footer.l-box.is-center
   [:div.pure-g
    [:div.pure-u.pure-u-md-1-4]
    [:div.pure-u.pure-u-md-1-8
     [:a {:title  "About Movement Session"
          :href   "/about"
          :target ""} "About"]]
    [:div.pure-u.pure-u-md-1-8
     [:a {:title  "Contact Us"
          :href   "/contact"
          :target ""} "Contact"]]
    [:div.pure-u.pure-u-md-1-8
     [:a {:title  "Read our Blog"
          :href   "/blog"
          :target ""} "Blog"]]
    [:div.pure-u.pure-u-md-1-8
     [:a {:title  "Terms and agreement"
          :href   "/terms"
          :target ""} "Terms"]]
    [:div.pure-u.pure-u-md-1-4]]
   [:div.pure-g.copyright
    [:div.pure-u.pure-u-md-1-4]
    [:div.pure-u.pure-u-md-1-2 [:i.fa.fa-copyright] "2015 Movement Session"]
    [:div.pure-u.pure-u-md-1-4]]])
