(ns movement.login
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [movement.util :refer [POST text-input]]))

(defn login! [user password error]
  (cond
    (empty? user) (reset! error "User name required")
    (empty? password) (reset! error "Password required")
    :else (POST "/login" {:headers {:username user
                                    :password password}
                          :handler #(if (= (:result %) "ok")
                                     (do
                                       (session/remove! :login)
                                       (session/put! :user-logged-in? true))
                                     (reset! error (:error %)))})))

(defn login-form []
  (let [user (atom nil)
        password (atom nil)
        error (atom nil)]
    (fn []
      [:div.login-form
       [text-input user {:placeholder "user"}]
       [text-input password {:type "password" :placeholder "password"}]]
      [:span.button.login-button {:on-click #(session/remove! :login)} "cancel"
       [:span.button.login-button {:on-click #(login! @user @password error)} "login"]
       (if-let [error @error]
         [:div.error error])])))