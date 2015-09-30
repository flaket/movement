(ns movement.template
 (:require [reagent.core :refer [atom]]
           [reagent.session :as session]
           [reagent-forms.core :refer [bind-fields]]
           [secretary.core :include-macros true :refer [dispatch!]]
           [movement.menu :refer [menu-component]]
           [movement.util :refer [text-input]]
           [movement.text :refer [text-input-component text-edit-component]]))

(def template-state (atom {:title ""
                           :parts []}))

(defn part-creator-component []
  (let []
    (fn [{:keys [title n]} i]
      [:div
       [:div.pure-g
        [:label.pure-u-1-2 "Part " (inc i) " is called "]
        [:input.pure-u-1-2 {:type      "text"
                            :on-change #(swap! template-state assoc-in [:parts i :title] (-> % .-target .-value))
                            :value     (get-in @template-state [:parts i :title])}]]
       [:div.pure-g
        [:label.pure-u "It consists of "
         [:span {:style {:color "red"}} n] " generated movements,"]
        [:button.pure-u
         {:on-click #(swap! template-state update-in [:parts i :n] inc)} "+"]
        [:button.pure-u
         {:on-click #(when (> n 0)
                      (swap! template-state update-in [:parts i :n] dec))} "-"]]
       [:div.pure-g
        [:label.pure-u {:for "tags"} "Drawn from the categories: "]
        [:div.pure-u
         [text-edit-component
          {:id "tags"
           :class   "edit" :placeholder "type to find and add category.."
           :on-save #(swap! template-state update-in [:parts i :categories] conj %)}]]]
       [:div.pure-g (for [c (get-in @template-state [:parts i :categories])]
                      ^{:key c} [:div.pure-u {:style {:margin-right "5px"}} c])]
       [:div.pure-g
        [:label.pure-u "Additionally, the following exercises should always be included:"]
        [:div.pure-u
         [text-input-component
          {:class   "edit" :placeholder "type to find and add movement.."
           :on-save #(swap! template-state update-in [:parts i :regular-movements] conj %)}]]]
       [:div.pure-g (for [c (get-in @template-state [:parts i :regular-movements])]
                      ^{:key c} [:div.pure-u {:style {:margin-right "5px"}} c])]])))

(defn template-creator-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content
        [:div.pure-g
         [:h1.pure-u "Create a new Template"]]
        [:div.pure-g
         [:label.pure-u-1-2 "This movement session is called "]
         [:input.pure-u-1-2 {:type      "text"
                  :on-change #(swap! template-state assoc :title (-> % .-target .-value))
                  :value     (:title @template-state)}]]
        [:div.pure-g
         [:label.pure-u "The session is divided into "
          [:span {:style {:color "red"}} (count (:parts @template-state))] " parts."]
         [:button.pure-u
          {:on-click #(swap! template-state update-in [:parts]
                             conj {:title    ""
                                   :categories []
                                   :n        0
                                   :regular-movements []})} "+"]
         [:button.pure-u
          {:on-click #(when (> (count (:parts @template-state)) 0)
                       (swap! template-state update-in [:parts] pop))} "-"]]
        [:div
         (let [parts (:parts @template-state)]
           (for [i (range 0 (count parts))]
             ^{:key i} [part-creator-component (get parts i) i]))]
        [:div.pure-g
         [:button.pure-u {:on-click #(print @template-state)} "Save"]]]])))