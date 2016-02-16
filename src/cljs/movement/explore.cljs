(ns movement.explore
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [reagent-forms.core :refer [bind-fields]]
            [cljs.core.async :as async :refer [timeout <!]]
            [secretary.core :include-macros true :refer [dispatch!]]
            [movement.menu :refer [menu-component]]
            [movement.util :refer [handler-fn text-input GET POST get-templates get-groups get-plans]]
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
                          :menu-selection    :temp}))

(defn results-slider []
  (let [temp-data (atom (:number-of-results @explore-state))]
    (fn [min max step]
      [:div
       [:div.pure-g
        [:div.pure-u.pure-u-md-3-5 (str "Max number of results: " @temp-data)]]
       [:div.pure-g
        [:input.pure-u.pure-u-md-3-5 {:type        "range"
                                      :value       @temp-data :min min :max max :step step
                                      :on-mouse-up #(do
                                                     (swap! explore-state assoc :number-of-results @temp-data)
                                                     (when-let [c (:selected-category @explore-state)]
                                                       (GET "movements-by-category"
                                                            {:params        {:n        (:number-of-results @explore-state)
                                                                             :category c}
                                                             :handler       (fn [r] (swap! explore-state assoc :movements r))
                                                             :error-handler (fn [r] (pr (str "error getting movements by category: " r)))})))
                                      :on-change   #(reset! temp-data (-> % .-target .-value))}]
        [:a.pure-u.pure-u-md-1-5 {:style    {:margin-left 5}
                                  :on-click #(do
                                              (reset! temp-data 1000)
                                              (swap! explore-state assoc :number-of-results 1000)
                                              (when-let [c (:selected-category @explore-state)]
                                                (GET "movements-by-category"
                                                     {:params        {:n        (:number-of-results @explore-state)
                                                                      :category c}
                                                      :handler       (fn [r] (swap! explore-state assoc :movements r))
                                                      :error-handler (fn [r] (pr (str "error getting movements by category: " r)))})))} "all"]]])))

(defn search-box [{:keys [type target placeholder]}]
  [:input.pure-u {:type        "text"
                  :placeholder placeholder
                  :size        32
                  :on-change   #(swap! explore-state assoc-in [type target] (-> % .-target .-value))
                  :value       (target (type @explore-state))}])

(defn select-buttons []
  (let []
    (fn [explore-state]
      [:div {:style {:margin-top '40}}
       [:div.pure-g
        [:div.pure-u-1-2.pure-u-md-1-5.button {:style     {:margin-right 5}
                                               :className (when (= :movements (:menu-selection @explore-state)) "button-primary")
                                               :on-click  #(swap! explore-state assoc :menu-selection :movements)} "Movements"]
        [:div.pure-u-1-2.pure-u-md-1-5.button {:style     {:margin-right 5}
                                               :className (when (= :templates (:menu-selection @explore-state)) "button-primary")
                                               :on-click  #(swap! explore-state assoc :menu-selection :templates)} "Templates"]
        [:div.pure-u-1-2.pure-u-md-1-5.button {:style     {:margin-right 5}
                                               :className (when (= :groups (:menu-selection @explore-state)) "button-primary")
                                               :on-click  #(swap! explore-state assoc :menu-selection :groups)} "Groups"]
        [:div.pure-u-1-2.pure-u-md-1-5.button {:style     {:margin-right 5}
                                               :className (when (= :plans (:menu-selection @explore-state)) "button-primary")
                                               :on-click  #(swap! explore-state assoc :menu-selection :plans)} "Plans"]
        #_[:div.pure-u-1-2.pure-u-md-1-5.button {:className (when (= :routines (:menu-selection @explore-state)) "button-primary")
                                                 :on-click  #(swap! explore-state assoc :menu-selection :routines)} "Routines"]]])))

(defn zone-data [val local-zone name]
  (cond
    (= :zone/one val) [:div.pure-u-1.center.dim
                       [:i.fa.fa-star.gold {:title "You're still in the learning phase with this movement"}]
                       [:i.fa.fa-star-o.star {:on-click #(POST "set-zone" {:params        {:email (session/get :email)
                                                                                           :name  name
                                                                                           :zone  :zone/two}
                                                                           :handler       (fn [r] (reset! local-zone :zone/two))
                                                                           :error-handler (fn [r] (pr "error setting zone data: " r))})
                                              :style    {:cursor 'pointer}
                                              :title    "Give two stars to indicate that you now know this movement well."}]
                       [:i.fa.fa-star-o.star {:on-click #(POST "set-zone" {:params        {:email (session/get :email)
                                                                                           :name  name
                                                                                           :zone  :zone/three}
                                                                           :handler       (fn [r] (reset! local-zone :zone/three))
                                                                           :error-handler (fn [r] (pr "error setting zone data: " r))})
                                              :style    {:cursor 'pointer}
                                              :title    "Give three stars to indicate that you have mastered this movement."}]]
    (= :zone/two val) [:div.pure-u-1.center.dim
                       [:i.fa.fa-star.gold.star {:on-click #(POST "set-zone" {:params        {:email (session/get :email)
                                                                                              :name  name
                                                                                              :zone  :zone/one}
                                                                              :handler       (fn [r] (reset! local-zone :zone/one))
                                                                              :error-handler (fn [r] (pr "error setting zone data: " r))})
                                                 :style    {:cursor 'pointer}
                                                 :title    "Go back to one star if you no longer can do this movement well."}]
                       [:i.fa.fa-star.gold {:title "You know this movement well, but it is not perfected. You're effective, but not efficient."}]
                       [:i.fa.fa-star-o.star {:on-click #(POST "set-zone" {:params        {:email (session/get :email)
                                                                                           :name  name
                                                                                           :zone  :zone/three}
                                                                           :handler       (fn [r] (reset! local-zone :zone/three))
                                                                           :error-handler (fn [r] (pr "error setting zone data: " r))})
                                              :style    {:cursor 'pointer}
                                              :title    "Give three stars to indicate that you have mastered this movement."}]]
    (= :zone/three val) [:div.pure-u-1.center.dim
                         [:i.fa.fa-star.gold.star {:on-click #(POST "set-zone" {:params        {:email (session/get :email)
                                                                                                :name  name
                                                                                                :zone  :zone/one}
                                                                                :handler       (fn [r] (reset! local-zone :zone/one))
                                                                                :error-handler (fn [r] (pr "error setting zone data: " r))})
                                                   :style    {:cursor 'pointer}
                                                   :title    "Go back to one star if you no longer can do this movement well."}]
                         [:i.fa.fa-star.gold.star {:on-click #(POST "set-zone" {:params        {:email (session/get :email)
                                                                                                :name  name
                                                                                                :zone  :zone/two}
                                                                                :handler       (fn [r] (reset! local-zone :zone/two))
                                                                                :error-handler (fn [r] (pr "error setting zone data: " r))})
                                                   :style    {:cursor 'pointer}
                                                   :title    "Go back to two stars if you no longer master this movement."}]
                         [:i.fa.fa-star.gold {:title "You have mastered this movement. You are both effective and efficient."}]]))

(defn movement-component [name zone selected? category]
  (let []
    (fn [name zone selected?]
      [:div.pure-u.movement {:className (if selected? "explore-selected" "small")}
       [:h3.pure-g.center
        (if selected?
          [:div.pure-u-1 {:style {:cursor 'default}} name]
          [:div.pure-u-1 {:style    {:cursor 'pointer}
                          :on-click #(GET "explore-movement"
                                          {:params        {:unique-name name
                                                           :email       (session/get :email)}
                                           :handler       (fn [r] (do
                                                                    (pr r)
                                                                    (swap! explore-state dissoc :movements)
                                                                    (swap! explore-state assoc :selected-movement r)))
                                           :error-handler (fn [r] (pr "error exploring-movement: " r))})}
           name])]
       [:div.pure-g
        (let [val @zone]
          (zone-data val zone name))]
       [:div.center
        (if selected?
          [:img.graphic.pure-img-responsive {:src   (image-url name) :title name :alt name
                                             :style {:margin-bottom 10}}]
          [:img.graphic.pure-img-responsive {:className (if selected? "" "small-graphic")
                                             :src       (image-url name) :title name :alt name
                                             :style     {:margin-bottom 10
                                                         :cursor        'pointer}
                                             :on-click  #(GET "explore-movement"
                                                              {:params        {:unique-name name
                                                                               :email       (session/get :email)}
                                                               :handler       (fn [r] (do
                                                                                        (pr r)
                                                                                        (swap! explore-state dissoc :movements)
                                                                                        (swap! explore-state assoc :selected-movement r)))
                                                               :error-handler (fn [r] (pr "error exploring-movement: " r))})}])]
       (when selected?
         (for [c category]
           [:div.pure-g
            [:div.pure-u-1.center.explore-link {:style    {:cursor 'pointer}
                                                :on-click #(GET "movements-by-category"
                                                                {:params        {:n        (:number-of-results @explore-state)
                                                                                 :category (:category/name c)}
                                                                 :handler       (fn [r] (do
                                                                                          (swap! explore-state assoc :selected-category (:category/name c))
                                                                                          (swap! explore-state dissoc :selected-movement)
                                                                                          (swap! explore-state assoc :movements r)))
                                                                 :error-handler (fn [r] (pr (str "error getting movements by category: " r)))})}
             (:category/name c)]]))])))

(defn movements-component []
  (let []
    (fn []
      [:div {:style {:margin-top '20}}
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-5
         [:div.pure-g
          [:span.pure-u {:style {:margin-bottom 10}} "See movements by category"]]
         (let [categories (sort (session/get :all-categories))]
           (doall
             (for [c categories]
               ^{:key c}
               [:div.pure-g {:style {:cursor           'pointer
                                     :color            (when (= c (:selected-category @explore-state)) "#fffff8")
                                     :background-color (when (= c (:selected-category @explore-state)) "gray")}}
                [:span.pure-u-1.explore-link
                 {:style    {:color (when (and (= "Practical Movements" c)
                                               (not (= c (:selected-category @explore-state))))
                                      "red")}
                  :on-click #(GET "movements-by-category"
                                  {:params        {:n        (:number-of-results @explore-state)
                                                   :category c}
                                   :handler       (fn [r] (do
                                                            (swap! explore-state assoc :selected-category c)
                                                            (swap! explore-state dissoc :selected-movement)
                                                            (swap! explore-state assoc :movements r)))
                                   :error-handler (fn [r] (pr (str "error getting movements by category: " r)))})} c]])))]
        [:div.pure-u.pure-u-md-4-5
         [:div.pure-g
          [:div.pure-u.pure-u-md-1-3
           [results-slider 1 30 1]]
          [:div.pure-u.pure-u-md-1-3
           (let [id (str "explore-mtags")
                 movements-ac-comp (with-meta text-input-component
                                              {:component-did-mount #(auto-complete-did-mount
                                                                      (str "#" id)
                                                                      (vec (session/get :all-movements)))})]
             [movements-ac-comp {:id          id
                                 :class       "edit"
                                 :placeholder "Search for movement"
                                 :size        32
                                 :on-save     #(when (some #{%} (session/get :all-movements))
                                                (GET "movement"
                                                     {:params        {:name (str %)}
                                                      :handler       (fn [r] (do
                                                                               (swap! explore-state dissoc :movements)
                                                                               (swap! explore-state assoc :selected-movement r)))
                                                      :error-handler (pr "error getting single movement through add.")}))}])]
          [:div.pure-u.pure-u-md-1-3
           [:button.button.button-primary {:on-click #(GET "user-movements"
                                                           {:params        {:email (session/get :email)}
                                                            :handler       (fn [r] (do
                                                                                     (swap! explore-state dissoc :selected-movement)
                                                                                     (swap! explore-state assoc :movements r)))
                                                            :error-handler (fn [r] (pr (str "error getting user movements: " r)))})}
            "Movements I have done"]]]
         (when-not (nil? (:movements @explore-state))
           [:div.pure-g
            [:div.pure-u-1 (str "Showing " (count (:movements @explore-state)) " results")]])
         (let [movements (:movements @explore-state)]
           (if movements
             [:div.pure-g.movements
              (doall
                (for [m movements]
                  ^{:key (:db/id m)}
                  [movement-component (if (nil? (:movement/unique-name m)) (:movement/name m) (:movement/unique-name m)) (atom (:db/ident (:movement/zone m))) false (:movement/category m)]))]
             (when-let [movement (:selected-movement @explore-state)]
               (let [easier (:movement/easier movement)
                     harder (:movement/harder movement)]
                 [:div.movements
                  [:div.pure-g
                   [:div.pure-u-1-3
                    [:div.pure-g
                     [:div.pure-u-3-4
                      (for [m easier]
                        [:div.pure-g.center
                         [movement-component (if (nil? (:movement/unique-name m)) (:movement/name m) (:movement/unique-name m)) (atom (:db/ident (:movement/zone m))) false (:movement/category m)]])]
                     [:div.pure-u-1-4
                      (when-not (empty? easier)
                        [:div.explore-green [:i.fa.fa-arrow-right]])]]]

                   [:div.pure-u-1-3
                    [:div.pure-g
                     [movement-component (if (nil? (:movement/unique-name movement)) (:movement/name movement) (:movement/unique-name movement)) (atom (:db/ident (:movement/zone movement))) true (:movement/category movement)]]]

                   [:div.pure-u-1-3
                    [:div.pure-g
                     [:div.pure-u-1-4
                      (when-not (empty? harder)
                        [:div.explore-green [:i.fa.fa-arrow-right]])]
                     [:div.pure-u-3-4
                      (for [m harder]
                        [:div.pure-g
                         [:div.pure-u-1-5]
                         [movement-component (if (nil? (:movement/unique-name m)) (:movement/name m) (:movement/unique-name m)) (atom (:db/ident (:movement/zone m))) false (:movement/category m)]])]]]]]))))]]])))

(defn template-result [t]
  (let [selected (atom false)
        title (:template/title t)
        created-by (:template/created-by t)
        description (:template/description t)
        parts (:template/part t)
        template-id (some #(when (= (:db/id t)
                                    (:db/id %)) (:db/id %))
                          (session/get :templates))]
    (fn [t]
      [:div.pure-u-1 {:style {:margin-top 10}}
       [:div.pure-g
        (when @selected
          [:div.pure-u [:button.button.button-secondary
                        {:on-click #(when-not (nil? template-id)
                                     (POST "dissoc/template"
                                           {:params        {:email (session/get :email) :id template-id}
                                            :handler       (fn [r] (do
                                                                     (reset! selected false)
                                                                     (get-templates)))
                                            :error-handler (fn [r] (pr (str "error dissoc'ing template: " (:message r))))}))}
                        "Remove"]])
        [:div.pure-u {:style {:margin-right 5}}
         (if-not (nil? template-id)
           [:div {:style    {:color 'green :cursor 'pointer}
                  :on-click #(handler-fn (reset! selected (not @selected)))} [:i.fa.fa-check.fa-2x]]
           [:button.button.button-secondary
            {:on-click #(POST "assoc/template"
                              {:params        {:email (session/get :email) :id (:db/id t)}
                               :handler       (fn [r] (get-templates))
                               :error-handler (fn [r] (pr (str "error assoc'ing template: " r)))})}
            "Add"])]
        [:div.pure-u {:style {:margin-right 5
                              :font-size    "150%"}} title]
        [:div.pure-u "by "
         [:span {:style    {:cursor 'pointer :text-decoration 'underline}
                 :on-click #(GET "search/template"
                                 {:params        {:template {:n 100 :username created-by}}
                                  :handler       (fn [r] (swap! explore-state assoc :templates r))
                                  :error-handler (fn [r] (pr r))})}
          created-by]]]
       [:div.pure-g
        [:div.pure-u-1 description]]
       [:div.pure-g
        [:div.pure-u (str "Template divided into " (count parts) " parts, named: "
                          (str/join (interpose ", " parts)))]]])))

(defn templates-component []
  (let []
    (fn []
      [:div {:style {:margin-top '20}}
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Words in the template title"]]
         [:p.pure-g
          (search-box {:type :template :target :title :placeholder "e.g. handstand 5x5 yoga gymnastic"})]]
        [:div.pure-u.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Words in the template description"]]
         [:p.pure-g
          (search-box {:type :template :target :description :placeholder "e.g. endurance aerobic interval"})]]
        [:div.pure-u.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Categories used in the template"]]
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
                                                (swap! explore-state update-in [:template :categories] conj %))}])]
         [:p.pure-g
          (for [c (get-in @explore-state [:template :categories])]
            ^{:key (str c (rand-int 10000))}
            [:span.pure-u {:style {:margin-right "5px"}}
             [:i.fa.fa-times {:style    {:cursor 'pointer :color 'red}
                              :on-click #(let [categories (vec (remove #{c} (get-in @explore-state [:template :categories])))]
                                          (swap! explore-state assoc-in [:template :categories] categories))}] c])]]]
       [:div.pure-g
        [:button.pure-u.pure-u-md-1-3.button.button-primary
         {:on-click #(let [t (:template @explore-state)
                           t (if (empty? (:title t)) (dissoc t :title) t)
                           t (if (empty? (:description t)) (dissoc t :description) t)
                           t (if (empty? (:categories t)) (dissoc t :categories) t)
                           t (if-not (empty? (:categories t))
                               (assoc t :categories (str/join (interpose " " (:categories t))))
                               t)
                           template (assoc t :n 100)]
                      (pr template)
                      (GET "search/template" {:params        {:template template}
                                              :handler       (fn [r] (swap! explore-state assoc :templates r))
                                              :error-handler (fn [r] (pr r))}))} "Search"]
        [:div.pure-u.pure-u-md-1-3 {:style {:margin-left 5}}
         [:div.pure-g
          [:a.pure-u
           {:style    {:text-decoration 'underline :margin-bottom 5}
            :on-click #(GET "search/template"
                            {:params        {:template {:n 100 :username "movementsession"}}
                             :handler       (fn [r] (swap! explore-state assoc :templates r))
                             :error-handler (fn [r] (pr r))})} "Templates created by movementsession"]]
         [:div.pure-g
          [:a.pure-u
           {:style    {:text-decoration 'underline :margin-bottom 5}
            :on-click #(GET "search/template"
                            {:params        {:template {:n 100 :username (session/get :username)}}
                             :handler       (fn [r] (swap! explore-state assoc :templates r))
                             :error-handler (fn [r] (pr r))})} "Templates you have created"]]
         [:div.pure-g
          [:a.pure-u
           {:style    {:text-decoration 'underline :margin-bottom 5}
            :on-click #(swap! explore-state assoc :templates (session/get :templates))} "Your current templates"]]]]
       (when-not (nil? (:templates @explore-state))
         [:div.pure-g
          [:div.pure-u-1 (str "Showing " (count (:templates @explore-state)) " results")]])
       (let [templates (:templates @explore-state)]
         [:div.pure-g.movements {:style {:border-top "dotted 1px"}}
          (doall
            (for [t templates]
              ^{:key (:db/id t)}
              [template-result t]))])])))

(defn group-result [g]
  (let [selected (atom false)
        title (:group/title g)
        created-by (:group/created-by g)
        description (:group/description g)
        templates (:group/template g)
        group-id (some #(when (= (:db/id g)
                                 (:db/id %)) (:db/id %))
                       (session/get :groups))]
    (fn [g]
      [:div.pure-g {:style {:margin-top 10}}
       [:div.pure-u-1
        [:div.pure-g
         (when @selected
           [:div.pure-u [:button.button.button-secondary
                         {:on-click #(when-not (nil? group-id)
                                      (POST "dissoc/group"
                                            {:params        {:email (session/get :email) :id group-id}
                                             :handler       (fn [r] (do
                                                                      (reset! selected false)
                                                                      (get-groups)))
                                             :error-handler (fn [r] (pr (str "error dissoc'ing group: " r)))}))}
                         "Remove"]])
         [:div.pure-u {:style {:margin-right 5}}
          (if-not (nil? group-id)
            [:div {:style    {:color 'green :cursor 'pointer}
                   :on-click #(handler-fn (reset! selected (not @selected)))} [:i.fa.fa-check.fa-2x]]
            [:button.button.button-secondary
             {:on-click #(POST "assoc/group"
                               {:params        {:email (session/get :email) :id (:db/id g)}
                                :handler       (fn [r] (get-groups))
                                :error-handler (fn [r] (pr (str "error assoc'ing group: " r)))})}
             "Add"])]
         [:div.pure-u {:style {:margin-right 5
                               :font-size    "150%"}} title]
         [:div.pure-u (str "by " (:db/id created-by))]]
        [:div.pure-g
         [:div.pure-u-1 description]]
        [:div.pure-g
         [:div.pure-u (str "Group contains " (count templates) " templates:")]
         (for [t templates]
           ^{:key (:db/id t)}
           [:div.pure-u {:style {:margin-left 5}} (str (:db/id t))])]]])))

(defn groups-component []
  (let []
    (fn []
      [:div {:style {:margin-top '20}}
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Words in the group title"]]
         [:p.pure-g
          (search-box {:type :group :target :title :placeholder ""})]]
        [:div.pure-u.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Words in the group description"]]
         [:p.pure-g
          (search-box {:type :group :target :description :placeholder ""})]]
        [:div.pure-u.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Categories used by templates of the group"]]
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
                                                (swap! explore-state update-in [:group :categories] conj %))}])]
         [:p.pure-g
          (for [c (get-in @explore-state [:group :categories])]
            ^{:key (str c (rand-int 10000))}
            [:span.pure-u {:style {:margin-right "5px"}}
             [:i.fa.fa-times {:style    {:cursor 'pointer :color 'red}
                              :on-click #(let [categories (vec (remove #{c} (get-in @explore-state [:group :categories])))]
                                          (swap! explore-state assoc-in [:group :categories] categories))}] c])]]]
       [:div.pure-g
        [:button.pure-u.pure-u-md-1-3.button.button-primary
         {:on-click #(let [g (:group @explore-state)
                           g (if (empty? (:title g)) (dissoc g :title) g)
                           g (if (empty? (:categories g)) (dissoc g :categories) g)
                           g (if (empty? (:description g)) (dissoc g :description) g)
                           g (if-not (empty? (:categories g))
                               (assoc g :categories (str/join (interpose " " (:categories g))))
                               g)
                           group (assoc g :n 1000)]
                      (pr group)
                      (GET "search/group" {:params        {:group group}
                                           :handler       (fn [r] (swap! explore-state assoc :groups r))
                                           :error-handler (fn [r] (pr r))}))} "Search"]
        [:div.pure-u.pure-u-md-1-3 {:style {:margin-left 5}}
         [:div.pure-g
          [:a.pure-u
           {:style    {:margin-bottom 5 :text-decoration 'underline}
            :on-click #(GET "search/group"
                            {:params        {:group {:n 1000 :username "movementsession"}}
                             :handler       (fn [r] (swap! explore-state assoc :groups r))
                             :error-handler (fn [r] (pr r))})} "Groups created by movementsession"]]
         [:div.pure-g
          [:a.pure-u
           {:style    {:margin-bottom 5 :text-decoration 'underline}
            :on-click #(GET "search/group"
                            {:params        {:group {:n 1000 :username (session/get :username)}}
                             :handler       (fn [r] (swap! explore-state assoc :groups r))
                             :error-handler (fn [r] (pr r))})} "Groups you have created"]]
         [:div.pure-g
          [:a.pure-u
           {:style    {:text-decoration 'underline :margin-bottom 5}
            :on-click #(swap! explore-state assoc :groups (session/get :groups))} "Your current groups"]]]]
       (when-not (nil? (:groups @explore-state))
         [:div.pure-g
          [:div.pure-u-1 (str "Showing " (count (:groups @explore-state)) " results")]])
       (let [groups (:groups @explore-state)]
         [:div.pure-g.movements {:style {:border-top "dotted 1px"}}
          (doall
            (for [g groups]
              ^{:key (:db/id g)}
              [group-result g]))])])))

(defn plan-result [p]
  (let [title (:plan/title p)
        created-by (:plan/created-by p)
        description (:plan/description p)
        days (:plan/day p)
        my-plan-id (some #(when (and (= (:plan/created-by p)
                                        (:plan/created-by %))
                                     (= (:plan/day p)
                                        (:plan/day %))) (:db/id %))
                         (session/get :plans))]
    (fn [p]
      [:div.pure-g {:style {:margin-top 10}}
       [:div.pure-u-1
        [:div.pure-g
         [:div.pure-u {:style {:margin-right 5}}
          (if-not (nil? my-plan-id)
            [:div {:style {:color 'green}} [:i.fa.fa-check.fa-2x]]
            [:button.button.button-secondary
             {:on-click #(POST "assoc/plan"
                               {:params        {:email (session/get :email) :id (:db/id p)}
                                :handler       (fn [r] (get-plans))
                                :error-handler (fn [r] (pr (str "error assoc'ing plan: " r)))})}
             "Add"])]
         [:div.pure-u {:style {:margin-right 5
                               :font-size    "150%"}} title]
         [:div.pure-u (str "by " (:db/id created-by))]]
        [:div.pure-g
         [:div.pure-u-1 description]]
        [:div.pure-g
         [:div.pure-u (str "Plan goes over " (count days) " days:")]
         (for [d days]
           ^{:key (:db/id d)}
           [:div.pure-u {:style {:margin-left 5}} (str (:db/id d))])]]])))

(defn plans-component []
  (let []
    (fn []
      [:div {:style {:margin-top '20}}
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Words in the plan title"]]
         [:p.pure-g
          (search-box {:type :plan :target :title :placeholder ""})]]
        [:div.pure-u.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Words in the plan description"]]
         [:p.pure-g
          (search-box {:type :plan :target :description :placeholder ""})]]
        [:div.pure-u.pure-u-md-1-3
         [:p.pure-g
          [:div.pure-u-1 "Categories used by templates in the plan"]]
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
                                                (swap! explore-state update-in [:plan :categories] conj %))}])]
         [:p.pure-g
          (for [c (get-in @explore-state [:plan :categories])]
            ^{:key (str c (rand-int 10000))}
            [:span.pure-u {:style {:margin-right "5px"}}
             [:i.fa.fa-times {:style    {:cursor 'pointer :color 'red}
                              :on-click #(let [categories (vec (remove #{c} (get-in @explore-state [:plan :categories])))]
                                          (swap! explore-state assoc-in [:plan :categories] categories))}] c])]]]
       [:div.pure-g
        [:button.pure-u.pure-u-md-1-3.button.button-primary
         {:on-click #(let [p (:plan @explore-state)
                           p (if (empty? (:title p)) (dissoc p :title) p)
                           p (if (empty? (:categories p)) (dissoc p :categories) p)
                           p (if (empty? (:description p)) (dissoc p :description) p)
                           p (if-not (empty? (:categories p))
                               (assoc p :categories (str/join (interpose " " (:categories p))))
                               p)
                           plan (assoc p :n 100)]
                      (pr plan)
                      (GET "search/plan" {:params        {:plan plan}
                                          :handler       (fn [r] (swap! explore-state assoc :plans r))
                                          :error-handler (fn [r] (pr r))}))} "Search"]
        [:div.pure-u.pure-u-md-1-3 {:style {:margin-left 5}}
         [:div.pure-g
          [:a.pure-u
           {:style    {:text-decoration 'underline :margin-bottom 5}
            :on-click #(GET "search/plan"
                            {:params        {:plan {:n 100 :username "movementsession"}}
                             :handler       (fn [r] (swap! explore-state assoc :plans r))
                             :error-handler (fn [r] (pr r))})} "Plans created by movementsession"]]
         [:div.pure-g
          [:a.pure-u
           {:style    {:text-decoration 'underline :margin-bottom 5}
            :on-click #(GET "search/plan"
                            {:params        {:plan {:n 100 :username (session/get :username)}}
                             :handler       (fn [r] (swap! explore-state assoc :plans r))
                             :error-handler (fn [r] (pr r))})} "Plans you have created"]]
         [:div.pure-g
          [:a.pure-u
           {:style    {:text-decoration 'underline :margin-bottom 5}
            :on-click #(swap! explore-state assoc :plans (session/get :plans))} "Your current ongoing plans"]]]]
       (when-not (nil? (:plans @explore-state))
         [:div.pure-g
          [:div.pure-u-1 (str "Showing " (count (:plans @explore-state)) " results")]])
       (let [plans (:plans @explore-state)]
         [:div.pure-g.movements {:style {:border-top "dotted 1px"}}
          (doall
            (for [p plans]
              ^{:key (:db/id p)}
              [plan-result p]))])])))

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