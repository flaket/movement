(ns movement.db.db
  (:require [clojure.core.async :as async :refer [<!!]]
            [hildebrand.core :as h]))

(def creds {:access-key "..."
            :secret-key "..."
            :endpoint   "http://localhost:8000"})

(h/delete-table! creds :movement)

(let [c (h/create-table! creds
                         {:table      :movement
                          :throughput {:read 1 :write 1}
                          :attrs      {:name :string}
                          :keys       [:name]})]
  (<!! c))

(let [c (h/describe-table! creds :movement)] (<!! c))

(let [c (h/list-tables! creds {})] (<!! c))

(def movements [
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
                ])

(map #(h/put-item! creds :movement %) movements)

; look-up on known item
(let [c (h/get-item! creds :movement {:name "Pull Up"})] (<!! c))

(let [c (h/scan-count! creds :movement)] (<!! c))

; query looks up values given keys
(let [c (h/query! creds :movement {:name "Pull Up"} {:filter [:contains [:name] "Pull"]})]
  (<!! c))

; scan is query without key constraint
(let [c (h/scan! creds :movement {:filter [:contains [:category] :pushing]})]
  (<!! c))

(let [c (h/get-item! creds :movement {:name "Dip"})
      previous (first (shuffle (:previous (<!! c))))
      c (h/get-item! creds :movement {:name previous})]
  (<!! c))