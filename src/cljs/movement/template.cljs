(ns movement.template
 (:require [reagent.core :refer [atom]]
           [reagent.session :as session]
           [reagent-forms.core :refer [bind-fields]]
           [secretary.core :include-macros true :refer [dispatch!]]
           [movement.menu :refer [menu-component]]
           [movement.util :refer [text-input]]
           [movement.text :refer [text-input-component auto-complete-did-mount]]))

(def template-state (atom {:title ""
                           :parts []}))

(defn part-creator-component []
  (let []
    (fn [{:keys [title n]} i]
      [:div
       [:div.pure-g
        [:label.pure-u-1-2 "Part " (inc i) " is called "]
        [:input.pure-u-1-2 {:type      "text"
                            :placeholder "\"warmup\", \"strength\", \"practice for 3 rounds\", etc.."
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
        [:label.pure-u "Drawn from the categories: "]
        [:div.pure-u
         (let [id (str "ctags" i)
               categories-ac-comp (with-meta text-input-component
                                             {:component-did-mount #(auto-complete-did-mount (str "#" id) (vec (session/get :all-categories)))})]
           [categories-ac-comp {:id     id
                               :class   "edit" :placeholder "type to find and add category.."
                               :on-save #(when (some #{%} (session/get :all-categories))
                                          (swap! template-state update-in [:parts i :categories] conj %))}])]]
       [:div.pure-g (for [c (get-in @template-state [:parts i :categories])]
                      ^{:key c} [:div.pure-u {:style {:margin-right "5px"}} c])]
       [:div.pure-g
        [:label.pure-u "Additionally, the following exercises should always be included:"]
        [:div.pure-u
         (let [id (str "mtags" i)
               movements-ac-comp (with-meta text-input-component
                                            {:component-did-mount #(auto-complete-did-mount (str "#" id) (vec (session/get :all-movements)))})]
           [movements-ac-comp {:id      (str "mtags" i)
                               :class   "edit" :placeholder "type to find and add movement.."
                               :on-save #(when (some #{%} (session/get :all-movements))
                                          (swap! template-state update-in [:parts i :regular-movements] conj %))}])]]
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
         [:label.pure-u-1-2 "Session title:"]
         [:input.pure-u-1-2 {:type      "text"
                             :placeholder "A unique title for your session, e.g. \"Handstand Practice\" or \"Morning Ritual\""
                             :on-change #(swap! template-state assoc :title (-> % .-target .-value))
                             :value     (:title @template-state)}]]
        [:div.pure-g
         [:label.pure-u-1-2 "Description:"]
         [:input.pure-u-1-2 {:type      "text"
                             :on-change #(swap! template-state assoc :description (-> % .-target .-value))
                             :value     (:description @template-state)}]]
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