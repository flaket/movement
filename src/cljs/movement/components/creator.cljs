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
   [:h1.pure-u-1.center
    [:input {:type        "text"
             :size        24
             :placeholder text
             :on-change   #(swap! state assoc :title (-> % .-target .-value))
             :value       (:title @state)}]]])

(defn description [state]
  [:div.pure-g
   [:p.pure-u-1.subtitle
    [:textarea {:rows        3
                :cols        72
                :placeholder "Optional description"
                :on-change   #(swap! state assoc :description (-> % .-target .-value))
                :value       (:description @state)}]]])

(defn error [message]
  [:div.pure-g
   [:h3.pure-u-1.center {:style {:color "red"}} message]])

(defn username [t]
  [:div
   [:div.pure-g
    [:div.pure-u-1 (str "To create a new " t " you must first select a username")]]
   [set-username-component]])

