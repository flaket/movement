(ns movement.components.login
  (:require [reagent.core :refer [atom]]
            [movement.util :refer [POST text-input]]
            [reagent.session :as session]
            [secretary.core :include-macros true :refer [dispatch!]]))

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
                          :name     "email"
                          :placeholder "Email"}]
       [:label {:for "password" :alt "Enter password" :placeholder "Password"} "Password"]
       [text-input password {:class    (when @loading? "disabled")
                             :type     "password"
                             :name     "password"
                             :placeholder "Password"}]


       (when-let [e @error]
         [:div.notice e])
       [:button.btn.btn-primary {:class    (when @loading? "disabled")
                                 :on-click #(do
                                             (if-not (and (seq @email) (seq @password))
                                               (swap! error "Both fields are required.")
                                               (POST "login" {:params  {:username @email
                                                                        :password @password}
                                                              :handler (fn [response]
                                                                         (do
                                                                           (println response)
                                                                           (session/put! :user-logged-in? true)
                                                                           (dispatch! "/generator")))
                                                              :error-handler
                                                                       (fn [response]
                                                                         (println response))}))
                                             #_(cond
                                                 (not (and (seq @email) (seq @password))) (swap! error "Both fields are required.")
                                                 (not (valid-email? @email)) (swap! error "Please enter a valid email address.")
                                                 :else (do
                                                         (swap! loading? true)
                                                         ; do ajax call
                                                         ; if success returned, update clientside state
                                                         (POST "login" {:params  {:username @email
                                                                                  :password @password}
                                                                        :handler (fn [response]
                                                                                   (do
                                                                                     (println response)
                                                                                     (session/put! :user-logged-in? true)
                                                                                     (dispatch! "/generator")))
                                                                        :error-handler
                                                                                 (fn [response]
                                                                                   (println response))}))))}
        (if @loading? "Logging in..." "Log In")]])))