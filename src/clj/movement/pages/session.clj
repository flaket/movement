(ns movement.pages.session
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [clojure.string :as str]
            [movement.pages.components :refer [html-head top-menu footer-always-bottom]]
            [movement.pages.signup :refer [signup-form]]))

(defn image-url [name]
  (str "../images/movements/" (str/replace (str/lower-case name) " " "-") ".png"))

(defn time-component [time]
  (let [minutes (int (/ time 60))
        seconds (mod time 60)]
    [:p (str minutes ":" seconds)]))

(defn comment-component [comment]
  [:p comment])

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
        duration (:movement/duration m)
        weight (:movement/weight m)
        rest (:movement/rest m)]
    [:div.pure-u.movement.is-center {:id (str "m-" id)}
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
  [:div
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
    [:h1.pure-u-1.pure-u-md-3-5 title]
    [:div.pure-u.pure-u-md-1-5]]
   [:div.pure-g
    [:div.pure-u.pure-u-md-1-9]
    [:p.pure-u-1.pure-u-md-7-9.subtitle description]
    [:div.pure-u.pure-u-md-1-9]]])

(defn epilog []
  [:div#epilog
   [:h2.content-head.center "Create and discover more sessions like this"]
   [:div.pure-g
    [:div.pure-u-1.center
     [:a.m-button.orange.x-large.upper
      {:title "Try Movement Session" :href "/" :target "_self"} "Try Movement Session"]]]])

(defn view-session-page [session]
  (html5
    [:head
     [:link {:rel "shortcut icon" :href "images/movements/static-air-baby.png"}]
     [:title "View your session | Movement Session"]
     [:script {:src "analytics.js" :type "text/javascript"}]
     (include-css
       "https://fonts.googleapis.com/css?family=Roboto"
       "https://fonts.googleapis.com/css?family=Raleway"
       "https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css"
       "/css/pure-min.css"
       "/css/grids-responsive-min.css"
       "/css/normalize.css"
       "/css/marketing.css"
       "/css/site.css")]
    [:body
     [:div
      [:div.content
       (header-component session)
       (doall
         (for [p (:parts session)]
           ^{:key p} (part-component p)))
       (when-let [time (:time session)]
         (time-component time))
       (comment-component (:comment session))
       (epilog)]
      #_(footer-always-bottom)]

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]

     ]))

(defn view-sub-activated-page [req]
  (html5
    (html-head "")
    [:body
     [:div (str req)]

     [:script {:src "//static.getclicky.com/js" :type "text/javascript"}]
     [:script {:type "text/javascript" :src "clicky.js"}]
     [:noscript
      [:p
       [:img {:alt "Clicky" :width 1 :height 1
              :src "//in.getclicky.com/100920866ns.gif"}]]]

     ]))