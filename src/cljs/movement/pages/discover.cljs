(ns movement.pages.discover
  (:require [reagent.core :refer [atom]]
            [movement.menu :refer [menu-component]]
            [movement.text :refer [text-edit-component]]
            [movement.util :refer [GET]]
            [movement.pages.feed :refer [session-view]]
            [reagent.session :as session]
            [secretary.core :include-macros true :refer [dispatch!]]))

(defn load-users []
  (GET "users" {:params       {}
               :handler       (fn [r] (session/put! :users r))
               :error-handler (fn [r] (pr (str "error loading users: " r)))}))

(defn load-user [e user-id]
  (.preventDefault e)
  (GET "user" {:params        {:user-id user-id}
               :handler       (fn [r]
                                (session/put! :viewing-user r)
                                (session/remove! :selected-menu-item)
                                (dispatch! "/user"))
               :error-handler (fn [r] nil)}))

(defn discover-page []
  (let [_ (when (nil? (session/get :users)) (load-users))]
    (fn []
      [:div
       [menu-component]
       [:div.content
        (when-let [users (session/get :users)]
          [:div.movements
           (doall
             (for [{:keys [name user-image profile-text user-id]} users]
               ^{:key name}
               [:div.pure-g {:style   {:padding "25px 25px 25px 25px" :cursor 'pointer :border-bottom " 1px solid lightgray"}
                             :onClick #(load-user % user-id) :onTouchEnd #(load-user % user-id)}
                [:div.pure-u
                 (if user-image
                   [:img {:src (str "http://s3.amazonaws.com/mumrik-user-profile-images/" user-id ".jpg")
                          :width 80 :height 80
                          :style {:margin-top 15 :margin-left 40
                                  :cursor     'pointer :border-radius "50% 50% 50% 50%"}}]
                   [:img {:src "images/profile-no-photo.png" :width 80 :height 80
                          :style {:margin-top 15 :margin-left 40
                                  :cursor     'pointer :border-radius "50% 50% 50% 50%"}}])]
                [:div.pure-u {:style {:margin-left 20}}
                 [:div.pure-g [:h2 [:a.pure-u name]]]
                 [:div.pure-g [:div.pure-u {:style {:margin-bottom 25}} profile-text]]]]))])]])))
