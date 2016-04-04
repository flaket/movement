(ns movement.menu
  (:require
    [reagent.core :refer [atom]]
    [reagent.session :as session]
    [movement.util :refer [POST]]
    [secretary.core :include-macros true :refer [dispatch!]]))

(defn menu-click-handler [event selected-item dispatch-page]
  (.preventDefault event)
  (session/put! :selected-menu-item selected-item)
  (dispatch! dispatch-page))

(defn menu-component []
  (let [selected (session/get :selected-menu-item)]
    [:div#menu
     [:ul.pure-g
      [:li.pure-u-1-4 {:className  (when (= selected :feed) "active")
                       :onClick    #(menu-click-handler % :feed "/feed")
                       :onTouchEnd #(menu-click-handler % :feed "/feed")}
       [:a [:i.fa.fa-feed.fa-2x]]]
      [:li.pure-u-1-4 {:className  (when (= selected :session) "active")
                       :onClick    #(menu-click-handler % :session "/session")
                       :onTouchEnd #(menu-click-handler % :session "/session")}
       [:a [:i.fa.fa-pencil-square-o.fa-2x]]]
      [:li.pure-u-1-4 {:className  (when (= selected :discover) "active")
                       :onClick    #(menu-click-handler % :discover "/discover")
                       :onTouchEnd #(menu-click-handler % :discover "/discover")}
       [:a [:i.fa.fa-search.fa-2x]]]
      [:li.pure-u-1-4 {:className  (when (= selected :user) "active")
                       :onClick    #(menu-click-handler % :user "/user")
                       :onTouchEnd #(menu-click-handler % :user "/user")}
       [:a [:i.fa.fa-user.fa-2x]]]]]))