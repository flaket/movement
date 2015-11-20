(ns movement.activation
  (:require [postal.core :refer [send-message]]
            [taoensso.timbre :refer [info error]]))

(defn generate-activation-id []
  (str (java.util.UUID/randomUUID)))

(defn send-activation-email [email activation-id]
  (try
    (send-message
      ^{:host "mail.privateemail.com"
        :user "admin@movementsession.com"
        :pass "13movementsession13"
        :ssl :yes}
      {:from    "admin@movementsession.com"
       :to      email
       :subject "Movementsession activation link"
       :body    (str "Click the following activation link to verify your email address:\n "
                     "http://localhost:8000/activate/" activation-id)})
    (info "sent activation email to: " email)
    true
       (catch Exception e
         (error e "could not send email!\n"))))