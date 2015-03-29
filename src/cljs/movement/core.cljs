(ns movement.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljsjs.react :as react])
    (:import goog.History))

;; -------------------------
;; Movements in categories
; Warmup
(def warmup [:joint-mobility :jump-rope :running])

; Mobility
(def hip-mobility [:squat-routine :movnat-routine])
(def shoulder-mobility [:shoulder-rom-stabilisation :scapula-mobilisation])
(def wrist-mobility [:wrist-prep])
(def ankle-mobility [:ankle-prep])
(def spine-mobility [:bridge-rotation :locked-knees-deadlift])

; Hanging
(def hanging [:passive-hang :active-hang :side-to-side-swing
              :arching-active-hang :front-stationary-swing
              :one-arm-passive :one-arm-active :shawerma
              :swing-grip-routine :figure-8])
; Locomotion
(def locomotion [:swing-to-handstand :handstand-walk :bridge-walk
                 :duck-walk :horse-walk :lizard-crawl :ostrich-walk])
; Equilibre
(def equilibre [:gatherings :wall-walk :wall-kick :handstand-walk
                :handstand-push-up :air-baby :qdr])
; Leg strength
(def leg-strength [:basic-squat :back-squat :front-squat :overhead-squat
                   :basic-lunge :back-lunge :front-lunge :overhead-lunge
                   :deadlift :pistols :shrimp :behind-leg-squat
                   :jump-onto-box-standing :jump-onto-box-squatting
                   :explosive-flipping :natural-leg-curl])
; Auxiliary strength
(def auxiliary [:l-sit :v-up :sitting-leg-lift :swedish-leg-lift
               :hanging-leg-lift :gatherings :archups])
; Straight arm scapular strength
(def sass [:swedish-bar-hold-front :swedish-bar-hold-back
           :back-lever :front-lever :side-lever :planche :handstand])
; Bent arm strength
(def bas [:push-up-basic :push-up-russian :push-up-wide
          :push-up-diamond :push-up-hindu :push-up-lateral :push-up-bridge
          :push-up-archer :push-up-one-arm :push-up-one-leg-one-arm
          :dips-basic :dips-russian :dips-single-bar :dips-korean :dips-ring
          :dips-ring-wide :dips-ring-archer
          :handstand-push-up-head-wall :handstand-push-up-wall :handstand-push-up-free
          :planche-push-up :pull-up-basic :pull-up-rings :pull-up-rings-wide
          :pull-up-chest :pull-up-waist :pull-up-weighted :pull-up-scapula
          :one-arm-pull-up-forearm :one-arm-pull-up-bicep :one-arm-ring-negative
          :archer-pull-up :one-arm-pull-up-band :one-arm-pull-up-shoulder
          :one-arm-pull-up :row-basic :row-wide :front-lever-row
          :german-hang-pull :pull-over :front-lever-pull :back-lever-pull
          :tick-tock :back-lever-negative :front-lever-negative
          :muscle-up :false-grip-hang :false-grip-pull-up :muscle-up-negative
          :muscle-up-l-sit :rope-climb])

(def mobility (concat hip-mobility shoulder-mobility
                      wrist-mobility ankle-mobility spine-mobility))
(def strength (concat leg-strength auxiliary sass bas))

(def running [:sprint :interval :5K])
(def hiking [])
(def parkour [])
(def movnat-climbing [])
(def movnat-sitting [])
(def movnat-jumping [])
(def movnat (concat movnat-climbing movnat-sitting))
(def swimming [])
(def rock-climbing [])
(def squash [])
(def football [])

(def template (atom []))

(defn generate [name category n]
  (swap! template conj {:name name
                        :category category
                        :movements n}))

(defn list-movements [category]
  [:div.row
   [:h3 (:name category)]
   [:table.table.table-striped
    [:tbody]
    (for [i (take (:movements category) (shuffle (:category category)))]
      [:tr
       [:td (name i)]])]])

(defn home-page []
  [:div
   [:h1 "Movement session"]
   [:button {:type "submit"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 2)
                         (generate "Hanging" hanging 1)
                         (generate "Equilibre" equilibre 1)
                         (generate "Strength" strength 1))}
    "Morning ritual"]
   [:button {:type "submit"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 2)
                         (generate "Strength" strength 4))}
    "Strength"]
   [:button {:type "submit"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 4)
                         (generate "Prehab" mobility 4))}
    "Mobility/Prehab"]
   [:button {:type "submit"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 2)
                         (generate "Locomotion" locomotion 6))}
    "Locomotion"]
   [:button {:type "submit"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 1)
                         (generate "BAS" bas 5))}
    "BAS"]
   [:button {:type "submit"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 1)
                         (generate "SASS" sass 4))}
    "SASS"]
   [:button {:type "submit"
             :class "btn btn-default"
             :on-click #(do
                         (reset! template [])
                         (generate "Warmup" warmup 1)
                         (generate "Mobility" mobility 1)
                         (generate "Leg Strength" leg-strength 3)
                         (generate "Auxiliry" auxiliary 2))}
    "Leg/Auxiliary strength"]
   [:div.container
    (for [category @template]
    (list-movements category))]
   [:p "@move.com"]])

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
