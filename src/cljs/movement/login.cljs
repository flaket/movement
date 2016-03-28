(ns movement.login
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [secretary.core :include-macros true :refer [dispatch!]]
            [movement.util :refer [GET POST text-input get-stored-sessions
                                   get-templates get-movements get-categories]]))

(defn login-handler [user password loading? error show-payment?]
  (if-not (and (seq @user) (seq @password))
    (reset! error "Both fields are required.")
    (do
      (reset! loading? true)
      (POST "login" {:params        {:email    @user
                                     :password @password}
                     :handler       (fn [{:keys [token email]}]
                                      (session/put! :token token)
                                      (session/put! :email email)
                                      (session/put! :selected-menu-item :feed)
                                      (dispatch! "/session"))
                     :error-handler (fn [response]
                                      (let [r (:response response)
                                            update-payment? (:update-payment? r)]
                                        (reset! loading? false)
                                        (reset! error (:message (:response response)))
                                        (when update-payment?
                                          (reset! show-payment? true))))}))))

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
        [text-input user
         {:class       (str "pure-u-1" (when @loading? " disabled"))
          :name        "email"
          :placeholder "email"}]]
       [:div.pure-g {:style {:padding 5}}
        [text-input password
         {:class       (str "pure-u-1" (when @loading? " disabled"))
          :type        "password"
          :name        "password"
          :placeholder "password"}]]
       (when-let [e @error]
         [:div.pure-g
          [:div.pure-u-1.notice.center e]])
       [:div.pure-g
        [:button.pure-u-1.button.button-primary
         {:class    (when @loading? " disabled")
          :onClick #(login-handler user password loading? error show-payment?)
          :onTouchEnd #(login-handler user password loading? error show-payment?)}
         (if @loading? "Logging in..." "Log In")]]])))

(defn login-page []
  [:div
   [:div.content.is-center {:style {:margin-top 200}}
    [:div.pure-g
     [:div.pure-u.pure-u-md-1-5]
     [:div.pure-u-1.pure-u-md-3-5
      [login]]
     [:div.pure-u.pure-u-md-1-5]]]])