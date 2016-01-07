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
            [movement.share :refer [share-component]]
            [movement.group :refer [group-creator-component]]
            [movement.routine :refer [routine-creator-component]]
            [movement.plan :refer [plan-creator-component]]
            [movement.components.login :refer [login-page]])
  (:import goog.History))

(enable-console-print!)

;; -------------------------
;; Client side routes
(secretary/defroute "/" [] (set-page! #'login-page))
(secretary/defroute "/generator" [] (set-page! #'generator-component))
(secretary/defroute "/user" [] (set-page! #'user-component))
(secretary/defroute "/template" [] (set-page! #'template-creator-component))
(secretary/defroute "/group" [] (set-page! #'group-creator-component))
(secretary/defroute "/routine" [] (set-page! #'routine-creator-component))
(secretary/defroute "/plan" [] (set-page! #'plan-creator-component))

;---------------------------
(defn page []
  [(session/get :current-page)])

;; -------------------------
(defn mount-root []
  (render-component [page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (secretary/set-config! :prefix "#")
  (if (session/get :user)
    ;todo: if registered more than 14 days and not payed, show payment-component
    (set-page! #'group-creator-component))

  (.initializeTouchEvents js/React true)
  (mount-root))

(init!)

