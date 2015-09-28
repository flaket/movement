(ns movement.components.landing
  (:require [reagent.core :refer [atom]]
            [movement.components.header :refer [header]]
            [movement.components.footer :refer [footer]]
            [movement.components.login :refer [login]]
            [movement.components.signup :refer [sign-up]]))

(defn prolog []
  (let []
    (fn []
      [:section.home-prolog
       [:a.home-action {:href "/signup"} "Sign Up Free"]
       [:div.home-cover]
       [:div.home-top-shelf]
       [:div.home-slogans
        [:h1.slogan.proverb "Learn new movements"]
        [:h3.slogan.context "You have a body to move,
         let MovementSession inspire you with new movements and exciting programming."]]])))

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
       [:a.home-action {:href "/signup"} "Sign Up Free"]
       [:div.home-cover]
       [:div.home-top-shelf]
       [:div.home-slogans
        [:h1.slogan.proverb "So, are you ready to move more?"]
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