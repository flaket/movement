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
       [:p.pure-u-1 "
      Picture of how easy it is to generate a new session + show the session.
      "]]
      [:div.pure-g
       [:div.pure-u-1
        [:img {:src "session-1.png" :height "369" :width "945"}]]]
      [:div.pure-g
       [:div.pure-u-1
        [:img {:src "session-2.png" :height "500" :width "600"}]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Interactivity"]]
      [:div.pure-g
       [:p.pure-u-1 "Refresh, easier, harder, remove, add, search-and-add, add time, add comments, log session."]]
      [:div.pure-g
       [:div.pure-u-1
        [:img {:src "add.png" :height "475" :width "950"}]]]
      [:div.pure-g
       [:div.pure-u-1
        [:img {:src "movement.png" :height "478" :width "318"}]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Log & Share"]]
      [:div.pure-g
       [:p.pure-u-1 "set reps/sets/.., add time, add comments, log session."]]
      [:div.pure-g
       [:div.pure-u-1
        [:img {:src "time-comment.png" :height "300" :width "700"}]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Create templates, groups and plans"]]
      [:div.pure-g
       [:p.pure-u-1 "Creating"]]
      [:div.pure-g
       [:div.pure-u-1
        [:img {:src "create-plan.png" :height "500" :width "600"}]]]

      [:div.pure-g
       [:h2.content-head.is-center.pure-u-1 "Explore and reuse what other users create"]]
      [:div.pure-g
       [:p.pure-u-1 "asd"]]
      [:div.pure-g
       [:div.pure-u-1
        [:img {:src "explore-movements.png" :height "500" :width "650"}]]]
      [:div.pure-g
       [:div.pure-u-1
        [:img {:src "explore-templates.png" :height "450" :width "600"}]]]


      [:div.pure-g
       [:p.pure-u-1.is-center
        [:a.button.button-secondary {:title  "Pricing" :href   "/pricing" :target ""} "Pricing"]]]]
     (footer-2)]))
