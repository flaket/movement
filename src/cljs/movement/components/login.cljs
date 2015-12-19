(ns movement.components.login
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [movement.util :refer [GET POST text-input get-all-movements
                                   get-stored-sessions get-templates
                                   get-equipment get-all-categories]]
            [secretary.core :include-macros true :refer [dispatch!]]
            [reagent.session :as session]))

(defn header []
  [:div
   [:div.pure-g
    [:div.pure-u-1
     [:div.home-menu.pure-menu-horizontal
      [:a.pure-menu-heading {:title  "Home"
                             :href   "/"
                             :target ""} "Movement Session"]]]]])

(defn footer []
  [:div#footer.l-box.is-center
   [:div#footer-links.pure-g
    [:div.pure-u.pure-u-md-1-3]
    [:div.pure-u.pure-u-md-1-12
     [:a {:title  "About Movement Session"
          :href   "/about"
          :target ""} "About"]]
    [:div.pure-u.pure-u-md-1-12
     [:a {:title  "Contact Us"
          :href   "/contact"
          :target ""} "Contact"]]
    [:div.pure-u.pure-u-md-1-12
     [:a {:title  "Read our Blog"
          :href   "/blog"
          :target ""} "Blog"]]
    [:div.pure-u.pure-u-md-1-12
     [:a {:title  "Terms and agreement"
          :href   "/terms"
          :target ""} "Terms"]]
    [:div.pure-u.pure-u-md-1-3]]
   [:div#footer-logo.pure-g
    [:div.pure-u.pure-u-md-1-3]
    [:div.pure-u.pure-u-md-1-3 [:img {:width 75 :height 75 :src ""}]]
    [:div.pure-u.pure-u-md-1-3]]
   [:div#footer-copyright.pure-g
    [:div.pure-u.pure-u-md-1-3]
    [:div.pure-u.pure-u-md-1-3 [:i.fa.fa-copyright] "2015 Movement Session"]
    [:div.pure-u.pure-u-md-1-3]]])

(defn login []
  (let [user (atom "")
        password (atom "")
        error (atom "")
        loading? (atom false)]
    (fn []
      [:div {:style {:font-size 24}}
       [:div.pure-g {:style {:padding 5}}
        [:div.pure-u.pure-u-md-1-5]
        [text-input user
         {:class       (str "pure-u pure-u-md-3-5" (when @loading? " disabled"))
          :name        "email"
          :placeholder "email or username"}]
        [:div.pure-u.pure-u-md-1-5]]
       [:div.pure-g {:style {:padding 5}}
        [:div.pure-u.pure-u-md-1-5]
        [text-input password
         {:class       (str "pure-u pure-u-md-3-5" (when @loading? " disabled"))
          :type        "password"
          :name        "password"
          :placeholder "password"}]
        [:div.pure-u.pure-u-md-1-5]]
       (when-let [e @error]
         [:div.notice e])
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-5]
        [:button.pure-u.pure-u-md-3-5.button.button-primary
         {:class    (when @loading? " disabled")
          :on-click #(if-not (and (seq @user) (seq @password))
                      (reset! error "Both fields are required.")
                      (do
                        (reset! loading? true)
                        (POST "login" {:params        {:username @user
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
         (if @loading? "Logging in..." "Log In")]
        [:div.pure-u.pure-u-md-1-5]]])))

(defn login-page []
  [:div
   (header)
   [:div.content.is-center {:style {:margin-top 50}}
    [:div.pure-g
     [:div.pure-u.pure-u-md-1-3]
     [:div.pure-u.pure-u-md-1-3 [login]]
     [:div.pure-u.pure-u-md-1-3]]
    (footer)]
   ])