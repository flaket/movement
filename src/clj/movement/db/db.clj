(ns movement.db.db
  (:require [clojure.core.async :as async :refer [<!! <! go]]
            [hildebrand.core :as h]
            [clojure.set :as set]
            [buddy.hashers :as hashers]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import (java.util UUID Date)
           datomic.Util))

(def creds {:access-key "..."
            :secret-key "..."
            :endpoint   "http://localhost:8080"})

#_(let [tables (<!! (h/list-tables! creds {}))]
    (doseq [i (range (count tables))]
      (h/delete-table! creds (get tables i))))

#_(h/delete-table! creds :users)                   ; :users :movements :sessions :templates :user-movements

#_(def tables [{:table      :movements
                :throughput {:read 1 :write 1}
                :attrs      {:name :string}
                :keys       [:name]}

               {:table      :users
                :throughput {:read 1 :write 1}
                :attrs      {:email :string :activation-id :string}
                :keys       [:email]
                :indexes    {:global [{:name       :user-by-activation-id
                                       :keys       [:activation-id]
                                       :project    [:keys-only]
                                       :throughput {:read 1 :write 1}}]}}

               {:table      :templates
                :throughput {:read 1 :write 1}
                :attrs      {:title :string :creator :string}
                :keys       [:title :creator]}

               {:table      :sessions
                :throughput {:read 1 :write 1}
                :attrs      {:url :string :email :string}
                :keys       [:url]
                :indexes    {:global [{:name       :session-by-email
                                       :keys       [:email]
                                       :project    [:keys-only]
                                       :throughput {:read 1 :write 1}}]}}

               {:table      :user-movements
                :throughput {:read 1 :write 1}
                :attrs      {:email :string :movement-name :string}
                :keys       [:email :movement-name]
                :indexes    {:global [{:name       :movements-by-user
                                       :keys       [:email]
                                       :throughput {:read 1 :write 1}
                                       :project    [:all]}]}}])

#_(defn create-tables! [tables]
    (doseq [i (range (count tables))]
      (let [c (h/create-table! creds (get tables i))]
        (pr (<!! c)))))

#_(h/create-table! creds {:table      :user-movements
                          :throughput {:read 1 :write 1}
                          :attrs      {:email :string :movement-name :string}
                          :keys       [:email :movement-name]
                          :indexes    {:global [{:name       :movements-by-user
                                                 :keys       [:email]
                                                 :throughput {:read 1 :write 1}
                                                 :project    [:all]}]}})

#_(h/delete-table! creds :user-movements)

#_(let [c (h/list-tables! creds {})] (<!! c))

#_(h/put-item! creds :user-movements {:email "fsa" :movement-name "Pull Up" :zone 2})

#_(let [c (h/query! creds :user-movements
                    {:email [:= "a"]}
                    {:index :movements-by-user})]
    (<!! c))

;; template: title description background part
;; parts: slot(s)
;; slot: category natural movement rep distance duration set weight rest
#_(let [t {:title       "Test"
         :creator     "Andreas"
         :description "test"
         :background  "test"
         :parts        [[{:category   #{:natural :balance}
                         :repetition [4 8 12] :distance [5 12 20] :duration 30 :set 4}
                        {:category   #{:natural :climb}
                         :repetition [2 4 6] :set 4}]]}]
  (h/put-item! creds :templates t))

;; stored-sessions: email url template date
;; comment time part->movement(rep set duration distance weight rest)
;; time-taken image location

#_(let [c (h/list-tables! creds {})] (<!! c))
#_(let [c (h/describe-table! creds :user-movements)] (<!! c))

#_(let [balancing (first (Util/readAll (io/reader (io/resource "data/movements/balancing.edn"))))
        climbing (first (Util/readAll (io/reader (io/resource "data/movements/climbing.edn"))))
        all-movements (vec (concat balancing climbing))]
    (map #(h/put-item! creds :movements %) all-movements))

#_(let [user {:email             "andflak@gmail.com"
              :password          (hashers/encrypt "pw")
              :sign-up-timestamp (.getTime (Date.))
              :activation-id     (str (UUID/randomUUID))}]
    (h/put-item! creds :users user))

; look-up on known item
#_(let [c (h/get-item! creds :templates
                       {:title "Test"
                        :creator "Andreas"
                        })] (<!! c))

#_(let [c (h/scan-count! creds :user-movements)] (<!! c))

; query looks up values given keys
#_(let [c (h/query! creds :movements {:name "Pull Up"} {:filter [:contains [:name] "Pull"]})]
    (<!! c))

; scan is query without key constraint
#_(let [c (h/scan! creds :user-movements {:filter [:contains [:email] "andflak@gmail.com"]})]
    (<!! c))

#_(let [c (h/scan! creds :user-movements {})]
    (<!! c))

#_(let [c (h/get-item! creds :movement {:name "Dip"})
        previous (first (shuffle (:previous (<!! c))))
        c (h/get-item! creds :movement {:name previous})]
    (<!! c))

#_(<!! (h/get-item! creds :movements {:name "Balancing Walk"}))

#_(h/put-item! creds :user-movements {:email "andflak@gmail.com" :name "Pull Up" :zone 1})

#_(keyword (str/replace (str/lower-case name) " " "-"))

;; ----------------------------------------------------

(defn find-user [email]
  (let [c (h/get-item! creds :users {:email email})] (<!! c)))
#_(find-user "andflak@gmail.com")

(defn item-by-id [id]
  (->>
    (h/query! creds :users {:activation-id [:= id]} {:index :user-by-activation-id})
    <!!
    first))

;;---------- get data ----------

(defn movements []
  (let [c (h/scan! creds :movements {})
        x (map :name (<!! c))]
    x))
#_(movements)

(defn categories []
  (let [c (h/scan! creds :movements {})
        movements (<!! c)
        x (apply set/union (map :category movements))]
    (seq x)))
#_(categories)

(defn movement [name]
  (let [c (h/get-item! creds :movements {:name name})]
    (<!! c)))
#_(movement "Pull Up")

(defn movements-from-category [n category]
  "todo: accept several categories"
  (let [c (h/scan! creds :movements {:filter [:contains [:category] category]})
        movements (take n (sort-by :name (<!! c)))]
    movements))
#_(movements-from-category 3 :pushing)

(defn template [title creator]
  (<!! (h/get-item! creds :templates {:title   title :creator creator})))
#_(template "Naturlige Bevegelser 2" "Andreas")

(defn create-session [email session-type]
  (let [session {:description "hellu"
                 :template    {:creator "Andreas" :title "Test"}
                 :parts       [[
                                {:name     "Balancing Backward Walk"
                                 :slot-category #{:balance :walk :beam :balancing-locomotion :natural}
                                 :measurement :distance
                                 :previous ["Balancing Lateral Walk"]
                                 :distance 10
                                 :set 4}
                                {:name     "Toes To Bar"
                                 :rep      5
                                 :set      4
                                 :category #{:natural :climb}
                                 :measurement :repetitions
                                 :previous ["Hanging Knee Tuck"] :next ["Hanging Side Foot Lift"]}

                                ]
                               [
                                {:name     "Balancing Backward Walk"
                                 :slot-category #{:balance :walk :beam :balancing-locomotion :natural}
                                 :measurement :distance
                                 :previous ["Balancing Lateral Walk"]
                                 :distance 10
                                 :set 4}
                                {:name     "Toes To Bar"
                                 :rep      5
                                 :set      4
                                 :category #{:natural :climb}
                                 :measurement :repetitions
                                 :previous ["Hanging Knee Tuck"] :next ["Hanging Side Foot Lift"]}

                                ]]}]
    session
    #_(<!! (h/get-item! creds :templates {:title title :creator creator}))))

;;---------- add/update data ----------

(defn add-user! [email password activation-id]
  (let [user {:email               email
              :password            (hashers/encrypt password)
              :sign-up-timestamp   (.getTime (Date.))
              :activation-id       activation-id
              :activated?          false
              :valid-subscription? false
              :badges              #{:newbie}
              :settings            {:receive-push-notifications? true
                                    :goals                       []
                                    :priorities                  []}}]
    (h/put-item! creds :users user)))
#_(add-user! "andflak@gmail.com" "pw" "1")

(defn add-session! [user session]
  (let [session (assoc session :url (str (UUID/randomUUID))
                               :email (:email user))]
    (h/put-item! creds :sessions session)))

(defn activate-user! [id]
  (let [email (:email (item-by-id id))]
    (h/update-item! creds :users {:email email}
                    {:activated?    [:set true]
                     :activation-id [:remove]})))
#_(activate-user! "1")

(defn add-movement! [email movement]
  ; todo: filter; don't add if exists
  (h/put-item! creds :user-movements
               {:email email :movement-name movement :zone 1}))

(defn update-subscription! [email value]
  (h/update-item! creds :users {:email email}
                  {:valid-subscription? [:set value]}))
#_(update-subscription! "andflak@gmail.com" true)

(defn update-name!
  [email value]
  (h/update-item! creds :users {:email email} {:name [:set value]})
  "Username changed successfully!")
#_(update-name! "andflak@gmail.com" "Andreas")

(defn update-password!
  [email password]
  (let [value (hashers/encrypt password)]
    (h/update-item! creds :users {:email email} {:password [:set value]})
    "Password changed successfully!"))

(defn update-zone! [email movement zone]
  (h/put-item! creds :user-movements
               {:email email :movement-name movement :zone zone}))