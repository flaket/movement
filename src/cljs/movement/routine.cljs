(ns movement.routine
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [cljs.core.async :as async :refer [timeout <!]]
            [movement.menu :refer [menu-component]]
            [movement.util :refer [handler-fn text-input POST]]
            [movement.text :refer [text-input-component auto-complete-did-mount]]
            [movement.template :refer [movement-component]]
            [movement.user :refer [set-username-component]]
            [movement.components.creator :refer [title description error username]]
            [clojure.string :as str]))

(def routine-state (atom {:movements []}))

(defn movements-component []
  (let [show-movements-list? (atom false)]
    (fn []
      [:div.pure-g
       [:div.pure-u-1-2
        (doall
          (for [m (reverse (:movements @routine-state))]
            ^{:key (rand-int 1000)}
            (movement-component {} (str m) (str "images/" (str/replace (str/lower-case m) " " "-") ".png"))))]
       [:div.pure-u-1-2
        [:div.pure-g
         [:h3.pure-u-1 "All Movements"]]
        [:div.pure-g
         [:div.pure-u
          (let [id "routine-mtags"
                movements-ac-comp (with-meta text-input-component
                                             {:component-did-mount #(auto-complete-did-mount
                                                                     (str "#" id)
                                                                     (vec (session/get :all-movements)))})]
            [movements-ac-comp {:id          id
                                :class       "edit"
                                :placeholder "type to find and add movement.."
                                :size        32
                                :on-save     #(when (and (some #{%} (session/get :all-movements))
                                                         (not (some #{%} (:movements @routine-state))))
                                               (swap! routine-state update :movements conj %))}])]]
        [:div.pure-g
         [:div.button.pure-u {:on-click #(handler-fn (reset! show-movements-list? (not @show-movements-list?)))}
          (if @show-movements-list? "Hide list of movements" "Show list of movements")]]
        (when @show-movements-list?
          (doall (for [t (sort (session/get :all-movements))]
                   ^{:key (rand-int 1000)}
                   [:div.pure-g
                    [:div.pure-u {:style    {:cursor     'pointer
                                             :color (when (some #{t} (:movements @routine-state)) "#fffff8")
                                             :background (when (some #{t} (:movements @routine-state)) "gray")}
                                  :on-click #(if (some #{t} (:movements @routine-state))
                                              (let [new-movements (remove #{t} (:movements @routine-state))]
                                                (swap! routine-state assoc :movements new-movements))
                                              (swap! routine-state update :movements conj t))} t]])))]])))

(defn save-routine-component [error]
  (let [routine-stored-successfully? (atom false)]
    (fn []
      (if @routine-stored-successfully?
        (let []
          (go (<! (timeout 3000))
              (reset! routine-stored-successfully? false)
              (reset! routine-state {}))
          [:div.pure-g
           [:div.pure-u {:style {:margin-top 15 :font-size 24 :color "green"}} "Routine stored successfully!"]])
        [:div.pure-g
         [:p.pure-u.pure-u-md-2-5.button.button-primary
          {:on-click #(let [name (:title @routine-state)
                            movements (:movements @routine-state)]
                       (cond
                         (nil? name) (swap! error assoc :message "The routine needs a name")
                         (empty? movements) (swap! error assoc :message "A routine consists of 1 or more movements")
                         :else (let [username (session/get :username)
                                     email (session/get :email)
                                     routine (assoc @routine-state
                                               :public? true
                                               :created-by username)]
                                 (pr routine)
                                 #_(POST "routine"
                                       {:params        {:email   email
                                                        :routine routine}
                                        :handler       (fn [response] (do
                                                                        (reset! error {})
                                                                        (reset! routine-stored-successfully? true)
                                                                        (get-routines)))
                                        :error-handler (fn [response] (do (pr response)
                                                                          (reset! error response)))}))))}
          "Save Routine"]]))))

(defn routine-creator-component []
  (let [error-atom (atom {:message ""})]
    (fn []
      [:div {:style {:margin-top "20px"}}
       (title routine-state "Routine Name")
       (description routine-state)
       [movements-component]
       (error (:message @error-atom))
       (let [usr (session/get :username)]
         (if (nil? usr)
           (username "routine")
           [save-routine-component error-atom]))])))