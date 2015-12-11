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
      [:div {:style {:font-size 30}}
       [:div.pure-g
        [:div.pure-u {:style {:padding 5}}
         [:i.fa.fa-envelope-o.fa-fw]
         [text-input email {:class       (str "pure-u" (when @loading? "disabled"))
                            :type        "email"
                            :name        "email"
                            :placeholder "Email"}]]]
       [:div.pure-g
        [:div.pure-u {:style {:padding 5}}
         [:i.fa.fa-key.fa-fw]
         [text-input password {:class       (str "pure-u" (when @loading? "disabled"))
                               :type        "password"
                               :name        "password"
                               :placeholder "Password"}]]]
       (when-let [e @error]
         [:div.notice e])
       [:button.button.button-primary
        {:class    (when @loading? "disabled")
         :on-click #(if-not (and (seq @email) (seq @password))
                     (reset! error "Both fields are required.")
                     (do
                       (reset! loading? true)
                       (POST "login" {:params        {:email    @email
                                                      :password @password}
                                      :handler       (fn [response] (do
                                                                      (session/put! :token (:token response))
                                                                      (session/put! :user (:user response))
                                                                      (session/put! :m-counter (atom 0))
                                                                      (get-templates)
                                                                      (get-all-categories)
                                                                      (get-all-movements)
                                                                      (get-stored-sessions)
                                                                      (get-equipment)
                                                                      (dispatch! "/generator")))
                                      :error-handler (fn [response] (do
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
       [:a.pure-menu-heading {:title  "Home"
                              :href   "/"
                              :target ""} "Movement Session"]
       [:ul.pure-menu-list
        [:li.pure-menu-item
         [:a.pure-menu-link {:title  "Visit blog"
                             :href   "/blog"
                             :target ""} "Blog"]]]]]]]
   [:div.pure-g.splash
    [:div.pure-u.content
     [login]]]])