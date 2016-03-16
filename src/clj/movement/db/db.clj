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

#_(h/delete-table! creds :users)

(let [c (h/create-table! creds {:table      :movements
                                :throughput {:read 1 :write 1}
                                :attrs      {:name :string}
                                :keys       [:name]})] (<!! c))
(let [c (h/create-table! creds {:table      :users
                                :throughput {:read 1 :write 1}
                                :attrs      {:email :string :activation-id :string}
                                :keys       [:email]
                                :indexes    {:global [{:name       :user-by-activation-id
                                                       :keys       [:activation-id]
                                                       :project    [:keys-only]
                                                       :throughput {:read 1 :write 1}}]}})] (<!! c))
(let [c (h/create-table! creds {:table      :templates
                                :throughput {:read 1 :write 1}
                                :attrs      {:title :string :creator :string}
                                :keys       [:title :creator]})] (<!! c))
;; template: title description background part
;; part: slot(s)
;; slot: category natural movement rep distance duration set weight rest

(let [c (h/create-table! creds {:table      :sessions
                                :throughput {:read 1 :write 1}
                                :attrs      {:url :string :email :string}
                                :keys       [:url :email]})] (<!! c))
;; stored-sessions: email url template date
;; comment time part->movement(rep set duration distance weight rest)
;; time-taken image location

#_(let [c (h/list-tables! creds {})] (<!! c))
#_(let [c (h/describe-table! creds :users)] (<!! c))

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
            :email               "affa"
            :password            (hashers/encrypt "pw")
            :sign-up-timestamp   (.getTime (Date.))
            :activation-id       (str (UUID/randomUUID))
            }]
  (h/put-item! creds :users user))

; look-up on known item
(let [c (h/get-item!! creds :users {:email "affa"})] c)

(let [c (h/scan-count! creds :user)] (<!! c))

; query looks up values given keys
(let [c (h/query! creds :movements {:name "Pull Up"} {:filter [:contains [:name] "Pull"]})]
  (<!! c))

; scan is query without key constraint
(let [c (h/scan! creds :movement {:filter [:contains [:category] :pushing]})]
  (<!! c))

(let [c (h/get-item! creds :movement {:name "Dip"})
      previous (first (shuffle (:previous (<!! c))))
      c (h/get-item! creds :movement {:name previous})]
  (<!! c))

;; ----------------------------------------------------

(keyword (str/replace (str/lower-case name) " " "-"))

(defn item-by-id [id]
  (->>
    (h/query! creds :users {:activation-id [:= id]} {:index :user-by-activation-id})
    <!!
    first))

(defn activate-user! [id]
  (let [email (:email (item-by-id id))]
    (h/update-item! creds :users {:email email}
                    {:activated? [:init true]
                     :activation-id [:remove]
                     :movements  [:init []]
                     :preferences [:init [:natural]]
                     :goals      [:init []]
                     :badges     [:init #{:newbie}]
                     :settings   [:init {:push-notifications true}]})))

(defn add-movement! [email movement]
  (let [kw-m (keyword (str/replace (str/lower-case movement) " " "-"))
        m (hash-map kw-m 1)]
    (h/update-item! creds :users {:email email}
                    {:movements [:concat [movement]]}
                    {:when [:not [:exists [:movements kw-m]]]})))

(defn update-subscription! [email value]
  (h/update-item! creds :users {:email email}
                  {:valid-subscription? [:init value]}))

(defn update-name! [email value]
  (h/update-item! creds :users {:email email}
                  {:name [:init value]}))

(defn all-movement-names []
  (let [c (h/scan! creds :movements {})
        x (map :name (<!! c))]
    x))
#_(all-movement-names)

(defn all-category-names []
  (let [c (h/scan! creds :movements {})
        movements (<!! c)
        x (apply set/union (map :category movements))]
    (seq x)))
#_(all-category-names)

(defn entity-by-movement-name [name]
  (let [c (h/get-item! creds :movements {:name name})]
    (<!! c)))
#_(entity-by-movement-name "Pull Up")

(defn n-movements-from-category [n category]
  "todo: accept several categories"
  (let [c (h/scan! creds :movements {:filter [:contains [:category] category]})
        movements (take n (sort-by :name (<!! c)))]
    movements))
#_(n-movements-from-category 3 :pushing)

(defn item-by-activation-id [id]
  (let [c (h/get-item! creds :movements {:activation-id id})]
    (<!! c)))

