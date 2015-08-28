(ns movement.core
  (:require [reagent.core :refer [render-component]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true :refer [dispatch!]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]
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
                    (session/put! :current-page #'generator-component))

(secretary/defroute "/user" []
                    (session/put! :current-page #'user-component))

(secretary/defroute "/template" []
                    (session/put! :current-page #'template-creator-component))

(secretary/defroute "/movements" []
                    (session/put! :current-page #'explorer-component))

;---------------------------
(defn page []
  [(session/get :current-page)])

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
  (render-component [page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (secretary/set-config! :prefix "#")
  (session/put! :current-page #'generator-component)
  (session/put! :logged-sessions [])
  (mount-root))