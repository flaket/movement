(ns movement.core
  (:require [reagent.core :refer [atom render-component]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]
            [movement.util :refer [GET hook-browser-navigation! set-page!]]
            [movement.user :refer [user-component]]
            [movement.template :refer [template-creator-component]]
            [movement.generator :refer [generator-component]]
            [movement.explorer :refer [explorer-component]]
            [movement.share :refer [share-component]]


            [movement.components.landing :refer [home]]
            [movement.components.footer :refer [footer]]
            [movement.components.header :refer [header]]
            [movement.components.signup :refer [sign-up]])
  (:import goog.History))

(enable-console-print!)

;; -------------------------
;; Client side routes
(secretary/defroute "/" [] (set-page! #'home))
(secretary/defroute "/signup" [] (set-page! #'sign-up))
(secretary/defroute "/generator" [] (set-page! #'generator-component))
(secretary/defroute "/user" [] (set-page! #'user-component))
(secretary/defroute "/template" [] (set-page! #'template-creator-component))
(secretary/defroute "/movements" [] (set-page! #'explorer-component))
(secretary/defroute "/share" [] (set-page! #'share-component))

;---------------------------
(defn page []
  [(session/get :current-page)])

;; -------------------------
(defn mount-root []
  (render-component [page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (secretary/set-config! :prefix "#")
  (set-page! #'home)
  (session/put! :logged-sessions [])
  (session/put! :m-counter (atom 0))

  (.initializeTouchEvents js/React true)

  (mount-root))

(init!)
