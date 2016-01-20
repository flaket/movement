(ns movement.plan
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [cljs.core.async :as async :refer [timeout <!]]
            [movement.menu :refer [menu-component]]
            [movement.util :refer [positions text-input POST get-plans]]
            [movement.text :refer [text-input-component auto-complete-did-mount]]
            [movement.template :refer [movement-component]]
            [movement.user :refer [set-username-component]]
            [movement.components.creator :refer [title description error username]]
            [clojure.string :as str]))

(def plan-state (atom {:plan []
                       :selected nil}))
;title created-by public? description completed? day day/template day/completed?

(defn templates-component []
  (let []
    (fn []
      [:div
       (when-not (nil? (:selected @plan-state))
         [:div
          [:div.pure-g
           [:h3.pure-u "Select one or several templates for this day"]]
          [:div.pure-g
           (doall (for [template (sort-by :template/title (session/get :templates))]
                    ^{:key (:db/id template)}
                    (let [t (:template/title template)]
                      [:div.pure-u.button {:style    {:cursor 'pointer
                                                      :margin "0 5px 5px 0"}
                                           :on-click #(let [selected (:selected @plan-state)]
                                                       (swap! plan-state update-in [:plan selected] conj t))} t])))]])])))

(defn adjust-days-component []
  [:div.pure-g {:style {:margin-top "10px"}}
   [:div.pure-u {:style {:margin-right "5px"}} "This plan consists of "
    [:span {:style {:color "red" :font-size "150%"}} (count (:plan @plan-state))] (if (= 1 (count (:plan @plan-state))) " day" " days")]
   [:button.pure-u
    {:on-click #(when (> (count (:plan @plan-state)) 0)
                 (do
                   (when (= (inc (:selected @plan-state)) (count (:plan @plan-state)))
                     (swap! plan-state assoc :selected nil))
                   (swap! plan-state update :plan pop)))} [:i.fa.fa-minus]]
   [:button.pure-u
    {:on-click #(swap! plan-state update :plan conj [])} [:i.fa.fa-plus]]])

(defn day-component []
  (let [selected (:selected @plan-state)]
    (fn [day i]
      [:div.pure-u.movement.day {:on-click  #(swap! plan-state assoc :selected i)
                                 :style {:cursor 'pointer}
                                 :className (str "" (when (= i selected) " day-selected"))}
       [:h3.pure-g
        [:div.pure-u-1-12]
        [:div.pure-u.title (str "Day " (inc i))]]
       (for [t (range (count day))]
         ^{:key (rand-int 1000)}
         [:h4.pure-g [:span.pure-u-1
                      [:i.fa.fa-times {:on-click #(let [template (get day t)
                                                        new-day (vec (remove #{template} day))]
                                                   (swap! plan-state assoc-in [:plan i] new-day))
                                       :style    {:cursor 'pointer
                                                  :color  'red
                                                  :margin "0 5px 0 3px"}}]
                      (get day t)]])])))

(defn plan-component [plan]
  [:div.pure-g.movements {:style {:margin-top "10px"
                                  :border-top "dotted 1px"}}
   (for [day (range (count plan))]
     ^{:key (rand-int 1000)} [day-component (get plan day) day])])

(defn save-plan-component [error-atom]
  (let [stored-successfully? (atom false)]
    (fn []
      (if @stored-successfully?
        (let []
          (go (<! (timeout 3000))
              (reset! stored-successfully? false)
              (reset! plan-state {}))
          [:div.pure-g
           [:div.pure-u {:style {:margin-top 15 :font-size 24 :color "green"}} "Plan stored successfully!"]])
        [:div.pure-g
         [:p.pure-u-1.pure-u-md-2-5.button.button-primary
          {:on-click #(let [title (:title @plan-state)
                            plan (:plan @plan-state)]
                       (cond
                         (nil? title) (swap! error-atom assoc :message "The plan needs a title")
                         (empty? plan) (swap! error-atom assoc :message "A plan consists of 1 or more days")
                         (every? empty? plan) (swap! error-atom assoc :message "At least one day must contain template(s)")
                         :else (let [username (session/get :username)
                                     email (session/get :email)
                                     plan (assoc @plan-state
                                            :public? true
                                            :created-by username)]
                                 (POST "plan"
                                         {:params        {:email email
                                                          :plan  plan}
                                          :handler       (fn [response] (do
                                                                          (reset! error-atom {})
                                                                          (reset! stored-successfully? true)
                                                                          (get-plans)))
                                          :error-handler (fn [response] (do (pr response)
                                                                            (reset! error-atom response)))}))))}
          "Save Plan"]]))))

(defn plan-creator-component []
  (let [error-atom (atom {:message ""})]
    (fn []
      [:div {:style {:margin-top "20px"}}
       (title plan-state "Plan Title")
       (description plan-state)
       [templates-component]
       (adjust-days-component)
       (plan-component (:plan @plan-state))
       (error (:message @error-atom))
       (let [usr (session/get :username)]
         (if (nil? usr)
           (username "plan")
           [save-plan-component error-atom]))])))