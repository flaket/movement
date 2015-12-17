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
                             :target ""} "Movement Session"]
      [:ul.pure-menu-list
       [:li.pure-menu-item
        [:a.pure-menu-link {:title  "Blog"
                            :href   "/blog"
                            :target ""} "Blog"]]
       [:li.pure-menu-item
        [:a.pure-menu-link {:title  "Log in"
                            :href   "/app"
                            :target ""} "Log in"]]]]]]])

(defn footer []
  [:div.footer.l-box.is-center
   [:div.pure-g
    [:div.pure-u.pure-u-md-1-4]
    [:div.pure-u.pure-u-md-1-8
     [:a {:title  "About Movement Session"
          :href   "/about"
          :target ""} "About"]]
    [:div.pure-u.pure-u-md-1-8
     [:a {:title  "Contact Us"
          :href   "/contact"
          :target ""} "Contact"]]
    [:div.pure-u.pure-u-md-1-8
     [:a {:title  "Read our Blog"
          :href   "/blog"
          :target ""} "Blog"]]
    [:div.pure-u.pure-u-md-1-8
     [:a {:title  "Terms and agreement"
          :href   "/terms"
          :target ""} "Terms"]]
    [:div.pure-u.pure-u-md-1-4]]
   [:div.pure-g.copyright
    [:div.pure-u.pure-u-md-1-4]
    [:div.pure-u.pure-u-md-1-2 [:i.fa.fa-copyright] "2015 Movement Session"]
    [:div.pure-u.pure-u-md-1-4]]])

(defn login []
  (let [email (atom "")
        password (atom "")
        error (atom "")
        loading? (atom false)]
    (fn []
      [:div {:style {:font-size 30}}
       [:div.pure-g {:style {:padding 5}}
        [:div.pure-u.pure-u-md-1-4 [:i.fa.fa-envelope-o.fa-fw]]
        [:div.pure-u.pure-u-md-3-4 [text-input email
                                    {:class (str "pure-u" (when @loading? "disabled"))
                                     :type        "email"
                                     :name        "email"
                                     :placeholder "Email"}]]]
       [:div.pure-g {:style {:padding 5}}
        [:div.pure-u.pure-u-md-1-4 [:i.fa.fa-key.fa-fw]]
        [:div.pure-u.pure-u-md-3-4 [text-input password
                                    {:class       (str "pure-u" (when @loading? "disabled"))
                                     :type        "password"
                                     :name        "password"
                                     :placeholder "Password"}]]]
       (when-let [e @error]
         [:div.notice e])
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-4]
        [:div.pure-u.pure-u-md-3-4
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
          (if @loading? "Logging in..." "Log In")]]]])))

(defn login-page []
  [:div
   (header)
   [:div.content.is-center
    [:div.pure-g {:style {:margin-top 100}}
     [:div.pure-u.pure-u-md-1-3]
     [:div.pure-u.pure-u-md-1-3 [login]]
     [:div.pure-u.pure-u-md-1-3]]]
   (footer)])