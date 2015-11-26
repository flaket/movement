(ns movement.components.login
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [movement.util :refer [GET POST text-input get-all-movements
                                   get-stored-sessions get-templates
                                   get-equipment get-all-categories]]
            [secretary.core :include-macros true :refer [dispatch!]]
            [reagent.session :as session]))

(defn login []
  (let [email (atom "")
        password (atom "")
        error (atom "")
        loading? (atom false)]
    (fn []
      [:div
       [:div.pure-g
        [:span.pure-u [:i.fa.fa-envelope-o.fa-fw]]
        [text-input email {:class       (str "pure-u" (when @loading? "disabled"))
                           :type        "email"
                           :name        "email"
                           :placeholder "Your Email"}]]
       [:div.pure-g
        [:span.pure-u [:i.fa.fa-key.fa-fw]]
        [text-input password {:class       (str "pure-u" (when @loading? "disabled"))
                              :type        "password"
                              :name        "password"
                              :placeholder "Your Password"}]]
       (when-let [e @error]
         [:div.notice e])
       [:button.pure-button
        {:class    (when @loading? "disabled")
         :on-click #(if-not (and (seq @email) (seq @password))
                     (reset! error "Both fields are required.")
                     (do
                       (reset! loading? true)
                       (POST "login" {:params          {:username @email
                                                        :password @password}
                                      :handler         (fn [response] (do
                                                                          (session/put! :token (:token response))
                                                                          (session/put! :user (:user response))
                                                                          (session/put! :m-counter (atom 0))
                                                                          (get-templates)
                                                                          (get-all-categories)
                                                                          (get-all-movements)
                                                                          (get-stored-sessions)
                                                                          (get-equipment)
                                                                          (dispatch! "/generator")))
                                      :error-handler   (fn [response] (do
                                                                        (reset! loading? false)
                                                                        (reset! error (:message (:response response)))
                                                                        (println (str "error! " response))))})))}
        (if @loading? "Logging in..." "Log In")]])))

(defn home []
  [:div
   [:div.pure-g
    [:div.pure-u
     [:div.header
      [:div.home-menu.pure-menu.pure-menu-horizontal.pure-menu-fixed
       [:a.pure-menu-heading "Movement Session"]
       [:ul.pure-menu-list
        [:li.pure-menu-item
         [:a.pure-menu-link {:title  "Visit blog"
                             :href   "/blog"
                             :target ""} "Blog"]]
        [:li.pure-menu-item
         [:a.pure-menu-link {:title  "Log in"
                             :href   "/app"
                             :target ""} "Log In"]]]]]]]
   [:div.splash-container
    [:div.splash
     [:div.splash-head
      [login]]]]])