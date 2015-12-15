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
      [:p.splash-subhead.animated.fadeInDown "Stop searching for and creating static training programs.
      Let Movement Session generate your workout sessions and be inspired to learn new and challenging ways of moving your body."]
      [:p.animated.fadeInDown
       [:a.pure-button.pure-button-primary {:title  "Sign Up Free"
                                            :href   "/signup"
                                            :target ""} "Sign Up Free"]]]]))

(defn sell []
  (html
    [:div.content
     [:h2.content-head.is-center "Movement Session"]
     [:div.pure-g
      [:div.pure-u
       [:p "You want to move more, be healthier and have fun in the process.
       Movement Session is a workout generator and a logging tool designed to help you with these goals.
       "]]]
     [:div.pure-g
      [:div.pure-u.pure-u-md-1-5]
      [:img.pure-u {:src "movement-cards.png" :height "600" :width "600"}]]]))

(defn benefits []
  (html
    [:div.content
     [:h2.content-head.is-center "Benefits"]
     [:div.pure-g
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead [:i.fa.fa-exclamation] "Learn new movements"]
       [:p "Sessions are generated from a growing database of 300+ movements."]]
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead [:i.fa.fa-diamond] "Unique sessions"]
       [:p "Create countless unique training sessions, either fully planned,
           randomly generated or a suitable combination of the two."]]
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead [:i.fa.fa-random] "Refresh movements"]
       [:p "Your workout specifies that you perform an seemingly impossible exercise?
       No problem, refresh the movement with an easier variation or a totally different exercise."]]
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead [:i.fa.fa-share-alt] "Log & Share"]
       [:p "Log your sessions with repetitions, sets, time taken and your own comments.
       View logged sessions later and share them with others."]]]]))

(defn pricing []
  (html
    [:div.ribbon
     [:div.pure-g.content-head-ribbon
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead ""]
       [:p ""]]
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead "Free for 14 days"]
       [:p "Try Movement Session for free for two weeks"]]
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead "Monthly subscription"]
       [:p "$7/month after the first two weeks"]]]
     [:div.pure-g.content-head-ribbon
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead ""]
       [:p ""]]
      [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
       [:h3.content-subhead "30 day guarantee"]
       [:p "If you for any reason are not satisfied we will refund you within 30 days, no questions asked."]]]]))

(defn epilog []
  (html
    [:div.content
     [:h2.content-head.is-center "So, are you ready to start moving more?"]
     [:div.pure-g
      [:div.l-box-lrg.pure-u-1.pure-u-md-1-4]
      [:div.l-box-lrg.pure-u-1.pure-u-md-1-2
       (signup-form)]
      [:div.l-box-lrg.pure-u-1.pure-u-md-1-4]]]))

(defn footer []
  (html
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
            :target "_blank"} "Instagram"]]]]))

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
     #_(include-js "http://code.jquery.com/ui/1.11.2/jquery-ui.min.js"
                 "https://code.jquery.com/jquery-1.11.2.min.js")
     (include-js "/js/analytics.js")
     (include-css
       ;"http://code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.min.css"
       ;"http://yui.yahooapis.com/pure/0.6.0/pure-min.css"
       ;"http://yui.yahooapis.com/pure/0.6.0/grids-responsive-min.css"
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
      (header)
      (prolog)
      [:div.content-wrapper
       (sell)
       (benefits)
       (pricing)
       (epilog)
       (footer)]]]))

