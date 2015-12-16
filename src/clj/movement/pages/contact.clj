(ns movement.pages.contact
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [movement.pages.landing :refer [header footer]]))

(defn contact-page []
  (html5
    [:head
     [:title ""]
     (include-js "js/analytics.js")
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
                                :target ""} "Log in"]]]]]]]

      [:div.content.is-center
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-5]
        [:div.pure-u-1.pure-u-md-2-5
         [:div.pure-g
          [:h1.pure-u "Contact Us"]]
         [:div.pure-g
          [:p.pure-u "support@movementsession.com"]]]
        [:div.pure-u.pure-u-md-1-5]]]

      [:div.footer.l-box.is-center
       [:div.pure-g
        [:div.pure-u.pure-u-md-2-5]
        [:div.pure-u.pure-u-md-1-5 "Movement Session 2015"]
        [:div.pure-u.pure-u-md-2-5]]
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-8
         [:a {:title  "Contact Us"
              :href   "/contact"
              :target ""} "Contact Us"]]
        #_[:div.pure-u.pure-u-md-1-8
           [:a {:title  "Movement Session"
                :href   "/"
                :target ""} "Movement Session"]]
        #_[:div.pure-u.pure-u-md-1-8
           [:a {:title  "About Movement Session"
                :href   "/about"
                :target ""} "About"]]
        #_[:div.pure-u.pure-u-md-1-8
           [:a {:title  "Read our Blog"
                :href   "/blog"
                :target ""} "Blog"]]

        #_[:div.pure-u.pure-u-md-1-8
           [:a {:title  "Follow MovementSession on Twitter"
                :href   "https://twitter.com/movementsessionapp"
                :target "_blank"} "Tweet"]]
        #_[:div.pure-u.pure-u-md-1-8
           [:a {:title  "Follow MovementSession on Instagram"
                :href   "https://instagram.com/movementsession"
                :target "_blank"} "Instagram"]]]]]]))