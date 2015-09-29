(ns movement.components.signup
  (:require [reagent.core :refer [atom]]
            [movement.util :refer [POST text-input]]))

(defn valid-email? [email]
  true)

(defn sign-up []
  (let [name (atom "")
        email (atom "")
        password (atom "")
        error (atom "")
        loading? (atom false)]
    (fn []
      [:div
       [:label {:for "email" :alt "Enter email" :placeholder "Email"} "Email"]
       [text-input email {:class    (when @loading? "disabled")
                          :type     "email"
                          :name     "email"}]

       [:label {:for "name" :alt "Enter name" :placeholder "Name"} "Name"]
       [text-input name {:class    (when @loading? "disabled")
                          :type     "text"
                          :name     "name"}]
       [:label {:for "password" :alt "Enter password" :placeholder "Password"} "Password"]
       [text-input password {:class    (when @loading? "disabled")
                         :type     "password"
                         :name     "password"}]
       (when-let [e @error]
         [:div.notice e])
       [:button.btn.btn-primary {:class    (when @loading? "disabled")
                                 :on-click #(do (cond
                                                  (not (and (seq name) (seq email) (seq password)))
                                                  (reset! error "All fields are required.")

                                                  (not (valid-email? email))
                                                  (reset! error "Please enter a valid email address.")

                                                  :else
                                                  (do
                                                    (swap! loading? true)
                                                    ; do ajax call
                                                    ; if success returned, update clientside state
                                                    ;else
                                                    #_(do
                                                      (swap! loading? false)
                                                      (swap! error "Sorry! We experienced an error trying to sign you up")))

                                                  ))}
        (if @loading? "Signing up..." "Sign Up")]])))