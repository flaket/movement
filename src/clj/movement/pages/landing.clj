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
                          :target "_blank"} "Sign In"]]]]])

(defn splash []
  [:div.pure-g.splash-container
   [:div.pure-u-1
    [:div.pure-g.splash-head
     [:h1.pure-u-1.center
      "Workouts for functional and practical movement training"
      #_"Plan less, move more"]]
    [:div.pure-g.splash
     [:p.pure-u-1.splash-subhead
      "Movement Session was created so that you can spend less time searching for training programs and more time creating a strong and healthy body that is capable of performing practical movements when you need it to."]]]])

(defn sell []
  [:div#sell
   [:div.pure-g
    [:div.pure-u-1.pure-u-lg-1-2
     [:img {:src "images/marketing/session-circuit.png" :height "475" :width "500"}]]
    [:div.pure-u-1.pure-u-md-1-2
     [:div.pure-g
      [:p.pure-u-1 "
   Movement Session is a tool for creating workouts and for logging them. You can use it to create your workouts be inspired to move your body in new ways
   and to log your efforts and review your progress towards your goals.
   "]]
     [:div.pure-g
      [:p.pure-u-1 "
   Every workout is interactive. That means that you can easily add and remove exercises. Exercises that are part of progressions can also be replaced by easier or harder variations.
   "]]]]
   [:div.pure-g [:p.pure-u-1 "
   A problem I have had with training is ending up doing the same things over and over.
   I want variation in my training. Moving should be a playful and fun experience. Searching for training programs is not fun.
   Going to the gym and doing fitness is not fun. Playing and learning new skills is fun!
   So after spending some time researching I know a lot of movements, but when it comes time to move I have trouble recalling them all and often end up doing the same things as yesterday.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   So I created a different way to write down workouts. With Movement Session you can create fully planned workout sessions and specify every exercise, every rep and every set.
   Or you can say what kind of exercise should appear and let the tool create the workout for you.
   "]]
   [:div.pure-g
    [:div.pure-u-1.center
     [:img {:src "images/marketing/create-template.png" :height "700" :width "700"}]]]

   [:div.pure-g [:p.pure-u-1 "
    movement training\nfunctional movement\nnatural movement\nprimal movement\nprimal training\nmovement practice\nmovement training app\ntraining plan\nworkout plan\ngenerate\nlog\ninteractive workouts\n
   "]]
   [:div.pure-g [:p.pure-u-1 "
   Have you ever spent
   "]]

   [:div.pure-g [:p.pure-u-1 [:b "
   And even if you find something that looks good, you want a way to easily scale the workout.
   There should be a way to find a replacement movement that is either easier or harder, without having a personal trainer to guide you.
   "]]]
   [:div.pure-g [:p.pure-u-1 "
   Moving your body should be a playful and fun experience. Searching for training programs is not fun. Going to the gym and doing fitness is not fun.
   Playing and learning new skills is fun!"]]
   [:div.pure-g [:p.pure-u-1 "
   If this sounds like a good idea, you're going to love Movement Session.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   With Movement Session you will create unique workouts, either fully planned or generated.
   You can stop roaming the internet, searching for workout plans, and instead generate thousands of unique, interactive and beautifully presented sessions.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   Movement Session is about learning new skills and training hard and training with great variety. Why? Because it's more fun
   and because we want to build strong, healthy bodies that can play hard, will not let us down if we need to perform
    and that will last us a good, long life."]]

   [:div.pure-g
    [:div.pure-u.pure-u-md-1-3
     [:div.pure-g [:p.pure-u-1 [:b "Create workouts"]]]
     [:div.pure-g [:p.pure-u-1 "Reach your movement goals by creating unique sessions and workout plans or explore what others create."]]]
    [:div.pure-u.pure-u-md-1-3
     [:div.pure-g.l-m [:p.pure-u-1 [:b "Log your progress"]]]
     [:div.pure-g.l-m [:p.pure-u-1 "Log your workouts with rich data, view past sessions and share sessions with your friends."]]]
    [:div.pure-u.pure-u-md-1-3
     [:div.pure-g.l-m [:p.pure-u-1 [:b "Set your skill level"]]]
     [:div.pure-g.l-m [:p.pure-u-1 "Set your skill level for each movement. Movement Session will create workouts with more advanced
       movements as you log your progress. You can also adjust the difficulty of a workout by swapping movements with easier or harder variations."]]]]])

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
    (html-head "Movement Session :: Log and generate workouts for your functional, natural and practical movement training")
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
              :src "//in.getclicky.com/100920866ns.gif"}]]]]))

