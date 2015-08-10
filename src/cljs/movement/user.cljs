(ns movement.user
 (:require [reagent.core :refer [atom]]
           [reagent.session :as session]
           [secretary.core :include-macros true :refer [dispatch!]]))

(defn movement [{:keys [id title category-ref comment animation]}]
  [:li
   [:label title]])

(defn category [{:keys [id title]} movements]
  [:div
   [:h5 title]
   [:ul
    (for [m movements]
      ^{:key (:id m)} [movement m])]])

(defn logged-sessions []
  )

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
    [:section#header
     [:h1 "Movement Session"]]
    [:section#nav
     [:button.button {:on-click #(dispatch! "/")} "Generator"]
     [:button.button {:on-click #(dispatch! "/user")} "User"]
     [:button.button {:on-click #(dispatch! "/")} "Template Creator"]]
    [:section#log
     (let [logged-sessions (session/get :logged-sessions)]
       [:div
        [:h3 (str "# Logged sessions: " (count logged-sessions))]
        (for [s logged-sessions] ^{:key s}
                                 [session s])])]
    [:section#user
     [:div
      [:h3 "This is the user section."]
      [:h5 "Update personal settings."]]]]])