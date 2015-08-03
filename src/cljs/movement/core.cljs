(ns movement.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]
            [clojure.string :as str]
            [movement.movements :refer [warmup mobility hanging equilibre strength
                                        locomotion bas sass leg-strength auxiliary
                                        movnat movnat-warmup
                                        m-styrke m-oppvarming m-kombinasjon]])
  (:import goog.History))

(enable-console-print!)

(def default-template [])
(def default-buttons {:ritual     "button"
                      :strength   "button"
                      :mobility   "button"
                      :locomotion "button"
                      :bas        "button"
                      :sass       "button"
                      :leg        "button"
                      :movnat     "button"
                      :maya       "button"})

(def ttemplate (atom default-template))

(def buttons (atom default-buttons))

(defn generate! [name category n]
  (swap! ttemplate conj {:name      name
                        :category  category
                        :movements (vec (take n (shuffle category)))}))

(defn button-selected! [button]
  (swap! buttons assoc button "button button-primary"))

(defn prep-name [kw]
  (str/replace (str/capitalize (name kw)) "-" " "))

(defn refresh-button [part]
  [:div.one.column
   {:type     "button"
    :class    "button"
    :on-click #(generate! (:name part) (:category part) 1)}
   "#"])

(defn list-movements [part]
  (for [movement (:movements part)]
    [:div.row
     [:div.six.columns {:draggable true} (prep-name movement)]
     (refresh-button part)]))

(defn update! [button]
  (do
    (reset! ttemplate [])
    (reset! buttons default-buttons)
    (button-selected! button)))

(defonce movements (atom (sorted-map)))
(defonce template (atom (sorted-map)))
(defonce m-counter (atom 0))
(defonce c-counter (atom 0))

(defn add-category! [text category]
  (let [id (swap! c-counter inc)]
    (swap! template assoc id {:id id :title text
                              :category category :movements (atom (sorted-map))})))

(defn add-movement! [text]
  (let [id (swap! m-counter inc)]
    (swap! movements assoc id {:id id :title text :done false})))

(defn update-category! [id title] (swap! template assoc-in [id :title] title))
(defn delete-category! [id] (swap! template dissoc id))

(defn update-movement! [id title] (swap! movements assoc-in [id :title] title))
(defn delete-movement! [id] (swap! movements dissoc id))

(defn refresh-movement! [id] (update-movement! id (prep-name (first (take 1 (shuffle strength))))))

(defn mmap [m f a] (->> m (f a) (into (empty m))))
(defn complete-all [v] (swap! movements mmap map #(assoc-in % [1 :done] v)))
(defn clear-done [] (swap! movements mmap remove #(get-in % [1 :done])))

(defn movement-input [{:keys [title on-save on-stop]}]
  (let [val (atom title)
        stop #(do (reset! val "")
                  (if on-stop (on-stop)))
        save #(let [v (-> @val str clojure.string/trim)]
               (if-not (empty? v) (on-save v))
               (stop))]
    (fn [props]
      [:input (merge props
                     {:type "text" :value @val :on-blur save
                      :on-change #(reset! val (-> % .-target .-value))
                      :on-key-down #(case (.-which %)
                                     13 (save)
                                     27 (stop)
                                     nil)})])))

(def movement-edit (with-meta movement-input
                              {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn movement-item []
  (let [editing (atom false)]
    (fn [{:keys [id done title]}]
      [:li {:class (str (if done "completed ")
                        (if @editing "editing"))}
       [:div.view
        [:label {:on-double-click #(reset! editing true)} title]
        [:button.button.button-primary {:on-click #(refresh-movement! id)}]
        [:button.destroy {:on-click #(delete-movement! id)}]]
       (when @editing
         [movement-edit {:class "edit" :title title
                         :on-save #(update-movement! id %)
                         :on-stop #(reset! editing false)}])])))

(defn category-item []
  (fn [{:keys [id category title movements]}]
    [:div
     [:label title]
     (let [items (vals movements)]
       (when (-> items count pos?)
         [:div
          [:section#main
           [:ul#todo-list
            (for [movement items]
              ^{:key (:id movement)} [movement-item movement])]]])
       [movement-input {:id          "new-todo"
                        :placeholder "Add movement.."
                        :on-save     add-movement!}])]))

(defn temp []
  [:div
   (for [part @ttemplate]
     [:div
      [:div.row [:h3 (:name part)]]
      (list-movements part)])])

(defn home-page []
  [:div
   [:div.section.buttons
    [:div.container
     [:h1 "Movement Session"]
     [:p "Select a template and be inspired by randomly generated movements."]
     [:div.row
      [:div.three.columns
       {:type     "button"
        :class    (:ritual @buttons)
        :on-click #(do
                    (update! :ritual)
                    (generate! "Warmup" warmup 1)
                    (generate! "Mobility" mobility 5)
                    (generate! "Hanging" hanging 1)
                    (generate! "Equilibre" equilibre 1)
                    (generate! "Strength" strength 1))}
       "Morning ritual"]
      [:div.three.columns
       {:type     "button"
        :class    (:strength @buttons)
        :on-click #(let [wu-n 1
                         mob-n 6
                         st-n 4
                         wu (take wu-n (shuffle warmup))
                         mob (take mob-n (shuffle mobility))
                         st (take st-n (shuffle strength))]
                    (add-category! "Warmup" warmup)
                    (add-category! "Mobility" mobility)
                    (add-category! "Strength" strength)
                    (dotimes [n wu-n] (add-movement! (prep-name (nth wu n))))
                    (dotimes [n mob-n] (add-movement! (prep-name (nth mob n))))
                    (dotimes [n st-n] (add-movement! (prep-name (nth st n))))

                    #_(update! :strength)
                    #_(generate! "Warmup" warmup 1)
                    #_(generate! "Mobility" mobility 5)
                    #_(generate! "Strength" strength 4))}
       "Strength"]
      [:div.three.columns
       {:type     "button"
        :class    (:mobility @buttons)
        :on-click #(do
                    (update! :mobility)
                    (generate! "Warmup" warmup 1)
                    (generate! "Mobility" mobility 8)
                    (generate! "Prehab" mobility 4))}
       "Mobility/Prehab"]
      [:div.three.columns
       {:type     "button"
        :class    (:locomotion @buttons)
        :on-click #(do
                    (update! :locomotion)
                    (generate! "Warmup" warmup 1)
                    (generate! "Mobility" mobility 6)
                    (generate! "Locomotion" locomotion 6))}
       "Locomotion"]]
     [:div.row
      [:div.three.columns
       {:type     "button"
        :class    (:bas @buttons)
        :on-click #(do
                    (update! :bas)
                    (generate! "Warmup" warmup 1)
                    (generate! "Mobility" mobility 6)
                    (generate! "Bent Arm Strength" bas 5))}
       "BAS"]
      [:div.three.columns
       {:type     "button"
        :class    (:sass @buttons)
        :on-click #(do
                    (update! :sass)
                    (generate! "Warmup" warmup 1)
                    (generate! "Mobility" mobility 6)
                    (generate! "Straight Arm Scapular Strength" sass 4))}
       "SASS"]
      [:div.three.columns
       {:type     "button"
        :class    (:leg @buttons)
        :on-click #(do
                    (update! :leg)
                    (generate! "Warmup" warmup 1)
                    (generate! "Mobility" mobility 6)
                    (generate! "Leg Strength" leg-strength 3)
                    (generate! "Auxiliary" auxiliary 2))}
       "Leg strength"]
      [:div.three.columns
       {:type     "button"
        :class    (:movnat @buttons)
        :on-click #(do
                    (update! :movnat)
                    (generate! "Warmup Mobility (3 rounds)" movnat-warmup 3)
                    (generate! "Skill (30 reps)" movnat 1)
                    (generate! "Combo (4 rounds)" movnat 4))}
       "MovNat"]]
     [:div.row
      [:div.three.columns
       {:type     "button"
        :class    (:maya @buttons)
        :on-click #(do
                    (update! :maya)
                    (generate! "Oppvarming/Bevegelighet (2 runder rolig)" m-oppvarming 3)
                    (generate! "Styrke/Ferdighet (30 reps)" m-styrke 1)
                    (generate! "Kombinasjon (3 runder hurtig)" m-kombinasjon 4))}
       "Maya"]]]]
   [:div {:class "section movements"}
    [:div.container
     ;----

     ;-----
     (let [categories [vals @template]]
       [:section
        (when (-> categories count pos?)
          [:div
           (for [c categories]
             ^{:key (:id c)} [category-item c])])])

     #_(let [items (vals @movements)]
       [:section#todoapp
        [:header#header
         [:h1 "Movements"]
        (when (-> items count pos?)
          [:div
           [:section#main
            [:ul#todo-list
             (for [todo items]
               ^{:key (:id todo)} [movement-item todo])]]])
         [movement-input {:id          "new-todo"
                          :placeholder "Add movement.."
                          :on-save     add-movement!}]]])
     ]]
   [:div.footer
    [:div.container
     [:em "If you have suggestions for a new session template, some sorely missing movements
     or general improvements (such as adding users and allowing you to add your own
     templates): let your wishes be known by sending an email to movementsession@gmail.com"]]]])

(defn user-page []
  [:div
   [:div.container
    [:p "this will be the user page"]]])

;-------------

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Client side routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
                    (session/put! :current-page #'home-page))

(secretary/defroute "/user" []
                    (session/put! :current-page #'user-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))


