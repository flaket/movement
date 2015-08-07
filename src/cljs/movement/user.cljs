(ns movement.user
 (:require [reagent.core :as reagent :refer [atom]]
           [reagent.session :as session]))

(defn movement [{:keys [id title category-ref comment animation]}]
  [:li
   [:label title]])

(defn category [{:keys [id title]} movements]
  [:div
   [:h5 title]
   [:ul
    (for [m movements]
      ^{:key (:id m)} [movement m])]])

(defn user-page []
  [:div
   [:section#header]
   [:section#nav]
   [:section#user
    [:div.container

     (let [logged-session (session/get :logged-sessions)
           categories (vals (:categories logged-session))
           movements (vals (:movements logged-session))]
       (for [c categories]
         ^{:key (:id c)} [category c
                          (filter #(= (:id c) (:category-ref %)) movements)]))]]])