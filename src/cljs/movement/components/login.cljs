(ns movement.components.login
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [movement.util :refer [GET POST text-input get-all-movements get-templates get-templates-1 get-all-categories]]
            [secretary.core :include-macros true :refer [dispatch!]]
            [reagent.session :as session]))

(defn login []
  (let [email (atom "")
        password (atom "")
        error (atom "")
        loading? (atom false)]
    (fn []
      [:div
       [:label {:for "email" :alt "Enter email"} "Your Email"]
       [text-input email {:class (when @loading? "disabled")
                          :type  "email"
                          :name  "email"
                          :placeholder "Your Email"}]
       [:label {:for "password" :alt "Enter password"} "Your Password"]
       [text-input password {:class (when @loading? "disabled")
                             :type  "password"
                             :name  "password"
                             :placeholder "Your Password"}]
       (when-let [e @error]
         [:div.notice e])
       [:button.pure-button
        {:class    (when @loading? "disabled")
         :on-click #(if-not (and (seq @email) (seq @password))
                     (reset! error "Both fields are required.")
                     (do
                       (reset! loading? true)
                       (POST "login" {:format          :edn
                                      :response-format :edn
                                      :params          {:username @email
                                                        :password @password}
                                      :handler         (fn [response] (do (println response)
                                                                          (session/put! :token (:token response))
                                                                          (session/put! :user (:user response))
                                                                          (get-templates-1 (:token response))
                                                                          (get-all-categories)
                                                                          (get-all-movements)
                                                                          (dispatch! "/generator")
                                                                          (print (session/get :user))))
                                      :error-handler   (fn [response] (do
                                                                        (reset! loading? false)
                                                                        (reset! error (:message (:response response)))
                                                                        (println (str "error! " response))))})))}
        (if @loading? "Logging in..." "Log In")]])))