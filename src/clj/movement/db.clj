(ns movement.db
  (:import java.util.Date)
  (:require [buddy.hashers :as hashers]
            [datomic.api :as d]
            [clojure.string :as str]
            [clojure.set :refer [rename-keys]]
            [taoensso.timbre :refer [info error]]
            [movement.activation :refer [generate-activation-id send-activation-email]]
            [clojure.set :as set]))

#_(defn single-movement [email part]
  (let [db (:db @tx)
        ; convert string values to ints. Why is this needed? A mistake in the client?
        part (into {} (for [[k v] part]
                        (if (and (string? v)
                                 (or (= k :rep) (= k :set) (= k :distance)
                                     (= k :duration) (= k :weight) (= k :rest)))
                          [k (read-string v)]
                          [k v])))
        movement (first (get-n-movements-from-categories 1 (vals (:categories part)) (:practical part)))
        user-movements (d/q '[:find [(pull ?m [*]) ...]
                              :in $ ?email
                              :where
                              [?u :user/email ?email]
                              [?u :user/movements ?m]]
                            db
                            email)
        movement (if-let [user-movement (some #(when
                                                (= (:movement/name movement) (:movement/name %))
                                                %)
                                              user-movements)]
                   (merge movement (dissoc user-movement :db/id))
                   (loop [m movement]
                     (let [easier (:movement/easier m)]
                       (if (nil? easier)
                         m
                         (let [new (d/pull db '[*] (:db/id (first (shuffle easier))))]
                           (if-let [user-movement (some #(when
                                                          (= (:movement/name new) (:movement/name %))
                                                          (dissoc % :db/id))
                                                        user-movements)]
                             (merge new user-movement)
                             (recur new)))))))
        movement (merge movement (dissoc part :categories))
        movement (prep-new-movement-2 movement)]
    movement))

#_(defn movement [email type id part]
  (let [; convert string values to ints. Why is this needed? A mistake in the client?
        db (:db @tx)
        part (into {} (for [[k v] part]
                        (if (and (string? v)
                                 (or (= k :rep) (= k :set) (= k :distance)
                                     (= k :duration) (= k :weight) (= k :rest)))
                          [k (read-string v)]
                          [k v])))
        movement (cond (= type :name) (entity-by-movement-name id)
                       (= type :id) (entity-by-id id)
                       (= type :category) (first (get-n-movements-from-categories 1 (vals (:categories part)) (:part/practical part))))
        user-movements (d/q '[:find [(pull ?m [*]) ...]
                              :in $ ?email
                              :where
                              [?u :user/email ?email]
                              [?u :user/movements ?m]]
                            db
                            email)
        movement (if-let [user-movement (some #(when
                                                (= (:movement/name movement) (:movement/name %))
                                                %)
                                              user-movements)]
                   (merge movement (dissoc user-movement :db/id))
                   movement)
        movement (merge movement (dissoc part :categories))
        movement (prep-new-movement-2 movement)]
    movement))

#_(defn create-session [email template]
  (let [db (:db @tx)
        parts (vec
                (for [part (:template/part template)]
                  (let
                    [n (:part/number-of-movements part)
                     specific-movements (:part/specific-movement part)
                     category-names (vec (map :category/name (:part/category part)))
                     generated-movements (if (nil? n)
                                           []
                                           (get-n-movements-from-categories n category-names (:part/practical part)))
                     user-movements (d/q '[:find [(pull ?m [*]) ...]
                                           :in $ ?email
                                           :where
                                           [?u :user/email ?email]
                                           [?u :user/movements ?m]]
                                         db
                                         email)
                     generated-movements (for [movement generated-movements]
                                           ; if the user has done the movement before
                                           (if-let [user-movement (some #(when
                                                                          (= (:movement/name movement) (:movement/name %))
                                                                          (dissoc % :db/id))
                                                                        user-movements)]
                                             ; assoc :zone data
                                             (merge movement user-movement)
                                             ; else: movement has not been performed, swap recursively to the easiest variationwhen generated has easier: swap
                                             (loop [m movement]
                                               (let [easier (:movement/easier m)]
                                                 (if (nil? easier)
                                                   m
                                                   (let [new (d/pull db '[*] (:db/id (first (shuffle easier))))]
                                                     (if-let [user-movement (some #(when
                                                                                    (= (:movement/name new) (:movement/name %))
                                                                                    (dissoc % :db/id))
                                                                                  user-movements)]
                                                       (let [zone (:db/ident (:movement/zone user-movement))]
                                                         ; if user is effective or have mastered the easier movement, return the original, else return the easier
                                                         (if (or (= :zone/two zone) (= :zone/three zone))
                                                           m
                                                           (merge new user-movement)))
                                                       (recur new))))))))
                     movements (concat specific-movements generated-movements)
                     movements (vec (for [m movements] (prep-new-movement m part)))]
                    {:title      (:part/title part)
                     :categories category-names
                     :movements  movements
                     :practical  (:part/practical part)
                     :rep        (:part/rep part)
                     :set        (:part/set part)
                     :distance   (:part/distance part)
                     :duration   (:part/duration part)
                     :weight     (:part/weight part)
                     :rest       (:part/rest part)})))
        session (assoc template :parts parts)]
    {:title         (:template/title session)
     :description   (:template/description session)
     :template-id   (:db/id session)
     :plan-id       (:plan-id session)
     :last-session? (:last-session? session)
     :parts         (:parts session)}))