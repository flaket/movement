(ns movement.explore
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [reagent-forms.core :refer [bind-fields]]
            [cljs.core.async :as async :refer [timeout <!]]
            [secretary.core :include-macros true :refer [dispatch!]]
            [movement.menu :refer [menu-component]]
            [movement.util :refer [handler-fn text-input GET POST get-templates]]
            [movement.text :refer [text-input-component auto-complete-did-mount]]
            [movement.generator :refer [image-url add-movement-from-search]]
            [movement.components.creator :refer [heading title description error]]
            [movement.template :refer [template-creator-component]]
            [movement.routine :refer [routine-creator-component]]
            [movement.group :refer [group-creator-component]]
            [movement.plan :refer [plan-creator-component]]
            [clojure.string :as str]
            [cljs.reader :refer [read-string]]))

(def explore-state (atom {:number-of-results 8
                          :menu-selection :temp}))

(defn results-slider []
  (let [temp-data (atom (:number-of-results @explore-state))]
    (fn [min max step]
      [:div
       [:div.pure-g
        [:div.pure-u.pure-u-md-3-5 (str "Max number of results: " @temp-data)]]
       [:div.pure-g
        [:input.pure-u.pure-u-md-3-5 {:type        "range"
                                      :value       @temp-data :min min :max max :step step
                                      :on-mouse-up #(swap! explore-state assoc :number-of-results @temp-data)
                                      :on-change   #(reset! temp-data (-> % .-target .-value))}]
        [:a.pure-u.pure-u-md-1-5 {:style    {:margin-left 5}
                                  :on-click #(do
                                              (reset! temp-data 1000)
                                              (swap! explore-state assoc :number-of-results 1000))} "all"]]])))

(defn search-box [{:keys [type target placeholder]}]
  [:input.pure-u {:type      "text"
                      :placeholder placeholder
                      :size      32
                      :on-change #(swap! explore-state assoc-in [type target] (-> % .-target .-value))
                      :value     (target (type @explore-state))}])

(defn select-buttons []
  (let []
    (fn [explore-state]
      [:div {:style {:margin-top '40}}
       [:div.pure-g
        [:div.pure-u-1-2.pure-u-md-1-5.button {:className (when (= :movements (:menu-selection @explore-state)) "button-primary")
                                               :on-click  #(swap! explore-state assoc :menu-selection :movements)} "Movements"]
        [:div.pure-u-1-2.pure-u-md-1-5.button {:className (when (= :templates (:menu-selection @explore-state)) "button-primary")
                                               :on-click  #(swap! explore-state assoc :menu-selection :templates)} "Templates"]
        [:div.pure-u-1-2.pure-u-md-1-5.button {:className (when (= :groups (:menu-selection @explore-state)) "button-primary")
                                               :on-click  #(swap! explore-state assoc :menu-selection :groups)} "Groups"]
        [:div.pure-u-1-2.pure-u-md-1-5.button {:className (when (= :plans (:menu-selection @explore-state)) "button-primary")
                                               :on-click  #(swap! explore-state assoc :menu-selection :plans)} "Plans"]
        #_[:div.pure-u-1-2.pure-u-md-1-5.button {:className (when (= :routines (:menu-selection @explore-state)) "button-primary")
                                               :on-click  #(swap! explore-state assoc :menu-selection :routines)} "Routines"]]])))

(defn movements-component []
  (let []
    (fn []
      [:div {:style {:margin-top '20}}
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-5
         [:div.pure-g
          [:h3.pure-u "Categories"]]
         (doall (for [c (sort (session/get :all-categories))]
                  ^{:key c}
                  [:div.pure-g {:style {:cursor     'pointer
                                        :color (when (= c (:selected-category @explore-state)) "#fffff8")
                                        :background-color (when (= c (:selected-category @explore-state)) "gray")}}
                   [:span.pure-u-1
                    {:on-click #(GET "movements-by-category"
                                     {:params        {:n        (:number-of-results @explore-state)
                                                      :category c}
                                      :handler       (fn [r] (do
                                                               (swap! explore-state assoc :selected-category c)
                                                               (swap! explore-state assoc :movements r)))
                                      :error-handler (fn [r] (pr (str "error getting movements by category: " r)))})} c]]))]
        [:div.pure-u.pure-u-md-4-5
         [:div.pure-g
          [:div.pure-u.pure-u-md-1-2
           [results-slider 1 30 1]]
          [:div.pure-u.pure-u-md-1-2
           (let [id (str "explore-mtags")
                 movements-ac-comp (with-meta text-input-component
                                              {:component-did-mount #(auto-complete-did-mount
                                                                      (str "#" id)
                                                                      (vec (session/get :all-movements)))})]
             [movements-ac-comp {:id          id
                                 :class       "edit"
                                 :placeholder "Search for movement"
                                 :size        80
                                 :on-save     #(when (some #{%} (session/get :all-movements))
                                                (GET "movement"
                                                     {:params        {:name (str %)}
                                                      :handler       (fn [r] (swap! explore-state assoc
                                                                                    :movements [r]
                                                                                    :selected-category nil))
                                                      :error-handler (pr "error getting single movement through add.")}))}])]]
         (when-not (nil? (:movements @explore-state))
           [:div.pure-g
            [:div.pure-u-1 (str "Showing " (count (:movements @explore-state)) " results")]])
         (let [movements (:movements @explore-state)]
           [:div.pure-g.movements
            (doall
              (for [m movements]
                ^{:key (rand-int 10000)}
                (let [name (:movement/unique-name m)]
                  [:div.pure-u.movement.small.is-center
                   [:h3.pure-g
                    [:div.pure-u-1-24]
                    [:div.pure-u-11-12.title name]
                    [:div.pure-u-1-24]]
                   [:img.graphic.small-graphic.pure-img-responsive {:src (image-url name) :title name :alt name}]
                   [:div {:style {:margin-bottom 10}}]])))])]]])))

(defn templates-component []
  (let []
    (fn []
      [:div {:style {:margin-top '20}}
       [:div.pure-g
        [:div.pure-u-1-2.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Words in the template title"]]
         [:p.pure-g
          (search-box {:type :template :target :title :placeholder "handstand 5x5 yoga gymnastic"})]
         [:p.pure-g
          [:div.pure-u-1 "Categories used in the template"]]
         [:p.pure-g
          (for [c (get-in @explore-state [:template :categories])]
            ^{:key (str c (rand-int 10000))}
            [:span.pure-u {:style {:margin-right "5px"}}
             [:i.fa.fa-times {:style    {:cursor 'pointer :color 'red}
                              :on-click #(let [categories (vec (remove #{c} (get-in @explore-state [:template :categories])))]
                                          (swap! explore-state assoc-in [:template :categories] categories))}]
             c])]
         [:p.pure-g
          (let [id "category-tags"
                categories-ac-comp (with-meta text-input-component
                                              {:component-did-mount #(auto-complete-did-mount
                                                                      (str "#" id)
                                                                      (vec (session/get :all-categories)))})]
            [categories-ac-comp {:id          id
                                 :className   "pure-u-1"
                                 :class       "text"
                                 :placeholder "type to find and add category"
                                 :size        32
                                 :on-save     #(when (and (some #{%} (session/get :all-categories))
                                                          (not (some #{%} (get-in @explore-state [:template :categories]))))
                                                (swap! explore-state update-in [:template :categories] conj %))}])]]
        [:div.pure-u-1-2.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Words in the description"]]
         [:p.pure-g
          (search-box {:type :template :target :description :placeholder "endurance aerobic interval"})]
         [:p.pure-g
          [:div.pure-u-1 "Find all templates by a nickname"]]
         [:p.pure-g
          (search-box {:type :template :target :username :placeholder "movementsession"})]]]
       [:p.pure-g
        [:button.pure-u-1.pure-u-md-1-3.button.button-primary
         {:on-click #(let [t (:template @explore-state)
                           t (if (empty? (:title t)) (dissoc t :title) t)
                           t (if (empty? (:description t)) (dissoc t :description) t)
                           t (if (empty? (:categories t)) (dissoc t :categories) t)
                           template (assoc t :n 10)]
                      (pr template)
                      (GET "search/template" {:params        {:template template}
                                              :handler       (fn [r] (swap! explore-state assoc :templates r))
                                              :error-handler (fn [r] (pr r))}))} "Search"]]
       [:div.pure-g
        [:div.pure-u-1
         [:div.pure-g
          (when-not (nil? (:templates @explore-state))
            [:div.pure-g
             [:div.pure-u-1 (str "Showing " (count (:templates @explore-state)) " results")]])]
         (let [templates (:templates @explore-state)]
           [:div.pure-g.movements
            (doall
              (for [t templates]
                ^{:key (rand-int 10000)}
                (let [title (:template/title t)]
                  [:div.pure-u.movement.small.is-center
                   [:h3.pure-g
                    [:div.pure-u-1-12]
                    [:div.pure-u-3-4.title title]
                    [:div.pure-u-1-12]]
                   [:div {:style {:margin-bottom 10}}]])))])]]])))

(defn groups-component []
  (let []
    (fn []
      [:div {:style {:margin-top '20}}
       [:div.pure-g
        [:div.pure-u-1-2.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Words in the group title"]]
         [:p.pure-g
          (search-box {:type :group :target :title :placeholder ""})]
         [:p.pure-g
          [:div.pure-u-1 "Categories used by templates of the group"]]
         [:p.pure-g
          (for [c (get-in @explore-state [:group :categories])]
            ^{:key (str c (rand-int 10000))}
            [:span.pure-u {:style {:margin-right "5px"}}
             [:i.fa.fa-times {:style    {:cursor 'pointer :color 'red}
                              :on-click #(let [categories (vec (remove #{c} (get-in @explore-state [:group :categories])))]
                                          (swap! explore-state assoc-in [:group :categories] categories))}]
             c])]
         [:p.pure-g
          (let [id "category-tags"
                categories-ac-comp (with-meta text-input-component
                                              {:component-did-mount #(auto-complete-did-mount
                                                                      (str "#" id)
                                                                      (vec (session/get :all-categories)))})]
            [categories-ac-comp {:id          id
                                 :className   "pure-u-1"
                                 :class       "text"
                                 :placeholder "type to find and add category"
                                 :size        32
                                 :on-save     #(when (and (some #{%} (session/get :all-categories))
                                                          (not (some #{%} (get-in @explore-state [:group :categories]))))
                                                (swap! explore-state update-in [:group :categories] conj %))}])]]
        [:div.pure-u-1-2.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Words in the group description"]]
         [:p.pure-g
          (search-box {:type :group :target :description :placeholder ""})]
         [:p.pure-g
          [:div.pure-u-1 "Find all groups by a nickname"]]
         [:p.pure-g
          (search-box {:type :group :target :username :placeholder "movementsession"})]]]
       [:p.pure-g
        [:button.pure-u-1.pure-u-md-1-3.button.button-primary
         {:on-click #(let [g (:group @explore-state)
                           g (if (empty? (:title g)) (dissoc g :title) g)
                           g (if (empty? (:categories g)) (dissoc g :categories) g)
                           g (if (empty? (:description g)) (dissoc g :description) g)
                           group (assoc g :n 10)]
                      (pr group)
                      (GET "search/group" {:params        {:group group}
                                              :handler       (fn [r] (swap! explore-state assoc :groups r))
                                              :error-handler (fn [r] (pr r))}))} "Search"]]
       [:div.pure-g
        [:div.pure-u-1
         [:div.pure-g
          (when-not (nil? (:groups @explore-state))
            [:div.pure-g
             [:div.pure-u-1 (str "Showing " (count (:groups @explore-state)) " results")]])]
         (let [groups (:groups @explore-state)]
           [:div.pure-g.movements
            (doall
              (for [g groups]
                ^{:key (rand-int 10000)}
                (let [title (:group/title g)]
                  [:div.pure-u.movement.small.is-center
                   [:h3.pure-g
                    [:div.pure-u-1-12]
                    [:div.pure-u-3-4.title title]
                    [:div.pure-u-1-12]]
                   [:div {:style {:margin-bottom 10}}]])))])]]])))

(defn plans-component []
  (let []
    (fn []
      [:div {:style {:margin-top '20}}
       [:div.pure-g
        [:div.pure-u-1-2.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Words in the plan title"]]
         [:p.pure-g
          (search-box {:type :plan :target :title :placeholder ""})]
         [:p.pure-g
          [:div.pure-u-1 "Categories used by templates in the plan"]]
         [:p.pure-g
          (for [c (get-in @explore-state [:plan :categories])]
            ^{:key (str c (rand-int 10000))}
            [:span.pure-u {:style {:margin-right "5px"}}
             [:i.fa.fa-times {:style    {:cursor 'pointer :color 'red}
                              :on-click #(let [categories (vec (remove #{c} (get-in @explore-state [:plan :categories])))]
                                          (swap! explore-state assoc-in [:plan :categories] categories))}]
             c])]
         [:p.pure-g
          (let [id "category-tags"
                categories-ac-comp (with-meta text-input-component
                                              {:component-did-mount #(auto-complete-did-mount
                                                                      (str "#" id)
                                                                      (vec (session/get :all-categories)))})]
            [categories-ac-comp {:id          id
                                 :className   "pure-u-1"
                                 :class       "text"
                                 :placeholder "type to find and add category"
                                 :size        32
                                 :on-save     #(when (and (some #{%} (session/get :all-categories))
                                                          (not (some #{%} (get-in @explore-state [:plan :categories]))))
                                                (swap! explore-state update-in [:plan :categories] conj %))}])]]
        [:div.pure-u-1-2.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Words in the plan description"]]
         [:p.pure-g
          (search-box {:type :plan :target :description :placeholder ""})]
         [:p.pure-g
          [:div.pure-u-1 "Find all plans by a nickname"]]
         [:p.pure-g
          (search-box {:type :plan :target :username :placeholder "movementsession"})]]]
       [:p.pure-g
        [:button.pure-u-1.pure-u-md-1-3.button.button-primary
         {:on-click #(let [p (:plan @explore-state)
                           p (if (empty? (:title p)) (dissoc p :title) p)
                           p (if (empty? (:categories p)) (dissoc p :categories) p)
                           p (if (empty? (:description p)) (dissoc p :description) p)
                           plan (assoc p :n 10)]
                      (pr plan)
                      (GET "search/plan" {:params        {:plan plan}
                                           :handler       (fn [r] (swap! explore-state assoc :plans r))
                                           :error-handler (fn [r] (pr r))}))} "Search"]]
       [:div.pure-g
        [:div.pure-u-1
         [:div.pure-g
          (when-not (nil? (:plans @explore-state))
            [:div.pure-g
             [:div.pure-u-1 (str "Showing " (count (:plans @explore-state)) " results")]])]
         (let [plans (:plans @explore-state)]
           [:div.pure-g.movements
            (doall
              (for [p plans]
                ^{:key (rand-int 10000)}
                (let [title (:plan/title p)]
                  [:div.pure-u.movement.small.is-center
                   [:h3.pure-g
                    [:div.pure-u-1-12]
                    [:div.pure-u-3-4.title title]
                    [:div.pure-u-1-12]]
                   [:div {:style {:margin-bottom 10}}]])))])]]])))

(defn routines-component []
  (let []
    (fn []
      [:div {:style {:margin-top '20}}
       "Searching routines"])))

(defn explore-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content {:style {:margin-top '20}}
        (heading "Explore")
        [select-buttons explore-state]
        (case (:menu-selection @explore-state)
          :movements [movements-component]
          :templates [templates-component]
          :groups [groups-component]
          :plans [plans-component]
          ;:routines [routines-component]
          "")]])))