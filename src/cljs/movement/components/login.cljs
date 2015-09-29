(ns movement.components.login
  (:require [reagent.core :refer [atom]]
            [movement.util :refer [GET POST text-input]]
            [reagent.session :as session]
            [secretary.core :include-macros true :refer [dispatch!]]
            [cljs.core.async :as async :refer [>! <! take!]]))

(defn valid-email? [email]
  true)

(defn login []
  (let [email (atom "")
        password (atom "")
        error (atom "")
        loading? (atom false)]
    (fn []
      [:div
       [:label {:for "email" :alt "Enter email"} "Email"]
       [text-input email {:class    (when @loading? "disabled")
                          :type     "email"
                          :name     "email"}]
       [:label {:for "password" :alt "Enter password" :placeholder "Password"} "Password"]
       [text-input password {:class    (when @loading? "disabled")
                             :type     "password"
                             :name     "password"}]


       (when-let [e @error]
         [:div.notice e])
       [:button.btn.btn-primary {:class    (when @loading? "disabled")
                                 :on-click #(if-not (and (seq @email) (seq @password))
                                             (reset! error "Both fields are required.")
                                             (POST "login" {:format          :edn
                                                            :response-format :edn
                                                            :params          {:username @email
                                                                              :password @password}
                                                            :handler         (fn [response] (do (println response) #_(session/put! :user-logged-in? true) #_(dispatch! "/generator")))
                                                            :error-handler   (fn [response] (println (str "error! " response)))}))}
        (if @loading? "Logging in..." "Log In")]])))