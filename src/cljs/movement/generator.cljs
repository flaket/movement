(ns movement.generator
  (:require
    [reagent.session :as session]
    [reagent.core :refer [atom]]
    [clojure.string :as str]
    [cljs.reader :refer [read-string]]
    [cljs.core.async :refer [<! chan close!]]
    [movement.text :refer [text-edit-component]]
    [movement.nav :refer [nav-component]]
    [movement.state :refer [movement-session handler-fn log-session GET]])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))

(defn movement-component []
  (let []
    (fn [m]
      (let [;id movement/name category graphic animation equipment rep set distance duration
            name (:movement/name m)]
        [:div
         [:div.row
          [:div.two.columns
           [:button.refresh
            {:on-click #()}]
           [:button.textedit
            {:on-click #()}]
           [:button.destroy
            {:on-click #()}]]
          [:label.four.columns name]]
         ]))))

(defn part-component []
  (let []
    (fn [{:keys [title categories movements]}]
      [:div
       [:div.row
        [:h4.ten.columns title]]
       [:div#movement-list
        (for [m movements]
          ^{:key m} [movement-component m])]
       [:button {:type     "submit"
                 :on-click #()} "+"]])))

(defn session-component []
  (let [adding-description (atom false)]
    (fn [{:keys [title description parts]}]
      [:div
       [:div.row
        [:h3.ten.columns title]
        [:label.one.colum (str (.getDay (js/Date.)) "/" (.getMonth (js/Date.)))]]
       [:div.row
        [:div.eight.columns
         [:div description]]
        (when @adding-description
          [:div.eight.columns
           [text-edit-component {:class   "edit" :title description
                                 :on-save #(handler-fn (session/assoc-in! [:movement-session :description] %))
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
                 (session/put! :movement-session session)))}
       url])))

(defn generator-component []
  (let []
    (fn []
      [:div
       [:div.container
        [nav-component]
        [:section#templates
         (doall
           (for [t (session/get :templates)]
             ^{:key t} [template-component t]))]
        [:section#session
         (when (not (nil? (session/get :movement-session)))
           [session-component (session/get :movement-session)])]]])))