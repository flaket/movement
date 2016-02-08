(ns movement.pages.landing
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [movement.pages.signup :refer [signup-form]]
            [movement.pages.components :refer [html-head top-menu footer-after-content footer-always-bottom]]
            [movement.activation :refer [generate-activation-id send-activation-email]]))

(defn landing-header []
  [:div.header
   [:div.home-menu.pure-menu.pure-menu-horizontal
    [:a.pure-menu-heading {:title  "Home"
                           :href   "/"
                           :target "_self"} "Movement Session"]
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
      [:a.pure-menu-link {:title  "Pricing"
                          :href   "/pricing"
                          :target "_self"} "Pricing"]]
     [:li.pure-menu-item
      [:a.pure-menu-link {:title  "Sign in"
                          :href   "/app"
                          :target "_self"} "Sign In"]]]]])

(defn splash []
  [:div.pure-g.splash-container
   [:div.pure-u-1
    [:div.pure-g
     [:h1.pure-u-1.splash-head.center
      "Plan less, move more"]]
    [:div.pure-g.splash
     [:p.pure-u-1.splash-subhead
      "Be inspired by generated workouts for functional and natural movement training."]]]
   #_[:div.pure-u-1.splash

    [:p.splash-subhead
     "Spend less time searching for and creating static training programs
      and spend more time moving your body with functional and natural movement patterns."]
    #_[:div.animated.fadeInDown
       [:a.pure-button.pure-button-primary {:title  "Sign Up"
                                            :href   "/pricing"
                                            :target ""} "Sign Up"]]]])

(defn sell []
  [:div#sell
   [:div.pure-g [:p.pure-u-1 "
    movement training\nfunctional movement\nnatural movement\nprimal movement\nprimal training\nmovement practice\nmovement training app\ntraining plan\nworkout plan\ngenerate\nlog\ninteractive workouts\n
   "]]
   [:div.pure-g [:p.pure-u-1 "
   Maybe you spend a lot of time pondering what to do each time you go to the gym or work out in your back yard.
   "]]
   [:div.pure-g [:p.pure-u-1 [:b "
   You know a lot of movements, but have trouble recalling them all and often end up doing the same things as yesterday.
   "]]]

   [:div.pure-g [:p.pure-u-1 [:b "
   You want a way to easily scale a workout. You want a way to swap an exercise with another one that's upwards or downwards in the progression.
   "]]]
   [:div.pure-g [:p.pure-u-1 "
   If that's the case, you're going to love Movement Session.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   With Movement Session you will create unique workouts, either fully planned or generated.
   You can stop roaming the internet, searching for workout plans, and instead generate thousands of unique, interactive and beautifully presented sessions.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   Movement Session is about learning new skills and training hard and training with great variety. Why? Because it's more fun
   and because we want to build strong, healthy bodies that can play hard, will not let us down if we need to perform
    and that will last us a good, long life."]]
   [:div.pure-g [:p.pure-u-1
                 [:b.highlight "Moving your body should be a playful and fun experience."]
                 " Searching for training programs is not fun. Going to the gym and doing fitness is not fun.
   Playing and learning new skills is fun!"]]

   [:div.pure-g [:p.pure-u-1 "
  workout generation benefits\n- thousands of unique workouts\n- variation
   "]]
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
  [:div#epilog
   [:h2.content-head.center "So, are you ready to start moving more?"]
   [:div.pure-g
    [:div.pure-u-1.center
     [:a.m-button.orange.x-large.upper
      {:title "Sign Up" :href "/pricing" :target ""} "Try Movement Session"]
     [:a.l-m {:title "Learn more" :href "/tour" :target ""} "Or learn more"]]]])

(defn landing-page []
  (html5
    (html-head "Movement Session :: The workout generator for functional and natural movement training")
    [:body
     (landing-header)
     (splash)
     [:div.content
      (sell)
      (epilog)]
     (footer-after-content)

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]

     ]))

