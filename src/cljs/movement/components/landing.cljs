(ns movement.components.landing
  (:require [reagent.core :refer [atom]]
            [movement.components.header :refer [header]]
            [movement.components.footer :refer [footer]]
            [movement.components.login :refer [login]]
            [movement.components.signup :refer [sign-up]]
            [secretary.core :as secretary
             :include-macros true :refer [dispatch!]]
            [reagent.session :as session]))

(defn prolog []
  (let []
    (fn []
      [:div.splash-container
       [:div.splash
        [:h1.splash-head "Plan less, move more"]
        [:p.splash-subhead "You have a body to move; stop creating static training programs and
         let MovementSession inspire you to plan and learn new and challenging ways of moving your body."]
        [:p
         [:a.pure-button.pure-button-primary "Get Started"]]]])))

(defn benefits []
  (let []
    (fn []
      [:div.content
       [:h2.content-head.is-center "Benefits"]
       [:div.pure-g
        [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
         [:h3.content-subhead
          "Learn new movements"]
         [:p "Explore a database with hundreds of movements."]]
        [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
         [:h3.content-subhead
          "Unique sessions"]
         [:p "Generate countless unique training sessions, either fully planned,
           randomly generated or a suitable combination."]]
        [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
         [:h3.content-subhead
          "Share"]
         [:p "Share your sessions with others."]]
        [:div.l-box.pure-u-1.pure-u-md-1-2.pure-u-lg-1-4
         [:h3.content-subhead
          "Log"]
         [:p "Log your sessions and review them later."]]]])))

(defn main []
  (let []
    (fn []
      [:div.ribbon.l-box-lrg.pure-g
       [:div.l-box-lrg.is-center.pure-u-1.pure-u-md-1-2.pure-u-lg-2-5
        [:img.pure-img-responsive {:width 300}]]
       [:div.pure-u-1.pure-u-md-1-2.pure-u-lg-3-5
        [:h2.content-head.content-head-ribbon "Gekko smekko lekko grekko"]
        [:p "Whadasdasdadhsjsadhjsafhjsafhjsafhjasfh asf hasjfhas j fhasj fhsajf"]]])))


(defn epilog []
  (let []
    (fn []
      [:div.content
       [:h2.content-head.is-center "So, are you ready to start moving more?"]
       [:div.pure-g
        [:div.l-box-lrg.pure-u-1.pure-u-md-2-5
         [:div.pure-form.pure-form-stacked
          [sign-up]]]
        [:div.l-box-lrg.pure-u-1.pure-u-md-3-5
         [:h4 "Contact Us"]
         [:p "asdasdahjsdhasjhj hajsdhjashd"]
         [:h4 "More Information"]
         [:p "12058135 dasfgagf 198274"]
         ]]])))

(defn home []
  (let []
    (fn []
      [:div
       [header]
       [prolog]
       [:div.content-wrapper
        [benefits]
        [main]
        [epilog]
        [footer]]])))