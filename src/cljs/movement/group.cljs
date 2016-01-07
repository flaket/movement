(ns movement.group
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [cljs.core.async :as async :refer [timeout <!]]
            [movement.menu :refer [menu-component]]
            [movement.util :refer [positions text-input POST get-templates get-groups]]
            [movement.text :refer [text-input-component auto-complete-did-mount]]
            [movement.user :refer [set-username-component]]))

(def group-state (atom {}))

(defn heading-component []
  [:div.pure-g
   [:h2.pure-u "Create a new Group"]
   [:button.pure-u {:on-click #(pr (session/get :groups))} "My Groups"]])

(defn title-component []
  [:div
   [:div.pure-g
    [:h1.pure-u-1
     [:input {:type        "text"
              :size 50
              :placeholder "Group Title"
              :on-change   #(swap! group-state assoc :title (-> % .-target .-value))
              :value       (:title @group-state)}]]]
   [:div.pure-g
    [:div.pure-u (str "by " (session/get :username))]]])

(defn description-component []
  [:div.pure-g
   [:div.pure-u
    [:textarea {:rows        3
                :cols        58
                :placeholder "Optional description of this group"
                :on-change   #(swap! group-state assoc :description (-> % .-target .-value))
                :value       (:description @group-state)}]]])

(defn templates-component []
  [:div.pure-g
   [:div.pure-u-1-2
    (doall (for [t (:templates @group-state)]
             ^{:key (rand-int 1000)} [:div t]))]
   [:div.pure-u-1-2
    [:div.pure-g
     [:h3.pure-u-1 "Your templates"]]
    (doall (for [t (sort (session/get :templates))]
             ^{:key (rand-int 1000)}
             [:div.pure-u.button {:style    {:cursor     'pointer
                                             :background (when (some #{t} (:templates @group-state)) "yellow")}
                                  :on-click #(if (some #{t} (:templates @group-state))
                                              (let [new-templates (remove #{t} (:templates @group-state))]
                                                (swap! group-state assoc :templates new-templates))
                                              (swap! group-state update :templates conj t))} t]))]])

(defn error-component [error-atom]
  (let [message (:message @error-atom)]
    [:div
     [:div.pure-g
      [:h3.pure-u {:style {:color "red"}} message]]]))

(defn username-component []
  [:div
   [:div.pure-g
    [:div.pure-u-1 "To create new groups you must first select a username"]]
   [set-username-component]])

(defn save-group-component [error-atom]
  (let [group-stored-successfully? (atom false)]
    (fn []
      (if @group-stored-successfully?
        (let []
          (go (<! (timeout 3000))
              (reset! group-stored-successfully? false)
              (reset! group-state {}))
          [:div.pure-g
           [:div.pure-u {:style {:margin-top 15 :font-size 24 :color "green"}} "Group stored successfully!"]])
        [:div.pure-g
         [:p.pure-u.pure-u-md-2-5.button.button-primary
          {:on-click #(let [title (:title @group-state)
                            templates (:templates @group-state)]
                       (cond
                         (nil? title) (swap! error-atom assoc :message "The group needs a title.")
                         (empty? templates) (swap! error-atom assoc :message "A group consists of 1 or more templates.")
                         :else (let [username (session/get :username)
                                     email (session/get :email)
                                     group (assoc @group-state
                                             :public? true
                                             :created-by username)]
                                 (POST "group"
                                         {:params        {:email email
                                                          :group group}
                                          :handler       (fn [response] (do
                                                                          (reset! error-atom {})
                                                                          (reset! group-stored-successfully? true)
                                                                          (get-groups)))
                                          :error-handler (fn [response] (do (pr response)
                                                                            (reset! error-atom response)))}))))}
          "Save Group"]]))))

(defn group-creator-component []
  (let [error (atom {:message ""})]
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content {:style {:margin-top "20px"}}
        (heading-component)
        (title-component)
        (description-component)
        (templates-component)
        (error-component error)
        (let [username (session/get :username)]
          (if (nil? username)
            (username-component)
            [save-group-component error]))]])))