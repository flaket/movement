(ns movement.template
 (:require [reagent.core :refer [atom]]
           [reagent.session :as session]
           [reagent-forms.core :refer [bind-fields]]
           [secretary.core :include-macros true :refer [dispatch!]]
           [movement.menu :refer [menu-component]]
           [movement.util :refer [text-input POST]]
           [movement.text :refer [text-input-component auto-complete-did-mount]]
           [movement.state :refer [handler-fn]]))

(def template-state (atom {:title ""
                           :parts []}))

(defn part-creator-component-old []
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
                                             {:component-did-mount #(auto-complete-did-mount (str "#" id) (vec (keys (session/get :all-categories))))})]
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
           [movements-ac-comp {:id      id
                               :class   "edit" :placeholder "type to find and add movement.."
                               :on-save #(when (some #{%} (session/get :all-movements))
                                          (swap! template-state update-in [:parts i :specific-movements] conj %))}])]]
       [:div.pure-g (for [c (get-in @template-state [:parts i :specific-movements])]
                      ^{:key c} [:div.pure-u {:style {:margin-right "5px"}} c])]])))

(defn template-creator-component-old []
  (let [error (atom "")]
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content
        [:div.pure-g
         [:h1.pure-u "Create a new Template"]]
        [:div.pure-g
         [:p.pure-u {:style {:color "red"}} @error]]
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
                             :value     (:description @template-state)}]] [:div.pure-g
         [:label.pure-u "The session is divided into "
          [:span {:style {:color "red"}} (count (:parts @template-state))] " parts."]
         [:button.pure-u
          {:on-click #(swap! template-state update-in [:parts]
                             conj {:title    ""
                                   :categories []
                                   :n        0
                                   :specific-movements []})} "+"]
         [:button.pure-u
          {:on-click #(when (> (count (:parts @template-state)) 0)
                       (swap! template-state update-in [:parts] pop))} "-"]]



        [:div
         (let [parts (:parts @template-state)]
           (for [i (range 0 (count parts))]
             ^{:key i} [part-creator-component-old (get parts i) i]))]




        [:div.pure-g
         [:button.pure-u {:on-click #(do
                                      (swap! template-state assoc :user (session/get :user))
                                      (let [title (:title @template-state)
                                            parts (:parts @template-state)]
                                        (cond
                                          (nil? title) (reset! error "The template needs a title.")
                                          (empty? parts) (reset! error "A session must have 1 or more parts.")

                                          :else (POST "template"
                                                      {:params        @template-state
                                                       :handler       (fn [response] (print response))
                                                       :error-handler (fn [response] (reset! error (:response response)))}))))}
          "Save"]]]])))

(defn part-creator-component []
  (let [show-categories-list (atom false)]
    (fn [{:keys [title n categories]} i]
      [:div
       [:div.pure-g
        [:h2.pure-u [:input {:type        "text"
                             :placeholder "Part title"
                             :on-change   #(swap! template-state assoc-in [:parts i :title] (-> % .-target .-value))
                             :value       (get-in @template-state [:parts i :title])}]]]

       [:div.pure-g
        [:div.pure-u "Movements in this part should be generated from the following categories: "]
        [:button.pure-button {:on-click #(handler-fn (reset! show-categories-list (not @show-categories-list)))}
         "Show list of categories"]]

       [:div.pure-g
        (let [id (str "ctags" i)
              categories-ac-comp (with-meta text-input-component
                                            {:component-did-mount #(auto-complete-did-mount
                                                                    (str "#" id)
                                                                    (vec (session/get :all-categories)))})]
          [:div.pure-u
           [categories-ac-comp {:id      (str "ctags" i)
                                :class   "edit" :placeholder "type to find and add category.."
                                :on-save #(when (some #{%} (session/get :all-categories))
                                           (swap! template-state update-in [:parts i :categores] conj %))}]])]
       (when @show-categories-list
         (for [c (session/get :all-categories)]
           ^{:key c} [:div.pure-g
                      [:div.pure-u (str c)]]))

       [:div.pure-g (for [c (get-in @template-state [:parts i :categories])]
                      ^{:key c} [:div.pure-u {:style {:margin-right "5px"}} c])]

       [:div.pure-g (for [c categories]
                      ^{:key c} [:div.pure-u {:style {:margin-right "5px"}} c])]




       [:div.pure-g
        [:label.pure-u "It consists of "
         [:span {:style {:color "red"}} n] " generated movements,"]
        [:button.pure-u
         {:on-click #(swap! template-state update-in [:parts i :n] inc)} "+"]
        [:button.pure-u
         {:on-click #(when (> n 0)
                      (swap! template-state update-in [:parts i :n] dec))} "-"]]


       [:div.pure-g
        [:label.pure-u "Additionally, the following exercises should always be included:"]
        [:div.pure-u
         (let [id (str "mtags" i)
               movements-ac-comp (with-meta text-input-component
                                            {:component-did-mount #(auto-complete-did-mount
                                                                    (str "#" id)
                                                                    (vec (session/get :all-movements)))})]
           [movements-ac-comp {:id      (str "mtags" i)
                               :class   "eit" :placeholder "type to find and add movement.."
                               :on-save #(when (some #{%} (session/get :all-movements))
                                          (swap! template-state update-in [:parts i :specific-movements] conj %))}])]]
       [:div.pure-g (for [c (get-in @template-state [:parts i :specific-movements])]
                      ^{:key c} [:div.pure-u {:style {:margin-right "5px"}} c])]])))

(defn title-component []
  [:div.pure-g
   [:h1.pure-u
    [:input {:type        "text"
             :placeholder "Your Title"
             :on-change   #(swap! template-state assoc :title (-> % .-target .-value))
             :value       (:title @template-state)}]]])

(defn description-component []
  [:div.pure-g
   [:p.subtitle.pure-u
    [:input {:type        "text"
             :placeholder "A suitable description"
             :on-change   #(swap! template-state assoc :description (-> % .-target .-value))
             :value       (:description @template-state)}]]])

(defn heading-component []
  [:div.pure-g
   [:h1.pure-u "Create a new Template"]])

(defn error-component [error-atom]
  [:div.pure-g
   [:h3.pure-u {:style {:color "red"}} @error-atom]])

(defn adjust-parts-component []
  [:div.pure-g
   [:div.pure-u "Movements belong to "
    [:span {:style {:color "red"}} (count (:parts @template-state))] " parts."]
   [:button.pure-button
    {:on-click #(swap! template-state update-in [:parts]
                       conj {:title    ""
                             :categories []
                             :n        0
                             :specific-movements []})} "+"]
   [:button.pure-button
    {:on-click #(when (> (count (:parts @template-state)) 0)
                 (swap! template-state update-in [:parts] pop))} "-"]])

(defn template-creator-component []
  (let [error (atom "")]
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content
        (heading-component)
        (error-component error)
        (title-component)
        (description-component)
        (adjust-parts-component)
        (let [parts (:parts @template-state)]
          (for [i (range 0 (count parts))]
            ^{:key i} [part-creator-component (get parts i) i]))]])))