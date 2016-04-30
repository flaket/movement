(ns movement.db.db
      (:require [clojure.core.async :as async :refer [<!! <! go]]
                [hildebrand.core :as h]
                [aws.sdk.s3 :as s3]
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

(defn vec-remove
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(defn positions
  "Finds the integer positions of the elements in the collection, that matches the predicate."
  [pred coll]
  (keep-indexed (fn [idx x] (when (pred x) idx)) coll))

;;-------------------- aws credentials --------------------

(def local-creds {:access-key ""
                  :secret-key ""
                  :endpoint   "http://localhost:8080"})

(def creds {:access-key "AKIAJG4MLZ7TON7BLNCQ"
            :secret-key "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"
            :region :eu-central-1})

;;-------------------- get data --------------------

(defn user [user-id]
  (<!! (h/get-item! creds :users {:user-id user-id})))

(defn user-by-email [email]
  (first (<!! (h/query! creds :users {:email [:= email]} {:index :user-by-email}))))

(defn user-by-activation-id [id]
  (first (<!! (h/query! creds :users {:activation-id [:= id]} {:index :user-by-activation-id}))))

(defn user-by-name [name]
  (first (<!! (h/query! creds :users {:name [:= name]} {:index :user-by-name}))))

(defn sessions-by-user-id [user-id]
  (let [sessions (<!! (h/query! creds :sessions {:user-id [:= user-id]} {:index :session-by-user-id}))
        primary-user (user user-id)                         ; find user info on the logged in user;
        sessions (map (fn [session]                         ; go through sessions and add name and profile-image-url to each session
                        (if (= (:user-id primary-user) (:user-id session))
                          (assoc session :user-name (:name primary-user)
                                         :user-image (:user-image primary-user))
                          (let [user (user (:user-id session))] ; we only look up info on other users when necessary
                            (assoc session :user-name (:name user)
                                           :user-image (:user-image user))))) sessions)]
    sessions))

(defn users []
  (<!! (h/scan! creds :users {:project [:name :profile-text :user-id :user-image]})))

(defn create-feed [user-id]
  (let [users (conj (:follows (user user-id)) user-id)
        sessions (flatten (for [u users] (sessions-by-user-id u)))
        sessions (reverse (sort-by (comp :date-time :session) sessions))]
    sessions))

(defn create-user-only-feed [user-id]
  (reverse (sort-by (comp :date-time :session) (sessions-by-user-id user-id))))

(defn movements
  "Gives a lazy sequence over all movement names as strings in the :movements table."
  []
  (let [c (h/scan! creds :movements {})
        x (map :name (<!! c))]
    x))

(defn categories
  "Gives a lazy sequence over all unique category keywords."
  []
  (let [c (h/scan! creds :movements {})
        x (apply set/union (map :category (<!! c)))]
    (seq x)))

(defn movement
  "Returns a map representing a movement with a new unique id."
  [name]
  (let [m (<!! (h/get-item! creds :movements {:name name}))
        id (str (UUID/randomUUID))]
    (assoc m :id id)))

(defn movements-from-category [n category]
  "Returns a random lazy sequence over n movements that share a given category.
  todo: accept several categories. Or should the category be picked randomly on the client side?"
  (let [c (h/scan! creds :movements {:filter [:contains [:category] (keyword category)]})
        movements (take n (shuffle (<!! c)))
        movements (map #(assoc % :id (str (UUID/randomUUID))) movements)]
    movements))

(defn user-movement [user-id movement-name]
  (<!! (h/get-item! creds :user-movements {:user-id user-id :movement-name movement-name})))

(defn template [title]
  (<!! (h/get-item! creds :templates {:title title})))

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
  ; todo: movements-from-category skal ta flere categorier: slik det står nå vil øvelser fra kategorier med få øvelser dukke opp for ofte
  ; todo: reduce consecutive duplicates on reps
  ; todo: filter on user preferences/goals
  (-> (merge template-movement
             (if-let [m-name (:movement template-movement)]
               (let [m (movement m-name)
                     user-movement (user-movement user-id m-name)
                     zone (if (:zone user-movement) (:zone user-movement) 0)
                     m (assoc m :zone zone)]
                 m)  ; if template calls for a specific movement: fetch this
               (loop [m (first (movements-from-category 1 (first (shuffle (:slot-category template-movement))))) ; pick a random movement that belongs to one of the categories
                        counter 0]

                 ; Check what the user has reported on this movement
                 (let [um (<!! (h/get-item! creds :user-movements {:user-id user-id :movement-name (:name m)}))
                       zone (if (:zone um) (:zone um) 0)
                       m (assoc m :zone zone)]
                   (if (> counter 4)
                     ; if looping too much: return m
                     (do
                       (.println System/out "*** Looping out of control ***")
                       m)
                     (cond
                       (= 3 zone) m ; user has mastered movement todo: if (:next m), with some probability, add user zone data to that and return
                       (= 2 zone) m ; user is effective, but not also efficient, return this movement
                       :default (if-let [previous-movement-names (:previous m)]
                                  (let [user-previous-movements (map #(user-movement user-id %) previous-movement-names)
                                        mastered-movements (->> user-previous-movements
                                                                (remove #(nil? %))
                                                                (remove #(< (:zone %) 2))
                                                                (map :movement-name)
                                                                set)
                                        _ (.println System/out (str "Movement: " (:name m)))
                                        _ (.println System/out (str "Previous: " previous-movement-names))
                                         _ (.println System/out (str "Mastered: " mastered-movements))
                                        diff (set/difference (set previous-movement-names) mastered-movements)
                                        _ (.println System/out (str "Diff: " diff))
                                        ]
                                    ; if all prerequisites mastered (previous has (< 1N zone)): return m
                                       (if (empty? diff)
                                         m
                                         (let [new-m (movement (first (shuffle diff)))]
                                           (.println System/out (str "Recurring with: " (:name new-m) "\n"))
                                           (recur new-m (inc counter))))
                                       ); else: pick on of the previous with zone 1 or 0 and recur
                                     ; has no previous: return m
                                     m)))))))
      (fix-measurement)))

(defn create-session [user-id session-type]
  (let [
        ; todo: select template based on user reporting on energy and sleep levels.
        templates (case session-type "Naturlig bevegelse" ["Naturlige Bevegelser 1" "Naturlige Bevegelser 2"
                                                           "Naturlige Bevegelser 3" "Naturlige Bevegelser 4"
                                                           "Locomotion 1"]
                                     "Styrketrening" ["Gymnastic Strength 1" "Gymnastic Strength 2"]
                                     "Mobilitet" ["Mobility 1" "Mobility 2" "Mobility 3" "Mobility 4"]
                                     ["Naturlige Bevegelser 2"])
        template (template (first (shuffle templates)))
        ; todo: movement-selection-algorithm (create-movement) should depend on reported energy/sleep levels, preferences and zone data
        new-parts (mapv (fn [p] (mapv #(create-movement user-id %) p)) (:parts template))
        session (assoc template :parts new-parts)]
    session))

;;---------- add/update data ----------

(defn add-user! [email name password activation-id]
  (let [user {:user-id             (str (UUID/randomUUID))
              :email               email
              :name                name
              :password            (hashers/encrypt password)
              :sign-up-timestamp   (c/to-string (l/local-now))
              :activation-id       activation-id
              :activated?          false
              :paid-subscription?  false
              :settings            {}
              :statistics          {}
              :follows             []
              :badges              []
              }]
    (h/put-item! creds :users user)))

(defn follow-user! [{:keys [user-id follow-id]}]
  (h/update-item! creds :users {:user-id user-id} {:follows [:concat [follow-id]]})
  "ok")

(defn unfollow-user! [{:keys [user-id follow-id]}]
  (let [follows (:follows (user user-id))
        pos (first (positions #{follow-id} follows))
        new-follows (vec-remove follows pos)]
    (h/update-item! creds :users {:user-id user-id} {:follows [:set new-follows]})
    "ok"))

(defn add-badge! [user-id badge]
  (h/update-item! creds :users {:user-id user-id}
                  {:badges [:concat [badge]]}))

(defn add-movement! [user-id movement-name zone]
  (h/put-item! creds :user-movements {:user-id user-id :movement-name movement-name :zone zone}))

(defn add-session! [params]
  ; todo: logging instead of console print. Only log when channels close successfully.
  (let [{:keys [user-id session]} params
        url (str (UUID/randomUUID)) ; create a unique url for this session
        unique-movements (:unique-movements session)
        image-file (:photo session)
        [_ file-type _ photo] (if image-file (str/split image-file #"[:;,]") [])
        session (dissoc session :photo :unique-movements :user-name :user-image)
        session (assoc session :parts (mapv (fn [part] (mapv (fn [m] (dissoc m :zone :id)) part)) (:parts session)))
        session (assoc session :image (if photo true false) :comments [] :likes [])]
    ; If upload file is png or jpeg: send to S3
    (when (= file-type "image/jpeg")
      (go
        (let [decoded-photo (b64/decode (.getBytes photo))
              file (io/input-stream decoded-photo)]
          (s3/put-object creds "mumrik-session-images" (str url ".jpg") file)))
      (.println System/out "Sent session photo to S3.."))
    ; Store session in :sessions table
    (h/put-item! creds :sessions {:user-id user-id :url url :session session})
    (.println System/out "Sent session to db..")
    ; Store each unique movement from the session with its updated zone data in :user-movements table
    (when-let [u (vec unique-movements)]
      (doseq [m u]
        (add-movement! user-id (:name m) (:zone m)))
      (.println System/out "Sent user movements to db.."))
    :ok))

(defn activate-user! [uuid]
  (let [user (:user-id (user-by-activation-id uuid))]
    (h/update-item! creds :users {:user-id user}
                    {:activated? [:set true]
                     :activation-id [:remove]})))

(defn update-subscription! [user-id value]
  (h/update-item! creds :users {:user-id user-id}
                  {:valid-subscription? [:set value]}))

(defn update-profile! [user-id profile]
  (let [image-file (:photo profile)
        [_ file-type _ photo] (if image-file (str/split image-file #"[:;,]") [])
        profile (dissoc profile :photo)
        update-map (into {} (for [[k v] profile] [k [:set v]]))
        update-map (if image-file (assoc update-map :user-image [:set true]) update-map)]
    ; If upload file is jpeg: send to S3
    (when (= file-type "image/jpeg")
      (go
        (let [decoded-photo (b64/decode (.getBytes photo))
              file (io/input-stream decoded-photo)]
          (s3/put-object creds "mumrik-user-profile-images" (str user-id ".jpg") file)))
      (.println System/out "Sent profile photo to S3.."))
    (h/update-item! creds :users {:user-id user-id} update-map)
    "Profilen ble oppdatert!"))

(defn update-password!
  [user-id password]
  (let [value (hashers/encrypt password)]
    (h/update-item! creds :users {:user-id user-id} {:password [:set value]})
    "Passordet ble oppdatert!"))

(defn like! [{:keys [session-url user-id]}]
  (h/update-item! creds :sessions {:url session-url} {:session {:likes [:concat [user-id]]}})
  "ok")

(defn comment! [{:keys [session-url user-id user comment]}]
  (let [new-comment {:user user :user-id user-id :comment comment}]
    (h/update-item! creds :sessions {:url session-url} {:session {:comments [:concat [new-comment]]}}))
  "ok")

;; ------ LAB -------

#_(add-user! "andflak@gmail.com" "Andreas" "mumrikM9n8b7v6" (str (UUID/randomUUID)))

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

; S3 buckets:
; mumrik-user-profile-images
; mumrik-session-images
; mumrik-movement-images ; har lastet opp disse

#_(let [movements-table {:table :movements :throughput {:read 10 :write 1}
                         :attrs {:name :string} :keys [:name]}
        user-movements-table {:table   :user-movements :throughput {:read 10 :write 5}
                              :attrs   {:user-id :string :movement-name :string} :keys [:user-id :movement-name]
                              :indexes {:global [{:name       :movements-by-user-id
                                                  :keys       [:user-id]
                                                  :throughput {:read 10 :write 1}
                                                  :project    [:all]}]}}
        templates-table {:table :templates :throughput {:read 10 :write 1}
                         :attrs {:title :string} :keys [:title]}
        users-table {:table   :users :throughput {:read 10 :write 5}
                     :attrs   {:user-id :string :activation-id :string :email :string :name :string} :keys [:user-id]
                     :indexes {:global [{:name       :user-by-activation-id
                                         :keys       [:activation-id]
                                         :project    [:keys-only]
                                         :throughput {:read 5 :write 1}}
                                        {:name       :user-by-email
                                         :keys       [:email]
                                         :project    [:all]
                                         :throughput {:read 10 :write 1}}
                                        {:name       :user-by-name
                                         :keys       [:name]
                                         :project    [:all]
                                         :throughput {:read 10 :write 1}}]}}
        sessions-table {:table   :sessions :throughput {:read 10 :write 5}
                        :attrs   {:url :string :user-id :string} :keys [:url]
                        :indexes {:global [{:name       :session-by-user-id
                                            :keys       [:user-id]
                                            :project    [:all]
                                            :throughput {:read 10 :write 1}}]}}
        tables (vector movements-table user-movements-table templates-table users-table sessions-table)]
    (map (fn [table] (h/create-table! creds table)) tables))

#_(<!! (h/list-tables! creds {}))

#_(<!! (h/describe-table! creds :templates))

#_(<!! (h/create-table! creds {}))

#_(h/delete-table! creds :sessions)

#_(let [tables (<!! (h/list-tables! creds {}))]
    (doseq [i (range (count tables))]
      (h/delete-table! creds (get tables i))))