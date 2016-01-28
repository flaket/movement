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
      [:a.pure-menu-link {:title  "Home"
                          :href   "/"
                          :target ""} "Home"]]
     [:li.pure-menu-item
      [:a.pure-menu-link {:title  "Tour"
                          :href   "/tour"
                          :target ""} "Tour"]]
     [:li.pure-menu-item
      [:a.pure-menu-link {:title  "Pricing"
                          :href   "/pricing"
                          :target ""} "Pricing"]]
     [:li.pure-menu-item
      [:a.pure-menu-link {:title  "Sign in"
                          :href   "/app"
                          :target ""} "Sign In"]]]]])

(defn splash []
  [:div.splash-container
   [:div.splash
    [:h1.splash-head.animated.fadeInDown "Plan less, move more"]
    [:p.splash-subhead.animated.fadeInDown "Spend less time searching for and creating static training programs
      and spend more time moving your body."]
    [:div.animated.fadeInDown
     [:a.pure-button.pure-button-primary {:title  "Sign Up"
                                          :href   "/pricing"
                                          :target ""} "Sign Up"]]]])

(defn sell []
  [:div#sell.content
   [:div.pure-g [:p.pure-u-1 "
   Working out. So many ways of moving the body, so little time.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   Maybe you spend a lot of time pondering what to do each time you go to the gym or work out in your back yard.
   "]]
   [:div.pure-g [:p.pure-u-1 [:b "
   You know a lot of movements, but have trouble recalling them all and often end up doing the same things as yesterday.
   "]]]
   [:div.pure-g [:p.pure-u-1 "
   If that's the case, you're going to love Movement Session.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   With Movement Session you can create or find templates that define the structure of the workout. When it comes time to move
   you will get a generated workout session from this template. You can stop roaming the internet searching for static workout plans,
   and instead generate thousands of unique and beautifully presented sessions.
   "]]
   [:div.pure-g
    [:div.pure-u-1
     [:img {:src "old-style-to-new.png" :height "1200" :width "800"}]]]
   [:div.pure-g [:p.pure-u-1 "
   At this time there are more than 300 movements in the database. We have created the database with a focus on bodyweight exercises
    typically seen in gymnastics and calisthenics. There are also some weighted exercises related to crossfit and olympic lifting.
   We want the Movement Session database to grow large and to encompass all sorts of varied movements and activities. Therefore we're exploring
    many disciplines, including gymnastics, calisthenics, crossfit, olympic lifting, parkour,
     yoga, dance, martial arts and other sports. We will be adding ten new movements every week.
   "]]
   [:div.pure-g
    [:div.pure-u.pure-u-lg-1-4]
    [:div.pure-u
     [:img {:src "movement-cards.png" :height "500" :width "500"}]]]
   [:div.pure-g
    [:div.pure-u-1
     [:div.pure-g
      [:p.pure-u.pure-u-md-1-3.is-center [:b "Generate"]]
      [:p.pure-u.pure-u-md-1-3.is-center [:b "Swap"]]
      [:p.pure-u.pure-u-md-1-3.is-center [:b "Log & Share"]]]
     [:div.pure-g
      [:p.pure-u.pure-u-md-1-3 "Create your own unique sessions and workout plans or explore what others have created. Find sessions and workout plans to help reach your movement goals."]
      [:p.pure-u.pure-u-md-1-3
       "Adjust the difficulty of a workout by swapping exercises with easier or harder variations."]
      [:p.pure-u.pure-u-md-1-3 "Log your workouts with rich data, view past sessions and share sessions with your friends"]]]]])

(defn epilog []
  [:div#epilog.content
   [:h2.content-head.is-center "So, are you ready to start moving more?"]
   [:div.pure-g
    [:div.pure-u.pure-u-md-1-12.pure-u-lg-1-4]
    [:p.pure-u.pure-u-md-1-3.pure-u-lg-1-4
     [:a.pure-button-primary {:title  "Sign Up" :href   "/pricing" :target ""} "Sign Up"]]
    [:p.pure-u.pure-u-md-1-3.pure-u-lg-1-4
     [:a {:title  "Take a tour" :href   "/tour" :target ""} "Or take a tour"]]
    [:div.pure-u.pure-u-md-1-12.pure-u-lg-1-4]]])

(defn landing-page []
  (html5
    [:head
     [:link {:rel "shortcut icon" :href "images/static-air-baby.png"}]
     [:meta {:http-equiv "cache-control"
             :content "no-cache"}]
     [:meta {:http-equiv "expires"
             :content "0"}]
     [:meta {:http-equiv "pragma"
             :content "no-cache"}]
     [:title "Movement Session"]
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
      (splash)
      [:div.content-wrapper
       (sell)
       (epilog)
       (footer)]]]))

