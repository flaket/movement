(ns movement.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true :refer [dispatch!]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]
            [clojure.string :as str]
            [movement.nav :refer [nav-component]]
            [movement.user :refer [user-component]]
            [movement.template :refer [form-component]]
            [movement.generator :refer [generator-component]]
            [movement.movements :refer [all warmup mobility hanging equilibre strength
                                        locomotion bas sass leg-strength auxiliary
                                        movnat movnat-warmup
                                        m-styrke m-oppvarming m-kombinasjon]])
  (:import goog.History))

;; The core namespace is the client entry point.
;; The global state of the application is handled with the reagent.session utility namespace.
;; The generator namespace houses the main application for generating movement sessions.
;; The user namespace displays the user specific information.
;; The movements namespace temporarily houses lists of exercises.

(enable-console-print!)

(defonce movement-session (atom {}))
(defonce m-counter (atom 0))
(defonce c-counter (atom 0))

(swap! movement-session assoc :categories (sorted-map))
(swap! movement-session assoc :movements (sorted-map))
(swap! movement-session assoc :description "")


(def default-buttons {:ritual     "button"
                      :strength   "button"
                      :mobility   "button"
                      :locomotion "button"
                      :bas        "button"
                      :sass       "button"
                      :leg        "button"
                      :movnat     "button"
                      :maya       "button"})

(def buttons (atom default-buttons))

(defn button-selected! [button]
  (swap! buttons assoc button "button button-primary"))

(defn prep-name [kw]
  (str/replace (str/capitalize (name kw)) "-" " "))

(defn set-button-selected! [button]
  (do
    (reset! buttons default-buttons)
    (button-selected! button)))

(defn add-title! [title]
  (swap! movement-session assoc :title title))

(defn add-category! [title category]
  (let [id (swap! c-counter inc)]
    (swap! movement-session assoc-in [:categories id] {:id id :title title :category category})))

(defn add-movement! [title category-id]
  (let [id (swap! m-counter inc)]
    (swap! movement-session assoc-in [:movements id]
           {:id id :title title
            :category-ref category-id
            :comment "optional user comment.." :animation "animation will run here."})))

(defn update! [kw id title] (swap! movement-session assoc-in [kw id :title] title))

(defn delete! [kw id] (swap! movement-session update-in [kw] dissoc id))

(defn refresh! [id category] (update! :movements id (prep-name (first (take 1 (shuffle category))))))

(defn handler-fn [func]
  "Wrapper function to force component handler functions to return nil.
  This is a React requirement."
  (fn [] func nil))

(defn reset-session! []
  (do
    (reset! movement-session {})
    (reset! c-counter 0)
    (reset! m-counter 0)
    (swap! movement-session assoc :categories (sorted-map))
    (swap! movement-session assoc :movements (sorted-map))))

(defn auto-complete-did-mount []
  "Attaches the jQuery autocomplete functionality to DOM elements."
  (js/$ (fn []
          (let [available-tags (map prep-name all)]
            (.autocomplete (js/$ "#tags")
                           (clj->js {:source available-tags}))))))

;--------------------
; Components (views)

(defn text-input-component [{:keys [title on-save on-stop]}]
  (let [val (atom title)
        stop #(do (reset! val "")
                  (if on-stop (on-stop)))
        save #(let [v (-> @val str clojure.string/trim)]
               (if-not (empty? v) (on-save v))
               (stop))]
    (fn [props]
      [:input#tags (merge props
                     {:type "text"
                      :value @val
                      :on-blur #(do (reset! val (-> % .-target .-value))
                                    (save))
                      :on-change #(reset! val (-> % .-target .-value))
                      :on-key-down #(case (.-which %)
                                     13 (save)
                                     27 (stop)
                                     nil)})])))

(def text-edit-component
  (with-meta text-input-component {:component-did-mount #(do (.focus (reagent/dom-node %))
                                                   (auto-complete-did-mount))}))

(defn movement-component []
  (let [editing (atom false)
        show-buttons (atom false)]
    (fn [{:keys [id title category-ref comment animation]}]
      [:li
       [:div.view {:class (str (if @editing "editing"))
                   :on-click #(handler-fn (reset! show-buttons (not @show-buttons)))}
        [:div.row
         (when @show-buttons
           [:div.two.columns
            [:button.refresh
             {:on-click #(refresh! id (:category (get (:categories @movement-session) category-ref)))}]
            [:button.textedit
             {:on-click #(handler-fn (reset! editing true))}]
            [:button.destroy
             {:on-click #(delete! :movements id)}]])
         [:label.four.columns {:style {:display (if @editing "none" "")}}
          title]
         (when @editing
           [:label.four.columns
            [text-edit-component {:class   "edit" :title title
                                  :on-save #(handler-fn (update! :movements id %))
                                  :on-stop #(handler-fn (reset! editing false))}]])
         [:span.four.columns animation]]
        [:div.row
         [:span.four.columns {:style {:font-size "small"}} comment]]
        ]])))

(defn category-component []
  (let [editing (atom false)]
    (fn [{:keys [id title category]} movements]
      [:div
       [:div.row
        [:h4.ten.columns {:style {:display (if @editing "none" "")}
                          :on-click #(handler-fn (reset! editing true))} title]
        (when @editing
          [:h4.ten.columns
           [text-edit-component {:class   "edit" :title title
                                 :on-save #(handler-fn (update! :categories id %))
                                 :on-stop #(handler-fn (reset! editing false))}]])]
       (when (-> movements count pos?)
         [:ul#movement-list
          (for [m movements]
            ^{:key (:id m)} [movement-component m])])
       [:button {:type     "submit"
                 :on-click #(add-movement!
                             (prep-name (first (take 1 (shuffle category))))
                             id)} "+"]])))

(defn log-session []
  (let [log (session/get :logged-sessions)
        timestamp (.getTime (js/Date.))
        s (swap! movement-session assoc :timestamp)
        new-sessions (conj log movement-session)]
    (session/put! :logged-sessions new-sessions)))

(defn session-component []
  (let [editing (atom false)
        adding-description (atom false)]
    (fn [{:keys [categories movements title]}]
      [:div
       [:div.row
        [:h3.ten.columns {:style {:display (if @editing "none" "")}
                          :on-click #(handler-fn (reset! editing true))} title]
        (when @editing
          [:h3.ten.columns [text-edit-component {:class   "edit" :title title
                                        :on-save #(handler-fn (add-title! %))
                                        :on-stop #(handler-fn (reset! editing false))}]])
        [:label.one.colum "date"]]
       [:div.row
        [:div.eight.columns
         [:div (:description @movement-session)]]
        (when @adding-description
          [:div.eight.columns
           [text-edit-component {:class   "edit" :title (:description @movement-session)
                       :on-save #(handler-fn (swap! movement-session assoc :description %))
                       :on-stop #(handler-fn (reset! adding-description false))}]])
        [:div.one.colum
         [:button {:type     "submit"
                          :on-click #(handler-fn (reset! adding-description true))} "!"]]]
       (when (-> categories count pos?)
         [:div
          (for [c categories]
            ^{:key (:id c)} [category-component
                             c
                             (filter
                               #(= (:id c) (:category-ref %))
                               movements)])])
       [:button.button {:type     "submit"
                        :on-click log-session}
        "Log this movement session!"]
       [:button.button {:on-click #()} "Make PDF"]])))

(defn home-component []
  [:div
   [:div.container
    (nav-component)
    [:section#templates
     [:div.row
      [:div.three.columns
       {:type     "button"
        :class    (:ritual @buttons)
        :on-click #(do
                    (reset-session!)
                    (set-button-selected! :ritual)

                    (let [date (js/Date.)]
                      (add-title! (str "Morning Ritual " (.getDate date) "/" (+ 1 (.getMonth date)))))
                    (add-category! "Warmup" warmup)
                    (dotimes [n 1] (add-movement! (prep-name (nth (take 1 (shuffle warmup)) n)) 1))
                    (add-category! "Mobility" mobility)
                    (dotimes [n 5] (add-movement! (prep-name (nth (take 5 (shuffle mobility)) n)) 2))
                    (add-category! "Hanging" hanging)
                    (dotimes [n 1] (add-movement! (prep-name (nth (take 1 (shuffle hanging)) n)) 3))
                    (add-category! "Equilibre" equilibre)
                    (dotimes [n 1] (add-movement! (prep-name (nth (take 1 (shuffle equilibre)) n)) 4))
                    (add-category! "Strength" strength)
                    (dotimes [n 1] (add-movement! (prep-name (nth (take 1 (shuffle strength)) n)) 5)))}
       "Morning ritual"]
      [:div.three.columns
       {:type     "button"
        :class    (:strength @buttons)
        :on-click #(do
                    (reset-session!)
                    (set-button-selected! :strength)
                    (let [date (js/Date.)]
                      (add-title! (str "Strength " (.getDate date) "/" (+ 1 (.getMonth date)))))
                    (add-category! "Warmup" warmup)
                    (dotimes [n 1] (add-movement! (prep-name (nth (take 1 (shuffle warmup)) n)) 1))
                    (add-category! "Mobility" mobility)
                    (dotimes [n 6] (add-movement! (prep-name (nth (take 6 (shuffle mobility)) n)) 2))
                    (add-category! "Strength" strength)
                    (dotimes [n 4] (add-movement! (prep-name (nth (take 4 (shuffle strength)) n)) 3)))
        }
       "Strength"]
      [:div.three.columns
       {:type     "button"
        :class    (:mobility @buttons)
        :on-click #(do
                    (reset-session!)
                    (set-button-selected! :mobility)
                    (let [date (js/Date.)]
                      (add-title! (str "Mobility/Prehab " (.getDate date) "/" (+ 1 (.getMonth date)))))
                    (add-category! "Warmup" warmup)
                    (dotimes [n 1] (add-movement! (prep-name (nth (take 1 (shuffle warmup)) n)) 1))
                    (add-category! "Mobility" mobility)
                    (dotimes [n 6] (add-movement! (prep-name (nth (take 6 (shuffle mobility)) n)) 2))
                    (add-category! "Prehab" mobility)
                    (dotimes [n 4] (add-movement! (prep-name (nth (take 4 (shuffle mobility)) n)) 3)))}
       "Mobility/Prehab"]
      [:div.three.columns
       {:type     "button"
        :class    (:locomotion @buttons)
        :on-click #(do
                    (reset-session!)
                    (set-button-selected! :locomotion)
                    (let [date (js/Date.)]
                      (add-title! (str "Locomotion " (.getDate date) "/" (+ 1 (.getMonth date)))))
                    (add-category! "Warmup" warmup)
                    (dotimes [n 1] (add-movement! (prep-name (nth (take 1 (shuffle warmup)) n)) 1))
                    (add-category! "Mobility" mobility)
                    (dotimes [n 6] (add-movement! (prep-name (nth (take 6 (shuffle mobility)) n)) 2))
                    (add-category! "Locomotion" locomotion)
                    (dotimes [n 6] (add-movement! (prep-name (nth (take 6 (shuffle locomotion)) n)) 3)))}
       "Locomotion"]]
     [:div.row
      [:div.three.columns
       {:type     "button"
        :class    (:bas @buttons)
        :on-click #(do
                    (reset-session!)
                    (set-button-selected! :bas)
                    (let [date (js/Date.)]
                      (add-title! (str "Bent Arm Strength " (.getDate date) "/" (+ 1 (.getMonth date)))))
                    (add-category! "Warmup" warmup)
                    (dotimes [n 1] (add-movement! (prep-name (nth (take 1 (shuffle warmup)) n)) 1))
                    (add-category! "Mobility" mobility)
                    (dotimes [n 6] (add-movement! (prep-name (nth (take 6 (shuffle mobility)) n)) 2))
                    (add-category! "BAS" bas)
                    (dotimes [n 5] (add-movement! (prep-name (nth (take 5 (shuffle bas)) n)) 3)))}
       "BAS"]
      [:div.three.columns
       {:type     "button"
        :class    (:sass @buttons)
        :on-click #(do
                    (reset-session!)
                    (set-button-selected! :sass)
                    (let [date (js/Date.)]
                      (add-title! (str "Straigth Arm Scapular Strength " (.getDate date) "/" (+ 1 (.getMonth date)))))
                    (add-category! "Warmup" warmup)
                    (dotimes [n 1] (add-movement! (prep-name (nth (take 1 (shuffle warmup)) n)) 1))
                    (add-category! "Mobility" mobility)
                    (dotimes [n 6] (add-movement! (prep-name (nth (take 6 (shuffle mobility)) n)) 2))
                    (add-category! "SASS" sass)
                    (dotimes [n 4] (add-movement! (prep-name (nth (take 4 (shuffle sass)) n)) 3)))}
       "SASS"]
      [:div.three.columns
       {:type     "button"
        :class    (:leg @buttons)
        :on-click #(do
                    (reset-session!)
                    (set-button-selected! :leg)
                    (let [date (js/Date.)]
                      (add-title! (str "Leg Strength " (.getDate date) "/" (+ 1 (.getMonth date)))))
                    (add-category! "Warmup" warmup)
                    (dotimes [n 1] (add-movement! (prep-name (nth (take 1 (shuffle warmup)) n)) 1))
                    (add-category! "Mobility" mobility)
                    (dotimes [n 6] (add-movement! (prep-name (nth (take 6 (shuffle mobility)) n)) 2))
                    (add-category! "Leg Strength" leg-strength)
                    (dotimes [n 3] (add-movement! (prep-name (nth (take 3 (shuffle leg-strength)) n)) 3))
                    (add-category! "Auxiliary" auxiliary)
                    (dotimes [n 2] (add-movement! (prep-name (nth (take 2 (shuffle auxiliary)) n)) 4)))}
       "Leg strength"]
      [:div.three.columns
       {:type     "button"
        :class    (:movnat @buttons)
        :on-click #(do
                    (reset-session!)
                    (set-button-selected! :movnat)
                    (let [date (js/Date.)]
                      (add-title! (str "MovNat " (.getDate date) "/" (+ 1 (.getMonth date)))))
                    (add-category! "Warmup Mobility (3 rounds)" movnat-warmup)
                    (dotimes [n 3] (add-movement! (prep-name (nth (take 3 (shuffle movnat-warmup)) n)) 1))
                    (add-category! "Skill (30 reps)" movnat)
                    (dotimes [n 1] (add-movement! (prep-name (nth (take 1 (shuffle movnat)) n)) 2))
                    (add-category! "Combo (4 rounds)" movnat)
                    (dotimes [n 4] (add-movement! (prep-name (nth (take 4 (shuffle movnat)) n)) 3)))}
       "MovNat"]]]
    [:div.row
     [:div.three.columns
      {:type     "button"
       :class    (:maya @buttons)
       :on-click #(do
                   (reset-session!)
                   (set-button-selected! :maya)
                   (let [date (js/Date.)]
                     (add-title! (str "Maya " (.getDate date) "/" (+ 1 (.getMonth date)))))
                   (add-category! "Oppvarming/Bevegelighet (2 runder rolig)" m-oppvarming)
                   (dotimes [n 3] (add-movement! (prep-name (nth (take 3 (shuffle m-oppvarming)) n)) 1))
                   (add-category! "Styrke/Ferdighet (30 reps)" m-styrke)
                   (dotimes [n 1] (add-movement! (prep-name (nth (take 1 (shuffle m-styrke)) n)) 2))
                   (add-category! "Kombinasjon (3 runder hurtig)" m-kombinasjon)
                   (dotimes [n 4] (add-movement! (prep-name (nth (take 4 (shuffle m-kombinasjon)) n)) 3)))}
      "Maya"]]
    [:section#session
     (let [movement-session @movement-session
           c (vals (:categories movement-session))
           m (vals (:movements movement-session))
           t (:title movement-session)
           session-data {:categories c :movements m :title t}]
       (when (pos? (count c))
         [session-component session-data]))]
    [:footer#info
     [:button.button {:on-click #(dispatch! "/about")} "About"]]]])

(defn movement-detail-component []
  [:div
   [:div.container
    (nav-component)
    [:section
     [:div "This section will allow users to discover the movements in the database,
     view the animations more clearly, add their own comments to the movements
     and mark the subjective difficulty level of the movements."]]]])

(defn about-component []
  [:div
   [:div.container
    (nav-component)
    [:section
     [:div "movementsession@gmail.com"]]]])

(defn template-component []
  [:div
   [:div.container
    [:section
     [:div.row
      [:label.three.columns "The title of the session is "]
      [:input.four.columns
       {:type "text" :placeholder "title " :on-click #()}]
      [:input.one.column {:type "checkbox"} " Include the date?"]]
     [:div.row]]]])

;; -------------------------
;; Client side routes
(secretary/defroute "/" []
                    (session/put! :current-page #'home-component))

(secretary/defroute "/user" []
                    (session/put! :current-page #'user-component))

(secretary/defroute "/template" []
                    (session/put! :current-page #'template-component))

(secretary/defroute "/movements" []
                    (session/put! :current-page #'movement-detail-component))

(secretary/defroute "/about" []
                    (session/put! :current-page #'about-component))

;---------------------------
(defn page []
  [(session/get :current-page)])

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
  (reagent/render-component [page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (secretary/set-config! :prefix "#")
  (session/put! :current-page #'home-component)
  (session/put! :logged-sessions [])
  (mount-root))