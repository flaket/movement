(ns movement.user
 (:require [reagent.core :refer [atom]]
           [reagent.session :as session]
           [movement.nav :refer [nav-component]]))

(defn movement [{:keys [title]}]
  [:li
   [:label title]])

(defn category [{:keys [title]} movements]
  [:div
   [:h5 title]
   [:ul
    (for [m movements]
      ^{:key (:id m)} [movement m])]])

(defn session [{:keys [title parts movements]}]
  [:div
   [:h4 title]
   [:div
    (for [c (vals parts)] ^{:key (:id c)}
                        [category c
                         (filter #(= (:id c) (:category-ref %)) (vals movements))])]])

(defn user-component []
  (let []
    (fn []
      [:div
       [:div.container
        [nav-component]
        [:section#log
         (let [logged-sessions (session/get :logged-sessions)]
           [:div
            [:h3 (str "# Logged sessions: " (count logged-sessions))]
            #_(for [s logged-sessions] ^{:key (:timestamp s)}
                                     [session s])])]
        [:section#user
         [:div
          [:h3 "This is the user section."]
          [:h5 "Update personal settings."]]]]])))