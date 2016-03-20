(ns movement.core
  (:require [reagent.core :refer [atom render-component]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [movement.util :refer [hook-browser-navigation! set-page!]]
            [movement.user :refer [user-page]]
            [movement.session :refer [session-page]]
            [movement.login :refer [login-page]])
  (:import goog.History))

(enable-console-print!)

;; -------------------------
;; Client side routes
(secretary/defroute "/" [] (set-page! #'login-page))
(secretary/defroute "/session" [] (set-page! #'session-page))
(secretary/defroute "/user" [] (set-page! #'user-page))

;---------------------------
(defn page []
  [(session/get :current-page)])

;; -------------------------
(defn mount-root []
  (render-component [page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (secretary/set-config! :prefix "#")
  (when (session/get :email)
    (set-page! #'session-page))
  #_(.initializeTouchEvents js/React true)
  (mount-root))

(init!)

