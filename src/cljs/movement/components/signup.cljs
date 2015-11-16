(ns movement.components.signup
  (:require [reagent.core :refer [atom]]
            [movement.util :refer [POST text-input get-templates get-all-categories get-all-movements]]
            [reagent.session :as session]
            [secretary.core :include-macros true :refer [dispatch!]]))

(defn login-after-signup [email password]
  (POST "login" {:params        {:username email
                                  :password password}
                  :handler       (fn [response] (do
                                                  (session/put! :token (:token response))
                                                  (session/put! :user (:user response))
                                                  (session/put! :m-counter (atom 0))
                                                  (get-templates)
                                                  (get-all-categories)
                                                  (get-all-movements)
                                                  (dispatch! "/generator")))
                  :error-handler (fn [response] (println (str "error! " response)))}))

(defn sign-up []
  (let [email (atom "")
        password (atom "")
        error (atom "")
        loading? (atom false)]
    (fn []
      [:div
       [:span.pure-u [:i.fa.fa-envelope-o.fa-fw]]
       [text-input email {:class    (when @loading? "disabled")
                          :type     "email"
                          :name     "email"
                          :placeholder "Your Email"}]
       [:span.pure-u [:i.fa.fa-key.fa-fw]]
       [text-input password {:class    (when @loading? "disabled")
                         :type     "password"
                         :name     "password"
                             :placeholder "Your Password"    }]
       (when-let [e @error]
         [:div.notice e])
       [:button.pure-button {:class    (when @loading? "disabled")
                             :on-click #(if-not (and (seq @email) (seq @password))
                                         (reset! error "Both fields are required.")
                                         (POST "signup" {:params        {:username @email
                                                                         :password @password}
                                                         :handler       (fn [response] (login-after-signup @email @password))
                                                         :error-handler (fn [response] (println (str "error! " response)))}))}
        (if @loading? "Signing up..." "Sign Up Free")]])))