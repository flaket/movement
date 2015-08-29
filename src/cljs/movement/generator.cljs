(ns movement.generator
  (:require
    [reagent.session :as session]
    [reagent.core :refer [atom]]
    [clojure.string :as str]
    [cljs.reader :refer [read-string]]
    [cljs.core.async :refer [<! chan close!]]
    [movement.text :refer [text-edit-component]]
    [movement.menu :refer [menu-component]]
    [movement.state :refer [movement-session handler-fn log-session GET POST]])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))

(defn equipment-symbol [equipment-name]
  "images/sketch-push-up.png")

(defn movement-component []
  (let []
    (fn [{:keys [id category graphic animation equipment rep set distance duration] :as m}]
      (let [name (:movement/name m)
            rep 0
            set 0
            graphic "images/sketch-push-up.png"]
        [:div#movement
         [:div.pure-g
          [:button.pure-u.refresh {:on-click #()}]
          [:h4.pure-u.title name]
          [:button.pure-u.destroy {:on-click #()}]]
         [:div.pure-g
          [:img.pure-u {:src graphic :width "250px" :height "250px"}]]
         [:div.pure-g
          [:div.pure-u
           [:span "Rep"]
           [:span rep]]
          [:img.pure-u {:src (equipment-symbol equipment) :width "30px" :height "30px"}]
          [:div.pure-u
           [:span "Set"]
           [:span set]]]]
        ))))

(defn get-new-movement [categories]
  (go
    (let [categories-prepped (mapv #(str/replace % " " "-") categories)
          m (read-string (<! (POST "singlemovement/" {:categories categories})))]
      (print categories-prepped)
      (print m))))

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
                         :on-click #(get-new-movement categories)} "+"]]])))

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
    (fn [template-name]
      [:div.pure-u-1-3.pure-u-md-1-8
       {:on-click #(go
                    (let [session (read-string
                                    (<! (GET (str "template/" (str/replace template-name " " "-")))))]
                      (session/put! :movement-session session)))}
       template-name])))

(defn generator-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}

       [menu-component]

       [:div#main
        [:div.header
         [:h1 "Movement Session"]

         [:div.pure-g
          (doall
            (for [t (session/get :templates)]
              ^{:key t} [template-component t]))]]

        [:div.content
         (when (not (nil? (session/get :movement-session)))
           [session-component (session/get :movement-session)])]]])))