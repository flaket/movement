(ns movement.menu
  (:require
    [reagent.core :refer [atom]]
    [reagent.session :as session]
    [movement.util :refer [POST]]
    [secretary.core :include-macros true :refer [dispatch!]]))

(defn menu-click-handler [selected-item dispatch-page]
  (session/put! :selected-menu-item selected-item)
  (dispatch! dispatch-page))

(defn menu-component []
  (let [selected (session/get :selected-menu-item)]
    [:div#menu
     [:ul.pure-g
      [:li.pure-u-1-3 {:className  (when (= selected :feed) "active")
                       :onClick    #(menu-click-handler :feed "/session")
                       :onTouchEnd #(menu-click-handler :feed "/session")}
       [:a [:i.fa.fa-book.fa-2x]]]
      [:li.pure-u-1-3 {:className  (when (= selected :discover) "active")
                       :onClick    #(menu-click-handler :discover "/user")
                       :onTouchEnd #(menu-click-handler :discover "/user")}
       [:a [:i.fa.fa-search.fa-2x]]]
      [:li.pure-u-1-3 {:className  (when (= selected :user) "active")
                       :onClick    #(menu-click-handler :user "/user")
                       :onTouchEnd #(menu-click-handler :user "/user")}
       [:a [:i.fa.fa-user.fa-2x]]]]]))