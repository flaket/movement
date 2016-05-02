(ns movement.pages.login
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [secretary.core :include-macros true :refer [dispatch!]]
            [movement.util :refer [GET POST text-input get-stored-sessions
                                   get-templates get-movements get-categories]]))

(defn login-handler [event login-state]
  (.preventDefault event)
  (let [{:keys [user password]} @login-state]
    (if-not (and (seq user) (seq password))
      (swap! login-state assoc :error "Begge feltene må fylles ut")
      (do
        (swap! login-state assoc :loading? true)
        (POST "login" {:params        {:email    user
                                       :password password}
                       :handler       (fn [user]
                                        (session/put! :user user)
                                        (session/put! :selected-menu-item :feed)
                                        (dispatch! "/feed"))
                       :error-handler (fn [response]
                                        (let [r (:response response)
                                              update-payment? (:update-payment? r)]
                                          (swap! login-state assoc :loading? false)
                                          (swap! login-state assoc :error (:message (:response response)))
                                          (when update-payment?
                                            (swap! login-state assoc :show-payment? true))))})))))

(defn login []
  (let [login-state (atom {:user "" :password "" :error "" :loading? false :show-payment? false})]
    (fn []
      [:form
       (when (:show-payment? @login-state)
         [:div.pure-g
          [:a.pure-u-1.button.button-primary
           {:href (str "http://sites.fastspring.com/roebucksoftware/product/movementsessionsubscription"
                       "?referrer="
                       (:user @login-state))} "Purchase subscription"]])
       [:div.pure-g {:style {:margin-bottom 5 :font-size "150%"}}
        [:input {:type        "text"
                 :className   "pure-u-1"
                 :name        "email"
                 :placeholder "email"
                 :on-change   #(swap! login-state assoc :user (-> % .-target .-value))
                 :value       (:user @login-state)}]]
       [:div.pure-g {:style {:margin-bottom 5 :font-size "150%"}}
        [:input {:type        "password"
                 :className   "pure-u-1"
                 :name        "password"
                 :placeholder "password"
                 :on-change   #(swap! login-state assoc :password (-> % .-target .-value))
                 :value       (:password @login-state)}]]
       (when-let [e (:error @login-state)]
         [:div.pure-g {:style {:font-size "150%"}}
          [:div.pure-u-1.notice.center e]])
       [:div.pure-g
        [:button.pure-u-1.button.button-primary
         {:disabled   (when (:loading? @login-state) "disabled")
          :onClick    #(login-handler % login-state)
          :onTouchEnd #(login-handler % login-state)}
         (if (:loading? @login-state) "Logger inn..." "Logg inn")]]])))

(defn login-page []
  [:div
   [:div.content.is-center {:style {:margin-top 200}}
    [:div.pure-g
     [:div.pure-u.pure-u-md-1-5]
     [:div.pure-u-1.pure-u-md-3-5
      [login]]
     [:div.pure-u.pure-u-md-1-5]]]])