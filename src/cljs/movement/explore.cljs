(ns movement.explore
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [reagent-forms.core :refer [bind-fields]]
            [cljs.core.async :as async :refer [timeout <!]]
            [secretary.core :include-macros true :refer [dispatch!]]
            [movement.menu :refer [menu-component]]
            [movement.util :refer [handler-fn text-input GET POST get-templates]]
            [movement.text :refer [text-input-component auto-complete-did-mount]]
            [movement.generator :refer [image-url]]
            [movement.components.creator :refer [heading title description error]]
            [movement.template :refer [template-creator-component]]
            [movement.routine :refer [routine-creator-component]]
            [movement.group :refer [group-creator-component]]
            [movement.plan :refer [plan-creator-component]]
            [clojure.string :as str]
            [cljs.reader :refer [read-string]]))

(def selection (atom :temp))
(def search-state (atom {:number-of-results 4}))

(defn select-buttons []
  (let []
    (fn [selection]
      [:div {:style {:margin-top '40}}
       [:div.pure-g
        [:div.pure-u-1-2.pure-u-md-1-5.button {:className (when (= :movements @selection) "button-primary")
                                               :on-click  #(reset! selection :movements)} "Movements"]
        [:div.pure-u-1-2.pure-u-md-1-5.button {:className (when (= :templates @selection) "button-primary")
                                               :on-click  #(reset! selection :templates)} "Templates"]
        [:div.pure-u-1-2.pure-u-md-1-5.button {:className (when (= :groups @selection) "button-primary")
                                               :on-click  #(reset! selection :groups)} "Groups"]
        [:div.pure-u-1-2.pure-u-md-1-5.button {:className (when (= :plans @selection) "button-primary")
                                               :on-click  #(reset! selection :plans)} "Plans"]
        [:div.pure-u-1-2.pure-u-md-1-5.button {:className (when (= :routines @selection) "button-primary")
                                               :on-click  #(reset! selection :routines)} "Routines"]]])))

(defn movements-component []
  (let [show-categories? (atom true)
        all-results (atom false)]
    (fn []
      [:div {:style {:margin-top '20}}
       [:div.pure-g

        [:div.pure-u.pure-u-md-4-5
         [:div.pure-g
          [:div.pure-u.pure-u-md-1-2
           (let [id (str "explore-mtags")
                 movements-ac-comp (with-meta text-input-component
                                              {:component-did-mount #(auto-complete-did-mount
                                                                      (str "#" id)
                                                                      (vec (session/get :all-movements)))})]
             [movements-ac-comp {:id          id
                                 :class       "edit"
                                 :placeholder "Search for movements"
                                 :size        54
                                 :on-save     #()}])]
          [:div.pure-u.pure-u-md-1-2
           [:div.pure-g
            [:div.pure-u-1-2.button.button-primary "10 results"]
            [:div.pure-u-1-2.button "all results"]]]]
         (let [movements (:movements @search-state)]
           [:div.pure-g.movements
            (doall
              (for [m movements]
                ^{:key m}
                (let [name (:movement/unique-name m)]
                  [:div.pure-u.movement.small.is-center
                   [:h3.pure-g
                    [:div.pure-u-1-12]
                    [:div.pure-u.title name]]
                   [:img.graphic.small-graphic.pure-img-responsive {:src (image-url name) :title name :alt name}]
                   [:div {:style {:margin-bottom 10}}]])))])]

        [:div.pure-u.pure-u-md-1-5
         [:div.pure-g
          [:div.button.pure-u {:on-click #(handler-fn (reset! show-categories? (not @show-categories?)))}
           (str (if @show-categories? "Hide" "Show") " categories")]]
         (when @show-categories?
           (for [c (sort (session/get :all-categories))]
             ^{:key c}
             [:div.pure-g
              [:span.pure-u
               {:style    {:cursor     'pointer
                           :background (when (= c (:selected-category @search-state)) "gray")}
                :on-click #(GET "movements-by-category"
                                {:params        {:n        (:number-of-results @search-state)
                                                 :category c}
                                 :handler       (fn [r] (do
                                                          (swap! search-state assoc :selected-category c)
                                                          (swap! search-state assoc :movements r)))
                                 :error-handler (fn [r] (pr (str "error getting movements by category: " r)))})} c]]))]]

       ])))

(defn groups-component []
  (let []
    (fn []
      [:div {:style {:margin-top '20}}
       "Searching groups"])))

(defn plans-component []
  (let []
    (fn []
      [:div {:style {:margin-top '20}}
       "Searching plans"])))

(defn templates-component []
  (let []
    (fn []
      [:div {:style {:margin-top '20}}
       "Searching templates"])))

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
        [select-buttons selection]
        (case @selection
          :movements [movements-component]
          :groups [groups-component]
          :routines [routines-component]
          :templates [templates-component]
          :plans [plans-component]
          "")]])))