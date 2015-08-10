(ns movement.template
 (:require [reagent.core :refer [atom]]
           [reagent.session :as session]
           [secretary.core :include-macros true :refer [dispatch!]]))

(def counter (atom 0))
(def include-date (atom true))

(defn count-component []
  [:div
   "The value is: "
   @counter "."
   [:input {:type "button" :value "Click!"
            :on-click #(swap! counter inc)}]])

(defn input [value]
  [:input {:type "text" :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn btn [val]
  [:input {:type     "button" :value val
           :on-click #(reset! include-date (if @include-date false true))}])

(defn title-component []
  (let [val (atom "My Favourite Movements")]
    (fn []
      [:div "The title of sessions created with this template is: " [input val]
       " and it " [btn (if @include-date "should" "should not")]
       " include the date in the title."])))

(def data (atom {:m 4 :category :strength}))

(defn slider [param value min max]
  [:input {:type      "range" :value value
           :min min :max max
           :style     {:width "100%"}
           :on-change (fn [e]
                        (swap! data assoc param (.-target.value e)))}])

(defn slider-component []
  (let [{:keys [m category]} @data]
    [:div
     [:div m
      [slider :m m 0 10]]]))

(defn template-page []
  [:div
   [:div.container
    [:section#header
     [:h1 "Movement Session"]]
    [:section#nav
     [:button.button {:on-click #(dispatch! "/")} "Session Generator"]
     [:button.button {:on-click #(dispatch! "/user")} "User Profile"]
     [:button.button {:on-click #(dispatch! "/template")} "Template Creator"]
     [:button.button {:on-click #(dispatch! "/movements")} "Movement Explorer"]]
    [:section#template
     [:div "Template Creator."]
     [title-component]
     [:div "The session starts off with "]]]])