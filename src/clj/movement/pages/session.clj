(ns movement.pages.session
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [clojure.string :as str]
            [movement.pages.components :refer [header footer]]
            [movement.pages.signup :refer [signup-form]]))

(defn image-url [name]
  (str "../images/" (str/replace (str/lower-case name) " " "-") ".png"))

(defn time-component [time]
  (let [minutes (int (/ time 60))
        seconds (mod time 60)]
    [:div (str minutes ":" seconds)]))

(defn comment-component [comment]
  [:div comment])

(defn header-menu []
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
       (if (and rep (< 0 rep))
         [:div.pure-u-5-12 "Reps"]
         [:div.pure-u-5-12.no-data "Reps"])
       (if (and set (< 0 set))
         [:div.pure-u-5-12 "Set"]
         [:div.pure-u-5-12.no-data "Set"])
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
       (if (and distance (< 0 distance))
         [:div.pure-u-5-12 "Meters"]
         [:div.pure-u-5-12.no-data "Meters"])
       (if (and duration (< 0 duration))
         [:div.pure-u-5-12 "Seconds"]
         [:div.pure-u-5-12.no-data "Seconds"])
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
  [:div#epilog.content
   [:h2.content-head.is-center "Create and discover more sessions like this"]
   [:div.pure-g
    [:div.pure-u.pure-u-md-2-5]
    [:a.pure-u-1.pure-u-md-1-5.pure-button.pure-button-primary
     {:title  "Movement Session Learn More"
      :href   "/"
      :target ""} "Learn more"]
    [:div.pure-u.pure-u-md-2-5]]])

(defn view-session-page [session]
  (html5
    [:head
     [:title ""]
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
      (header)
      [:div.content.is-center
       (header-component session)
       (doall
         (for [p (:parts session)]
           ^{:key p} (part-component p)))
       (when-let [time (:time session)]
         (when (not= 0 time) (time-component time)))
       (comment-component (:comment session))
       (epilog)]
      #_(footer)]]))

(defn view-sub-activated-page [req]
  (html5
    [:head
     [:title ""]]
    [:body
     [:div (str req)]]))