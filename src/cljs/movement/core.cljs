(ns movement.core
  (:require [reagent.core :refer [atom render-component]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]
            [movement.util :refer [get-all-categories get-all-movements get-templates hook-browser-navigation! set-page!]]
            [movement.user :refer [user-component payment-component]]
            [movement.template :refer [template-creator-component]]
            [movement.generator :refer [generator-component]]
            [movement.share :refer [share-component]]
            [movement.components.login :refer [home login]])
  (:import goog.History))

(enable-console-print!)

;; -------------------------
;; Client side routes
(secretary/defroute "/" [] (set-page! #'home))
(secretary/defroute "/login" [] (set-page! #'login))
(secretary/defroute "/generator" [] (set-page! #'generator-component))
(secretary/defroute "/user" [] (set-page! #'user-component))
(secretary/defroute "/template" [] (set-page! #'template-creator-component))
(secretary/defroute "/share" [] (set-page! #'share-component))
(secretary/defroute "/pay" [] (set-page! #'payment-component))

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
    (set-page! #'template-creator-component)
    (set-page! #'home))

  (.initializeTouchEvents js/React true)
  (mount-root))

(init!)

