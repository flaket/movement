(ns movement.plan
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [cljs.core.async :as async :refer [timeout <!]]
            [movement.menu :refer [menu-component]]
            [movement.util :refer [text-input POST get-routines]]
            [movement.text :refer [text-input-component auto-complete-did-mount]]
            [movement.state :refer [handler-fn]]
            [movement.template :refer [movement-component]]
            [movement.user :refer [set-username-component]]
            [clojure.string :as str]))

(def plan-state (atom {}))
;title created-by public? description completed? template schedule schedule/sessions-per-day

(defn heading-component []
  [:div.pure-g
   [:h2.pure-u "Create a new Plan"]
   [:button.pure-u {:on-click #(pr (session/get :plans))} "My Plans"]])

(defn title-component []
  [:div
   [:div.pure-g
    [:h1.pure-u-1
     [:input {:type        "text"
              :size        50
              :placeholder "Plan Title"
              :on-change   #(swap! plan-state assoc :title (-> % .-target .-value))
              :value       (:title @plan-state)}]]]
   [:div.pure-g
    [:div.pure-u (str "by " (session/get :username))]]])

(defn description-component []
  [:div.pure-g
   [:div.pure-u
    [:textarea {:rows        3
                :cols        58
                :placeholder "Optional description of this plan"
                :on-change   #(swap! plan-state assoc :description (-> % .-target .-value))
                :value       (:description @plan-state)}]]])

(defn templates-component []
  [:div.pure-g
   [:div.pure-u-1-2
    (doall (for [t (:templates @plan-state)]
             ^{:key (rand-int 1000)} [:div t]))]
   [:div.pure-u-1-2
    [:div.pure-g
     [:h3.pure-u-1 "Your templates"]]
    (doall (for [t (sort (session/get :templates))]
             ^{:key (rand-int 1000)}
             [:div.pure-u.button {:style    {:cursor     'pointer
                                             :background (when (some #{t} (:templates @plan-state)) "yellow")}
                                  :on-click #(if (some #{t} (:templates @plan-state))
                                              (let [new-templates (remove #{t} (:templates @plan-state))]
                                                (swap! plan-state assoc :templates new-templates))
                                              (swap! plan-state update :templates conj t))} t]))]])

(defn error-component [error-atom]
   (let [message (:message @error-atom)]
     [:div
      [:div.pure-g
       [:h3.pure-u {:style {:color "red"}} message]]]))cat

(defn username-component []
  [:div
   [:div.pure-g
    [:div.pure-u-1 "To create new routines you must first select a username"]]
   [set-username-component]])

(defn save-plan-component [error-atom]
  (let [plan-stored-successfully? (atom false)]
    (fn []
      (if @plan-stored-successfully?
        (let []
          (go (<! (timeout 3000))
              (reset! plan-stored-successfully? false)
              (reset! plan-state {}))
          [:div.pure-g
           [:div.pure-u {:style {:margin-top 15 :font-size 24 :color "green"}} "Plan stored successfully!"]])
        [:div.pure-g
         [:p.pure-u.pure-u-md-2-5.button.button-primary
          {:on-click #(let [title (:title @plan-state)
                            templates (:templates @plan-state)]
                       (cond
                         (nil? title) (swap! error-atom assoc :message "The plan needs a title.")
                         (empty? templates) (swap! error-atom assoc :message "A plan consists of 1 or more templates.")
                         :else (let [username (session/get :username)
                                     email (session/get :email)
                                     plan (assoc @plan-state
                                               :public? true
                                               :created-by username)]
                                 (pr plan)
                                 #_(POST "plan"
                                       {:params        {:email   email
                                                        :plan plan}
                                        :handler       (fn [response] (do
                                                                        (reset! error-atom {})
                                                                        (reset! plan-stored-successfully? true)
                                                                        (get-routines)))
                                        :error-handler (fn [response] (do (pr response)
                                                                          (reset! error-atom response)))}))))}
          "Save Plan"]]))))

(defn plan-creator-component []
  (let [error (atom {:message ""})]
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content {:style {:margin-top "20px"}}
        (heading-component)
        (title-component)
        (description-component)
        [templates-component]
        (error-component error)
        (let [username (session/get :username)]
          (if (nil? username)
            (username-component)
            [save-plan-component error]))]])))