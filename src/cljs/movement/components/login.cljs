(ns movement.components.login
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [movement.util :refer [GET POST text-input get-templates get-all-categories]]
            [secretary.core :include-macros true :refer [dispatch!]]
            [reagent.session :as session]))

(defn login []
  (let [email (atom "")
        password (atom "")
        error (atom "")
        loading? (atom false)]
    (fn []
      [:div
       [:label {:for "email" :alt "Enter email"} "Email"]
       [text-input email {:class (when @loading? "disabled")
                          :type  "email"
                          :name  "email"}]
       [:label {:for "password" :alt "Enter password" :placeholder "Password"} "Password"]
       [text-input password {:class (when @loading? "disabled")
                             :type  "password"
                             :name  "password"}]


       (when-let [e @error]
         [:div.notice e])
       [:button.btn.btn-primary {:class    (when @loading? "disabled")
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
                                                                                                  (get-templates)
                                                                                                  (get-all-categories)
                                                                                                  (dispatch! "/generator")
                                                                                                  (print @session/state)))
                                                              :error-handler   (fn [response] (do
                                                                                                (reset! loading? false)
                                                                                                (println (str "error! " response))))})))}
        (if @loading? "Logging in..." "Log In")]])))