(ns movement.components.header
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary
             :include-macros true :refer [dispatch!]]
            [movement.components.login :refer [login]]))

(defn header []
  (let [logged-in? (session/get :user)
        sign-in-pressed? (atom false)]
    (fn []
      [:div.header
       [:div.home-menu.pure-menu.pure-menu-horizontal.pure-menu-fixed
        [:a.pure-menu-heading {:on-click #(dispatch! "/")} "Movement Session"]
        [:ul.pure-menu-list
         [:li.pure-menu-item
          [:a.pure-menu-link "Blog"]]
         (if logged-in?
           [:li.pure-menu-item.pure-menu-selected
            [:a.pure-menu-link {:on-click #(dispatch! "/generator")} "Back to app"]]
           [:li.pure-menu-item
            [:a.pure-menu-link {:on-click #(reset! sign-in-pressed? true)} "Log In"]])]
        (if @sign-in-pressed?
          [:div.pure-g
           [:div.pure-u-3-4]
           [:div.pure-u
            [:div.pure-form
             [login]]]]
          )]])))