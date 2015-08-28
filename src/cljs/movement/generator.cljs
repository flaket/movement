(ns movement.generator
  (:require
    [reagent.session :as session]
    [reagent.core :refer [atom]]
    [clojure.string :as str]
    [cljs.reader :refer [read-string]]
    [cljs.core.async :refer [<! chan close!]]
    [movement.text :refer [text-edit-component]]
    [movement.menu :refer [menu-component]]
    [movement.state :refer [movement-session handler-fn log-session GET]])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))

(defn movement-component []
  (let []
    (fn [{:keys [id category graphic animation equipment rep set distance duration] :as m}]
      (let [name (:movement/name m)]
        [:div#movement
         [:div.pure-g
          [:button.pure-u-1-2.refresh
           {:on-click #()}]
          [:button.pure-u-1-2.destroy
           {:on-click #()}]]
         [:div.pure-g
          [:label.pure-u name]]
         [:div.pure-g
          [:label.pure-u graphic]]
         [:div.pure-g
          [:label.pure-u-3-24 rep]
          [:label.pure-u-3-24 set]]]
        ))))

(defn part-component []
  (let []
    (fn [{:keys [title categories movements]}]
      [:div
       [:div.pure-g
        [:h2.pure-u title]]
       [:div
        (for [m movements]
          ^{:key m} [movement-component m])]
       [:div.pure-g
        [:button.pure-u {:type     "submit"
                         :on-click #()} "+"]]])))

(defn session-component []
  (let [adding-description (atom false)]
    (fn [{:keys [title description parts]}]
      [:div
       [:div.pure-g
        [:h1.pure-u title]
        [:label.pure-u (str (.getDay (js/Date.)) "/" (.getMonth (js/Date.)))]]
       [:div.pure-g
        [:div.pure-u
         [:div description]]
        (when @adding-description
          [:div.pure-g
           [text-edit-component {:class   "edit" :title description
                                 :on-save #(handler-fn (session/assoc-in! [:movement-session :description] %))
                                 :on-stop #(handler-fn (reset! adding-description false))}]])
        [:div.pure-g
         [:button.pure-u {:type     "submit"
                          :on-click #(handler-fn (reset! adding-description true))} "!"]]]
       [:div
        (for [p parts]
          ^{:key p} [part-component p])]
       [:div.pure-g
        [:button.pure-u {:type     "submit"
                         :on-click log-session}
         "Log this movement session!"]
        [:button.pure-u {:on-click #()} "Make PDF"]]])))

(defn template-component []
  (let []
    (fn [url]
      [:div.pure-u-1-3.pure-u-md-1-8
       {:on-click #(go
                    (let [session (read-string (<! (GET (str "template/" (str/replace url " " "-")))))]
                      (session/put! :movement-session session)))}
       url])))

(defn generator-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}

       [menu-component]

       [:div#main
        [:div.header
         [:h1 "Movement Session Generator"]

         [:div.pure-g
          (doall
            (for [t (session/get :templates)]
              ^{:key t} [template-component t]))]]

        [:div.content
         (when (not (nil? (session/get :movement-session)))
           [session-component (session/get :movement-session)])]]])))