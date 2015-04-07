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

(def default-template [])
(def default-buttons {:ritual "btn btn-default"
                      :strength "btn btn-default"
                      :mobility "btn btn-default"
                      :locomotion "btn btn-default"
                      :bas "btn btn-default"
                      :sass "btn btn-default"
                      :leg "btn btn-default"
                      :movnat "btn btn-default"})

(def template (atom default-template))
(def buttons (atom default-buttons))

(defn generate [name category n]
  (swap! template conj {:name name
                        :category category
                        :movements n}))

(defn button-selected! [button]
  (swap! buttons assoc button "btn btn-info"))

(defn list-movements [category]
  [:div.row
   [:h3 {:style {:color "gray"}} (:name category)]
   ;[:ol {:type "A"}]
   (for [i (take (:movements category) (shuffle (:category category)))]
     [:li (name i)])])

(defn home-page []
  [:div
   [:h1 "Movement session"]
   [:p "Select a template and be inspired by randomly generated movements."]
   [:button {:type     "button"
             :class    (:ritual @buttons)
             :on-click #(do
                         (reset! template [])
                         (reset! buttons default-buttons)
                         (button-selected! :ritual)
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 2)
                         (generate "Hanging" hanging 1)
                         (generate "Equilibre" equilibre 1)
                         (generate "Strength" strength 1))}
    "Morning ritual"]
   [:button {:type "button"
             :class (:strength @buttons)
             :on-click #(do
                         (reset! template [])
                         (reset! buttons default-buttons)
                         (button-selected! :strength)
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 2)
                         (generate "Strength" strength 4))}
    "Strength"]
   [:button {:type "button"
             :class (:mobility @buttons)
             :on-click #(do
                         (reset! template [])
                         (reset! buttons default-buttons)
                         (button-selected! :mobility)
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 4)
                         (generate "Prehab" mobility 4))}
    "Mobility/Prehab"]
   [:button {:type "button"
             :class (:locomotion @buttons)
             :on-click #(do
                         (reset! template [])
                         (reset! buttons default-buttons)
                         (button-selected! :locomotion)
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 2)
                         (generate "Locomotion" locomotion 6))}
    "Locomotion"]
   [:button {:type "button"
             :class (:bas @buttons)
             :on-click #(do
                         (reset! template [])
                         (reset! buttons default-buttons)
                         (button-selected! :bas)
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 1)
                         (generate "BAS" bas 5))}
    "BAS"]
   [:button {:type "button"
             :class (:sass @buttons)
             :on-click #(do
                         (reset! template [])
                         (reset! buttons default-buttons)
                         (button-selected! :sass)
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 1)
                         (generate "SASS" sass 4))}
    "SASS"]
   [:button {:type "button"
             :class (:leg @buttons)
             :on-click #(do
                         (reset! template [])
                         (reset! buttons default-buttons)
                         (button-selected! :leg)
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 1)
                         (generate "Leg Strength" leg-strength 3)
                         (generate "Auxiliary" auxiliary 2))}
    "Leg/Auxiliary strength"]
   [:button {:type "button"
             :class (:movnat @buttons)
             :on-click #(do
                         (reset! template [])
                         (reset! buttons default-buttons)
                         (button-selected! :movnat)
                         (generate "Warmup Mobility (3 rounds)" movnat-warmup 3)
                         (generate "Skill" movnat 1)
                         (generate "Combo (4 rounds)" movnat 4))}
    "MovNat"]
   [:div.container
    [:ol {:type "A"}
     (for [category @template]
       (list-movements category))]]])

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
