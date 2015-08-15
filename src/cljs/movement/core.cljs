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
            [movement.movements :refer [morning-ritual-template strength-template
                                        mobility-template locomotion-template bas-template
                                        sass-template leg-strength-template movnat-template
                                        maya-template all-categories all-movements]])
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

(defn prep-name [kw]
  (str/replace (str/capitalize (name kw)) "-" " "))

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
          (let [available-tags (map prep-name all-movements)]
            (.autocomplete (js/$ "#tags")
                           (clj->js {:source available-tags}))))))

(defn template-auto-complete-did-mount []
  "Attaches the jQuery autocomplete functionality to DOM elements."
  (js/$ (fn []
          (let [available-tags (keys all-categories)]
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
  (with-meta text-input-component {:component-did-mount
                                   #(do (.focus (reagent/dom-node %))
                                        (auto-complete-did-mount))}))

(def template-text-edit-component
  (with-meta text-input-component {:component-did-mount
                                   #(do (.focus (reagent/dom-node %))
                                        (template-auto-complete-did-mount))}))

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

(defn add-part! [{:keys [title category n]} category-ref]
  (add-category! title category)
  (dotimes [i n]
    (add-movement!
      (prep-name (nth (take n (shuffle category)) i))
      category-ref)))

(defn create-new-session! [kw {:keys [title parts]}]
  (let [count (atom 0)]
    (do
      (reset-session!)
      (add-title! title)
      (doseq [p parts] (add-part! p (swap! count inc))))))

(defn template-component []
  (let [date (js/Date.)
        day (.getDate date)
        month (+ 1 (.getMonth date))]
    (fn [kw template]
      [:div {:on-click #(create-new-session! kw template)}
       (:title template)])))

(defn home-component []
  [:div
   [:div.container
    (nav-component)
    [:section#templates
     [:div
      [template-component :ritual morning-ritual-template]
      [template-component :strength strength-template]
      [template-component :mobility mobility-template]
      [template-component :locomotion locomotion-template]
      [template-component :bas bas-template]
      [template-component :sass sass-template]
      [template-component :leg leg-strength-template]
      [template-component :movnat movnat-template]
      [template-component :maya maya-template]]]
    [:section#session
     (let [movement-session @movement-session
           c (vals (:categories movement-session))
           m (vals (:movements movement-session))
           t (:title movement-session)
           session-data {:categories c :movements m :title t}]
       (when (pos? (count c))
         [session-component session-data]))]
    #_[:footer#info
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

(def template-state (atom {:title ""
                           :parts []}))

(defn category-creator-component []
  (let [buttons (atom [])]
    (fn [{:keys [title n]} i]
      [:div
       [:div.row
        [:label.five.columns "Part " (inc i) " is called "]
        [:input.seven.columns {:type "text"}]]
       [:div.row
        [:label.ten.columns "It consists of "
         [:span {:style {:color "red"}} n] " generated movements,"]
        [:button.one.column
         {:on-click #(swap! template-state update-in [:parts i :n] inc)} "+"]
        [:button.one.column
         {:on-click #(when (> n 0)
                      (swap! template-state update-in [:parts i :n] dec))} "-"]]
       #_[:div.row
        [:label.four.columns "Drawn from the categories: "]
        [:div.three.columns
         [template-text-edit-component
          {:class   "edit" :placeholder "type to find and add category.."
           :on-save #(handler-fn (swap! buttons conj %))
           :on-stop #(handler-fn (fn [] nil))}]]
        [:div.five.columns (for [b @buttons] ^{:key b} [:div.two.columns b])]]
       [:div.row
        [:label.four.columns "Additionally, the following exercises should always be included:"]
        [:div.three.columns
         [text-edit-component {:class   "edit" :placeholder "type to find and add movement.."
                               :on-save #(handler-fn (swap! template-state assoc-in [:parts i :extra] %))
                               :on-stop #(handler-fn (fn [] nil))}]]
        [:div.five.columns (:extra (get (:parts @template-state) i))]]])))

(defn template-creator-component []
  (let []
    (fn []
      [:div
       [:div.container
        (nav-component)
        [:section
         [:div.row
          [:label.five.columns "This movement session is called "]
          [:input.seven.columns
           {:type "text" :placeholder "My Favourite Movement Session"}]]
         [:div.row
          [:label.ten.columns "The session is divided into "
           [:span {:style {:color "red"}} (count (:parts @template-state))] " parts."]
          [:button.one.column
           {:on-click #(swap! template-state update-in [:parts]
                              conj {:title ""
                                    :category nil
                                    :n 0})} "+"]
          [:button.one.column
           {:on-click #(when (> (count (:parts @template-state)) 0)
                        (swap! template-state update-in [:parts] pop))} "-"]]
         [:div
          (let [parts (:parts @template-state)]
            (for [i (range 0 (count parts))]
              ^{:key i} [category-creator-component (get parts i) i]))]]]])))

;; -------------------------
;; Client side routes
(secretary/defroute "/" []
                    (session/put! :current-page #'home-component))

(secretary/defroute "/user" []
                    (session/put! :current-page #'user-component))

(secretary/defroute "/template" []
                    (session/put! :current-page #'template-creator-component))

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