(ns movement.pages.session
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [clojure.string :as str]
            [movement.pages.landing :refer [header]]
            [movement.pages.signup :refer [signup-form]]))

(defn image-url [name]
  (str "../images/" (str/replace (str/lower-case name) " " "-") ".png"))

(defn comment-component [comment]
  [:div comment])

(defn header []
  (html
    [:div.header
     [:div.home-menu.pure-menu.pure-menu-horizontal.pure-menu-fixed
      [:a.pure-menu-heading {:title  "Home"
                             :href   "/"
                             :target ""} "Movement Session"]]]))

(defn movement-component [m]
  (let [id (:db/id m)
        name (:movement/name m)
        graphic (image-url name)
        rep (:movement/rep m)
        set (:movement/set m)
        distance (:movement/distance m)
        duration (:movement/duration m)]
    [:div.pure-u.movement {:id (str "m-" id)}
     [:h3.pure-g
      [:div.pure-u-1-12]
      [:div.pure-u.title name]]

     [:img.graphic.pure-img-responsive {:src graphic :title name :alt name}]

     [:div
      [:div.pure-g
       [:div.pure-u-1-12]
       [:div.pure-u-5-12 {:className (str (when-not (and rep (< 0 rep)) " no-data"))}
        [:div.pure-u "Reps"]]
       [:div.pure-u-5-12 {:className (str (when-not (and set (< 0 set)) " no-data"))}
        [:div.pure-u "Set"]]
       [:div.pure-u-1-12]]
      [:div.pure-g
       [:div.pure-u-1-12]
       [:div.pure-u-5-12
        (when (and rep (< 0 rep))
          [:div.rep-set rep])]
       [:div.pure-u-5-12
        (when (and set (< 0 set))
          [:div.rep-set set])]
       [:div.pure-u-1-12]]]

     [:div
      [:div.pure-g
       [:div.pure-u-1-12]
       [:div.pure-u-5-12 {:className (str (when-not (and distance (< 0 distance)) " no-data"))}
        [:div "Meters"]]
       [:div.pure-u-5-12 {:className (str (when-not (and duration (< 0 duration)) " no-data"))}
        [:div "Seconds"]]
       [:div.pure-u-1-12]]
      [:div.pure-g
       [:div.pure-u-1-12]
       [:div.pure-u-5-12
        (when (and distance (< 0 distance))
          [:div.rep-set distance])]
       [:div.pure-u-5-12
        (when (and duration (< 0 duration))
          [:div.rep-set duration])]
       [:div.pure-u-1-12]]]]))

(defn part-component [{:keys [title movements]}]
  [:div.part
   [:h2 title]
   [:div.pure-g.movements
    (doall
      (for [m movements]
        ^{:key (str m (rand-int 100000))}
        (movement-component m)))]])

(defn header-component [{:keys [title description]}]
  [:div
   [:div.pure-g
    [:div.pure-u.pure-u-md-1-5]
    [:h1.pure-u.pure-u-md-3-5 title]]
   [:div.pure-g
    [:p.pure-u.subtitle description]]])

(defn epilog []
  [:div.content
   [:h2.content-head.is-center "Sign up and be inspired by sessions like this"]
   [:div.pure-g
    [:div.l-box-lrg.pure-u-1.pure-u-md-2-5
     (signup-form)]
    [:div.l-box-lrg.pure-u-1.pure-u-md-3-5
     [:h4 "Contact Us"]
     [:p "support@movementsession.com"]]]])

(defn view-session-page [session]
  (html5
    [:head
     [:title ""]
     (include-css
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
     [:div#layout
      (header)
      [:div.content
       [:div.logged-session
        (header-component session)
        [:div (:time session)]
        (doall
          (for [p (:parts session)]
            ^{:key p} (part-component p)))
        (comment-component (:comment session))]]
      (epilog)]]))
