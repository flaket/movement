(ns movement.util
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import datomic.Util))

(def uri "datomic:dev://localhost:4334/movement")

(d/create-database uri)

(def conn (d/connect uri))

(def schema-tx (first (Util/readAll (io/reader (io/resource "data/schema.edn")))))
(def data-tx (first (Util/readAll (io/reader (io/resource "data/initialdata.edn")))))
(d/transact conn schema-tx)
(d/transact conn data-tx)


(d/q '[:find ?c ?name
       :where [?c :movement/name ?name]]
     (d/db conn))