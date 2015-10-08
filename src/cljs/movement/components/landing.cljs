(ns movement.components.landing
  (:require [reagent.core :refer [atom]]
            [movement.components.header :refer [header]]
            [movement.components.footer :refer [footer]]
            [movement.components.login :refer [login]]
            [movement.components.signup :refer [sign-up]]
            [secretary.core :as secretary
             :include-macros true :refer [dispatch!]]))

(defn prolog []
  (let []
    (fn []
      [:section.home-prolog
       [:a.home-action {:on-click #(dispatch! "/signup")} "Sign Up"]
       [:div.home-cover]
       [:div.home-top-shelf]
       [:div.home-slogans
        [:h1.slogan.proverb "Learn new movements"]
        [:h3.slogan.context "You have a body to move; stop creating static training programs and
         let MovementSession inspire you to learn new and exciting ways of moving your body."]]])))

(defn benefits []
  (let []
    (fn []
      [:section.home-benefits
       [:h1 "Benefits"]
       [:div.home-potential-bullets
        [:ul
         [:li "1"]
         [:li "2"]]
        [:ul
         [:li "3"]
         [:li "4"]]
        [:ul
         [:li "5"]
         [:li "6"]]]])))

(defn epilog []
  (let []
    (fn []
      [:section.home-epilog
       [:a.home-action {:on-click #(dispatch! "/signup")} "Sign Up Free"]
       [:div.home-cover]
       [:div.home-top-shelf]
       [:div.home-slogans
        [:h1.slogan.proverb "So, are you ready to start moving more?"]
        [:h3.slogan.context "Here's what you do next.."]]])))

(defn home []
  (let []
    (fn []
      [:div
       [header]
       [login]
       [prolog]
       [sign-up]
       [benefits]
       [epilog]
       [sign-up]
       [footer]])))