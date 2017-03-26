(ns movement.util
      (:require [clojure.java.io :as io])
  (:import datomic.Util))

;;-------------------- aws credentials --------------------

(def creds {
            :access-key "AKIAJG4MLZ7TON7BLNCQ"
            :secret-key "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"
            :region :eu-central-1
            ;:access-key ""
            ;:secret-key ""
            ;:endpoint   "http://localhost:8080"
            })

;; ------ LAB -------

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