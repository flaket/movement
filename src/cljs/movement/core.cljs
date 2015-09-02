(ns movement.core
  (:require [reagent.core :refer [render-component]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true :refer [dispatch!]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]
            [movement.util :refer [hook-browser-navigation! set-page!]]
            [movement.user :refer [user-component]]
            [movement.template :refer [template-creator-component]]
            [movement.generator :refer [generator-component]]
            [movement.explorer :refer [explorer-component]])
  (:import goog.History))

;; The core namespace is the client entry point.
;; The global state of the application is handled with the reagent.session utility namespace.
;; The generator namespace houses the main application for generating movement sessions.
;; The user namespace displays the user specific information.
;; The explorer namespace allows users to search and view the movements in the database.
;; The template namespace allows users to create their own templates.
;; The movements namespace temporarily houses lists of exercises.

(enable-console-print!)

;; -------------------------
;; Client side routes
(secretary/defroute "/" []
                    (set-page! #'generator-component))

(secretary/defroute "/user" []
                    (set-page! #'user-component))

(secretary/defroute "/template" []
                    (set-page! #'template-creator-component))

(secretary/defroute "/movements" []
                    (set-page! #'explorer-component))

;---------------------------
(defn page []
  [(session/get :current-page)])



;; -------------------------
(defn init! []
  (hook-browser-navigation!)
  (secretary/set-config! :prefix "#")
  (set-page! #'generator-component)
  (session/put! :logged-sessions [])

  (render-component [page] (.getElementById js/document "app")))

(init!)