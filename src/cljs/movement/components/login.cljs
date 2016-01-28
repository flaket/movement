(ns movement.components.login
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [movement.util :refer [GET POST text-input get-all-movements
                                   get-stored-sessions get-templates get-routines
                                   get-plans get-groups get-ongoing-plan
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
        [:a.pure-menu-link {:title  "Home"
                            :href   "/"
                            :target ""} "Home"]]
       [:li.pure-menu-item
        [:a.pure-menu-link {:title  "Tour"
                            :href   "/tour"
                            :target ""} "Tour"]]
       [:li.pure-menu-item
        [:a.pure-menu-link {:title  "Pricing"
                            :href   "/pricing"
                            :target ""} "Pricing"]]
       [:li.pure-menu-item
        [:a.pure-menu-link {:title  "Sign in"
                            :href   "/app"
                            :target ""} "Sign in"]]]]]]])

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
    [:div.pure-u.pure-u-md-1-3 [:img {:width 75 :height 75 :src "images/static-air-baby.png"}]]
    [:div.pure-u.pure-u-md-1-3]]
   [:div#footer-copyright.pure-g
    [:div.pure-u.pure-u-md-1-3]
    [:div.pure-u.pure-u-md-1-3 [:i.fa.fa-copyright] "2015 Movement Session"]
    [:div.pure-u.pure-u-md-1-3]]])

(defn login []
  (let [user (atom "")
        password (atom "")
        error (atom "")
        loading? (atom false)
        show-payment? (atom false)]
    (fn []
      [:div {:style {:font-size 24}}
       (when @show-payment?
         [:div.pure-g
          [:a.pure-u-1.button.button-primary
           {:href (str "http://sites.fastspring.com/roebucksoftware/product/movementsessionsubscription"
                       "?referrer="
                       @user)} "Purchase subscription"]])
       [:div.pure-g {:style {:padding 5}}
        [:div.pure-u.pure-u-md-1-5]
        [text-input user
         {:class       (str "pure-u pure-u-md-3-5" (when @loading? " disabled"))
          :name        "email"
          :placeholder "email"}]
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
                                                                       (session/put! :email (:email response))
                                                                       (session/put! :username (:username response))
                                                                       (get-templates)
                                                                       (get-routines)
                                                                       (get-groups)
                                                                       (get-plans)
                                                                       (get-ongoing-plan)
                                                                       (get-all-categories)
                                                                       (get-all-movements)
                                                                       (get-stored-sessions)
                                                                       (dispatch! "/generator")))
                                       :error-handler (fn [response] (let [r (:response response)
                                                                           update-payment? (:update-payment? r)]
                                                                       (reset! loading? false)
                                                                       (reset! error (:message (:response response)))
                                                                       (when update-payment?
                                                                         (reset! show-payment? true))))})))}
         (if @loading? "Logging in..." "Log In")]
        [:div.pure-u.pure-u-md-1-5]]])))

(defn login-page []
  [:div
   (header)
   [:div.content.is-center {:style {:margin-top 200}}
    [:div.pure-g
     [:div.pure-u-1-12.pure-u-md-1-5]
     [:div.pure-u-5-12.pure-u-md-3-5
      [login]]
     [:div.pure-u-1-12.pure-u-md-1-5]]
    #_(footer)]])