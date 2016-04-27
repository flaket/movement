(ns movement.db.db
      (:require [clojure.core.async :as async :refer [<!! <! go]]
                [hildebrand.core :as h]
                [taoensso.faraday :as far]
                [clojure.set :as set]
                [buddy.hashers :as hashers]
                [clojure.string :as str]
                [clojure.java.io :as io]
                [clojure.data.codec.base64 :as b64]
                [clj-time.core :as t]
                [clj-time.coerce :as c]
                [clj-time.local :as l])
  (:import (java.util UUID)
           datomic.Util))

(def creds {:access-key "..."
            :secret-key "..."
            :endpoint   "http://localhost:8080"})

#_(let [movements-table {:table :movements :throughput {:read 1 :write 1}
                       :attrs {:name :string} :keys [:name]}
      user-movements-table {:table   :user-movements :throughput {:read 1 :write 1}
                              :attrs   {:user-id :string :movement-name :string} :keys [:user-id :movement-name]
                              :indexes {:global [{:name       :movements-by-user-id
                                                  :keys       [:user-id]
                                                  :throughput {:read 1 :write 1}
                                                  :project    [:all]}]}}
      templates-table {:table :templates :throughput {:read 1 :write 1}
                         :attrs {:title :string} :keys [:title]}
      users-table {:table   :users :throughput {:read 1 :write 1}
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
                                         :throughput {:read 1 :write 1}}]}}
      sessions-table {:table   :sessions :throughput {:read 1 :write 1}
                      :attrs   {:url :string :user-id :string} :keys [:url]
                      :indexes {:global [{:name       :session-by-user-id
                                          :keys       [:user-id]
                                            :project    [:all]
                                            :throughput {:read 1 :write 1}}]}}
      tables (vector movements-table user-movements-table templates-table users-table sessions-table)]
    (map (fn [table] (h/create-table! creds table)) tables))

#_(let [c (h/list-tables! creds {})] (<!! c))
#_(far/list-tables creds)

#_(let [c (h/describe-table! creds :user-movements)] (<!! c))
#_(far/describe-table creds :users)

#_(h/create-table! creds sessions-table)

#_(h/delete-table! creds :movements)
#_(far/delete-table creds :sessions)

#_(let [tables (<!! (h/list-tables! creds {}))]
    (doseq [i (range (count tables))]
      (h/delete-table! creds (get tables i))))

;;-------------------- get data --------------------

(defn user [user-id]
  (<!! (h/get-item! creds :users {:user-id user-id})))
#_(user "577d84e2-0b7a-48b3-a3ac-317d78e7eab6")

(defn user-by-email [email]
  (->>
    (h/query! creds :users {:email [:= email]} {:index :user-by-email})
    <!!
    first))
#_(user-by-email "andflak@gmail.com")
#_(user-by-email "andreas.flakstad@gmail.com")
#_(user-by-email "a")

#_(defn user-by-email2 [email]
  (far/get-item creds :users {:email email} {:index "user-by-email"}))
#_(user-by-email2 "a")

(defn user-by-activation-id [id]
  (->>
    (h/query! creds :users {:activation-id [:= id]} {:index :user-by-activation-id})
    <!!
    first))

#_(defn user-by-activation-id2 [id]
  (first (far/query creds :users {:activation-id [:eq id]} {:index "user-by-activation-id"})))

(defn user-by-name [name]
  (->>
    (h/query! creds :users {:name [:= name]} {:index :user-by-name})
    <!!))
#_(user-by-name "andreas")

#_(defn user-by-name2 [name]
  (first (far/query creds :users {:name [:eq name]} {:index "user-by-name"})))
#_(user-by-name2 "kåre")

(defn sessions-by-user-id [user-id]
  (let [sessions (<!! (h/query! creds :sessions {:user-id [:= user-id]} {:index :session-by-user-id}))
        sessions (map #(assoc % :user-name (:name (user (:user-id %)))) sessions)]
    sessions))
#_(sessions-by-user-id "577d84e2-0b7a-48b3-a3ac-317d78e7eab6")

(defn create-feed [user-id]
  (let [users (conj (:follows (user user-id)) user-id)
        sessions (flatten (for [u users] (sessions-by-user-id u)))
        sessions (reverse (sort-by :date-time sessions))]
    sessions))

(defn create-user-only-feed [user-id]
  (reverse (sort-by :date-time (sessions-by-user-id user-id))))

(defn movements
  "Gives a lazy sequence over all movement names as strings in the :movements table."
  []
  (let [c (h/scan! creds :movements {})
        x (map :name (<!! c))]
    x))
#_(movements)

#_(defn movements2 []
  (far/scan creds :movements {:attrs [:name]}))
#_(movements2)

(defn categories
  "Gives a lazy sequence over all unique category keywords."
  []
  (let [c (h/scan! creds :movements {})
        x (apply set/union (map :category (<!! c)))]
    (seq x)))
#_(categories)

(defn movement
  "Returns a map representing a movement with a new unique id."
  [name]
  (let [c (h/get-item! creds :movements {:name name})
        id (str (UUID/randomUUID))]
    (assoc (<!! c) :id id)))
#_(movement "Balansegang")

#_(defn movement2
  "Returns a map representing a movement with a new unique id."
  [name]
  (let [m (far/get-item creds :movements {:name name})]
    (assoc m :id (str (UUID/randomUUID)))))
#_(movement2 "Balansegang")

(defn movements-from-category [n category]
  "Returns a random lazy sequence over n movements that share a given category.
  todo: accept several categories. Or should the category be picked randomly on the client side?"
  (let [c (h/scan! creds :movements {:filter [:contains [:category] (keyword category)]})
        movements (take n (shuffle (<!! c)))
        movements (map #(assoc % :id (str (UUID/randomUUID))) movements)]
    movements))
#_(movements-from-category 1 :balance)

(defn user-movement [user-id movement-name]
  (<!! (h/get-item! creds :user-movements {:user-id user-id :movement-name movement-name})))

(defn template [title]
  (<!! (h/get-item! creds :templates {:title title})))
#_(template "Naturlige Bevegelser 2")

(defn templates []
  (map :title (<!! (h/scan! creds :templates {}))))

(defn fix-measurement [m]
  (case (:measurement m)
    "repetitions" (dissoc m :duration :distance)
    "distance" (dissoc m :rep :duration)
    "duration" (dissoc m :rep :distance)
    m))

(defn create-movement [user-id template-movement]
  ; todo: filter on {:natural-only? true}
  ; todo: movements-from-category skal ta flere categorier
  ; todo: remove duplicates from a part (scan through keep hash-map and refresh if in hash-map)
  ; todo: filter on user preferences/goals
  (-> (merge template-movement
             (if-let [m-name (:movement template-movement)]
               (movement m-name)
               (let [; draw a random movement from the slot-categories
                     random-movement (first (movements-from-category 1 (first (shuffle (:slot-category template-movement)))))]
                 ; check in table user-movements if the user has done this movement before
                 (if-let [user-movement (<!! (h/get-item! creds :user-movements {:user-id user-id :movement-name (:name random-movement)}))]
                   ; yes-> assoc :zone data and return movement
                   (merge random-movement user-movement)
                   ; no-> movement has not been performed, swap recursively with 'previous' variations
                   (loop [m random-movement]
                     (if (nil? (:previous m))
                       m                                    ; if movement has no 'previous': return movement
                       (let [new (movement (first (shuffle (:previous m))))] ; pick random 'previous'
                         ; check if user has done this movement before
                         (if-let [user-movement (<!! (h/get-item! creds :user-movements {:user-id user-id :movement-name (:name new)}))]
                           (let [zone (:zone user-movement)]
                             ; if user is effective or have mastered the easier movement, return the original, else return the easier
                             (if (or (= 2N zone) (= 3N zone))
                               m
                               (merge new user-movement)))
                           (recur new)))))))))
      (fix-measurement)))

(defn create-session [user-id session-type]
  (let [
        ; todo: select template based on user reporting on energy and sleep levels.
        templates (case session-type "Naturlig bevegelse" ["Naturlige Bevegelser 1" "Naturlige Bevegelser 2" "Naturlige Bevegelser 3" "Naturlige Bevegelser 4"]
                                     "Styrketrening" ["Gymnastic Strength 1" "Locomotion 1"]
                                     "Mobilitet" ["Mobility 1"]
                                     ["Naturlige Bevegelser 2"])
        template (template (first (shuffle templates)))
        session (assoc template :parts (mapv (fn [p] (mapv #(create-movement user-id %) p)) (:parts template)))]
    session))

;;---------- add/update data ----------

(defn add-user! [email name password activation-id]
  (let [user {:user-id             (str (UUID/randomUUID))
              :email               email
              :name                name
              :password            (hashers/encrypt password)
              :sign-up-timestamp   (c/to-string (l/local-now))
              :activation-id       activation-id
              :activated?          true
              :paid-subscription?  false
              :settings            {}
              :statistics          {}
              :follows             []
              :badges              []
              }]
    (h/put-item! creds :users user)))
#_(add-user! "a" "andreas" "pw" (str (UUID/randomUUID)))
#_(add-user! "b" "bob" "pw" (str (UUID/randomUUID)))
#_(add-user! "c" "kåre" "pw" (str (UUID/randomUUID)))

(defn follow-user! [user-id follow-id]
  (h/update-item! creds :users {:user-id user-id}
                  {:follows [:concat [follow-id]]}))
#_(follow-user! "7ccb2ebd-35d2-49b4-802b-a6fd7ef3706c" "198af054-61e9-48d7-b199-d03e3980fb40")

#_(user-by-email "b")

(defn add-badge! [user-id badge]
  (h/update-item! creds :users {:user-id user-id}
                  {:badges [:concat [badge]]}))
#_(add-badge! "andreas@roebuck.com" {:name "Newbie" :achieved-at (c/to-string (l/local-now))})

(defn add-movement! [user-id movement-name zone]
  (h/put-item! creds :user-movements {:user-id user-id :movement-name movement-name :zone zone}))

(defn add-session! [params]
  (let [{:keys [user-id session]} params
        unique-movements (:unique-movements session)
        image-file (:photo session)
        [_ file-type _ photo] (if image-file (str/split image-file #"[:;,]") [])
        session (dissoc session :photo :unique-movements)
        url (str (UUID/randomUUID))
        session (assoc session :url url :user-id user-id :comments [] :likes [])]
    (.println System/out (str "Saving session: " session))
    ; Store image to disk if png or jpeg.
    (when (or (= file-type "image/png")
            (= file-type "image/jpeg"))
      (let [decoded-photo (b64/decode (.getBytes photo))
            output-url (str "uploads/" url ".jpg")]
        (with-open [w (io/output-stream output-url)]
          (.write w decoded-photo))
        (.println System/out (str "Wrote photo to: " output-url))))
    ; Store session in :sessions table
    (h/put-item! creds :sessions session)
    ; Store each unique movement from the session with its updated zone data in :user-movements table
    (when-let [u (vec unique-movements)]
      (.println System/out (str "Saving user movements: " u))
      (doseq [m u]
        (add-movement! user-id (:name m) (:zone m))))
    :ok))

(defn activate-user! [uuid]
  (let [user (:user-id (user-by-activation-id uuid))]
    (h/update-item! creds :users {:user-id user}
                    {:activated?    [:set true]
                     :activation-id [:remove]})))
#_(activate-user! "97741783-9bb7-442f-9a73-e573acb9c3db")

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

(defn like! [{:keys [session-url likers]}]
  (h/update-item! creds :sessions {:url session-url} {:likes [:set likers]})
  "ok")

(defn comment! [{:keys [session-url comments]}]
  (h/update-item! creds :sessions {:url session-url} {:comments [:set comments]})
  "ok")

;; ------ LAB -------

; teste data:
; + hver øvelse har et bilde
; - hver øvelse med previous/next peker til et øvelsesnavn som finnes
; - hver øvelse har measurement, og measurement er en av [:repetitions :duration :distance]

#_(defn url->name [url]
    (let [name (-> url
                   (str/split (re-pattern ".png"))
                   (first)
                   (str/replace "-" " ")
                   (str/split (re-pattern " ")))
          name (map #(str/capitalize %) name)
          name (-> name
                   (interleave (cycle " "))
                   (drop-last)
                   (str/join))]
      name))

#_(defn find-no-data-images []
    (let [f (io/file "resources/public/images/movements")
          images (for [file (file-seq f)] (.getName file))
          images (drop 2 images)                            ; remove leading junk files
          no-data-images (filter #(has-no-data? %) images)]
      {:#images         (count images)
       :#no-data-images (count no-data-images)
       :no-data-images  (vec no-data-images)}))

#_(find-no-image-movements)
#_(find-no-data-images)

(def movement-urls ["balancing.edn" "climbing.edn" "crawling.edn" "hanging.edn" "jumping.edn" "lifting.edn" "rolling.edn" "running.edn" "throwing-catching.edn"
                    "walking.edn" "mobility/mobility.edn" "other/core.edn" "other/footwork.edn" "other/hand-balance.edn" "other/leg-strength.edn"
                    "other/planche-lever.edn" "other/pulling.edn" "other/pushing.edn" "other/ring.edn"])

(defn load-edn-file
  "Returns a vector of maps, read from a edn input file."
  [file]
  (first (Util/readAll (io/reader (io/resource (str "data/movements/" file))))))

(defn load-and-concat
  "Loads lists of movement maps and reduces to a single vector of movement maps."
  [files]
  (reduce into [] (map #(load-edn-file %) files)))

(defn has-image? [m]
  (if (io/resource (str "public/images/movements/" (:image m))) true false))

(defn movements-without-image []
  (let [no-image-movements (remove #(has-image? %) (load-and-concat movement-urls))]
    {:#                  (count no-image-movements)
     :no-image-movements no-image-movements}))

; add movements to db
#_(map #(h/put-item! creds :movements %) (load-and-concat movement-urls))

; add templates to db
#_(map #(h/put-item! creds :templates %) (first (Util/readAll (io/reader (io/resource "data/templates.edn")))))