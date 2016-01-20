(ns movement.core
  (:require [reagent.core :refer [atom render-component]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]
            [movement.util :refer [get-all-categories get-all-movements get-templates hook-browser-navigation! set-page!]]
            [movement.user :refer [user-component]]
            [movement.template :refer [template-creator-component]]
            [movement.generator :refer [generator-component]]
            [movement.group :refer [group-creator-component]]
            [movement.routine :refer [routine-creator-component]]
            [movement.plan :refer [plan-creator-component]]
            [movement.create :refer [create-component]]
            [movement.explore :refer [explore-component]]
            [movement.components.login :refer [login-page]])
  (:import goog.History))

(enable-console-print!)

;; -------------------------
;; Client side routes
(secretary/defroute "/" [] (set-page! #'login-page))
(secretary/defroute "/generator" [] (set-page! #'generator-component))
(secretary/defroute "/create" [] (set-page! #'create-component))
(secretary/defroute "/explore" [] (set-page! #'explore-component))
(secretary/defroute "/user" [] (set-page! #'user-component))

;---------------------------
(defn page []
  [(session/get :current-page)])

;; -------------------------
(defn mount-root []
  (render-component [page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (secretary/set-config! :prefix "#")
  (when (session/get :user)
    (set-page! #'generator-component))

  (.initializeTouchEvents js/React true)
  (mount-root))

(init!)

