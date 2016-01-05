(ns movement.generator
  (:import [goog.events EventType])
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [reagent.session :as session]
    [reagent.core :refer [atom]]
    [cljs.core.async :as async :refer [timeout <!]]
    [cljs.reader :as reader]
    [goog.events :as events]
    [clojure.string :as str]
    [movement.util :refer [positions GET POST get-stored-sessions get-equipment]]
    [movement.text :refer [text-edit-component text-input-component auto-complete-did-mount]]
    [movement.menu :refer [menu-component]]
    [movement.components.login :refer [footer]]
    [movement.state :refer [movement-session handler-fn log-session]]))

(defonce m-counter (atom 0))

(defn image-url [name]
  (when-not (nil? name)
    (str "images/" (str/replace (str/lower-case name) " " "-") ".png")))

(defn add-movement [part-title]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        categories (:categories (first (filter #(= part-title (:title %)) parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])]
    (if-let [equipment (session/get-in [:movement-session :parts position-in-parts :equipment])]
      (GET "movement-from-equipment"
           {:params        {:equipment equipment}
            :handler       #(let [id (swap! m-counter inc)
                                  new-movement (first %)
                                  new-movement (assoc new-movement :id id)
                                  new-movements (assoc movements id new-movement)]
                             (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
            :error-handler #(print "error getting movement from equipment through add.")})
      (GET "singlemovement"
           {:params        {:categories categories}
            :handler       #(let [id (swap! m-counter inc)
                                  new-movement (first %)
                                  new-movement (assoc new-movement :id id)
                                  new-movements (assoc movements id new-movement)]
                             (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
            :error-handler #(print "error getting single movement through add.")}))))

(defn refresh-movement
  ([m part-title]
   (let [parts (session/get-in [:movement-session :parts])
         position-in-parts (first (positions #{part-title} (map :title parts)))
         categories (:categories (first (filter #(= part-title (:title %)) parts)))
         movements (session/get-in [:movement-session :parts position-in-parts :movements])]
     (if-let [equipment (session/get-in [:movement-session :parts position-in-parts :equipment])]
       (GET "movement-from-equipment"
            {:params        {:equipment equipment}
             :handler       #(let [id (:id m)
                                   new-movement (first %)
                                   new-movement (assoc new-movement :id id)
                                   new-movements (assoc movements id new-movement)]
                              (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
             :error-handler #(print "error getting movement from equipment through refresh.")})
       (GET "singlemovement"
            {:params        {:categories categories}
             :handler       #(let [id (:id m)
                                   new-movement (first %)
                                   new-movement (assoc new-movement :id id)
                                   new-movements (assoc movements id new-movement)]
                              (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
             :error-handler #(print "error getting single movement through refresh.")}))))
  ([m part-title new-difficulty]
   (let [parts (session/get-in [:movement-session :parts])
         position-in-parts (first (positions #{part-title} (map :title parts)))
         movements (session/get-in [:movement-session :parts position-in-parts :movements])
         difficulty (case new-difficulty "easier" :movement/easier "harder" :movement/harder nil)]
     (when-let [entity (:db/id (first (shuffle (difficulty m))))]
       (GET "movement-by-id"
            {:params        {:entity entity}
             :handler       #(let [id (:id m)
                                   new-movement %
                                   new-movement (assoc new-movement :id id)
                                   new-movements (assoc movements id new-movement)]
                              (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
             :error-handler #(print (str "error: " %))})))))

(defn add-movement-from-search [part-title movement-name]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])]
    (GET "movement"
         {:params        {:name (str movement-name)}
          :handler       #(let [id (swap! m-counter inc)
                                new-movement %
                                new-movement (assoc new-movement :id id)
                                new-movements (assoc movements id new-movement)]
                           (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
          :error-handler #(print "error getting single movement through add.")})))

(defn remove-movement [m part-title]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])
        movements (dissoc movements (:id m))]
    (session/assoc-in! [:movement-session :parts position-in-parts :movements] movements)))

(defn list-to-sorted-map [list-of-movements]
  (let [movements (atom (sorted-map))]
    (doseq [m list-of-movements
            :let [id (swap! m-counter inc)]]
      (swap! movements assoc id (assoc m :id id)))
    @movements))

(defn add-session-handler [session]
  (let [new-parts (mapv #(assoc % :movements (list-to-sorted-map (:movements %)))
                        (:parts session))]
    (session/put! :movement-session (assoc session :parts new-parts :comment ""))))

(defn create-session-from-template [template-name]
  (GET "template"
       {:params        {:template-name template-name
                        :user          (session/get :user)}
        :handler       add-session-handler
        :error-handler (fn [] (print "error getting session data from server."))}))

(defn create-session-from-equipment [equipment-name]
  (GET "equipment-session"
       {:params        {:equipment equipment-name
                        :user      (session/get :user)}
        :handler       add-session-handler
        :error-handler (fn [e] (print (str "error getting session data from server: " e)))}))

(defn pick-random-template []
  (let [name (first (shuffle (session/get :templates)))]
    (create-session-from-template name)))

;;;;;; Components ;;;;;;
(defn buttons-component [m title]
  [:div.pure-g
   [:div.pure-u.refresh
    [:i.fa.fa-random {:on-click #(refresh-movement m title) :title "Swap with another movement"}]]
   (when (:movement/easier m)
     [:div.pure-u.refresh
      [:i.fa.fa-minus {:on-click #(refresh-movement m title "easier") :title "Swap with easier movement"}]])
   (when (:movement/harder m)
     [:div.pure-u.refresh
      [:i.fa.fa-plus {:on-click #(refresh-movement m title "harder") :title "Swap with harder movement"}]])
   [:div.pure-u.destroy
    [:i.fa.fa-remove {:on-click #(remove-movement m title) :title "Remove movement"}]]])

(defn slider-component []
  (let [data (atom 0)]
    (fn [position-in-parts id r min max step]
      [:div.pure-g
       [:div.pure-u-1-5 @data]
       [:a.pure-u-1-5 {:on-click #(session/assoc-in!
                                   [:movement-session :parts position-in-parts :movements id r]
                                   (int @data))} "save"]
       [:input {:className "pure-u"
                :type      "range" :value @data :min min :max max :step step
                :style     {:width "100%"}
                :on-change #(reset! data (-> % .-target .-value))}]])))

(defn movement-component [{:keys [id category distance rep set duration] :as m}
                          title categories]
  (let [unique-name (:movement/unique-name m)
        name (if (nil? unique-name) (:movement/name m) unique-name)
        graphic (image-url name)
        parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{title} (map :title parts)))
        rep-clicked? (atom false)
        set-clicked? (atom false)
        distance-clicked? (atom false)
        duration-clicked? (atom false)]
    (fn []
      [:div.pure-u.movement.is-center {:id (str "m-" id)}
       (buttons-component m title)
       [:h3.pure-g
        [:div.pure-u-1-12]
        [:div.pure-u.title name]]
       [:img.graphic.pure-img-responsive {:src graphic :title name :alt name}]
       [:div {:style {:cursor 'pointer}}
        [:div.pure-g
         [:div.pure-u-1-12]
         [:div.pure-u-5-12
          [:div.pure-u {:on-click  #(handler-fn (reset! rep-clicked? (not @rep-clicked?)))
                        :className (str (when-not (and rep (< 0 rep)) " no-data")
                                        (when @rep-clicked? " selected"))} "Reps"]]
         [:div.pure-u-5-12
          [:div.pure-u {:on-click  #(handler-fn (reset! set-clicked? (not @set-clicked?)))
                        :className (str (when-not (and set (< 0 set)) " no-data")
                                        (when @set-clicked? " selected"))} "Set"]]
         [:div.pure-u-1-12]]
        [:div.pure-g
         [:div.pure-u-1-12]
         [:div.pure-u-5-12
          (if (and rep (< 0 rep))
            [:div.rep-set {:on-click #(handler-fn (reset! rep-clicked? (not @rep-clicked?)))} rep])]
         [:div.pure-u-5-12
          (if (and set (< 0 set))
            [:div.rep-set {:on-click #(handler-fn (reset! set-clicked? (not @set-clicked?)))} set])]
         [:div.pure-u-1-12]]]
       [:div {:style {:cursor 'pointer :margin-bottom 10}}
        [:div.pure-g
         [:div.pure-u-1-12]
         [:div.pure-u-5-12
          [:div.pure-u {:on-click  #(handler-fn (reset! distance-clicked? (not @distance-clicked?)))
                        :className (str (when-not (and distance (< 0 distance)) " no-data"))} "Meters"]]
         [:div.pure-u-5-12
          [:div.pure-u {:on-click  #(handler-fn (reset! duration-clicked? (not @duration-clicked?)))
                        :className (str (when-not (and duration (< 0 duration)) " no-data"))} "Seconds"]]
         [:div.pure-u-1-12]]
        [:div.pure-g
         [:div.pure-u-1-12]
         [:div.pure-u-5-12
          (if (and distance (< 0 distance))
            [:div.rep-set {:on-click #(handler-fn (reset! distance-clicked? (not @distance-clicked?)))} distance])]
         [:div.pure-u-5-12
          (if (and duration (< 0 duration))
            [:div.rep-set {:on-click #(handler-fn (reset! duration-clicked? (not @duration-clicked?)))} duration])]
         [:div.pure-u-1-12]]]
       (when @rep-clicked?
         [slider-component position-in-parts id :rep 0 50 1])
       (when @set-clicked?
         [slider-component position-in-parts id :set 0 10 1])
       (when @distance-clicked?
         [slider-component position-in-parts id :distance 0 400 5])
       (when @duration-clicked?
         [slider-component position-in-parts id :duration 0 1800 10])])))

(defn add-movement-component []
  (let [show-search-input? (atom false)]
    (fn [title i]
      [:div.pure-u.movement
       [:div.pure-g.add-movement
        [:div.pure-u-2-5]
        [:div.pure-u-1-5
         [:i.fa.fa-plus.fa-2x
          {:on-click #(add-movement title)
           :style    {:cursor  'pointer}}]]]
       (if @show-search-input?
         [:div.pure-g.add-movement
          [:div.pure-u
           (let [id (str "mtags" i)
                 movements-ac-comp (with-meta text-input-component
                                              {:component-did-mount #(auto-complete-did-mount
                                                                      (str "#" id)
                                                                      (vec (session/get :all-movements)))})]
             [movements-ac-comp {:id          id
                                 :class       "edit"
                                 :placeholder "type to find and add movement.."
                                 :size        32
                                 :auto-focus  true
                                 :on-save     #(when (some #{%} (session/get :all-movements))
                                                (do
                                                  (reset! show-search-input? false)
                                                  (add-movement-from-search title %)))}])]]
         [:div.pure-g.add-movement
          [:div.pure-u-2-5]
          [:div.pure-u-1-5
           [:i.fa.fa-search-plus.fa-2x
            {:on-click #(handler-fn (reset! show-search-input? true))
             :style    {:cursor 'pointer}}]]])])))

(defn part-component []
  (let []
    (fn [{:keys [title movements categories]} i]
      [:div.part
       [:h2 title]
       [:div.pure-g.movements
        (for [m (vals movements)]
          ^{:key (str m (rand-int 100000))} [movement-component m title categories])
        (when-not (empty? categories)
          [add-movement-component title i])]])))

(defn header-component []
  (let [months {0 "January" 1 "February" 2 "March" 3 "April" 4 "May" 5 "June"
                6 "July" 7 "August" 8 "September" 9 "October" 10 "November" 11 "December"}
        date (js/Date.)
        day (.getDate date)
        month (get months (.getMonth date))]
    (fn [{:keys [title description]}]
      [:div {:style {:margin-top 20}}
       [:div.pure-g
        [:div.pure-u.pure-u-md-2-5]
        [:div.pure-u-1.pure-u-md-1-5.is-center (str month " " day)]
        [:div.pure-u.pure-u-md-2-5]]
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-5]
        [:h1.pure-u-1.pure-u-md-3-5 title]
        [:p.pure-u.pure-u-md-1-5]]
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-9]
        [:p.pure-u-1.pure-u-md-7-9.subtitle description]
        [:div.pure-u.pure-u-md-1-9]]])))

(defn template-component [name]
  [:div.pure-u.button {:on-click #(create-session-from-template name)} name])

(defn equipment-component [name]
  [:a.pure-u.button {:on-click #(create-session-from-equipment name)} name])

(defn blank-state-component []
  (let [templates-showing? (atom false)
        equipment-showing? (atom false)]
    (fn []
      [:div.blank-state
       [:div.pure-g {:style {:margin-bottom 50}}
        [:h1.pure-u "Let's create your next Movement Session"]]
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-8]
        [:div.pure-u.pure-u-md-1-4.button.button-primary {:on-click pick-random-template} "Start moving"]
        [:div.pure-u.pure-u-md-1-8]
        [:div.pure-u.pure-u-md-1-4.button {:className (when @templates-showing? "button-primary")
                                           :on-click  #(handler-fn
                                                        (do
                                                          (when (nil? (session/get :equipment))
                                                            (get-equipment))
                                                          (when equipment-showing?
                                                            (reset! equipment-showing? false))
                                                          (reset! templates-showing? (not @templates-showing?))))}
         "Choose template"]]
       #_[:div.pure-g
          [:h3.pure-u [:a.button {:className (when @equipment-showing? "button-primary")
                                  :on-click  #(handler-fn
                                               (do
                                                 (when (nil? (session/get :equipment))
                                                   (get-equipment))
                                                 (when templates-showing?
                                                   (reset! templates-showing? false))
                                                 (reset! equipment-showing? (not @equipment-showing?))))}
                       "Or choose your available equipment and do some movements with that."]]]
       [:div.pure-g {:style {:margin-top "10px"}}
        (when @templates-showing?
          (doall
            (for [t (session/get :templates)]
              ^{:key (str t (rand-int 1000))} (template-component t))))]
       #_[:div.pure-g
          (when @equipment-showing?
            (doall
              (for [e (session/get :equipment)]
                ^{:key (str e (rand-int 1000))} (equipment-component e))))]])))

(defn top-menu-component []
  (let [templates-showing? (atom false)
        equipment-showing? (atom false)]
    (fn []
      [:div
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-4]
        [:div.pure-u-1-2.pure-u-md-1-4.button.button-primary {:on-click pick-random-template} "Random session"]
        [:div.pure-u-1-2.pure-u-md-1-4.button {:className (when @templates-showing? "button-primary")
                                               :on-click  #(handler-fn
                                                            (do
                                                              (when (nil? (session/get :equipment))
                                                                (get-equipment))
                                                              (when equipment-showing?
                                                                (reset! equipment-showing? false))
                                                              (reset! templates-showing? (not @templates-showing?))))}
         "Template session"]
        [:div.pure-u.pure-u-md-1-5]
        #_[:h3.pure-u.button {:className (when @equipment-showing? "button-primary")
                              :on-click  #(handler-fn
                                           (do
                                             (when (nil? (session/get :equipment))
                                               (get-equipment))
                                             (when templates-showing?
                                               (reset! templates-showing? false))
                                             (reset! equipment-showing? (not @equipment-showing?))))}
           "Session from equipment"]]
       [:div.pure-g
        (when @templates-showing?
          (doall
            (for [t (session/get :templates)]
              ^{:key t} [template-component t])))]
       #_[:div.pure-g
          (when @equipment-showing?
            (doall
              (for [e (session/get :equipment)]
                ^{:key e} (equipment-component e))))]])))

(defn time-component []
  (let [adding-time (atom false)]
    (fn []
      [:div
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-5]
        [:div.button.pure-u-1-1.pure-u-md-3-5
         {:on-click #(handler-fn (reset! adding-time true))} [:i.fa.fa-clock-o.fa-2x] "Log time"]
        [:div.pure-u.pure-u-md-1-5]]
       (when @adding-time
         [:div
          [:div.pure-g
           [:div.pure-u.pure-u-md-1-3]
           [:label.pure-u-1-2.pure-u-md-1-6 "minutes"]
           [:label.pure-u-1-2.pure-u-md-1-6 "seconds"]
           [:div.pure-u.pure-u-md-1-3]]
          [:div.pure-g
           [:div.pure-u.pure-u-md-1-3]
           [:input.pure-u-1-2.pure-u-md-1-6 {:type      "number"
                                             :value     (session/get-in [:movement-session :time :minutes])
                                             :min       0
                                             :on-change #(try
                                                          (let [value (-> % .-target .-value)]
                                                            (session/assoc-in! [:movement-session :time :minutes] value))
                                                          (catch js/Error e
                                                            (print (str "Caught exception: " e))))}]
           [:input.pure-u-1-2.pure-u-md-1-6 {:type      "number"
                                             :value     (session/get-in [:movement-session :time :seconds])
                                             :min       0
                                             :on-change #(try
                                                          (let [value (-> % .-target .-value)]
                                                            (session/assoc-in! [:movement-session :time :seconds] value))
                                                          (catch js/Error e
                                                            (print (str "Caught exception: " e))))}]
           [:div.pure-u.pure-u-md-1-3]]])])))

(defn comment-component []
  (let [adding-comment (atom false)]
    (fn []
      [:div
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-5]
        [:div.button.pure-u-1-1.pure-u-md-3-5
         {:on-click #(handler-fn (reset! adding-comment true))} [:i.fa.fa-comment-o.fa-2x] "Add comments"]
        [:div.pure-u.pure-u-md-1-5]]
       (when @adding-comment
         [:div.pure-g
          [:div.pure-u.pure-u-md-1-5]
          [:div.pure-u.pure-u-md-3-5
           [:textarea {:rows      4
                       :cols      80
                       :on-change #(session/assoc-in! [:movement-session :comment] (-> % .-target .-value))
                       :value     (session/get-in [:movement-session :comment])}]]
          [:div.pure-u.pure-u-md-1-5]])])))

(defn finish-session-component []
  (let [finish-button-clicked? (atom false)
        session-stored-successfully? (atom false)]
    (fn []
      (if @session-stored-successfully?
        (let []
          (go (<! (timeout 3000))
              (reset! finish-button-clicked? false)
              (reset! session-stored-successfully? false))
          [:div.pure-g
           [:div.pure-u {:style {:color "green" :font-size 24}} "Session stored successfully!"]])
        (if @finish-button-clicked?
          [:div.pure-g
           [:div.pure-u.pure-u-md-1-5]
           [:div.pure-u-1-1.pure-u-md-3-5.button.button-secondary
            {:on-click #(let [min (session/get-in [:movement-session :time :minutes])
                              min (if (nil? min) 0 (int (reader/read-string min)))
                              sec (session/get-in [:movement-session :time :seconds])
                              sec (if (nil? sec) 0 (int (reader/read-string sec)))]
                         (session/assoc-in! [:movement-session :time] (+ (* 60 min) sec))
                         (POST "store-session"
                               {:params        {:session (session/get :movement-session)
                                                :user    (session/get :user)}
                                :handler       (fn [response] (do
                                                                (reset! session-stored-successfully? true)
                                                                (get-stored-sessions)))
                                :error-handler (fn [response] (print response))}))}
            "Confirm Finish Session"]
           [:div.pure-u.pure-u-md-1-5]]
          [:div.pure-g
           [:div.pure-u.pure-u-md-1-5]
           [:div.pure-u-1-1.pure-u-md-3-5.button.button-primary
            {:on-click #(handler-fn (reset! finish-button-clicked? true))}
            "Finish Movement Session"]
           [:div.pure-u.pure-u-md-1-5]])))))

(defn generator-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content {:style {:margin-top "20px"}}
        (if-let [session (session/get :movement-session)]
          [:div
           [top-menu-component]
           [header-component session]
           (let [parts (:parts session)]
             (doall
               (for [i (range (count parts))]
                 ^{:key i} [part-component (get parts i) i])))
           [time-component]
           [comment-component]
           [finish-session-component]]
          [blank-state-component])]])))