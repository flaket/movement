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
      "Workouts for functional and practical movement training"]]
    [:div.pure-g.splash
     [:p.pure-u-1.splash-subhead
      "Spend less time searching for training programs and more time creating a strong and healthy body that is capable of performing when you need it to."]]]])

(defn sell []
  [:div#sell
   [:h2.content-head.center "Interactive workouts"]
   [:div.pure-g
    [:div.pure-u-1.pure-u-lg-1-2
     [:img {:src "images/marketing/session-circuit.png" :height "475" :width "500"}]]
    [:div.pure-u-1.pure-u-md-1-2
     [:div.pure-g
      [:p.pure-u-1 "
   Movement Session is a movement training app for creating interactive workouts and for logging them.
   It is a tool in your movement practice to create your workouts, be inspired by discovering new movement patterns
   and to log your efforts and track your progress towards your goals.
   "]]
     [:div.pure-g
      [:p.pure-u-1 "
   Every workout that you create is interactive. This means that you can easily add and remove exercises, even to workouts that others have designed. Exercises that are part of progressions can be replaced by easier or harder variations.
   "]]]]
   [:h2.content-head.center "A new way to create workouts"]
   [:div.pure-g [:p.pure-u-1 "
   A problem I have had with training is ending up doing the same things over and over.
   I want variation in my training. Moving should be a playful and fun experience. Searching for training programs is not fun.
   Going to the gym and doing fitness is not fun. Playing around and learning new skills is fun!
   So after spending some time researching I know a lot of movements, but when it comes time to move I have trouble recalling them all and often end up doing the same things as yesterday.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   If you have had similar concerns you're going to love Movement Session.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   Movement Session offers a different way to write down workouts. You may still write down fully planned workout sessions and specify every exercise, every rep and every set.
   Or you can simply say what " [:b "kind"] " of exercise should appear and let the tool create the workout for you.
   This powerful feature allows you to stop roaming the internet, searching for workout plans, and instead generate thousands of unique, interactive and beautifully presented workouts."]]
   [:div.pure-g
    [:div.pure-u-1.center
     [:img {:src "images/marketing/create-template.png" :height "700" :width "700"}]]]
   [:div.pure-g [:p.pure-u-1 "
   In the above example I create a workout template. This template will define what a workout will look like. It starts off with some handstand training. I specify that it will always start with wrist stretches.
   Then Movement Session should pick three movements for me from the category \"handstand\". This technique allows me to either fully or partially have the tool create the variation that I seek in my training.
   "]]
   [:div.pure-g [:p.pure-u-1 "
   Practical, natural or primal movement patterns such as crawling, lifting, running, climbing and so on, have a higher priority within Movement Session.
   You can specify that select workouts will only use these patterns. Besides the practical movement patterns we also value functional movement and mobility training.
   Finally, you can of course create workouts such as Crossfit AMRAP's or calisthenic push up challenges if you so please.
   The database is filled with movement patterns from several disciplines, but has a focus on movements that doesn't utilize equipment.
   "]]
   [:div.pure-g
    [:div.pure-u-1.pure-u-lg-1-2
     [:img {:src "images/marketing/stars.png" :height "500" :width "500"}]]
    [:div.pure-u-1.pure-u-md-1-2
     [:div.pure-g
      [:p.pure-u-1 "
   To adapt to your skill level and to help you progress Movement Session uses self reporting to fine-tune how workouts are created.
   After you have completed a workout and saved it, all the movements you did will be marked with one star, indicating that you are learning this movement pattern.
   "]]
     [:div.pure-g
      [:p.pure-u-1 "
   When you have learned a movement and can do it effectively you should mark it with two stars. Movement Session will now create workouts
   with more advanced movement patterns. When you have mastered a movement and can do it both effectively and efficiently you should mark it with three stars.
   "]]]]

   [:h2.content-head.center "Summary"]
   [:div.pure-g
    [:div.pure-u.pure-u-md-1-3
     [:div.pure-g [:p.pure-u-1 "Reach your movement goals by creating unique sessions and workout plans or explore what others create."]]]
    [:div.pure-u.pure-u-md-1-3
     [:div.pure-g.l-m [:p.pure-u-1 "Log your workouts with rich data, view past sessions and share sessions with your friends."]]]
    [:div.pure-u.pure-u-md-1-3
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

