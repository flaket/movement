(ns movement.activation
  (:require [postal.core :refer [send-message]]
            [taoensso.timbre :refer [info error]]
            [environ.core :refer [env]]))

(defn generate-activation-id []
  (str (java.util.UUID/randomUUID)))

(def url (if (env :dev?) "http://localhost:8000/activate/"
                          "http://movementsession.com/activate/"))

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
       :body    (str "Thanks for signing up for MovementSession! Please click the link below to confirm your email address.
                     \n "
                     url activation-id
                     "\n\n"
                     "If you did not recently sign up for an account at movementsession.com, you can simply ignore this email.")})
    (info "sent activation email to: " email)
    true
       (catch Exception e
         (error e "could not send email!\n"))))