(ns movement.pages.tour
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [movement.pages.components :refer [html-head top-menu footer-always-bottom footer-after-content]]))

(defn tour-page []
  (html5
    (html-head "Tour | Movement Session")
    [:body
     (top-menu)
     [:div.content
      [:div.pure-g
       [:h2.content-head.center.pure-u-1 "Create workout sessions"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "
         The next workout is a click of a button away. Continue a plan you're enrolled in or create a new inspiring session.
         You can get a session picked randomly, or you can select something you or others have created.
         If a generated session is not to your liking you can quickly replace it with another."]]]
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1.center
          [:img.pure-img-responsive {:src "images/marketing/tour-session.png"}]]]]]

      [:div.pure-g
       [:div.pure-u-1
        [:div.pure-g
         [:h2.content-head.is-center.pure-u-1 "Go from static to dynamic workouts"]]
        [:div.pure-g
         [:p.pure-u-1 "Stop searching for static and fully planned workouts.
         Use Movement Session to create beautifully presented, dynamic workouts and embrace variation, learning and new movement challenges."]]
        [:div.pure-g
         [:p.pure-u-1.center
          [:img.pure-img-responsive {:src "images/marketing/old-style-to-new.png"}]]]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Interactivity"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "You can always expand any part of a session by adding more movements of the same kind.
         You can also add any specific movement by searching for it."]]
        [:div.pure-g
         [:p.pure-u-1 "Maybe you don't have access to some equipment a movement needs, like a bar or some weights.
         The green crossing arrows allows you to swap any movement with another one."]]
        [:div.pure-g
         [:p.pure-u-1 "If a movement shows the arrow icons they can be replaced by easier and/or harder variations.
         Swapping the movement with a variation is a great way to adjust the difficulty of the session or to learn a new movement."]]
        [:div.pure-g
         [:p.pure-u-1 "Repetitions, sets, rest period, distance, duration and weights can all be set. This way you can log your efforts in detail."]]]
       [:div.pure-u.pure-u-lg-1-2
        [:p.pure-u-1.center
         [:img.pure-img-responsive {:src "images/marketing/session-circuit.png"}]]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Log & Share"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p "
        Add how long your session lasted and write a comment. Your session will be stored with all the data you provide and
        the session can be reviewed at a later time. Each session gets a unique url address so you can share your workout with your friends.
        "]]
       [:div.pure-u.pure-u-lg-1-2.center
        [:img.pure-img-responsive {:src "images/marketing/time-comment.png"}]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Create your own workouts and plans"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p.center
         [:img.pure-img-responsive {:src "images/marketing/create-plan.png"}]]]
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "
         You can create your own sessions,
         you can collect sessions in groups
         and you can create plans that span over days or weeks."]]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Explore and reuse what other users create"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "Exploring the movement database is a sure-fire way to understand how Movement Session works,
         to learn new movements and to be inspired to create your own content."]]
        [:div.pure-g
         [:p.pure-u-1 "
         You can browse through the different categories.
         By selecting the category 'Handstand' we can see all the movements in the database that are related to this category.
         You can also search for and view a specific movement.
         "]]]
       [:div.pure-u.pure-u-lg-1-2
        [:p.center
         [:img.pure-img-responsive {:src "images/marketing/explore-movements-2.png"}]]]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p.center
         [:img.pure-img-responsive {:src "images/marketing/explore-templates.png"}]]]
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1.l-m "Templates, groups and plans can be found by searching for words or by categories used."]]
        [:div.pure-g
         [:p.pure-u-1.l-m "You can always find content you have created, content movementsession has created and all the content
         that is currently linked to your account."]]]]
      [:div.pure-g
       [:p.pure-u-1.center
        [:a.m-button.orange.x-large.upper {:title  "Sign Up" :href   "/signup" :target ""} "Try Movement Session"]]]]
     (footer-after-content)

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]

     ]))
