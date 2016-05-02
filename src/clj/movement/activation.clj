(ns movement.activation
  (:require [postal.core :refer [send-message]]
            [taoensso.timbre :refer [info error]]
            [environ.core :refer [env]]
            [buddy.hashers :as hashers])
  (:import (java.util UUID)))

(defn generate-activation-id []
  (str (UUID/randomUUID)))

(def url (if (env :dev?) "http://localhost:8000/activate/"
                          "http://mumrik.no/activate/"))

(defn send-email [to-email subject body]
  (try
    (send-message
      ^{:host "smtpin.isphuset.no"
        :port 587
        :user "andreas@mumrik.no"
        :pass "mumrikM9n8b7v6"
        ;:ssl :yes
        }
      {:from    "andreas@mumrik.no"
       :to      to-email
       :subject subject
       :body    body})
    (info "sent email to: " to-email)
    true
    (catch Exception e
      (error e "could not send email!\n"))))

(defn send-activation-email [email username activation-id]
  (try
    (send-message
      ^{:host "smtpin.isphuset.no"
        :port 587
        :user "andreas@mumrik.no"
        :pass "mumrikM9n8b7v6"
        ;:ssl :yes
        }
      {:from    "andreas@mumrik.no"
       :to      email
       :subject "Bekreftelseslenke fra mumrik.no"
       :body    (str
                  "Hei " username
                  "\nTakk for at du vil prøve ut Mumrik! Nå gjenstår det bare å trykke på lenken under for å bekrefte at du fikk denne eposten.\n"
                  url activation-id
                  "\n\n"
                  "Hvis du ikke registrerte deg på mumrik.no nylig kan du se bort i fra denne eposten.")})
    (info "sent activation email to: " email)
    true
       (catch Exception e
         (error e "could not send email!\n"))))