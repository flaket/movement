(ns movement.db
  (:require [buddy.hashers :as hashers]
            [datomic.api :as d]))

#_(def uri "datomic:dev://localhost:4334/testing3")
(def uri "datomic:ddb://us-east-1/movementsession/test-db?aws_access_key_id=AKIAJI5GV57L43PZ6MSA&aws_secret_key=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")

(def tx (atom {}))

(defn update-tx-conn! [] (swap! tx assoc :conn (d/connect uri)))
(defn update-tx-db! [] (swap! tx assoc :db (d/db (:conn @tx))))