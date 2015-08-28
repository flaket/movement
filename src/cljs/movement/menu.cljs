(ns movement.menu
  (:require
    [reagent.core :refer [atom]]
    [reagent.session :as session]
    [secretary.core :include-macros true :refer [dispatch!]]))

(defn menu-component []
  (let [menu-item-session "Generate session"
        menu-item-user "User"
        menu-item-template "Create template"
        menu-item-movements "View movements"]
    (fn []
      [:div

       [:a {:class    (str "menu-link " (when (session/get :active?) "active"))
            :on-click  #(session/put! :active? (not (session/get :active?)))}
        [:span]]
       ;;

       (let [selected (session/get :selected-menu-item)]
         [:div#menu {:class (str "" (when (session/get :active?) "active"))}
          [:div.pure-menu
           [:ul.pure-menu-list
            [:li {:className (str "pure-menu-item"
                                  (when (= menu-item-session selected)
                                    " menu-item-divided pure-menu-selected"))
                  :on-click  #(do
                               (session/put! :selected-menu-item menu-item-session)
                               (dispatch! "/"))}
             [:a.pure-menu-link menu-item-session]]
            [:li {:className (str "pure-menu-item"
                                  (when (= menu-item-user selected)
                                    " menu-item-divided pure-menu-selected"))
                  :on-click  #(do
                               (session/put! :selected-menu-item menu-item-user)
                               (dispatch! "/user"))}
             [:a.pure-menu-link menu-item-user]]
            [:li {:className (str "pure-menu-item"
                                  (when (= menu-item-template selected)
                                    " menu-item-divided pure-menu-selected"))
                  :on-click  #(do
                               (session/put! :selected-menu-item menu-item-template)
                               (dispatch! "/template"))}
             [:a.pure-menu-link menu-item-template]]
            [:li {:className (str "pure-menu-item"
                                  (when (= menu-item-movements selected)
                                    " menu-item-divided pure-menu-selected"))
                  :on-click  #(do
                               (session/put! :selected-menu-item menu-item-movements)
                               (dispatch! "/movements"))}
             [:a.pure-menu-link menu-item-movements]]]]])])))