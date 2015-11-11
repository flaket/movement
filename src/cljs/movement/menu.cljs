(ns movement.menu
  (:require
    [reagent.core :refer [atom]]
    [reagent.session :as session]
    [movement.util :refer [POST]]
    [secretary.core :include-macros true :refer [dispatch!]]))

(defn menu-component []
  (let [menu-item-session " Session"
        menu-item-user " User"
        menu-item-template " Templates"]
    (fn []
      [:div
       [:a {:class    (str "menu-link " (when (session/get :active?) "active"))
            :on-click  #(session/put! :active? (not (session/get :active?)))}
        [:span]]
       (let [selected (session/get :selected-menu-item)]
         [:div#menu {:class (str "" (when (session/get :active?) "active"))}
          [:div.pure-menu
           [:ul.pure-menu-list
            [:li {:className (str "pure-menu-item"
                                  (when (= menu-item-session selected)
                                    " menu-item-divided pure-menu-selected"))
                  :on-click  #(do
                               (session/put! :selected-menu-item menu-item-session)
                               (dispatch! "/generator"))}

             [:a.pure-menu-link [:i.fa.fa-home] menu-item-session]]
            [:li {:className (str "pure-menu-item"
                                  (when (= menu-item-user selected)
                                    " menu-item-divided pure-menu-selected"))
                  :on-click  #(do
                               (session/put! :selected-menu-item menu-item-user)
                               (dispatch! "/user"))}

             [:a.pure-menu-link [:i.fa.fa-user] menu-item-user]]
            [:li {:className (str "pure-menu-item"
                                  (when (= menu-item-template selected)
                                    " menu-item-divided pure-menu-selected"))
                  :on-click  #(do
                               (session/put! :selected-menu-item menu-item-template)
                               (dispatch! "/template"))}
             [:a.pure-menu-link [:i.fa.fa-shield] menu-item-template]]

            [:li {:className (str "pure-menu-item")
                  :on-click  #(do (session/clear!)
                                  (dispatch! "/"))}
             [:a.pure-menu-link [:i.fa.fa-power-off] " Log Out"]]]]])])))