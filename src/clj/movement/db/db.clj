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

(def creds {:access-key ""
            :secret-key ""
            :endpoint   "http://localhost:8080"
            })

(def iam-creds {:access-key "AKIAJG4MLZ7TON7BLNCQ"
                :secret-key "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"})

; buckets:
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

#_(h/create-table! creds {})

#_(h/delete-table! creds :sessions)

#_(let [tables (<!! (h/list-tables! creds {}))]
    (doseq [i (range (count tables))]
      (h/delete-table! creds (get tables i))))

;;-------------------- get data --------------------

(defn user [user-id]
  (<!! (h/get-item! creds :users {:user-id user-id})))
#_(user "9c0ca430-4da4-4b98-8614-e5ac5a19607e")

(defn user-by-email [email]
  (first (<!! (h/query! creds :users {:email [:= email]} {:index :user-by-email}))))
#_(user-by-email "andflak@gmail.com")
#_(user-by-email "andreas.flakstad@gmail.com")
#_(user-by-email "a")

(defn user-by-activation-id [id]
  (->>
    (h/query! creds :users {:activation-id [:= id]} {:index :user-by-activation-id})
    <!!
    first))

(defn user-by-name [name]
  (<!! (h/query! creds :users {:name [:= name]} {:index :user-by-name})))
#_(user-by-name "andreas")

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
#_(sessions-by-user-id "9c0ca430-4da4-4b98-8614-e5ac5a19607e")

(defn users []
  (<!! (h/scan! creds :users {:project [:name :profile-text :user-id :user-image]})))
#_(users)

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
#_(movements)

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
  (let [m (<!! (h/get-item! creds :movements {:name name}))
        id (str (UUID/randomUUID))]
    (assoc m :id id)))
#_(movement "Balansegang")

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

#_(<!! (h/scan! creds :user-movements {}))
#_(<!! (h/get-item! creds :user-movements {:user-id "577d84e2-0b7a-48b3-a3ac-317d78e7eab6" :movement-name "Beinsving bakover"}))

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
               (loop [; pick a random movement that belongs to one of the categories
                      m (first (movements-from-category 1 (first (shuffle (:slot-category template-movement)))))
                      counter 0]
                 ; Check what the user has reported on this movement
                 (let [user-movement (<!! (h/get-item! creds :user-movements {:user-id user-id :movement-name (:name m)}))
                       zone (if (:zone user-movement) (:zone user-movement) 0)
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
                                     (let [user-previous-movements (vec (for [pm previous-movement-names]
                                                                          (user-movement user-id pm)))
                                           mastered-movements (->> user-previous-movements
                                                                   (remove #(nil? %))
                                                                   (remove #(< (:zone %) 2))
                                                                   (map :movement-name)
                                                                   set)
                                           _ (.println System/out (str "Movement: " (:name m)))
                                           _ (.println System/out (str "Previous: " previous-movement-names))
                                           _ (.println System/out (str "Mastered: " mastered-movements))
                                           diff (set/difference (set previous-movement-names) mastered-movements)
                                           _ (.println System/out (str "Diff: " diff))]
                                       ; if all prerequisites mastered (previous has (< 1N zone)): return m
                                       (if (empty? diff)
                                         m
                                         (let [new-m (movement (first (shuffle diff)))]
                                           (.println System/out (str "Recurring with: " (:name new-m) "\n"))
                                           (recur new-m (inc counter))))); else: pick on of the previous with zone 1 or 0 and recur
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
                                     "Mobilitet" ["Mobility 1"]
                                     ["Naturlige Bevegelser 2"])
        template (template (first (shuffle templates)))
        ; todo: movement-selection-algorithm (create-movement) should depend on reported energy/sleep levels, preferences and zone data
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

(defn follow-user! [{:keys [user-id follow-id]}]
  (h/update-item! creds :users {:user-id user-id} {:follows [:concat [follow-id]]})
  "ok")
#_(follow-user! {:user-id "26737e9f-6b00-4f67-bdef-5a02e076a145" :follow-id "9c0ca430-4da4-4b98-8614-e5ac5a19607e"})

(defn unfollow-user! [{:keys [user-id follow-id]}]
  (let [follows (:follows (user user-id))
        pos (first (positions #{follow-id} follows))
        new-follows (vec-remove follows pos)]
    (h/update-item! creds :users {:user-id user-id} {:follows [:set new-follows]})
    "ok"))
#_(unfollow-user! {:user-id "26737e9f-6b00-4f67-bdef-5a02e076a145" :follow-id "9c0ca430-4da4-4b98-8614-e5ac5a19607e"})

#_(user "26737e9f-6b00-4f67-bdef-5a02e076a145")

(defn add-badge! [user-id badge]
  (h/update-item! creds :users {:user-id user-id}
                  {:badges [:concat [badge]]}))
#_(add-badge! "andreas@roebuck.com" {:name "Newbie" :achieved-at (c/to-string (l/local-now))})

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
    (when (or (= file-type "image/png")
            (= file-type "image/jpeg"))
      (let [decoded-photo (b64/decode (.getBytes photo))
            file (io/input-stream decoded-photo)]
        (go
          (s3/put-object iam-creds "mumrik-session-images" (str url ".jpg") file))
        (.println System/out "Sent photo to S3..")))
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

(defn like! [{:keys [session-url user-id]}]
  (h/update-item! creds :sessions {:url session-url} {:session {:likes [:concat [user-id]]}})
  "ok")

(defn comment! [{:keys [session-url user-id user comment]}]
  (let [new-comment {:user user :user-id user-id :comment comment}]
    (h/update-item! creds :sessions {:url session-url} {:session {:comments [:concat [new-comment]]}}))
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