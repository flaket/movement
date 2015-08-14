(ns movement.nav
  (:require [secretary.core :as secretary :include-macros true :refer [dispatch!]]))

(defn nav []
  [:div
   [:section#header
    [:header#header
     [:h1 "Movement Session"]]]
   [:section#nav
    [:button.button {:on-click #(dispatch! "/")} "Session Generator"]
    [:button.button {:on-click #(dispatch! "/user")} "User Profile"]
    [:button.button {:on-click #(dispatch! "/template")} "Template Creator"]
    [:button.button {:on-click #(dispatch! "/movements")} "Movement Explorer"]]])