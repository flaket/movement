(ns movement.template
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [reagent-forms.core :refer [bind-fields]]
            [secretary.core :include-macros true :refer [dispatch!]]
            [movement.menu :refer [menu-component]]
            [movement.util :refer [text-input POST]]
            [movement.text :refer [text-input-component auto-complete-did-mount]]
            [movement.state :refer [handler-fn]]
            [clojure.string :as str]))

(def template-state (atom {:parts []}))

(defn movement-component [title image]
  [:div.pure-u.movement
   [:div.pure-g
    [:div.pure-u.refresh {:on-click #()}]
    [:h3.pure-u.title title]
    [:div.pure-u.destroy {:on-click #()}]]
   [:img.graphic.pure-img-responsive {:src image}]
   [:div.pure-g
    [:div.pure-u 10]
    [:div.pure-u "reps"]]
   [:div.pure-g
    [:div.pure-u 3]
    [:div.pure-u "set"]]
   [:div.pure-g
    [:div.pure-u "-"]
    [:div.pure-u "meters"]]
   [:div.pure-g
    [:div.pure-u "-"]
    [:div.pure-u "seconds"]]])

(defn part-creator-component []
  (let [showing-categories-list (atom false)]
    (fn [{:keys [title n categories]} i]
      [:div
       [:div.pure-g
        [:h2.pure-u [:input {:type        "text"
                             :placeholder "\"warmup\", \"repeat for 3 rounds\" "
                             :on-change   #(swap! template-state assoc-in [:parts i :title] (-> % .-target .-value))
                             :value       (get-in @template-state [:parts i :title])}]]]

       [:div.pure-g
        (for [m (get-in @template-state [:parts i :specific-movements])]
          ^{:key (str m (rand-int 1000))} [:div {:on-click #(let [movements (vec (remove #{m} (get-in @template-state [:parts i :specific-movements])))]
                                       (swap! template-state assoc-in [:parts i :specific-movements] movements))}
                     (movement-component (str m) (str "images/" (str/replace (str/lower-case m) " " "-") ".png"))])
        (for [n (range (get-in @template-state [:parts i :n]))]
          ^{:key (str n (rand-int 1000))} (movement-component "movement name" "images/push-up.png"))]


       [:div.pure-g
        [:div.pure-u
         [:button
          {:on-click #(swap! template-state update-in [:parts i :n] inc)} "+"]
         [:button
          {:on-click #(when (> n 0)
                       (swap! template-state update-in [:parts i :n] dec))} "-"]
         [:span {:style {:color "red"}} n] " generated movements should be randomly drawn from the following categories: "]]

       [:div.pure-g (for [c categories]
                      ^{:key c}
                      [:div.pure-u
                       [:div {:style {:margin-right "5px"}} c]
                       [:div {:on-click #(let [categories (vec (remove #{c} (get-in @template-state [:parts i :categories])))]
                                          (swap! template-state assoc-in [:parts i :categories] categories))} "x"]])]
       [:div.pure-g
        (let [id (str "ctags" i)
              categories-ac-comp (with-meta text-input-component
                                            {:component-did-mount #(auto-complete-did-mount
                                                                    (str "#" id)
                                                                    (vec (session/get :all-categories)))})]
          [:div.pure-u
           [categories-ac-comp {:id      (str "ctags" i)
                                :class   "edit" :placeholder "type to find and add category.."
                                :on-save #(when (and (some #{%} (session/get :all-categories))
                                                     (not (some #{%} (get-in @template-state [:parts i :categories]))))
                                           (swap! template-state update-in [:parts i :categories] conj %))}]])
        [:button.pure-u {:on-click #(handler-fn (reset! showing-categories-list (not @showing-categories-list)))}
         (if @showing-categories-list "Hide list of categories" "Show list of categories")]]

       (when @showing-categories-list
         (for [c (sort (session/get :all-categories))]
           ^{:key c} [:div.pure-g
                      [:div.pure-u {:style    {:background (when (some #{c} (get-in @template-state [:parts i :categories]))
                                                             "yellow")}
                                    :on-click #(when (not (some #{c} (get-in @template-state [:parts i :categories])))
                                                (swap! template-state update-in [:parts i :categories] conj c))} c]]))








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
                               :on-save #(when (and (some #{%} (session/get :all-movements))
                                                    (not (some #{%} (get-in @template-state [:parts i :specific-movements]))))
                                          (swap! template-state update-in [:parts i :specific-movements] conj %))}])]]])))

(defn save-template-component [error-atom]
  [:div.pure-g
   [:button.pure-u {:on-click #(do
                                (swap! template-state assoc :user (session/get :user))
                                (let [title (:title @template-state)
                                      parts (:parts @template-state)]
                                  (cond
                                    (nil? title) (reset! error-atom "The template needs a title.")
                                    (empty? parts) (reset! error-atom "A session must have 1 or more parts.")

                                    :else (print @template-state) #_(POST "template"
                                                {:params        @template-state
                                                 :handler       (fn [response] (print response))
                                                 :error-handler (fn [response] (reset! error-atom (:response response)))}))))}
    "Save"]])

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
            ^{:key (* i (rand-int 1000))} [part-creator-component (get parts i) i]))
        (save-template-component error)]])))