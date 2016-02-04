(ns movement.pages.tour
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [movement.pages.components :refer [header footer footer-2]]
            [movement.auth :refer [google-analytics-string]]))

(defn tour-page []
  (html5
    [:head
     [:link {:rel "shortcut icon" :href "images/static-air-baby.png"}]
     [:title "Tour Movement Session"]
     [:script google-analytics-string]
     (include-css
       "https://fonts.googleapis.com/css?family=Roboto"
       "https://fonts.googleapis.com/css?family=Raleway"
       "https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css"
       "/css/pure-min.css"
       "/css/grids-responsive-min.css"
       "/css/normalize.css"
       "/css/marketing.css"
       "/css/site.css"
       "/css/pricing.css")]
    [:body
     (header)
     [:div.content

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Generate sessions"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "The next workout is a click of a button away. Continue a plan you're enrolled in or create a new inspiring session."]
         [:p.pure-u-1 "You can get a session picked randomly, or you can select something you or others have created."]
         [:p.pure-u-1 "If a generated session is not to your liking you can quickly replace it with another."]]]
       [:div.pure-u.pure-u-lg-1-2
        [:p [:img {:src "tour-session.png" :height "806" :width "699"}]]]
       ]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Interactivity"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p [:img {:src "add.png" :height "267" :width "535"}]]]
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "You can always expand any part of a session by adding more movements of the same kind."]]
        [:div.pure-g
         [:p.pure-u-1 "You can also add any specific movement by searching for it."]]]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "Maybe you don't have access to some equipment a movement needs, like a bar or some weights.
         The green crossing arrows allows you to swap any movement with another one."]]
        [:div.pure-g
         [:p.pure-u-1 "If a movement shows the + and - icons, it means that they have links to easier and/or harder movement variations.
         Swapping the movement with a variation is a great way to adjust the difficulty of the session."]]
        [:div.pure-g
         [:p.pure-u-1 "Repetitions, sets, distance and duration can be set. This way you can log your efforts in detail."]]]
       [:div.pure-u.pure-u-lg-1-2
        [:p [:img {:src "movement.png" :height "385" :width "242"}]]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Log & Share"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p "
        Add how long your session lasted and write a comment. Your session will be stored with all the data you provide and
        the session can be reviewed at a later time. Each session gets a unique url address so you can share your workout with your friends.
        "]]
       [:div.pure-u.pure-u-lg-1-2 [:img {:src "time-comment.png" :height "220" :width "544"}]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Create your own workouts and plans"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p [:img {:src "create-plan.png" :height "436" :width "511"}]]]
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
        [:p [:img {:src "explore-movements.png" :height "522" :width "556"}]]]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p [:img {:src "explore-templates.png" :height "462" :width "595"}]]]
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "Templates, groups and plans can be found by searching for words or by categories used."]]
        [:div.pure-g
         [:p.pure-u-1 "You can always find content you have created, content movementsession has created and all the content
         that is currently linked to your account."]]]]
      [:div.pure-g
       [:p.pure-u-1.is-center
        [:a.pure-button-primary {:title  "See Pricing" :href   "/pricing" :target ""} "See Pricing"]]]]
     (footer-2)]))
