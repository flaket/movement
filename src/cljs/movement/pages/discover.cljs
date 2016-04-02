(ns movement.pages.discover
  (:require [movement.menu :refer [menu-component]]
            [movement.text :refer [text-edit-component]]))

(defn discover-page []
  (let [search-results (atom ["a" "b" "c"])]
    (fn []
      [:div
       [menu-component]
       [:div.content
        [:h1.pure-g
         [:div.pure-u-1
          [:input {:type "search" :name "search" :placeholder "søk på brukernavn eller #hashtagg"
                   :size 60}]]]
        (doall
          (for [result @search-results]
            ^{:key result}
            [:div.pure-g [:div.pure-u-1 result]]))]])))
