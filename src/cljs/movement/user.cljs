(ns movement.user
 (:require [reagent.core :refer [atom]]
           [reagent.session :as session]
           [secretary.core :include-macros true :refer [dispatch!]]
           [movement.nav :refer [nav]]))

(defn movement [{:keys [title]}]
  [:li
   [:label title]])

(defn category [{:keys [title]} movements]
  [:div
   [:h5 title]
   [:ul
    (for [m movements]
      ^{:key (:id m)} [movement m])]])

(defn session [{:keys [title categories movements]}]
  [:div
   [:h4 title]
   [:div
    (for [c (vals categories)] ^{:key (:id c)}
                        [category c
                         (filter #(= (:id c) (:category-ref %)) (vals movements))])]])

(defn user-page []
  [:div
   [:div.container
    (nav)
    [:section#log
     (let [logged-sessions (session/get :logged-sessions)]
       [:div
        [:h3 (str "# Logged sessions: " (count logged-sessions))]
        (for [s logged-sessions] ^{:key (:timestamp s)}
                                 [session s])])]
    [:section#user
     [:div
      [:h3 "This is the user section."]
      [:h5 "Update personal settings."]]]]])