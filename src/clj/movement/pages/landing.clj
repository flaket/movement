(ns movement.pages.landing
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [movement.pages.signup :refer [signup-form]]
            [movement.activation :refer [generate-activation-id send-activation-email]]))

(defn header []
  (html
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
                            :target ""} "Log in"]]]]]))

(defn prolog []
  (html
    [:div.splash-container
     [:div.splash
      [:h1.splash-head.animated.fadeInDown "Plan less, move more"]
      [:p.splash-subhead.animated.fadeInDown "You have a body to move; stop creating static training programs and
         let MovementSession inspire you to plan and learn new and challenging ways of moving your body."]
      [:p.animated.fadeInDown
       [:a.pure-button.pure-button-primary {:title  "Sign Up Free"
                                            :href   "/signup"
                                            :target ""} "Sign Up Free"]]]]))

(defn benefits []
  (html
    [:div.content
     [:h2.content-head.is-center "Benefits"]
     [:div.pure-g
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead [:i.fa.fa-refresh] "Learn new movements"]
       [:p "Explore a database with hundreds of movements."]]
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead [:i.fa.fa-diamond] "Unique sessions"]
       [:p "Generate countless unique training sessions, either fully planned,
           randomly generated or a suitable combination."]]
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead [:i.fa.fa-share-alt] "Share"]
       [:p "Share your sessions with others."]]
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead [:i.fa.fa-floppy-o] "Log"]
       [:p "Log your sessions and review them later."]]]]))

(defn ribbon []
  (html
    [:div.ribbon.l-box-lrg.pure-g
     [:div.l-box-lrg.is-center.pure-u-1.pure-u-md-1-2.pure-u-lg-2-5]
     [:div.pure-u-1.pure-u-md-1-2.pure-u-lg-3-5
      [:h2.content-head.content-head-ribbon "tempheader"]
      [:p "temptext"]]]))

(defn epilog []
  (html
    [:div.content
     [:h2.content-head.is-center "So, are you ready to start moving more?"]
     [:div.pure-g
      [:div.l-box-lrg.pure-u-1.pure-u-md-2-5
       (signup-form)]
      [:div.l-box-lrg.pure-u-1.pure-u-md-3-5
       [:h4 "Contact Us"]
       [:i.fa.fa-envelope]
       [:p "support@movementsession.com"]]]]))

(defn footer []
  (html
    [:div.footer.l-box.is-center
     [:div.pure-g
      [:div.pure-u
       [:p "Movement Session"]]
      [:div.pure-u
       [:a {:title  "Movement Session"
            :href   "/"
            :target ""} "Home"]]
      [:div.pure-u
       [:a {:title  "About Movement Session"
            :href   "/about"
            :target ""} "About"]]
      [:div.pure-u
       [:a {:title  "Read our Blog"
            :href   "/blog"
            :target ""} "Blog"]]
      [:div.pure-u
       [:a {:title  "Contact Us"
            :href   "/contact"
            :target ""} "Contact Us"]]
      [:div.pure-u
       [:a {:title  "Follow MovementSession on Twitter"
            :href   "https://twitter.com/movementsessionapp"
            :target "_blank"} "Tweet"]]
      [:div.pure-u
       [:a {:title  "Follow MovementSession on Instagram"
            :href   "https://instagram.com/movementsession"
            :target "_blank"} "Instagram"]]]]))

(defn landing []
  (html5
    [:head
     [:title ""]
     (include-js "http://code.jquery.com/ui/1.11.2/jquery-ui.min.js"
                 "https://code.jquery.com/jquery-1.11.2.min.js")
     (include-css
       "http://code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.min.css"
       "http://yui.yahooapis.com/pure/0.6.0/pure-min.css"
       "http://yui.yahooapis.com/pure/0.6.0/grids-responsive-min.css"
       "https://fonts.googleapis.com/css?family=Roboto"
       "https://fonts.googleapis.com/css?family=Raleway"
       "https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css"

       "/css/normalize.css"
       "/css/animate.min.css"
       "/css/marketing.css"
       "/css/side-menu.css"
       "/css/site.css")]
    [:body
     [:div
      (header)
      (prolog)
      [:div.content-wrapper
       (benefits)
       (ribbon)
       (epilog)
       (footer)]]]))

