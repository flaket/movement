(ns movement.styles.core
  (:require [garden.def :refer [defstyles]]
            [garden.core :refer [css]]
            [garden.units :refer [px]]
            [clojure.java.io :as io]))

(defstyles screen
           [:html {:font-size (px 15)}]

           [:body {:background-color "#fffff6"
                   :color            "#333"}]

           [:.content {:margin        "0 auto"
                       :padding       "1rem 1rem"
                       :max-width     (px 1000)
                       :margin-bottom (px 50)}]
           )
