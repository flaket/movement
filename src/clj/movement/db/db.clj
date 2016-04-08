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

(def movements-table {:table :movements :throughput {:read 1 :write 1}
                      :attrs {:name :string} :keys [:name]})
(def user-movements-table {:table   :user-movements :throughput {:read 1 :write 1}
                           :attrs   {:user-id :string :movement-name :string} :keys [:user-id :movement-name]
                           :indexes {:global [{:name       :movements-by-user-id
                                               :keys       [:user-id]
                                               :throughput {:read 1 :write 1}
                                               :project    [:all]}]}})
(def templates-table {:table :templates :throughput {:read 1 :write 1}
                      :attrs {:title :string} :keys [:title]})
(def users-table {:table   :users :throughput {:read 1 :write 1}
                  :attrs   {:user-id :string :activation-id :string :email :string :name :string} :keys [:user-id]
                  :indexes {:global [{:name       :user-by-activation-id
                                      :keys       [:activation-id]
                                      :project    [:keys-only]
                                      :throughput {:read 1 :write 1}}
                                     {:name       :user-by-email
                                      :keys       [:email]
                                      :project    [:all]
                                      :throughput {:read 1 :write 1}}
                                     {:name       :user-by-name
                                      :keys       [:name]
                                      :project    [:all]
                                      :throughput {:read 1 :write 1}}]}})
(def sessions-table {:table   :sessions :throughput {:read 1 :write 1}
                     :attrs   {:url :string :user-id :string} :keys [:url]
                     :indexes {:global [{:name       :session-by-user-id
                                         :keys       [:user-id]
                                         :project    [:all]
                                         :throughput {:read 1 :write 1}}]}})

#_(let [c (h/list-tables! creds {})] (<!! c))
#_(let [c (h/describe-table! creds :movements)] (<!! c))
#_(h/create-table! creds sessions-table)
#_(h/delete-table! creds :sessions)
#_(let [tables (<!! (h/list-tables! creds {}))]
    (doseq [i (range (count tables))]
      (h/delete-table! creds (get tables i))))

;; template: title description background part
;; parts: slot(s)
;; slot: category natural movement rep distance duration set weight rest
#_(let [t {:title       "Test"
           :creator     "Andreas"
           :description "test"
           :background  "test"
           :parts       [[{:category   #{:natural :balance}
                           :repetition [4 8 12] :distance [5 12 20] :duration 30 :set 4}
                          {:category   #{:natural :climb}
                           :repetition [2 4 6] :set 4}]]}]
    (h/put-item! creds :templates t))

;; stored-sessions:
;; url user-id template date comment time(minutter/timer) image location tags(skrapes fra comment backend)
;; parts [[{movement-name rep set duration distance weight rest}]]

#_(let [balancing (first (Util/readAll (io/reader (io/resource "data/movements/balancing.edn"))))
        ;climbing (first (Util/readAll (io/reader (io/resource "data/movements/climbing.edn"))))
        ;all-movements (vec (concat balancing climbing))
        ]
    (map #(h/put-item! creds :movements %) balancing))

;; ----------------------------------------------------

(defn user [user-id]
  (<!! (h/get-item! creds :users {:user-id user-id})))
#_(user "b3b4196b-a131-463f-8ddf-bd174ae44d19")

(defn user-by-email [email]
  (->>
    (h/query! creds :users {:email [:= email]} {:index :user-by-email})
    <!!
    first))
#_(user-by-email "andflak@gmail.com")

(defn user-by-activation-id [id]
  (->>
    (h/query! creds :users {:activation-id [:= id]} {:index :user-by-activation-id})
    <!!
    first))

(defn user-by-name [name]
  (->>
    (h/query! creds :users {:name [:= name]} {:index :user-by-name})
    <!!))

(defn sessions-by-user-id [user-id]
  (let [
        ;sessions2 (<!! (h/scan! creds :sessions {:user-id user-id}))
        sessions (->>
                   (h/query! creds :sessions {:user-id [:= user-id]} {:index :session-by-user-id})
                   <!!)]
    sessions))
#_(sessions-by-user-id "30ed7fd8-3520-4b5c-a212-d4b2832ac02b")

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
#_(movement "Balansegang")

(defn movements-from-category [n category]
  "todo: accept several categories"
  (let [c (h/scan! creds :movements {:filter [:contains [:category] category]})
        movements (take n (shuffle (<!! c)))]
    movements))
#_(movements-from-category 1 :balance)

(defn template [title]
  (<!! (h/get-item! creds :templates {:title title})))
#_(template "Naturlige Bevegelser 2")

(defn create-session [email session-type]
  (let [session {:description "hellu"
                 :template    "template-title-1"
                 :parts       [[
                                {:name          "Balansere"
                                 :image         "balancing-walk.png"
                                 :slot-category #{:balance :walk :beam :balancing-locomotion :natural}
                                 :measurement   :distance
                                 :next      ["Balansere sideveis"]
                                 :distance      10
                                 :set           4}
                                {:name        "Tærne til stanga"
                                 :image       "toes-to-bar.png"
                                 :rep         5
                                 :set         4
                                 :slot-category    #{:natural :climb}
                                 :measurement :repetitions
                                 :previous    ["Hengende kneløft"] :next ["Hengende sideveis fotløft"]}

                                ]
                               [
                                {:name          "Balansere baklengs"
                                 :image         "balancing-backward-walk.png"
                                 :slot-category #{:balance :walk :beam :balancing-locomotion :natural}
                                 :measurement   :distance
                                 :previous      ["Balansere sideveis"]
                                 :distance      10
                                 :set           4}
                                {:name        "Tærne til stanga"
                                 :image         "toes-to-bar.png"
                                 :rep         5
                                 :set         4
                                 :slot-category    #{:natural :climb}
                                 :measurement :repetitions
                                 :previous    ["Hengende kneløft"] :next ["Hengende sideveis fotløft"]}

                                ]]}
        session-2 {:template "template-title-2"
                   :description "Her er hva du skal gjøre!"
                   :parts [[{:name          "Balansere baklengs"
                             :image         "balancing-backward-walk.png"
                             :slot-category #{:balance :walk :beam :balancing-locomotion :natural}
                             :measurement   :distance
                             :previous      ["Balancing Lateral Walk"]
                             :distance      10
                             :set           4}]]}
        ]
    (case session-type
      "Naturlig bevegelse" session-2
      "Styrke" session
      {:parts [[]]})
    #_(<!! (h/get-item! creds :templates {:title title :creator creator}))))

;;---------- add/update data ----------

(defn add-user! [email name password activation-id]
  (let [user {:user-id                     (str (UUID/randomUUID))
              :email                       email
              :name                        name
              :password                    (hashers/encrypt password)
              :sign-up-timestamp           (.getTime (Date.))
              :activation-id               activation-id
              :activated?                  false
              :valid-subscription?         false
              :receive-push-notifications? true
              :follows                     []
              :badges                      []
              :goals                       []
              :priorities                  []}]
    (h/put-item! creds :users user)))
#_(add-user! "andflak@gmail.com" "andreas" "pw" (str (UUID/randomUUID)))

(defn follow-user! [user-id follow-id]
  (h/update-item! creds :users {:user-id user-id}
                  {:follows [:concat [follow-id]]}))
#_(follow-user! "andreas@roebuck.com" (str (UUID/randomUUID)))
#_(user-by-email "andreas@roebuck.com")

(defn add-badge! [user-id badge]
  (h/update-item! creds :users {:user-id user-id}
                  {:badges [:concat [badge]]}))
#_(add-badge! "andreas@roebuck.com" {:name "Newbie" :achieved-at (.getTime (Date.))})

(defn add-session! [params]
  (let [{:keys [user-id session]} params
        tags []                                             ;scan comment-felt etter hashtagger -> lag liste ["løpetur" "sol" "vårstemning"]
        session (assoc session :url (str (UUID/randomUUID))
                               :user-id user-id
                               :tags tags)]
    (.println System/out (str session))
    (h/put-item! creds :sessions session)
    :ok))

(defn activate-user! [uuid]
  (let [user (:user-id (user-by-activation-id uuid))]
    (h/update-item! creds :users {:user-id user}
                    {:activated?    [:set true]
                     :activation-id [:remove]})))
#_(activate-user! "b794271f-cbca-4118-bf92-66cc15db477e")

(defn add-movement! [user-id movement]
  ; todo: filter; don't add if exists
  (h/put-item! creds :user-movements
               {:user-id user-id :movement-name movement :zone 1}))

(defn update-subscription! [user-id value]
  (h/update-item! creds :users {:user-id user-id}
                  {:valid-subscription? [:set value]}))
#_(update-subscription! "andflak@gmail.com" true)

(defn update-name!
  [user-id value]
  (h/update-item! creds :users {:user-id user-id} {:name [:set value]})
  "Username changed successfully!")
#_(update-name! "andflak@gmail.com" "Andreas")

(defn update-email!
  [user-id value]
  (h/update-item! creds :users {:user-id user-id} {:email [:set value]})
  "Email changed successfully!")

(defn update-password!
  [user-id password]
  (let [value (hashers/encrypt password)]
    (h/update-item! creds :users {:user-id user-id} {:password [:set value]})
    "Password changed successfully!"))

(defn update-zone! [user-id movement zone]
  (h/put-item! creds :user-movements
               {:user-id user-id :movement-name movement :zone zone}))
