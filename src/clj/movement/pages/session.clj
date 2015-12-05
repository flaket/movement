(ns movement.pages.session
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css include-js html5]]
            [clojure.string :as str]))

(defn image-url [name]
  (str "../images/" (str/replace (str/lower-case name) " " "-") ".png"))

(defn comment-component [comment]
  [:div comment])

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

     [:div {:style {:cursor 'pointer}}
      [:div.pure-g
       [:div.pure-u-1-12]
       [:div.pure-u-5-12 (when-not (and rep (< 0 rep)) {:style {:opacity "0.2"}})
        [:div.pure-u "Reps"]]
       [:div.pure-u-5-12 (when-not (and set (< 0 set)) {:style {:opacity "0.2"}})
        [:div.pure-u "Set"]]
       [:div.pure-u-1-12]]
      [:div.pure-g
       [:div.pure-u-1-12]
       [:div.pure-u-5-12 (if (and rep (< 0 rep))
                           [:div.pure-u {:style {:color     "#9999cc"
                                                 :font-size 24}} rep]
                           [:div.pure-u])]
       [:div.pure-u-5-12 (if (and set (< 0 set))
                           [:div.pure-u {:style {:color     "#9999cc"
                                                 :font-size 24}} set]
                           [:div.pure-u])]
       [:div.pure-u-1-12]]]

     [:div {:style {:cursor 'pointer}}
      [:div.pure-g
       [:div.pure-u-1-12]
       [:div.pure-u-5-12 (when-not (and distance (< 0 distance)) {:style {:opacity "0.2"}})
        [:div.pure-u "Meters"]]
       [:div.pure-u-5-12 (when-not (and duration (< 0 duration)) {:style {:opacity "0.2"}})
        [:div.pure-u "Seconds"]]
       [:div.pure-u-1-12]]
      [:div.pure-g
       [:div.pure-u-1-12]
       [:div.pure-u-5-12
        (if (and distance (< 0 distance))
          [:div.pure-u {:style {:color     "#9999cc"
                                :font-size 24}} distance]
          [:div.pure-u])]
       [:div.pure-u-5-12
        (if (and duration (< 0 duration))
          [:div.pure-u {:style {:color     "#9999cc"
                                :font-size 24}} duration]
          [:div.pure-u])]
       [:div.pure-u-1-12]]]]))

(defn part-component [{:keys [title movements] :as part}]
  [:div.part
   [:h2 title]
   [:div.pure-g]
   [:div.pure-g
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
     #_[:div.pure-g
      [:div.pure-u (str session)]]
     [:div#layout
      [:div.content
       [:div
        (header-component session)
        [:div (:time session)]
        (doall
          (for [p (:parts session)]
            ^{:key p} (part-component p)))
        (comment-component (:comment session))]]]]))

;id category distance rep set duration