(ns movement.components.header
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]))

(defn inner-header [])

(defn outer-header []
  (let [logged-in? (session/get :user-logged-in?)]
    (fn []
      [:div
       [:div.navbar.navbar-default.navbar-fixed-top
        [:div.container-fluid
         [:div.navbar-header
          [:a#logo.navbar-brand {:href "/"}]]
         (if logged-in?
           [:ul.nav.navbar-nav.navbar-right
            [:li [:a {:href "/"} "Back to app"]]]
           [:ul.nav.navbar-nav.navbar-right
            [:li
             [:a.login.login-link {:href ""
                                   :title "Log In"}
              "Log In"]]
            [:li
             [:a.signup-link.btn.btn-success.navbar-btn
              {:href "/signup"} "Sign Up"]]])]]])))

(defn header []
  (let [logged-in? (session/get :user-logged-in?)]
    (fn []
      (if logged-in?
        [inner-header]
        [outer-header]))))
