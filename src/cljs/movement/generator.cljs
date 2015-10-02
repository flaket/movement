(ns movement.generator
  (:import [goog.events EventType])
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :as async :refer [>! <! put! take! alts! chan sliding-buffer close!]]
    [reagent.session :as session]
    [reagent.core :refer [atom]]
    [goog.events :as events]
    [clojure.string :as str]
    [movement.util :refer [GET]]
    [movement.text :refer [text-edit-component]]
    [movement.menu :refer [menu-component]]
    [movement.state :refer [movement-session handler-fn log-session]]))

(defonce m-counter (atom 0))

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
                   "images/front-leg-swing.png"
                   "images/burpee.png"
                   "images/sit-up.png"
                   "images/sit-up-pike.png"
                   "images/jump-rope.png"
                   "images/pistol-single-leg-squat.png"
                   "images/hollow-body-rock.png"
                   "images/handstand-walk.png"
                   "images/handstand-push-up-kip.png"
                   "images/dip.png"
                   "images/russian-dip.png"
                   "images/kick-to-handstand.png"
                   "images/lying-roll.png"
                   "images/ring-l-sit.png"
                   "images/side-leg-swing.png"])))

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

    #_(go
      (let [m (<! (ajax "GET" "singlemovement" {:params {:categories categories}}))
            id (swap! m-counter inc)
            new-movement (first m)
            new-movement (assoc new-movement :id id)
            new-movements (assoc movements id new-movement)]
        (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements)))

    (GET "singlemovement"
         {:params        {:categories categories}
          :format        :edn
          :handler       #(let [id (swap! m-counter inc)
                                new-movement (first %)
                                new-movement (assoc new-movement :id id)
                                new-movements (assoc movements id new-movement)]
                           (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
          :error-handler #(print "error getting single movement through add.")})))

(defn add-movement-from-search [part-title movement-name]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])]
    (GET (str "movement/" (str/replace movement-name " " "-"))
         {:format        :edn
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

(defn set-element-values!
  [class value]
  (let [elements (array-seq (.getElementsByClassName js/document class))]
    (doseq [e elements]
      (do (set! (.-value e) value)))))

(defonce rep-atom (atom "-"))
(defn set-movement-rep-text! [])
(defn set-movement-set-text! [])
(defn set-movement-rep! [])
(defn set-movement-set! [])

(defn list-to-sorted-map [list-of-movements]
  (let [movements (atom (sorted-map))]
    (doseq [m list-of-movements
            :let [id (swap! m-counter inc)]]
      (swap! movements assoc id (assoc m :id id)))
    @movements))

(defn add-session-handler [session]
  (let [new-parts (mapv #(assoc % :movements (list-to-sorted-map (:movements %)))
                        (:parts session))
        new-session (assoc session :parts new-parts)
        new-session (assoc new-session :comment [])
        ]
    (session/put! :movement-session new-session)))

(defn create-session-from-template [template-name]
  (GET (str "template/" (str/replace template-name " " "-"))
       {:handler       add-session-handler
        :error-handler (fn [] (print "error getting session data from server."))}))

(defn pick-random-template []
  (let [name (first (shuffle (session/get :templates)))]
    (create-session-from-template name)))

(defn store-rep-set-info []
  ; go through every movement
  ; look up the select's by id and get the values
  ; add valyes to movement map
  (let [parts (session/get-in [:movement-session :parts])]
    (doseq [p parts]
      (let [position-in-parts (first (positions #{(:title p)} (map :title parts)))
            new-movements (mapv (fn [[_ m]]
                                 (if-let [
                                       rep-selector (.getElementById js/document (str "rep-" (:id m)))
                                          ;set-selector (.getElementById js/document (str "set-" (:id m)))
                                          ;distance-selector (.getElementById js/document (str "distance-" id))
                                          ;duration-selector (.getElementById js/document (str "duration-" id))
                                          ]
                                   (try
                                     (when (not (= "-" (.-value rep-selector)))
                                       (assoc m :rep (.-value rep-selector)))
                                     (catch js/Object e (str "caught exception: " (.log js/console e))))
                                   #_(try
                                     (when (not (= "-" (.-value set-selector)))
                                       (assoc m :set (.-value set-selector)))
                                     (catch js/Object e (str "caught exception: " (.log js/console e))))
                                   #_(try
                                     (when (not (= "-" (.-value distance-selector)))
                                       (assoc m :distance (.-value distance-selector)))
                                     (catch js/Object e (str "caught exception: " (.log js/console e))))
                                   #_(try
                                     (when (not (= "-" (.-value duration-selector)))
                                       (assoc m :duration (.-value duration-selector)))
                                     (catch js/Object e (str "caught exception: " (.log js/console e))))))
                               (:movements p))]
        (session/assoc-in! [:logging-session :parts position-in-parts :movements] new-movements)))
    (print (session/get :logging-session))))


;;;;;; Components ;;;;;;

(defn movement-component []
  (let [rep-text (atom "Rep")
        set-text (atom "Set")
        description-showing (atom false)]
    (fn [{:keys [id category graphic animation equipment] :as m} part-title]
      (let [name (:movement/name m)
            graphic (equipment-symbol "")
            description "..movement description.."]
        [:div.pure-u.movement {:id        (str "m-" id)
                               :className ""}

         [:div.pure-g
          [:div.pure-u-1-2.refresh {:on-click #(refresh-movement m part-title)
                                    :title    "Swap with another movement"}]
          [:div.pure-u-1-2.destroy {:on-click #(remove-movement m part-title)
                                    :title    "Remove movement"}]]

         [:div.pure-g
          [:h3.pure-u.title {:title    "Click to view movement description"
                             :alt      name
                             :on-click #(handler-fn (reset! description-showing (not @description-showing)))}
           name]]

         (when @description-showing
           [:div.pure-g
            [:p.pure-u.pure-u-md-1-1.description description]])

         [:div.pure-g
          [:img.pure-u.graphic.pure-img-responsive {:src graphic :title name
                                                    :alt name}]]

         [:div.pure-g

          [:div.pure-u-1-2.sw
           [:div.pure-g
            [:p.pure-u.rep-text {:on-click #(if (= @rep-text "Rep")
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
                                 :id        (str "rep-" id)
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
                                      :id        (str "distance-" id)
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


          [:div.pure-u-1-2.se
           [:div.pure-g
            [:p.pure-u.set-text {:on-click #(if (= @set-text "Set")
                                             (let [elements (array-seq (.getElementById js/document (str "set-" id)))]
                                               (doseq [e elements]
                                                 (set! (.-selected e) false))
                                               (reset! set-text "Duration"))
                                             (let [elements (array-seq (.getElementById js/document (str "duration-" id)))]
                                               (doseq [e elements]
                                                 (set! (.-selected e) false))
                                               (reset! set-text "Set")))
                                 :title    "Change between set and duration"}
             @set-text]
            (let [txt @set-text]
              (case txt
                "Set" [:div.pure-u.set {:className " custom-select"}
                       [:select {:className "set-select"
                                 :id        (str "set-" id)
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
                                      :id        (str "duration-" id)
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
  (let [show-search-input (atom false)]
    (fn [{:keys [title categories movements]}]
      [:div.part
       [:h2 title]
       [:div.pure-g
        [:p.pure-u-1-2 [:a.secondary-button {:on-click #(add-movement title)} "+"]]
        [:p.pure-u-1-2 [:a {:style    {:float "right"}
                            :on-click #(reset! show-search-input true)} "Find movement"]]
        #_[movements-ac-component
         {:id      "mmtags"
          :class   "edit" :placeholder "type to find and add movement.."
          :on-save #(when (some #{%} (session/get :all-movements))
                     nil #_(add-movement-from-search title %))}]]
       [:div.pure-g
        (doall
          (for [m (vals movements)]
            ^{:key (str m (rand-int 100000))} [movement-component m title]))]])))

(defn session-component []
  (let [adding-comment (atom false)]
    (fn [{:keys [title description parts]}]
      [:div
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-5]
        [:h1.pure-u.pure-u-md-3-5 title]]
       [:p.subtitle description]

       (doall
         (for [p parts]
           ^{:key p} [part-component p]))
       [:div.pure-g
        [:p.pure-u [:a.secondary-button {:on-click #(handler-fn (reset! adding-comment true))} "Add comments to your session"]]]
       [:div.pure-g
        (when @adding-comment [text-edit-component {:class   "edit"
                                                    :on-save #(handler-fn (session/update-in! [:movement-session :comment] conj %))
                                                    :on-stop #(handler-fn (reset! adding-comment false))
                                                    :size    38}])]
       (let [comments (session/get-in [:movement-session :comment])]
         [:div.pure-u
          (for [c comments]
            ^{:key c} [:div.pure-g.comment
                       [:p.pure-u (str c)]])])
       [:div.pure-g
        [:h3.pure-u [:div.pure-g [:a.pure-u.log-button {:on-click log-session} "Log this movement session"]]]
        [:h3.pure-u [:div.pure-g [:a.pure-u.secondary-button {:on-click store-rep-set-info} "Share"]]]
        ]])))


(defn template-component []
  (let []
    (fn [name]
      [:li {:on-click #(create-session-from-template name)}
       name])))

(defn blank-state-component []
  (let []
    (fn []
      [:div.blank-state
       [:div.pure-g
        [:h1.pure-u "Let's create your next Movement Session"]]
       [:div.pure-g.random-button
        [:h3.pure-u [:a {:on-click pick-random-template} "Get inspired by a random movement session."]]]
       [:div.pure-g.template-button
        [:h3.pure-u [:a "Or generate a new session based on one of your "]
         [:ul.templates
          [:li
           [:ul
            (doall
              (for [t (session/get :templates)]
                ^{:key t} [template-component t]))]
           "templates"]]]]])))

(defn top-menu-component []
  (let []
    (fn []
      [:div.pure-g
       [:h3.pure-u-1-3
        [:div.pure-g
         [:a.pure-u.secondary-button {:on-click pick-random-template} "New random session"]]]
       [:h3.pure-u-1-3
        [:div.pure-g
         [:a.pure-u.secondary-button "Select "
          [:ul.templates
           [:li
            [:ul
             (doall
               (for [t (session/get :templates)]
                 ^{:key t} [template-component t]))] "template"]]]]]
       [:h3.pure-u.pure-u-md-1-4 "Set rep/set scheme for all movements "

        [:a {:on-click #(do (set-element-values! "rep-select" 10)
                            (set-element-values! "set-select" 3))} "10*3 "]
        [:a {:on-click #(do (set-element-values! "rep-select" 10)
                            (set-element-values! "set-select" 1))} "10*1 "]
        [:a {:on-click #(do (set-element-values! "rep-select" 5)
                            (set-element-values! "set-select" 1))} "5*1 "]
        [:a {:on-click #(do (set-element-values! "rep-select" 5)
                            (set-element-values! "set-select" 3))} "5*3"]]])))

(defn generator-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content
        (if (nil? (session/get :movement-session))
          [blank-state-component]
          [:div
           [top-menu-component]
           [session-component (session/get :movement-session)]])]])))