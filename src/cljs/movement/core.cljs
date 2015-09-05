(ns movement.core
  (:require [reagent.core :refer [render-component]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]
            [movement.util :refer [GET hook-browser-navigation! set-page!]]
            [movement.user :refer [user-component]]
            [movement.template :refer [template-creator-component]]
            [movement.generator :refer [generator-component]]
            [movement.draggable :refer [draggable-number-component]]
            [movement.explorer :refer [explorer-component]]
            [movement.share :refer [share-component]]
            [movement.sparkline :refer [sparklines-page]])
  (:import goog.History))

(enable-console-print!)

;; -------------------------
;; Client side routes
(secretary/defroute "/" [] (set-page! #'generator-component))
(secretary/defroute "/user" [] (set-page! #'user-component))
(secretary/defroute "/template" [] (set-page! #'template-creator-component))
(secretary/defroute "/movements" [] (set-page! #'explorer-component))
(secretary/defroute "/share" [] (set-page! #'share-component))
(secretary/defroute "/drag" [] (set-page! #'draggable-number-component))
(secretary/defroute "/sparkline" [] (set-page! #'sparklines-page))

;---------------------------
(defn page []
  [(session/get :current-page)])

;; -------------------------
(defn init! []
  (hook-browser-navigation!)
  (secretary/set-config! :prefix "#")
  (set-page! #'draggable-number-component)
  (session/put! :logged-sessions [])

  (render-component [page] (.getElementById js/document "app")))

(init!)