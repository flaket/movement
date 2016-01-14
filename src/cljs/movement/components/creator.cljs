(ns movement.components.creator
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [secretary.core :include-macros true :refer [dispatch!]]
            [reagent.session :as session]
            [movement.user :refer [set-username-component]]))

(defn heading [text]
  [:div.pure-g
   [:h2.pure-u text]])

(defn title [state text]
  [:div.pure-g
   [:h1.pure-u-1
    [:input {:type        "text"
             :size        50
             :placeholder text
             :on-change   #(swap! state assoc :title (-> % .-target .-value))
             :value       (:title @state)}]]
   [:div.pure-g
    [:div.pure-u (str "by " (session/get :username))]]])

(defn description [state]
  [:div.pure-g
   [:div.pure-u
    [:textarea {:rows        3
                :cols        58
                :placeholder "Optional description"
                :on-change   #(swap! state assoc :description (-> % .-target .-value))
                :value       (:description @state)}]]])

(defn error [message]
  [:div.pure-g
   [:h3.pure-u {:style {:color "red"}} message]])

(defn username [t]
  [:div
   [:div.pure-g
    [:div.pure-u-1 (str "To create a new " t " you must first select a username")]]
   [set-username-component]])

