(ns movement.components.signup
  (:require [reagent.core :refer [atom]]
            [movement.util :refer [POST text-input]]))

(defn sign-up []
  (let [email (atom "")
        password (atom "")
        error (atom "")
        loading? (atom false)]
    (fn []
      [:div
       [:label {:for "email" :alt "Enter email" :placeholder "Email"} "Email"]
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
                                             (reset! error "All fields are required.")
                                             (POST "signup" {:format          :edn
                                                            :response-format :edn
                                                            :params          {:username @email
                                                                              :password @password}
                                                            :handler         (fn [response] (do (println response)
                                                                                                #_(session/put! :user-logged-in? true)
                                                                                                #_(get-templates)
                                                                                                #_(dispatch! "/generator")))
                                                            :error-handler   (fn [response] (println (str "error! " response)))}))}
        (if @loading? "Signing up..." "Sign Up")]])))