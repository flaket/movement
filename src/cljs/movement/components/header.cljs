(ns movement.components.header
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary
             :include-macros true :refer [dispatch!]]))

(defn header []
  (let [logged-in? (session/get :user)]
    (fn []
      [:div
       [:div.navbar.navbar-default.navbar-fixed-top
        [:div.container-fluid
         [:div.navbar-header
          [:a#logo.navbar-brand {:on-click #(dispatch! "/")} "MS"]]
         (if logged-in?
           [:ul.nav.navbar-nav.navbar-right
            [:li [:a {:on-click #(dispatch! "/generator")} "Back to app"]]]
           [:ul.nav.navbar-nav.navbar-right
            [:li
             [:a.login.login-link {:on-click #(dispatch! "/login")} "Log In"]]
            [:li
             [:a.signup-link.btn.btn-success.navbar-btn
              {:on-click #(dispatch! "/signup")} "Sign Up"]]])]]])))
