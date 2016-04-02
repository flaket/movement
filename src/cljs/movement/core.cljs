(ns movement.core
  (:require [reagent.core :refer [atom render-component]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [movement.util :refer [hook-browser-navigation! set-page!]]
            [movement.pages.login :refer [login-page]]
            [movement.pages.feed :refer [feed-page]]
            [movement.pages.session :refer [session-page]]
            [movement.pages.discover :refer [discover-page]]
            [movement.pages.user :refer [user-page]])
  (:import goog.History))

(enable-console-print!)

;; -------------------------
;; Client side routes
(secretary/defroute "/" [] (set-page! #'login-page))
(secretary/defroute "/feed" [] (set-page! #'feed-page))
(secretary/defroute "/session" [] (set-page! #'session-page))
(secretary/defroute "/discover" [] (set-page! #'discover-page))
(secretary/defroute "/user" [] (set-page! #'user-page))

;---------------------------
(defn application-view []
  [(session/get :current-page)])

;; -------------------------
(defn mount-root []
  (render-component [application-view] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (secretary/set-config! :prefix "#")
  (when (session/get :email)
    (set-page! #'discover-page))
  (mount-root))

(init!)

