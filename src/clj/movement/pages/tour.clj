(ns movement.pages.tour
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [movement.pages.components :refer [header footer footer-2]]))

(defn tour-page []
  (html5
    [:head
     [:link {:rel "shortcut icon" :href "images/static-air-baby.png"}]
     [:title "Tour Movement Session"]
     (include-js "analytics.js")
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
         [:p.pure-u-1 "Creating your next workout is as easy as clicking a button."]
         [:p.pure-u-1 "You can get a session picked randomly from one of your templates,
         you can select the template, you can get a session from one of your groups or
          you can continue a plan you have started."]
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
         [:p.pure-u-1 "The blank movement card allows you to add more movements of the same kind to any part of the session."]]
        [:div.pure-g
         [:p.pure-u-1 "You can also add any specific movement by searching for it."]]]
       ]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "If an exercise is not to your liking you can hit the green crossing arrows and swap it with another one,
         drawn randomly from the categories the template specifies."]]
        [:div.pure-g
         [:p.pure-u-1 "The + and - signs are links to easier and/or harder movement variations.
         Swapping the movement with a variation is a great way to adjust the difficulty of the session."]]
        [:div.pure-g
         [:p.pure-u-1 "Below the movement image, repetitions, sets, distance and duration can be set.
         This way you can log your efforts in detail."]]]
       [:div.pure-u.pure-u-lg-1-2
        [:p [:img {:src "movement.png" :height "385" :width "242"}]]]
       ]



      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Log & Share"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p "
        Add how long your session lasted and write a comment. Your session will be logged with all the data you provide and
        the session can be reviewed at a later time. Each session gets a unique url address so you can share your workout with your friends.
        "]]
       [:div.pure-u.pure-u-lg-1-2 [:img {:src "time-comment.png" :height "220" :width "544"}]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Create templates, groups and plans"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p [:img {:src "create-plan.png" :height "436" :width "511"}]]]
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "You can create your own templates."]]
        [:div.pure-g
         [:p.pure-u-1 "Templates can be gathered in groups. You might for example create
         a group with templates that only generate different running sessions. If you want to practice your running skills you can now select
         your running group and a session will be generated from one of the templates in the group."]]
        [:div.pure-g
         [:p.pure-u-1 "Finally, you can create plans. A plan consists of a number of days, where each day you want to train
         consists of one or several sessions."]]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Explore and reuse what other users create"]]
      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "Exploring the movement database is a sure-fire way to understand how Movement Session works,
         to learn new movements and to be inspired to create your own templates."]]
        [:div.pure-g
         [:p.pure-u-1 "You can search for and view a specific movement with the autocomplete-enabled search box.
         Or you can browse through the different categories. By selecting the category 'Handstand' we can see all the movements
         in the database that are related to this category."]]]
       [:div.pure-u.pure-u-lg-1-2
        [:p [:img {:src "explore-movements.png" :height "522" :width "556"}]]]]

      [:div.pure-g
       [:div.pure-u.pure-u-lg-1-2
        [:p [:img {:src "explore-templates.png" :height "462" :width "595"}]]]
       [:div.pure-u.pure-u-lg-1-2
        [:div.pure-g
         [:p.pure-u-1 "Templates, groups and plans can be found by searching for words or by categories used. You might for example
         search for templates related to climbing by searching for 'climbing' in the title and description, and uses the category 'Climbing'"]]
        [:div.pure-g
         [:p.pure-u-1 "You can also see content you have created, content movementsession has created and the content
         that is currently linked to your account."]]]]
      [:div.pure-g
       [:p.pure-u-1.is-center
        [:a.pure-button-primary {:title  "See Pricing" :href   "/pricing" :target ""} "See Pricing"]]]]
     (footer-2)]))
