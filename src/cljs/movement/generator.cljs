(ns movement.generator
  (:require-macros [cljs.core.async.macros :refer (go)]
                   [cljs.core :refer [this-as]])
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
    [movement.draggable :refer [draggable-number-component]]
    [movement.state :refer [movement-session handler-fn log-session]]))

(defonce m-counter (atom 0))
(defonce getting-templates
         (GET "templates" {:handler       #(session/put! :templates %)
                           :error-handler #(print "error retrieving templates.")}))

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
        categories (:categories (first (filter #(= part-title (:title %)) parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])]
    (GET "singlemovement"
         {:params        {:categories categories}
          :format        :edn
          :handler       #(let [id (swap! m-counter inc)
                                new-movement (first %)
                                new-movement (assoc new-movement :id id)
                                new-movements (assoc movements id new-movement)]
                           (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
          :error-handler #(print "error getting single movement through add.")})))

(defn refresh-movement [m part-title]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        categories (:categories (first (filter #(= part-title (:title %)) parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])]
    (GET "singlemovement"
         {:params        {:categories categories}
          :format        :edn
          :handler       #(let [id (:id m)
                                new-movement (first %)
                                new-movement (assoc new-movement :id id)
                                new-movements (assoc movements id new-movement)]
                           (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
          :error-handler #(print "error getting single movement through refresh.")})))

(defn remove-movement [m part-title]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])
        movements (dissoc movements (:id m))]
    (session/assoc-in! [:movement-session :parts position-in-parts :movements] movements)))

(defonce rep-atom (atom "-"))

(defn set-element-values!
  [class value]
  (let [elements (array-seq (.getElementsByClassName js/document class))]
    (doseq [e elements]
      (do (set! (.-value e) value)
          #_(.onchange e)))))

(defn set-movement-rep-text! [])
(defn set-movement-set-text! [])
(defn set-movement-rep! [])
(defn set-movement-set! [])

(defn movement-component []
  (let [rep-text (atom "Rep")
        set-text (atom "Set")]
    (fn [{:keys [id category graphic animation equipment] :as m} part-title]
      (let [name (:movement/name m)
            graphic (equipment-symbol "")
            description "movement description"]
        [:div.pure-u.movement

         [:div.pure-g
          [:div.pure-u-1-2.refresh {:on-click #(refresh-movement m part-title)
                                    :title "Swap with another movement"}]
          [:div.pure-u-1-2.destroy {:on-click #(remove-movement m part-title)
                                    :title "Remove movement"}]]

         [:div.pure-g.title
          [:span.pure-u name]]

         [:div.pure-g
          [:img.pure-u.graphic.pure-img-responsive {:src graphic :title name
                                                    :alt name}]]

         [:div.pure-g

          [:div.pure-u-1-3.sw
           [:div.pure-g
            [:label.pure-u.rep-text {:on-click #(if (= @rep-text "Rep")
                                                 (let [elements (array-seq (.getElementById js/document (str "rep-" id)))]
                                                   (doseq [e elements]
                                                     (set! (.-selected e) false))
                                                   (reset! rep-text "Distance"))
                                                 (let [elements (array-seq (.getElementById js/document (str "distance-" id)))]
                                                   (doseq [e elements]
                                                     (set! (.-selected e) false))
                                                   (reset! rep-text "Rep")))
                                     :title    "Change between rep and distance"}
             @rep-text]
            (let [txt @rep-text]
              (case txt
                "Rep" [:div.pure-u.rep {:className " custom-select"}
                       [:select {:className "rep-select"
                                 :id (str "rep-" id)
                                 :on-change #(let [x (.-value (.getElementById js/document (str "rep-" id)))]
                                                         (print x))}
                        [:option "-"]
                        [:option 1]
                        [:option 2]
                        [:option 3]
                        [:option 4]
                        [:option 5]
                        [:option 6]
                        [:option 7]
                        [:option 8]
                        [:option 9]
                        [:option 10]
                        [:option 15]
                        [:option 20]
                        [:option 25]
                        [:option 30]
                        [:option 40]
                        [:option 50]
                        [:option 60]
                        [:option 80]
                        [:option 100]]]
                "Distance" [:div.pure-u.rep {:className " custom-select"}
                             [:select {:className "distance-select"
                                       :id (str "distance-" id)
                                       :on-change #(let [x (.-value (.getElementById js/document (str "distance-" id)))]
                                                    (print x))}
                              [:option "-"]
                              [:option "10 m"]
                              [:option "20 m"]
                              [:option "50 m"]
                              [:option "100 m"]
                              [:option "200 m"]
                              [:option "300 m"]
                              [:option "400 m"]
                              [:option "600 m"]
                              [:option "800 m"]
                              [:option "1000 m"]
                              [:option "1.5 km"]
                              [:option "2 km"]
                              [:option "3 km"]
                              [:option "4 km"]
                              [:option "5 km"]
                              [:option "10 km"]
                              [:option "15 km"]
                              [:option "20 km"]]]))]]

          [:div.pure-u-1-3
           #_[:div.pure-g
            [:img.pure-u.icon {:src (equipment-symbol equipment)}]
            ]]

          [:div.pure-u-1-3.se
           [:div.pure-g
            [:label.pure-u.set-text {:on-click #(if (= @set-text "Set")
                                                 (let [elements (array-seq (.getElementById js/document (str "set-" id)))]
                                                   (doseq [e elements]
                                                     (set! (.-selected e) false))
                                                   (reset! set-text "Duration"))
                                                 (let [elements (array-seq (.getElementById js/document (str "duration-" id)))]
                                                   (doseq [e elements]
                                                     (set! (.-selected e) false))
                                                   (reset! set-text "Set")))
                                     :title "Change between set and duration"}
             @set-text]
            (let [txt @set-text]
              (case txt
                "Set" [:div.pure-u.set {:className " custom-select"}
                                     [:select {:className "set-select"
                                               :id (str "set-" id)
                                               :on-change #(let [x (.-value (.getElementById js/document (str "set-" id)))]
                                                            (print x))}
                                      [:option "-"]
                                      [:option 1]
                                      [:option 2]
                                      [:option 3]
                                      [:option 4]
                                      [:option 5]
                                      [:option 6]
                                      [:option 7]
                                      [:option 8]
                                      [:option 9]
                                      [:option 10]]]
                "Duration" [:div.pure-u.duration {:className " custom-select"}
                            [:select {:className "duration-select"
                                      :id (str "duration-" id)
                                      :on-change #(let [x (.-value (.getElementById js/document (str "duration-" id)))]
                                                   (print x))}
                             [:option "-"]
                             [:option "5 s"]
                             [:option "10 s"]
                             [:option "15 s"]
                             [:option "20 s"]
                             [:option "30 s"]
                             [:option "45 s"]
                             [:option "60 s"]
                             [:option "2 min"]
                             [:option "3 min"]
                             [:option "4 min"]
                             [:option "5 min"]
                             [:option "6 min"]
                             [:option "7 min"]
                             [:option "8 min"]
                             [:option "9 min"]
                             [:option "10 min"]
                             [:option "15 min"]
                             [:option "20 min"]
                             [:option "25 min"]
                             [:option "30 min"]
                             [:option "45 min"]
                             [:option "60 min"]
                             [:option "90 min"]
                             [:option "120 min"]]]))]]]

         ]))))

(defn part-component []
  (let []
    (fn [{:keys [title categories movements]}]
      [:div
       [:h2 title]
       [:button {:type     "submit"
                 :on-click #(add-movement title)} "+"]
       [:div.pure-g
        (doall
          (for [m (vals movements)]
            ^{:key (str m (rand-int 100000))} [movement-component m title]))]])))

(defn session-component []
  (let [adding-description (atom false)]
    (fn [{:keys [title description parts]}]
      [:div#session
       [:div.pure-g
        [:label.pure-u.pure-u-md-1-5]
        [:h1.pure-u.pure-u-md-3-5 title]
        [:button.pure-u.pure-u-md-1-5 {:on-click #(set-element-values! "rep-select" 5)} "Fill in 5 reps"]]
       [:div {:on-click #(handler-fn (reset! adding-description true))} description]
       (when @adding-description
         [text-edit-component {:class   "edit" :title description
                               :on-save #(handler-fn (session/assoc-in! [:movement-session :description] %))
                               :on-stop #(handler-fn (reset! adding-description false))}])

       (doall
         (for [p parts]
           ^{:key p} [part-component p]))
       [:div.pure-g
        [:button.pure-u-1-3 {:type     "submit"
                             :on-click log-session}
         "Log this movement session"]
        [:button.pure-u-1-3 {:on-click #()} "Share"]
        [:button.pure-u-1-3 {:on-click #()} "Make PDF"]]])))

(defn list-to-sorted-map [list-of-movements]
  (let [movements (atom (sorted-map))]
    (doseq [m list-of-movements
            :let [id (swap! m-counter inc)]]
      (swap! movements assoc id (assoc m :id id)))
    @movements))

(defn add-session-handler [session]
  (let [new-parts (mapv #(assoc % :movements (list-to-sorted-map (:movements %)))
                       (:parts session))
        new-session (assoc session :parts new-parts)]
    (session/put! :movement-session new-session)))

(defn template-component []
  (let []
    (fn [template-name]
      [:li {:on-click #(GET (str "template/" (str/replace template-name " " "-"))
                            {:handler       add-session-handler
                             :error-handler (fn [] (print "error getting session data from server."))})}
       template-name])))

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