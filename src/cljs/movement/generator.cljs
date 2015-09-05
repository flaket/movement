(ns movement.generator
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:import [goog.events EventType])
  (:require
    [reagent.session :as session]
    [reagent.core :refer [atom]]
    [cljs.core.async :refer (<!)]
    [goog.events :as events]
    [clojure.string :as str]
    [movement.util :refer [GET]]
    [movement.text :refer [text-edit-component]]
    [movement.menu :refer [menu-component]]
    [movement.state :refer [movement-session handler-fn log-session]]))

(defn equipment-symbol [equipment-name]
  (first (shuffle ["images/squat.png"
                   "images/push-up.png"
                   "images/high-bridge.png"
                   "images/frog-stand.png"
                   "images/broad-jump.png"
                   "images/elastic-band-overhead-pull-down.png"
                   "images/pull-up-reach.png"
                   "images/pull-up.png"
                   "images/stepping-over.png"
                   "images/side-swing.png"
                   "images/front-leg-swing.png"])))

(defn positions
  "Finds the integer positions of the elements in the collection, that matches the predicate."
  [pred coll]
  (keep-indexed
    (fn [idx x]
      (when (pred x)
        idx))
    coll))

(defn add-movement [part-title]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        categories (:categories (first (filter #(= part-title (:title %)) parts)))]
    (GET "singlemovement"
         {:params        {:categories categories}
          :format        :edn
          :handler       #(session/update-in! [:movement-session :parts position-in-parts :movements]
                                              conj (first %))
          :error-handler #(print "error getting single movement through add.")})
    ))

(defn refresh-movement [m part-title]
  (let [parts (session/get-in [:movement-session :parts])
        pos-in-parts (first (positions #{part-title} (map :title parts)))
        movement-list (vec (session/get-in [:movement-session :parts pos-in-parts :movements]))
        pos-in-movement-list (first (positions #{m} movement-list))
        ;todo: get categories from the movement, not the part.
        categories (:categories (first (filter #(= part-title (:title %)) parts)))]
    (GET "singlemovement"
         {:params        {:categories categories}
          :format        :edn
          :handler       #(session/assoc-in! [:movement-session :parts pos-in-parts :movements]
                                             (assoc movement-list pos-in-movement-list (first %)))
          :error-handler #(print "error getting single movement through refresh.")})))

(defn remove-movement [m part-title]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        movement-list (session/get-in [:movement-session :parts position-in-parts :movements])
        movement-list (remove #{m} movement-list)]
    (session/assoc-in! [:movement-session :parts position-in-parts :movements] movement-list)))



(defn movement-component []
  (let [rep-text (atom "Rep")
        set-text (atom "Set")]
    (fn [{:keys [id category graphic animation equipment rep set distance duration] :as m} part-title]
      (let [name (:movement/name m)
            rep 10
            set 3
            graphic (equipment-symbol "")]
        [:div.pure-u.movement

         [:div.pure-g
          [:div.pure-u-1-2.refresh {:on-click #(refresh-movement m part-title)}]
          [:div.pure-u-1-2.destroy {:on-click #(remove-movement m part-title)}]]

         [:div.pure-g.title
          [:span.pure-u name]]

         [:div.pure-g
          [:img.pure-u.graphic.pure-img-responsive {:src graphic}]]

         [:div.pure-g

          [:div.pure-u-1-4.sw
           [:div.pure-g
            [:span.pure-u.rep-text {:on-click #(if (= @rep-text "Rep")
                                                (reset! rep-text "Distance")
                                                (reset! rep-text "Rep"))} @rep-text]]
           [:div.pure-g
            [:span.pure-u.rep rep]]]

          [:div.pure-u-1-2
           [:div.pure-g
            [:img.pure-u.icon {:src (equipment-symbol equipment)}]
            ]]

          [:div.pure-u-1-4.se
           [:div.pure-g
            [:span.pure-u.set-text {:on-click #(if (= @set-text "Set")
                                                (reset! set-text "Duration")
                                                (reset! set-text "Set"))} @set-text]]
           [:div.pure-g
            [:span.pure-u.set set]]]]

         ]))))

(defn part-component []
  (let []
    (fn [{:keys [title categories movements]}]
      [:div
       [:h2 title]
       [:button {:type     "submit"
                 :on-click #(add-movement title)} "+"]
       [:div.pure-g
        (for [m movements]
          ^{:key (str m (rand-int 100000))} [movement-component m title])]])))

(defn session-component []
  (let [adding-description (atom false)]
    (fn [{:keys [title description parts]}]
      [:div#session
       [:div.pure-g
        [:label.pure-u.pure-u-md-1-5]
        [:h1.pure-u.pure-u-md-3-5 title]
        [:label.pure-u.pure-u-md-1-5 (str (.getDay (js/Date.)) "/" (.getMonth (js/Date.)))]]
       [:div description]
       (when @adding-description
         [text-edit-component {:class   "edit" :title description
                               :on-save #(handler-fn (session/assoc-in! [:movement-session :description] %))
                               :on-stop #(handler-fn (reset! adding-description false))}])
       [:button {:type     "submit"
                 :on-click #(handler-fn (reset! adding-description true))} "!"]
       (for [p parts]
         ^{:key p} [part-component p])
       [:div.pure-g
        [:button.pure-u-1-3 {:type     "submit"
                             :on-click log-session}
         "Log this movement session"]
        [:button.pure-u-1-3 {:on-click #()} "Share"]
        [:button.pure-u-1-3 {:on-click #()} "Make PDF"]]])))

(defn add-session-handler [session]
  (session/put! :movement-session session))

(defn template-component []
  (let []
    (fn [template-name]
      [:li {:on-click #(GET (str "template/" (str/replace template-name " " "-"))
                            {:handler       add-session-handler
                             :error-handler (fn [] (print "error getting session data from server."))})}
       template-name]

      #_[:div.pure-u-1-3.pure-u-md-1-8
         {:on-click #(GET (str "template/" (str/replace template-name " " "-"))
                          {:handler       add-session-handler
                           :error-handler (fn [] (print "error getting session data from server."))})}
         template-name])))

(defonce getting-templates
         (GET "templates" {:handler       #(session/put! :templates %)
                           :error-handler #(print "error retrieving templates.")}))

(defn generator-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div#main
        [:div.header
         [:h1 "Movement Session"]
         #_[:div.pure-g]
         [:div "Create a brand new movement session HERE."]
         [:div "Or generate a new session based on one of your "
          [:ul.templates
           [:li
            [:ul
             (doall
               (for [t (session/get :templates)]
                 ^{:key t} [template-component t]))]
            "templates"]]]]
        [:div.content
         (when (not (nil? (session/get :movement-session)))
           [session-component (session/get :movement-session)])]]])))