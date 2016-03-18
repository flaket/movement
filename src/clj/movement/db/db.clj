(ns movement.db.db
  (:require [clojure.core.async :as async :refer [<!! <! go]]
            [hildebrand.core :as h]
            [clojure.set :as set]
            [buddy.hashers :as hashers]
            [clojure.string :as str])
  (:import (java.util UUID Date)))

(def creds {:access-key "..."
            :secret-key "..."
            :endpoint   "http://localhost:8080"})

#_(let [tables (<!! (h/list-tables! creds {}))]
  (doseq [i (range (count tables))]
    (h/delete-table! creds (get tables i))))

#_(h/delete-table! creds :user-movements) ; :users :movements :sessions :templates :user-movements

(def tables [{:table      :movements
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
              :attrs      {:email :string :name :string}
              :keys       [:email :name]
              :indexes    {:global [{:name       :movements-by-user
                                     :keys       [:email]
                                     :project    [:keys-only]
                                     :throughput {:read 1 :write 1}}]}}])

(defn create-tables! [tables]
  (doseq [i (range (count tables))]
    (let [c (h/create-table! creds (get tables i))]
      (pr (<!! c)))))

(h/create-table! creds {:table      :user-movements
                        :throughput {:read 1 :write 1}
                        :attrs      {:email :string :name :string}
                        :keys       [:email :name]
                        :indexes    {:global [{:name       :movements-by-user
                                               :keys       [:email]
                                               :project    [:keys-only]
                                               :throughput {:read 1 :write 1}}]}})

;; template: title description background part
;; part: slot(s)
;; slot: category natural movement rep distance duration set weight rest


;; stored-sessions: email url template date
;; comment time part->movement(rep set duration distance weight rest)
;; time-taken image location

#_(let [c (h/list-tables! creds {})] (<!! c))
#_(let [c (h/describe-table! creds :user-movements)] (<!! c))

#_(let [movements [
                   {:name     "Negative Pull Up" :previous [] :next ["Pull Up"]
                    :category #{:climbing :natural :pulling :bent-arm-strength} :measurement :repetition}
                   {:name     "Jumping Pull Up" :previous [] :next ["Pull Up"]
                    :category #{:climbing :natural :pulling :bent-arm-strength} :measurement :repetition}
                   {:name     "Pull Up" :previous ["Negative Pull Up" "Jumping Pull Up"] :next ["Pull Up Reach"]
                    :category #{:climbing :natural :pulling :bent-arm-strength} :measurement :repetition}
                   {:name     "Pull Up Reach" :previous ["Pull Up"] :next []
                    :category #{:climbing :natural :pulling :bent-arm-strength} :measurement :repetition}

                   {:name     "Negative Dip" :previous [] :next ["Dip"]
                    :category #{:climbing :natural :dip :pushing :bent-arm-strength} :measurement :repetition}
                   {:name     "Jumping Dip" :previous [] :next ["Dip"]
                    :category #{:climbing :natural :dip :pushing :bent-arm-strength} :measurement :repetition}
                   {:name     "Dip" :previous ["Negative Dip" "Jumping Dip"] :next []
                    :category #{:climbing :natural :dip :pushing :bent-arm-strength} :measurement :repetition}
                   ]]
    (map #(h/put-item! creds :movements %) movements))

(let [user {
            :email             "andreas"
            :password          (hashers/encrypt "pw")
            :sign-up-timestamp (.getTime (Date.))
            :activation-id     (str (UUID/randomUUID))
            }]
  (h/put-item! creds :users user))

; look-up on known item
(let [c (h/get-item! creds :user-movements
                     {:email [:= "andflak@gmail.com"]}
                     {:index :movements-by-user})] (<!! c))

(let [c (h/scan-count! creds :user-movements)] (<!! c))

; query looks up values given keys
(let [c (h/query! creds :movements {:name "Pull Up"} {:filter [:contains [:name] "Pull"]})]
  (<!! c))

; scan is query without key constraint
(let [c (h/scan! creds :user-movements {:filter [:contains [:email] "andflak@gmail.com"]})]
  (<!! c))

(let [c (h/scan! creds :user-movements {})]
  (<!! c))

(let [c (h/get-item! creds :movement {:name "Dip"})
      previous (first (shuffle (:previous (<!! c))))
      c (h/get-item! creds :movement {:name previous})]
  (<!! c))

(h/put-item! creds :user-movements {:email "andflak@gmail.com"
                                    :name "Pull Up"
                                    :zone 1})

;; ----------------------------------------------------

(keyword (str/replace (str/lower-case name) " " "-"))

(defn find-user [email]
  (let [c (h/get-item! creds :users {:email email})] (<!! c)))

(defn item-by-id [id]
  (->>
    (h/query! creds :users {:activation-id [:= id]} {:index :user-by-activation-id})
    <!!
    first))

(defn add-user! [email password activation-id]
  (let [user {:email             email
              :password          (hashers/encrypt password)
              :sign-up-timestamp (.getTime (Date.))
              :activation-id     activation-id
              }]
    (h/put-item! creds :users user)))

(defn add-session! [user session]
  (let [session (assoc session :url (str (java.util.UUID/randomUUID))
                               :email (:email user))]
    (h/put-item! creds :sessions session)))

(defn add-new-movements! [user session]
  #_(let [movements session]
    (h/update-item! creds :users {:email email}
                    {:movements  [:concat []]})))

(defn update-zone! [email movement zone]
  (let [user (find-user email)
        session (assoc session :url (str (java.util.UUID/randomUUID))
                               :email (:email user))]
    (h/put-item! creds :sessions session)))

(defn activate-user! [id]
  (let [email (:email (item-by-id id))]
    (h/update-item! creds :users {:email email}
                    {:activated?    [:init true]
                     :activation-id [:remove]
                     :movements     [:init []]
                     :preferences   [:init [:natural]]
                     :goals         [:init []]
                     :badges        [:init #{:newbie}]
                     :settings      [:init {:push-notifications true}]})))

(defn add-movement! [email movement]
  (let [kw-m (keyword (str/replace (str/lower-case movement) " " "-"))
        m (hash-map kw-m 1)]
    (h/update-item! creds :users {:email email}
                    {:movements [:concat [movement]]}
                    {:when [:not [:exists [:movements kw-m]]]})))

(defn update-subscription! [email value]
  (h/update-item! creds :users {:email email}
                  {:valid-subscription? [:set value]}))

(defn update-name!
  "Sets the :name of a user."
  [email value]
  (h/update-item! creds :users {:email email} {:name [:set value]}))

(defn update-password!
  "Sets a new :password of a user."
  [email password]
  (let [value (hashers/encrypt password)]
    (h/update-item! creds :users {:email email} {:password [:set value]})))

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

(defn user-by-activation-id [id]
  (let [c (h/get-item! creds :users {:activation-id id})]
    (<!! c)))