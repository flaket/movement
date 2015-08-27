(ns movement.generator
  (:require
    [clojure.string :as str]
    [cljs.reader :refer [read-string]]
    [cljs.core.async :refer [<! chan close!]]
    [movement.text :refer [text-edit-component]]
    [movement.nav :refer [nav-component]]
    [movement.state :refer [movement-session templates handler-fn log-session GET]])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))

(defn movement-component []
  (let [editing (atom false)
        show-buttons (atom false)]
    (fn [m]
      (let [;id movement/name category graphic animation equipment rep set distance duration
            name (:movement/name m)]
        [:div
         [:div.view {:class    (str (if @editing "editing"))
                     :on-click #(do (reset! show-buttons (not @show-buttons)) nil)}
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

(defn generator-component []
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