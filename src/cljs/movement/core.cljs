(ns movement.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljsjs.react :as react]
              [movement.movements :refer [warmup mobility hanging equilibre strength
                                          locomotion bas sass leg-strength auxiliary
                                          movnat movnat-warmup]])
    (:import goog.History))

(def template (atom []))

(defn generate [name category n]
  (swap! template conj {:name name
                        :category category
                        :movements n}))

(defn list-movements [category]
  [:div.row
   [:h3 (:name category)]
   [:ul
    (for [i (take (:movements category) (shuffle (:category category)))]
      [:li (name i)])]])

(defn movement-session []
  )

(defn temp []
  (reset! template [])
  swap!)

(defn home-page []
  [:div
   [:h1 "Movement session"]
   [:button {:type "input"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 2)
                         (generate "Hanging" hanging 1)
                         (generate "Equilibre" equilibre 1)
                         (generate "Strength" strength 1))}
    "Morning ritual"]
   [:button {:type "input"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 2)
                         (generate "Strength" strength 4))}
    "Strength"]
   [:button {:type "input"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 4)
                         (generate "Prehab" mobility 4))}
    "Mobility/Prehab"]
   [:button {:type "input"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 2)
                         (generate "Locomotion" locomotion 6))}
    "Locomotion"]
   [:button {:type "input"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 1)
                         (generate "BAS" bas 5))}
    "BAS"]
   [:button {:type "input"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 1)
                         (generate "SASS" sass 4))}
    "SASS"]
   [:button {:type "input"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 1)
                         (generate "Leg Strength" leg-strength 3)
                         (generate "Auxiliary" auxiliary 2))}
    "Leg/Auxiliary strength"]
   [:button {:type "input"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup Mobility (3 rounds)" movnat-warmup 3)
                         (generate "Skill" movnat 1)
                         (generate "Combo (4 rounds)" movnat 4))}
    "MovNat"]
   [:div.container
    (for [category @template]
    (list-movements category))]
   [:p "andreas.flakstad@gmail.com"]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Client side routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
