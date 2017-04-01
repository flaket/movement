(ns movement.core
  (:require [reagent.core :refer [atom render-component]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [movement.util :refer [set-page!]]
            [movement.session :refer [session-page]]))

(enable-console-print!)
(secretary/defroute "/" [] (set-page! #'session-page))

(defn application-view []
  [(session/get :current-page)])

(defn mount-root []
  (render-component [application-view] (.getElementById js/document "app")))

(defn init! []
  (secretary/set-config! :prefix "#")
  (mount-root))

(init!)
