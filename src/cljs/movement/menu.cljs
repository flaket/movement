(ns movement.menu
  (:require
    [reagent.core :refer [atom]]
    [reagent.session :as session]
    [movement.util :refer [POST]]
    [secretary.core :include-macros true :refer [dispatch!]]))

(defn menu-component []
  (let [menu-item-session " Session"
        menu-item-create " Create"
        menu-item-explore " Explore"
        menu-item-user " User"]
    (fn []
      [:div
       [:div {:id "menu-hamburger"
            :name "menu-hamburger"
            :class    (str "menu-link " (when (session/get :active?) "active"))
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

             [:a.pure-menu-link #_[:i.fa.fa-home] menu-item-session]]
            [:li {:className (str "pure-menu-item"
                                  (when (= menu-item-explore selected)
                                    " menu-item-divided pure-menu-selected"))
                  :on-click  #(do
                               (session/put! :selected-menu-item menu-item-explore)
                               (dispatch! "/explore"))}
             [:a.pure-menu-link #_[:i.fa.fa-book] menu-item-explore]]
            [:li {:className (str "pure-menu-item"
                                  (when (= menu-item-create selected)
                                    " menu-item-divided pure-menu-selected"))
                  :on-click  #(do
                               (session/put! :selected-menu-item menu-item-create)
                               (dispatch! "/create"))}
             [:a.pure-menu-link #_[:i.fa.fa-book] menu-item-create]]
            [:li {:className (str "pure-menu-item"
                                  (when (= menu-item-user selected)
                                    " menu-item-divided pure-menu-selected"))
                  :on-click  #(do
                               (session/put! :selected-menu-item menu-item-user)
                               (dispatch! "/user"))}

             [:a.pure-menu-link #_[:i.fa.fa-user] menu-item-user]]
            [:li {:className (str "pure-menu-item")
                  :on-click  #(do (session/clear!)
                                  (dispatch! "/"))
                  :style {:margin-top "50px"}}
             [:a.pure-menu-link
              #_[:i.fa.fa-power-off] "Log Out"]]]]])])))