(ns movement.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true :refer [dispatch!]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [goog.net.XhrIo :as xhr]
            [cljs.core.async :as async :refer [chan close!]]
            [cljs.reader :refer [read-string]]
            [cljsjs.react :as react]
            [clojure.string :as str]
            [movement.nav :refer [nav-component]]
            [movement.user :refer [user-component]]
            [movement.template :refer [form-component]]
            [movement.generator :refer [generator-component]]
            [movement.movements :refer [all-movements]])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]])
  (:import goog.History))

;; The core namespace is the client entry point.
;; The global state of the application is handled with the reagent.session utility namespace.
;; The generator namespace houses the main application for generating movement sessions.
;; The user namespace displays the user specific information.
;; The movements namespace temporarily houses lists of exercises.

(enable-console-print!)

(defn GET
  "Issue a get request to a url through a core.async channel.
  Returns a channel that the result can be read from.
  goog.net.XhrIo.send(url, opt_callback, opt_method, opt_content, opt_headers, opt_timeoutInterval)
  https://developers.google.com/closure/library/docs/xhrio"
  [url]
  (let [ch (chan 1)]
    (xhr/send url
              (fn [event]
                (let [res (-> event .-target .getResponseText)]
                  (go (>! ch res)
                      (close! ch)))))
    ch))

(defonce templates (atom []))
(defonce movement-session (atom {}))

(go (reset! templates (read-string (<! (GET "templates")))))

(defn update! [kw id title] (swap! movement-session assoc-in [kw id :title] title))

(defn delete! [kw id] (swap! movement-session update-in [kw] dissoc id))

(defn refresh! [id category] (update! :movements id (first (take 1 (shuffle category)))))

(defn handler-fn
  "Wrapper function to force component handler functions to return nil.
  This is a React requirement."
  [func]
  (fn [] func nil))

(defn auto-complete-did-mount
  "Attaches the jQuery autocomplete functionality to DOM elements."
  []
  (js/$ (fn []
          (let [available-tags all-movements]
            (.autocomplete (js/$ "#tags")
                           (clj->js {:source available-tags}))))))

(defn log-session []
  (let [log (session/get :logged-sessions)
        timestamp (.getTime (js/Date.))
        s (swap! movement-session assoc :timestamp)
        new-sessions (conj log movement-session)]
    (session/put! :logged-sessions new-sessions)))

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
  (with-meta text-input-component {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn movement-component []
  (let [editing (atom false)
        show-buttons (atom false)]
    (fn [m]
      (let [;id movement/name category graphic animation equipment rep set distance duration
            name (:movement/name m)]
        [:div
         [:div.view {:class    (str (if @editing "editing"))
                     :on-click #(handler-fn (reset! show-buttons (not @show-buttons)))}
          [:div.row
           (when @show-buttons
             [:div.two.columns
              [:button.refresh
               {:on-click #(#_(refresh! id (:category (get (:parts @movement-session) category-ref))))}]
              [:button.textedit
               {:on-click #(#_(handler-fn (reset! editing true)))}]
              [:button.destroy
               {:on-click #(#_(delete! :movements id))}]])
           [:label.four.columns {:style {:display (if @editing "none" "")}} name]
           (when @editing
             [:label.four.columns
              [text-edit-component {:class   "edit" :title name
                                    :on-save #(#_(handler-fn (update! :movements id %)))
                                    :on-stop #(#_(handler-fn (reset! editing false)))}]])]
          ]]))))

(defn part-component []
  (let [editing (atom false)]
    (fn [{:keys [title categories movements]}]
      [:div
       [:div.row
        [:h4.ten.columns {:style {:display (if @editing "none" "")}
                          :on-click #(handler-fn (reset! editing true))} title]
        (when @editing
          [:h4.ten.columns
           [text-edit-component {:class   "edit" :title title
                                 :on-save #(#_(handler-fn (update! :parts id %)))
                                 :on-stop #(handler-fn (reset! editing false))}]])]
       [:div#movement-list
        (for [m movements]
          ^{:key m} [movement-component m])]
       [:button {:type     "submit"
                 :on-click #()} "+"]])))

(defn session-component []
  (let [editing (atom false)
        adding-description (atom false)]
    (fn [{:keys [title description parts]}]
      [:div
       [:div.row
        [:h3.ten.columns {:style {:display (if @editing "none" "")}
                          :on-click #(handler-fn (reset! editing true))} title]
        (when @editing
          [:h3.ten.columns [text-edit-component {:class   "edit" :title title
                                                 :on-save #(#_(handler-fn (add-title! %)))
                                                 :on-stop #(handler-fn (reset! editing false))}]])
        [:label.one.colum (str (.getDay (js/Date.)) "/" (.getMonth (js/Date.)))]]
       [:div.row
        [:div.eight.columns
         [:div description]]
        (when @adding-description
          [:div.eight.columns
           [text-edit-component {:class   "edit" :title (:description @movement-session)
                       :on-save #(handler-fn (swap! movement-session assoc :description %))
                       :on-stop #(handler-fn (reset! adding-description false))}]])
        [:div.one.colum
         [:button {:type     "submit"
                          :on-click #(handler-fn (reset! adding-description true))} "!"]]]
       [:div
        (for [p parts]
          ^{:key p} [part-component p])]
       [:button.button {:type     "submit"
                        :on-click log-session}
        "Log this movement session!"]
       [:button.button {:on-click #()} "Make PDF"]])))

(defn template-component []
  (let []
    (fn [url]
      [:div {:on-click
             #(go
               (let [session (read-string (<! (GET (str "template/" (str/replace url " " "-")))))]
                 (reset! movement-session session)))}
       url])))

(defn home-component []
  (let []
    (fn []
      [:div
       [:div.container
        [nav-component]
        [:section#templates
         (doall
           (for [t @templates]
             ^{:key t} [template-component t]))]
        [:section#session
         (when (not (nil? (:title @movement-session)))
           [session-component @movement-session])]]])))

(defn movement-detail-component []
  [:div
   [:div.container
    [nav-component]
    [:section
     [:div "This section allow users to discover the movements in the database,
    view the animations more clearly, add their own comments to the movements
    and mark the subjective difficulty level of the movements."]]]])

(defn about-component []
  [:div
   [:div.container
    [nav-component]
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
        [nav-component]
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