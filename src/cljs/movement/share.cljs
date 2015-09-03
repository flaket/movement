(ns movement.share
  (:require
    [reagent.session :as session]
    [reagent.core :refer [atom]]
    [clojure.string :as str]
    [movement.util :refer [GET]]
    [movement.text :refer [text-edit-component]]
    [movement.menu :refer [menu-component]]
    [movement.state :refer [movement-session handler-fn log-session]]))

(defn equipment-symbol [equipment-name]
  (first (shuffle ["images/squat.png"
                   "images/push-up.png"
                   "images/high-bridge.png"
                   "images/frog-stand.png"
                   "images/broad-jump.png"
                   "images/elastic-band-overhead-pull-down.png"
                   "images/pull-up-reach.png"])))

(defn movement-component []
  (let []
    (fn [{:keys [id category graphic animation equipment rep set distance duration] :as m} part-title]
      (let [name (:movement/name m)
            rep 10
            set 3
            graphic (equipment-symbol "")]
        [:div.pure-u.movement

         [:div.pure-g]

         [:div.pure-g.title
          [:span.pure-u name]]

         [:div.pure-g
          [:img.pure-u.graphic.pure-img-responsive {:src graphic}]]

         [:div.pure-g

          [:div.pure-u-1-4.sw
           [:div.pure-g
            [:span.pure-u.rep-text "Rep"]]
           [:div.pure-g
            [:span.pure-u.rep rep]]]

          [:div.pure-u-1-2
           [:div.pure-g
            [:img.pure-u.icon {:src (equipment-symbol equipment)}]
            ]]

          [:div.pure-u-1-4.se
           [:div.pure-g
            [:span.pure-u.set-text "Set"]]
           [:div.pure-g
            [:span.pure-u.set set]]]]

         ]))))

(defn part-component []
  (let []
    (fn [{:keys [title categories movements]}]
      [:div
       [:h2 title]
       [:div.pure-g
        (for [m movements]
          ^{:key (str m (rand-int 100000))} [movement-component m title])]])))

(defn session-component []
  (let []
    (fn [{:keys [title description parts date]}]
      [:div#session
       [:div.pure-g
        [:label.pure-u.pure-u-md-1-5]
        [:h1.pure-u.pure-u-md-3-5 title]
        [:label.pure-u.pure-u-md-1-5 date]]
       [:div description]
       (for [p parts]
         ^{:key p} [part-component p])])))

(defn share-component []
  (let [s (session/get :movement-session)]
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div#main

        [:div.content
         [session-component s]]]])))