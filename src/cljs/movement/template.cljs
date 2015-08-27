(ns movement.template
 (:require [reagent.core :refer [atom]]
           [reagent.session :as session]
           [reagent-forms.core :refer [bind-fields]]
           [secretary.core :include-macros true :refer [dispatch!]]
           [movement.text :refer [text-edit-component]]
           [movement.nav :refer [nav-component]]))


(def template-state (atom {:title ""
                           :parts []}))

(defn category-creator-component []
  (let [buttons (atom [])]
    (fn [{:keys [title n]} i]
      [:div
       [:div.row
        [:label.five.columns "Part " (inc i) " is called "]
        [:input.seven.columns {:type "text"}]]
       [:div.row
        [:label.ten.columns "It consists of "
         [:span {:style {:color "red"}} n] " generated movements,"]
        [:button.one.column
         {:on-click #(swap! template-state update-in [:parts i :n] inc)} "+"]
        [:button.one.column
         {:on-click #(when (> n 0)
                      (swap! template-state update-in [:parts i :n] dec))} "-"]]
       #_[:div.row
          [:label.four.columns "Drawn from the categories: "]
          [:div.three.columns
           [template-text-edit-component
            {:class   "edit" :placeholder "type to find and add category.."
             :on-save #(handler-fn (swap! buttons conj %))
             :on-stop #(handler-fn (fn [] nil))}]]
          [:div.five.columns (for [b @buttons] ^{:key b} [:div.two.columns b])]]
       [:div.row
        [:label.four.columns "Additionally, the following exercises should always be included:"]
        [:div.three.columns
         [text-edit-component {:class   "edit" :placeholder "type to find and add movement.."
                               :on-save #(do (swap! template-state assoc-in [:parts i :extra] %) nil)
                               :on-stop #()}]]
        [:div.five.columns (:extra (get (:parts @template-state) i))]]])))

(defn template-creator-component []
  (let []
    (fn []
      [:div
       [:div.container
        [nav-component]
        [:section
         [:div.row
          [:label.five.columns "This movement session is called "]
          [:input.seven.columns
           {:type "text" :placeholder "My Favourite Movement Session"}]]
         [:div.row
          [:label.ten.columns "The session is divided into "
           [:span {:style {:color "red"}} (count (:parts @template-state))] " parts."]
          [:button.one.column
           {:on-click #(swap! template-state update-in [:parts]
                              conj {:title ""
                                    :category nil
                                    :n 0})} "+"]
          [:button.one.column
           {:on-click #(when (> (count (:parts @template-state)) 0)
                        (swap! template-state update-in [:parts] pop))} "-"]]
         [:div
          (let [parts (:parts @template-state)]
            (for [i (range 0 (count parts))]
              ^{:key i} [category-creator-component (get parts i) i]))]]]])))