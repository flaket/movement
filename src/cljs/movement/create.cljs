(ns movement.create
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [reagent-forms.core :refer [bind-fields]]
            [cljs.core.async :as async :refer [timeout <!]]
            [secretary.core :include-macros true :refer [dispatch!]]
            [movement.menu :refer [menu-component]]
            [movement.util :refer [text-input POST get-templates]]
            [movement.text :refer [text-input-component auto-complete-did-mount]]
            [movement.components.creator :refer [heading title description error]]
            [movement.template :refer [template-creator-component]]
            [movement.routine :refer [routine-creator-component]]
            [movement.group :refer [group-creator-component]]
            [movement.plan :refer [plan-creator-component]]
            [clojure.string :as str]
            [cljs.reader :refer [read-string]]))

(def selection (atom :temp))

(defn new-buttons []
  (let []
    (fn [selection]
      [:div {:style {:margin-top '40}}
       [:div.pure-g
        [:div.pure-u-1-2.pure-u-md-1-4.button {:className (when (= :group @selection) "button-primary")
                                               :on-click  #(reset! selection :group)} "Group"]
        [:div.pure-u-1-2.pure-u-md-1-4.button {:className (when (= :routine @selection) "button-primary")
                                               :on-click #(reset! selection :routine)} "Routine"]
        [:div.pure-u-1-2.pure-u-md-1-4.button {:className (when (= :template @selection) "button-primary")
                                               :on-click #(reset! selection :template)} "Template"]
        [:div.pure-u-1-2.pure-u-md-1-4.button {:className (when (= :plan @selection) "button-primary")
                                               :on-click #(reset! selection :plan)} "Plan"]]])))

(defn create-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content {:style {:margin-top "20px"}}
        (heading "Create a new ..")
        [new-buttons selection]
        (case @selection
          :group [group-creator-component]
          :routine [routine-creator-component]
          :template [template-creator-component]
          :plan [plan-creator-component]
          "")]])))

