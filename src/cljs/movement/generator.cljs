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
    [movement.util :refer [GET POST]]
    [movement.text :refer [text-edit-component]]
    [movement.menu :refer [menu-component]]
    [movement.state :refer [movement-session handler-fn log-session]]))

(defonce m-counter (atom 0))

(defn image-url [name]
  (str "images/" (str/replace (str/lower-case name) " " "-") ".png"))

(defn positions
  "Finds the integer positions of the elements in the collection, that matches the predicate."
  [pred coll]
  (keep-indexed
    (fn [idx x]
      (when (pred x)
        idx))
    coll))

(defn update-session! [nav-list movement]
  (session/update-in! nav-list conj (first movement)))

(defn add-movement [part-title]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        categories (:categories (first (filter #(= part-title (:title %)) parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])]
    (GET "singlemovement"
         {:params        {:categories categories}
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
    (GET "movement/"
         {:params         {:name movement-name}
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
  (GET "template"
       {:params        {:template-name template-name
                        :user (session/get :user)}
        :handler       add-session-handler
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

(defn movement-component [{:keys [id category] :as m} {:keys [title rep set distance duration]}]
  (let [rep-text (cycle ["repetitions" "meters" "seconds"])
        rep-text (atom "repetitions")
        set-text (atom "set")
        show-rep-slider (atom false)
        show-set-slider (atom false)
        movement-data (atom {:rep      (if rep rep "-")
                             :set      (if set set "-")
                             :distance (if distance distance "-")
                             :duration (if duration duration "-")})]
    (fn []
      (let [name (:movement/name m)
            graphic (image-url name)]
        [:div.pure-u.movement {:id (str "m-" id)}
         [:div
          [:div.refresh {:on-click #(refresh-movement m title) :title "Swap with another movement"}]
          [:div.destroy {:on-click #(remove-movement m title) :title "Remove movement"}]]
         [:h3.title name]
         #_[:img.graphic.pure-img-responsive {:src graphic :title name :alt name}]
         (let [txt @rep-text]
           (case txt
             "repetitions" [:div
                            [:div.rep {:on-click #(reset! show-rep-slider true)} (:rep @movement-data)]
                            [:div.rep-text {:on-click #(if (= txt "repetitions")
                                                (reset! rep-text "meters")
                                                (reset! rep-text "repetitions"))
                                            :title    "Change between repetitions and distance"} txt]
                            (when @show-rep-slider
                              [:div
                               [:input {:type      "range" :value (:rep @movement-data) :min 1 :max 100
                                :style     {:width "100%"}
                                :on-change #(swap! movement-data assoc :rep (.-target.value %))}]
                               [:div {:on-click #(reset! show-rep-slider false)} "x"]])]
             "meters" [:div
                       [:div.rep {:on-click #(reset! show-rep-slider true)} (:distance @movement-data)]
                       [:div.rep-text {:on-click #(if (= txt "repetitions")
                                                   (reset! rep-text "meters")
                                                   (reset! rep-text "repetitions"))
                                                :title "Change between repetitions and distance"} txt]
                       (when @show-rep-slider
                         [:div
                          [:input {:type      "range" :value (:distance @movement-data) :min 1 :max 400
                                   :style     {:width "100%"}
                                   :on-change #(swap! movement-data assoc :distance (.-target.value %))}]
                          [:div {:on-click #(reset! show-rep-slider false)} "x"]])]))
         [:div
          [:div {:on-click #(reset! show-set-slider true)} (:set @movement-data)]
          [:div "set"]
          (when @show-set-slider
            [:div
             [:input {:type      "range" :value (:set @movement-data) :min 1 :max 10
                      :style     {:width "100%"}
                      :on-change #(swap! movement-data assoc :set (.-target.value %))}]
             [:div {:on-click #(reset! show-set-slider false)} "x"]])]]))))

(defn part-component []
  (let [show-search-input (atom false)]
    (fn [{:keys [title movements] :as part}]
      [:div.part
       [:h2 title]
       [:div.pure-g
        [:p.pure-u-1-2 [:a.secondary-button {:on-click #(add-movement title)} "+"]]]
       [:div.pure-g
        (doall
          (for [m (vals movements)]
            ^{:key (str m (rand-int 100000))} [movement-component m part]))]])))

(defn header-component []
  (let [date (js/Date.)
        day (.getDate date)
        month (.getMonth date)]
    (fn [{:keys [title description]}]
      [:div
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-5 (str day "/" month)]
        [:h1.pure-u.pure-u-md-3-5 title]]
       [:p.subtitle description]])))


(defn template-component []
  (let []
    (fn [name]
      [:a.pure-u.secondary-button {:on-click #(create-session-from-template name)} name])))

(defn blank-state-component []
  (let [templates-showing? (atom false)]
    (fn []
      [:div.blank-state
       [:div.pure-g
        [:h1.pure-u "Let's create your next Movement Session"]]
       [:div.pure-g
        [:h3.pure-u [:a.secondary-button {:on-click pick-random-template} "Get inspired by a random movement session."]]]
       [:div.pure-g
        [:h3.pure-u [:a.secondary-button {:on-click #(reset! templates-showing? true)} "Or generate a new session based on one of your templates"]]]
       [:div.pure-g
        (when @templates-showing?
          (doall
            (for [t (session/get :templates)]
              ^{:key t} [template-component t])))]])))



(defn top-menu-component []
  (let [templates-showing? (atom false)]
    (fn []
      [:div
       [:div.pure-g
        [:h3.pure-u.pure-u-md-1-4
         [:div.pure-g
          [:a.pure-u.secondary-button {:on-click pick-random-template} "Random session"]]]
        [:h3.pure-u.pure-u-md-1-4
         [:div.pure-g
          [:a.pure-u.secondary-button {:on-click #(handler-fn (reset! templates-showing? (not @templates-showing?)))} "Select template"]]]]
       [:div.pure-g
        (when @templates-showing?
          (doall
            (for [t (session/get :templates)]
              ^{:key t} [template-component t])))]])))

(defn comment-component []
  (let [adding-comment (atom false)]
    (fn [comments]
      [:div
       [:div.pure-g
        [:p.pure-u [:a.secondary-button {:on-click #(handler-fn (reset! adding-comment true))} "Add comments"]]]
       [:div.pure-g
        (when @adding-comment [text-edit-component {:class   "pure-u edit"
                                                    :on-save #(handler-fn (session/update-in! [:movement-session :comment] conj %))
                                                    :on-stop #(handler-fn (reset! adding-comment false))
                                                    :size    45}])]
       [:div.pure-u
        (for [c comments]
          ^{:key c} [:div.comment
                     [:p (str c)]])]])))

(defn finish-session-component []
  (let []
    (fn []
      [:div.pure-g
       [:h3.pure-u [:div.pure-g [:a.pure-u.log-button {:on-click #(do
                                                                   #_(store-rep-set-info)
                                                                   (print (vals (:movements (first (session/get-in [:movement-session :parts])))))
                                                                   #_(print (session/get :movement-session))
                                                                   #_(POST "store-session"
                                                                           {:params        {:session (session/get :movement-session)
                                                                                            :user    (session/get :user)}
                                                                            :handler       (fn [response] (print response))
                                                                            :error-handler (fn [response] (print response))}))}
                                 "Finish movement session"]]]])))

(defn generator-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content
        (if-let [session (session/get :movement-session)]
          [:div
           [top-menu-component]
           [header-component session]
           (doall
             (for [p (:parts session)]
               ^{:key p} [part-component p]))
           [comment-component (:comment session)]
           [finish-session-component]]
          [blank-state-component])]])))