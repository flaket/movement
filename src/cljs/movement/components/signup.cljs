(ns movement.components.signup
  (:require [reagent.core :refer [atom]]))

(defn valid-email? [email]
  true)

(defn sign-up []
  (let [name (atom "")
        email (atom "")
        password (atom "")
        error (atom "")
        loading? (atom false)]
    (fn []
      [:form.sign-up
       [:input {:value email
                :class (when @loading? "disabled")
                :type "email"
                :name "email"
                :required true
                :on-change #()}]
       [:label {:for "email" :alt "Enter email" :placeholder "Email"}]
       [:input {:value name
                :required true
                :class (when @loading? "disabled")
                :type "text"
                :name "name"
                :on-change #()}]
       [:label {:for "name" :alt "Enter name" :placeholder "Name"}]
       [:input {:value password
                :required true
                :class (when @loading? "disabled")
                :type "password"
                :name "password"
                :on-change #()}]
       [:label {:for "password" :alt "Enter password" :placeholder "Password"}]

       (when-let [e @error]
         [:div.notice e])
       [:button.btn.btn-primary {:class    (when @loading? "disabled")
                                 :on-click #(do (cond
                                                  (not (and (seq name) (seq email) (seq password)))
                                                  (swap! error "All fields are required.")

                                                  (not (valid-email? email))
                                                  (swap! error "Please enter a valid email address.")

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
        (if loading? "Signing up..." "Sign Up")]])))