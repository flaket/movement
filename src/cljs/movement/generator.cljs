(ns movement.generator
  (:import [goog.events EventType])
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [reagent.session :as session]
    [reagent.core :refer [atom]]
    [cljs.core.async :as async :refer [timeout <!]]
    [cljs.reader :as reader]
    [clojure.string :as str]
    [movement.util :refer [handler-fn positions GET POST get-plans get-ongoing-plan
                           get-stored-sessions get-groups]]
    [movement.text :refer [text-edit-component text-input-component auto-complete-did-mount]]
    [movement.menu :refer [menu-component]]
    [movement.components.login :refer [footer]]))

(defonce m-counter (atom 0))

(defn image-url [name]
  (when-not (nil? name)
    (str "images/movements/" (str/replace (str/lower-case name) " " "-") ".png")))

(defn add-movement [part-title]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        part (session/get-in [:movement-session :parts position-in-parts])
        movements (:movements part)
        part (apply dissoc part (for [[k v] part :when (nil? v)] k))
        part (dissoc part :title :movements)]
    (GET "singlemovement"
         {:params        {:part  part
                          :email (session/get :email)}
          :handler       #(let [id (swap! m-counter inc)
                                new-movement (assoc % :id id)
                                new-movements (assoc movements id new-movement)]
                           (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
          :error-handler #(pr "error getting single movement through add.")})))

(defn refresh-movement
  ([m part-title]
   (let [parts (session/get-in [:movement-session :parts])
         position-in-parts (first (positions #{part-title} (map :title parts)))
         part (session/get-in [:movement-session :parts position-in-parts])
         movements (:movements part)
         part (apply dissoc part (for [[k v] part :when (nil? v)] k))
         part (dissoc part :title :movements)]
     (GET "singlemovement"
          {:params        {:part  part
                           :email (session/get :email)}
           :handler       #(let [id (:id m)
                                 new-movement (assoc % :id id)
                                 new-movements (assoc movements id new-movement)]
                            (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
           :error-handler #(pr "error getting single movement through refresh.")})))
  ([m part-title new-difficulty]
   (let [parts (session/get-in [:movement-session :parts])
         position-in-parts (first (positions #{part-title} (map :title parts)))
         movements (session/get-in [:movement-session :parts position-in-parts :movements])
         part (session/get-in [:movement-session :parts position-in-parts])
         part (apply dissoc part (for [[k v] part :when (nil? v)] k))
         part (dissoc part :title :movements)]
     (when-let [id (:db/id (first (shuffle (new-difficulty m))))]
       (GET "movement-by-id"
            {:params        {:email (session/get :email)
                             :id    id
                             :part  part}
             :handler       #(let [id (:id m)
                                   new-movement %
                                   new-movement (assoc new-movement :id id)
                                   new-movements (assoc movements id new-movement)]
                              (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
             :error-handler #(pr (str "error: " %))})))))

(defn add-movement-from-search [part-title movement-name]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])
        part (session/get-in [:movement-session :parts position-in-parts])
        part (apply dissoc part (for [[k v] part :when (nil? v)] k))
        part (dissoc part :title :movements)]
    (GET "movement"
         {:params        {:email (session/get :email)
                          :name  (str movement-name)
                          :part  part}
          :handler       #(let [id (swap! m-counter inc)
                                new-movement %
                                new-movement (assoc new-movement :id id)
                                new-movements (assoc movements id new-movement)]
                           (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
          :error-handler #(pr "error getting single movement through add.")})))

(defn remove-movement [m part-title]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])
        movements (dissoc movements (:id m))]
    (session/assoc-in! [:movement-session :parts position-in-parts :movements] movements)))

(defn remove-part [i]
  (let [parts (session/get-in [:movement-session :parts])
        part (get parts i)
        new-parts (vec (remove #{part} parts))]
    (session/assoc-in! [:movement-session :parts] new-parts)))

(defn move-part [i direction]
  (when-not (and (= i 0) (= direction :up))
    (let [parts (session/get-in [:movement-session :parts])
          part (get parts i)
          new-parts (vec (remove #{part} parts))
          [before after] (split-at (cond (= direction :up) (dec i)
                                         (= direction :down) (inc i)) new-parts)
          new-parts (vec (concat before [part] after))]
      (session/assoc-in! [:movement-session :parts] new-parts))))

(defn move-movement [i id direction]
  (let [movements (session/get-in [:movement-session :parts i :movements])
        m (get movements id)
        new-id (cond
                 (= direction :up) (dec id)
                 (= direction :down) (inc id))
        m1 (get movements new-id)
        new-movements (assoc movements id m1 new-id m)]
    (session/assoc-in! [:movement-session :parts i :movements] new-movements)))

(defn list-to-sorted-map [list-of-movements]
  (let [movements (atom (sorted-map))]
    (doseq [m list-of-movements
            :let [id (swap! m-counter inc)]]
      (swap! movements assoc id (assoc m :id id)))
    @movements))

(defn add-session-handler [session]
  (let [new-parts (mapv #(assoc % :movements (list-to-sorted-map (:movements %)))
                        (:parts session))]
    (session/put! :movement-session (assoc session :parts new-parts))))

(defn create-session-from-template [id]
  (GET "template"
       {:params        {:template-id id
                        :email       (session/get :email)}
        :handler       add-session-handler
        :error-handler (fn [r] (pr r))}))

(defn create-session-from-group [group]
  (GET "group"
       {:params        {:group group
                        :email (session/get :email)}
        :handler       add-session-handler
        :error-handler (fn [] (pr "error getting group session data from server."))}))

(defn create-session-from-equipment [equipment-name]
  (GET "equipment-session"
       {:params        {:equipment equipment-name
                        :user      (session/get :user)}
        :handler       add-session-handler
        :error-handler (fn [e] (pr (str "error getting session data from server: " e)))}))

(defn pick-random-template []
  (create-session-from-template (:db/id (first (shuffle (session/get :templates))))))

;;;;;; Components ;;;;;;
(defn slider-component []
  (let [data (atom 0)]
    (fn [position-in-parts id r min max step]
      [:div.pure-g
       [:div.pure-u-1-5 @data]
       [:input.pure-u-4-5
        {:type        "range" :value @data :min min :max max :step step
         :style       {:width "100%"}
         :on-mouse-up #(session/assoc-in!
                        [:movement-session :parts position-in-parts :movements id r] (int @data))
         :on-change   #(reset! data (-> % .-target .-value))}]])))

(defn rep-component [rep id position-in-parts]
  (let [rep-clicked? (atom false)
        has-data? (and rep (< 0 rep))]
    (fn []
      [:div
       [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"
                                       :opacity (if has-data? 1 0.2)}
                               :on-click #(handler-fn (reset! rep-clicked? (not @rep-clicked?)))} "Repetisjoner"]
       (when @rep-clicked?
         [slider-component position-in-parts id :rep 0 50 1])])))

(defn distance-component [distance id position-in-parts]
  (let [distance-clicked? (atom false)
        has-data? (and distance (< 0 distance))]
    (fn []
      [:div
       [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"
                                       :opacity (if has-data? 1 0.2)}
                               :on-click #(handler-fn (reset! distance-clicked? (not @distance-clicked?)))} "Avstand"]
       (when @distance-clicked?
         [slider-component position-in-parts id :distance 0 400 5])])))

(defn duration-component [duration id position-in-parts]
  (let [duration-clicked? (atom false)
        has-data? (and duration (< 0 duration))]
    (fn []
      [:div
       [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"
                                       :opacity (if has-data? 1 0.2)}
                               :on-click #(handler-fn (reset! duration-clicked? (not @duration-clicked?)))} "Tid"]
       (when @duration-clicked?
         [slider-component position-in-parts id :duration 0 1800 10])])))


(defn weight-component [weight id position-in-parts]
  (let [weight-clicked? (atom false)
        has-data? (and weight (< 0 weight))]
    (fn []
      [:div
       [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"
                              :opacity (if has-data? 1 0.2)}
                      :on-click #(handler-fn (reset! weight-clicked? (not @weight-clicked?)))} "Vekt"]
       (when @weight-clicked?
         [slider-component position-in-parts id :weight 0 200 2.5])])))
(defn rest-component [rest id position-in-parts]
  (let [rest-clicked? (atom false)
        has-data? (and rest (< 0 rest))]
    (fn []
      [:div
       [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"
                                       :opacity (if has-data? 1 0.2)}
                               :on-click #(handler-fn (reset! rest-clicked? (not @rest-clicked?)))} "Hvile"]
       (when @rest-clicked?
         [slider-component position-in-parts id :rest 0 240 10])])))

(defn movement-component
  [{:keys [id unique name category measurement easier harder description zone
           rep set distance duration weight rest practical] :as m}
   title categories i]
  (let [name (if (nil? unique) name unique)
        graphic (image-url name)
        parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{title} (map :title parts)))
        expand (atom false)]
    (fn []
      [:div.pure-g.movement {:id (str "m-" id)}
       [:div.pure-u-1
        [:div.pure-g {:style {:cursor 'pointer}}
         [:div.pure-u-1-5 {:on-click #(reset! expand (not @expand))}
          [:img.pure-img-responsive.graphic {:src graphic :title name :alt name}]]
         [:div.pure-u-2-5 {:on-click #(reset! expand (not @expand))
                           :style {:display 'flex :text-align 'center}}
          [:h3.title {:style    {:margin 'auto}} name]]
         [:div.pure-u-1-5 {:on-click #(reset! expand (not @expand))
                           :style {:cursor 'pointer :display 'flex}}
          [:div.pure-g {:style {:margin 'auto}}
           (when (and rep (< 0 rep))
             [:div.pure-u
              [:div.pure-g
               [:div.pure-u {:style {:color "#9999cc" :font-size "200%" :text-align 'right :padding-right 10}} rep]
               [:div.pure-u {:style {:padding-top 10}} "reps"]]])]
          [:div.pure-g {:style {:margin 'auto}}
           (when (and distance (< 0 distance))
             [:div.pure-u
              [:div.pure-g
               [:div.pure-u {:style {:color "#9999cc" :font-size "200%" :text-align 'right :padding-right 10}} distance]
               [:div.pure-u {:style {:padding-top 10}} "m"]]])]
          [:div.pure-g {:style {:margin 'auto}}
           (when (and duration (< 0 duration))
             [:div.pure-u
              [:div.pure-g
               [:div.pure-u {:style {:color "#9999cc" :font-size "200%" :text-align 'right :padding-right 10}} duration]
               [:div.pure-u {:style {:padding-top 10}} "s"]]])]
          [:div.pure-g {:style {:margin 'auto}}
           (when (and weight (< 0 weight))
             [:div.pure-u
              [:div.pure-g
               [:div.pure-u {:style {:color "#9999cc" :font-size "200%" :text-align 'right :padding-right 10}} weight]
               [:div.pure-u {:style {:padding-top 10}} "kg"]]])]
          [:div.pure-g {:style {:margin 'auto}}
           (when (and rest (< 0 rest))
             [:div.pure-u
              [:div.pure-g
               [:div.pure-u {:style {:color "#9999cc" :font-size "200%" :text-align 'right :padding-right 10}} rest]
               [:div.pure-u {:style {:padding-top 10}} "s"]]])]]
         [:div.pure-u-1-5 {:style {:cursor 'pointer :border "2px solid lightgray"}}
          [:div.pure-g
           [:div.pure-u-1 {:style {:text-align 'center :margin-top 10 :opacity 0.25}} "set"]]
          [:div.pure-g {:style {:display 'flex}}
           [:div.pure-u {:style {:margin 'auto :opacity 0.15 :font-size "300%"}} set]]]]

        (when @expand
          [:div
           [:div.pure-g
            [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"}
                                    :on-click #(remove-movement m title) :title "Fjern øvelse"}
             [:i.fa.fa-remove {:style {:color "#CC9999" :opacity 0.8}}]
             "Fjern øvelse"]
            [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"}
                                    :on-click #(refresh-movement m title) :title "Bytt øvelse"}
             [:i.fa.fa-random {:style {:color "#99cc99" :opacity 0.8}}]
             "Bytt ut øvelse"]
            (when easier
              [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"}
                                      :on-click #(refresh-movement m title :easier) :title "Bytt med enklere"}
               [:i.fa.fa-arrow-down {:style {:color "#99cc99" :opacity 0.8}}]
               "Bytt med enklere"])
            (when harder
              [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"}
                                      :on-click #(refresh-movement m title :harder) :title "Bytt med vanskeligere"}
               [:i.fa.fa-arrow-up {:style {:color "#99cc99" :opacity 0.8}}]
               "Bytt med vanskeligere"])]

           [:div.pure-g
            [rep-component rep id position-in-parts]
            [distance-component distance id position-in-parts]
            [duration-component duration id position-in-parts]
            [weight-component weight id position-in-parts]
            [rest-component rest id position-in-parts]]]

          )]

       #_[:div.pure-u
          [:div.pure-g
           [:div.pure-u-2-3
            (when @expand
              [:div.pure-g {:style {:cursor 'pointer}}
               [:div.pure-u-1-12]
               [:h3.pure-u-1-5.no-data [:i.fa.fa-arrow-down {:on-click #(move-movement i id :down) :title "Move movement down"}]]
               [:h3.pure-u-1-5.no-data [:i.fa.fa-arrow-up {:on-click #(move-movement i id :up) :title "Move movement up"}]]])
            [:div.pure-g {:style {:cursor 'pointer}}
             [:h3.pure-u-1.title {:on-click #(reset! expand (not @expand))} name]]
            [:div.pure-g
             [:div.pure-u-1 {:style {:margin-left 15 :margin-top 20 :color 'gray :opacity 0.8}}
              (cond
                (= :zone/one zone) [:div {:title "You're still in the learning phase with this movement"}
                                    [:i.fa.fa-star] [:i.fa.fa-star-o] [:i.fa.fa-star-o]]
                (= :zone/two zone) [:div {:title "You know this movement well, but it is not perfected. You're effective, but not efficient."}
                                    [:i.fa.fa-star] [:i.fa.fa-star] [:i.fa.fa-star-o]]
                (= :zone/three zone) [:div {:title "You have mastered this movement. You are both effective and efficient."}
                                      [:i.fa.fa-star] [:i.fa.fa-star] [:i.fa.fa-star]])]]
            [:img.graphic.pure-img-responsive {:src graphic :title name :alt name}]]

           [:div.pure-u-1-3 {:style {:cursor 'pointer}}
            [:div.pure-g
             [:div.pure-u-1-2.refresh [:i.fa.fa-random {:on-click #(refresh-movement m title) :title "Swap with another movement"}]]
             [:div.pure-u-1-2.destroy [:i.fa.fa-remove {:on-click #(remove-movement m title) :title "Remove movement"}]]]
            [:div.pure-g
             (if easier
               [:div.pure-u-1-2.refresh [:i.fa.fa-arrow-down {:on-click #(refresh-movement m title :easier) :title "Swap with easier progression"}]]
               [:div.pure-u-1-2.refresh])
             (if harder
               [:div.pure-u-1-2.refresh [:i.fa.fa-arrow-up {:on-click #(refresh-movement m title :harder) :title "Swap with harder progression"}]]
               [:div.pure-u-1-2.refresh])]
            [:div.pure-g
             (when (and rep (< 0 rep))
               [:div.pure-u.rep-set {:on-click #(handler-fn (reset! rep-clicked? (not @rep-clicked?)))} rep])
             [:div.pure-u {:on-click  #(handler-fn (reset! rep-clicked? (not @rep-clicked?)))
                           :className (str (if-not (and rep (< 0 rep)) " no-data" " data")
                                           (when @rep-clicked? " selected"))} "reps"]]
            [:div.pure-g
             (when (and set (< 0 set))
               [:div.pure-u.rep-set {:on-click #(handler-fn (reset! set-clicked? (not @set-clicked?)))} set])
             [:div.pure-u {:on-click  #(handler-fn (reset! set-clicked? (not @set-clicked?)))
                           :className (str (if-not (and set (< 0 set)) " no-data" " data")
                                           (when @set-clicked? " selected"))} "set"]]
            [:div.pure-g
             (when (and distance (< 0 distance))
               [:div.pure-u.rep-set {:on-click #(handler-fn (reset! distance-clicked? (not @distance-clicked?)))} distance])
             [:div.pure-u {:on-click  #(handler-fn (reset! distance-clicked? (not @distance-clicked?)))
                           :className (str (if-not (and distance (< 0 distance)) " no-data" " data"))}
              (if (and distance (< 0 distance)) "m" "distance")]]
            [:div.pure-g
             (when (and duration (< 0 duration))
               [:div.pure-u.rep-set {:on-click #(handler-fn (reset! duration-clicked? (not @duration-clicked?)))} duration])
             [:div.pure-u {:on-click  #(handler-fn (reset! duration-clicked? (not @duration-clicked?)))
                           :className (str (if-not (and duration (< 0 duration)) " no-data" " data"))}
              (if (and duration (< 0 duration)) "s" "time")]]
            [:div.pure-g
             (when (and weight (< 0 weight))
               [:div.pure-u.rep-set {:on-click #(handler-fn (reset! weight-clicked? (not @weight-clicked?)))} weight])
             [:div.pure-u {:on-click  #(handler-fn (reset! weight-clicked? (not @weight-clicked?)))
                           :className (str (if-not (and weight (< 0 weight)) " no-data" " data"))}
              (if (and weight (< 0 weight)) "kg" "weight")]]
            [:div.pure-g
             (when (and rest (< 0 rest))
               [:div.pure-u.rep-set {:on-click #(handler-fn (reset! rest-clicked? (not @rest-clicked?)))} rest])
             [:div.pure-u {:on-click  #(handler-fn (reset! rest-clicked? (not @rest-clicked?)))
                           :className (str (if-not (and rest (< 0 rest)) " no-data" " data")
                                           (when @rest-clicked? " selected"))}
              (if (and rest (< 0 rest)) "s" "rest time")]]]]

          #_[:div
             (when @rep-clicked?
               [slider-component position-in-parts id :rep 0 50 1])
             (when @set-clicked?
               [slider-component position-in-parts id :set 0 20 1])
             (when @rest-clicked?
               [slider-component position-in-parts id :rest 0 240 10])
             (when @distance-clicked?
               [slider-component position-in-parts id :distance 0 400 5])
             (when @duration-clicked?
               [slider-component position-in-parts id :duration 0 1800 10])
             (when @weight-clicked?
               [slider-component position-in-parts id :weight 0 200 2.5])]]])))

(defn add-movement-component []
  (let [show-search-input? (atom false)]
    (fn [title i]
      [:div.pure-g.movement.search
       [:div.pure-u-1
        [:div.pure-g.add-movement
         [:div.pure-u-2-5]
         [:div.pure-u
          [:i.fa.fa-plus.fa-3x
           {:on-click #(add-movement title)
            :style    {:margin-right '50 :cursor 'pointer}}]]
         (if @show-search-input?
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
                                                  (add-movement-from-search title %)))}])
           [:i.fa.fa-search-plus.fa-3x
            {:on-click #(handler-fn (reset! show-search-input? true))
             :style    {:cursor 'pointer}}])]]])))

(defn part-component []
  (let []
    (fn [{:keys [title movements categories] :as part} i]
      [:div.pure-g.movements
       [:div.pure-u-1
        (for [m (vals movements)]
          ^{:key (str m (rand-int 100000))} [movement-component m title categories i])
        (when-not (empty? categories)
          [add-movement-component title i])]])))

(defn template-component [t]
  [:div.pure-u.button.button-primary {:on-click #(create-session-from-template (:db/id t))
                                      :style    {:margin "0 0 5px 5px"}} (:template/title t)])

(defn group-component [group]
  [:a.pure-u.button.button-primary {:on-click #(create-session-from-group (:group/title group))
                                    :style    {:margin "0 0 5px 5px"}} (:group/title group)])

(defn plan-component [plan]
  [:div.pure-g {:style {:margin-bottom 10 :padding-bottom 10 :border-bottom "dotted 1px"}}
   [:div.pure-u-1-2
    [:div.pure-g
     [:p.pure-u-1 (:plan/title plan)]]
    [:div.pure-g
     [:span.pure-u-1 (str (count (:plan/day plan)) " day plan")]]]
   [:p.pure-u.button.button-primary
    {:on-click #(POST "begin-plan"
                      {:params        {:email (session/get :email)
                                       :id    (:db/id plan)}
                       :handler       (fn [r] (session/put! :ongoing-plan plan))
                       :error-handler (fn [r] (pr r))})} "Begin plan"]])

(defn blank-state-component []
  (let [templates-showing? (atom false)
        groups-showing? (atom false)
        plans-showing? (atom false)]
    (fn []
      [:div.blank-state
       [:div.pure-g.center {:style {:margin-bottom 50}}
        [:h1.pure-u-1 "Create your next Movement Session"]]
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-8]
        [:div.pure-u-1.pure-u-md-3-4
         (when-let [plan (session/get :ongoing-plan)]
           [:div.pure-g
            [:div.pure-u-1.button
             {:style    {:margin-bottom 5}
              :on-click #(GET "next-session-from-plan"
                              {:params        {:email (session/get :email)}
                               :handler       add-session-handler
                               :error-handler (fn [r] (pr "error getting session data from server."))})}
             (str "Continue " (:plan/title plan))]])
         [:div.pure-g
          [:div.pure-u-1.button.button-primary {:style    {:margin-bottom 5}
                                                :on-click pick-random-template} "From random template"]]
         [:div.pure-g
          [:div.pure-u-1.button {:style    {:margin-bottom 5}
                                 :on-click #(handler-fn
                                             (do
                                               (when groups-showing?
                                                 (reset! groups-showing? false))
                                               (when plans-showing?
                                                 (reset! plans-showing? false))
                                               (reset! templates-showing? (not @templates-showing?))))} "From template"]]
         (when @templates-showing?
           [:div.pure-g.animated.fadeIn {:style {:margin "20px 0 20px 0"}}
            (doall
              (for [t (session/get :templates)]
                ^{:key (:db/id t)} (template-component t)))])
         [:div.pure-g
          [:div.pure-u-1.button {:style    {:margin-bottom 5}
                                 :on-click #(handler-fn
                                             (do
                                               (when templates-showing?
                                                 (reset! templates-showing? false))
                                               (when plans-showing?
                                                 (reset! plans-showing? false))
                                               (reset! groups-showing? (not @groups-showing?))))} "From group"]]
         (when @groups-showing?
           [:div.pure-g.animated.fadeIn {:style {:margin "20px 0 20px 0"}}
            (doall
              (for [e (session/get :groups)]
                ^{:key (:db/id e)} (group-component e)))])
         (when (and (< 0 (count (session/get :plans)))
                    (nil? (session/get :ongoing-plan)))
           [:div.pure-g
            [:div.pure-u-1.button {:style    {:margin-bottom 5}
                                   :on-click #(handler-fn
                                               (do
                                                 (when groups-showing?
                                                   (reset! groups-showing? false))
                                                 (when templates-showing?
                                                   (reset! templates-showing? false))
                                                 (reset! plans-showing? (not @plans-showing?))))} "Begin a new plan"]])
         (when (and @plans-showing? (nil? (session/get :ongoing-plan)))
           [:div.pure-g.animated.fadeIn {:style {:margin "20px 0 20px 0"}}
            [:div.pure-u-1
             (doall
               (for [e (session/get :plans)]
                 ^{:key (:db/id e)} (plan-component e)))]])]
        [:div.pure-u.pure-u-md-1-8]]])))

(defn top-menu-component []
  (let [templates-showing? (atom false)
        groups-showing? (atom false)]
    (fn []
      [:div
       [:div.pure-g
        [:a.pure-u.pure-u-md-1-5 {:style    {:text-decoration 'underline
                                             :text-align      'right
                                             :margin-right    5}
                                  :on-click #(session/remove! :movement-session)}
         "Clear session"]
        [:div.pure-u.pure-u-md-1-5.button.button-primary {:style    {:margin-right 5}
                                                          :on-click pick-random-template}
         "Random"]
        [:div.pure-u.pure-u-md-1-5.button {:style    {:margin-right 5}
                                           :on-click #(handler-fn
                                                       (do
                                                         (when groups-showing?
                                                           (reset! groups-showing? false))
                                                         (reset! templates-showing? (not @templates-showing?))))}
         "Select"]
        [:div.pure-u.pure-u-md-1-5.button {:style    {:margin-right 5}
                                           :on-click #(handler-fn
                                                       (do
                                                         (when templates-showing?
                                                           (reset! templates-showing? false))
                                                         (reset! groups-showing? (not @groups-showing?))))}
         "Group"]
        ]
       (when @templates-showing?
         [:div.pure-g.animated.fadeIn {:style {:margin-top '20}}
          (doall
            (for [t (session/get :templates)]
              ^{:key (:db/id t)}
              [:div.pure-u.button.button-primary {:on-click #(do
                                                              (go (<! (timeout 500))
                                                                  (reset! templates-showing? false))
                                                              (create-session-from-template (:db/id t)))
                                                  :style    {:margin "0 0 5px 5px"}} (:template/title t)]))])
       (when @groups-showing?
         [:div.pure-g.animated.fadeIn {:style {:margin-top '20}}
          (doall
            (for [g (session/get :groups)]
              ^{:key (:db/id g)}
              [:div.pure-u.button.button-primary {:on-click #(do
                                                              (go (<! (timeout 500))
                                                                  (reset! groups-showing? false))
                                                              (create-session-from-group (:group/title g)))
                                                  :style    {:margin "0 0 5px 5px"}} (:group/title g)]))])])))

(defn date-component []
  (let [months {0 "januar" 1 "februar" 2 "mars" 3 "april" 4 "mai" 5 "juni"
                6 "juli" 7 "august" 8 "september" 9 "oktober" 10 "november" 11 "desember"}
        date (js/Date.)
        day (.getDate date)
        month (get months (.getMonth date))
        ]
    (fn [{:keys [title description]}]
      [:div.pure-u-1
       [:div.pure-g
        [:div.pure-u-1-3 (str day ". " month)]
        [:div.pure-u-1-3
         [:a.pure-button "Sett en annen dato"]]]])))

(defn time-component []
  [:div.pure-u-1
   [:div.pure-g
    [:label.pure-u-1-2 "min"]
    [:label.pure-u-1-2 "sek"]]
   [:div.pure-g
    [:input.pure-u-1-2 {:type      "number"
                        :value     (session/get-in [:movement-session :time :minutes])
                        :min       0
                        :on-change #(try
                                     (let [value (-> % .-target .-value)]
                                       (session/assoc-in! [:movement-session :time :minutes] value))
                                     (catch js/Error e
                                       (pr (str "Caught exception: " e))))}]
    [:input.pure-u-1-2 {:type      "number"
                        :value     (session/get-in [:movement-session :time :seconds])
                        :min       0
                        :on-change #(try
                                     (let [value (-> % .-target .-value)]
                                       (session/assoc-in! [:movement-session :time :seconds] value))
                                     (catch js/Error e
                                       (pr (str "Caught exception: " e))))}]]])
(defn comment-component []
  [:div.pure-g {:style {:margin-top '25}}
   [:div.pure-u-1
    [:textarea {
                :rows      10
                :cols      120
                :style {:resize 'vertical}
                :on-change #(session/assoc-in! [:movement-session :comment] (-> % .-target .-value))
                :value     (session/get-in [:movement-session :comment])}]]])

(defn image-upload-component []
  [:div.pure-g {:style {:margin-top '10}}
   [:div.pure-u-1
    [:a.pure-button "Last opp bilde"]]])

(defn finish-session-component []
  (let [session-stored-successfully? (atom false)]
    (fn []
      [:div {:style {:margin-top '50}}
       (if @session-stored-successfully?
         (let []
           (go (<! (timeout 3000))
               (session/remove! :movement-session)
               (reset! session-stored-successfully? false))
           [:div.pure-g
            [:div.pure-u-1.center {:style {:color "green" :font-size 30}} "Økta er lagret!"]])
         [:div.pure-g
          [:a.pure-u-1.pure-button.pure-button-primary.button-xlarge
           {:on-click #(let [min (session/get-in [:movement-session :time :minutes])
                             min (when-not (nil? min) (int (reader/read-string min)))
                             sec (session/get-in [:movement-session :time :seconds])
                             sec (when-not (nil? sec) (int (reader/read-string sec)))]
                        (session/assoc-in! [:movement-session :time] (+ (* 60 min) sec))
                        (POST "store-session"
                              {:params        {:session (session/get :movement-session)
                                               :user    (session/get :user)}
                               :handler       (fn [response] (do
                                                               (reset! session-stored-successfully? true)
                                                               (get-stored-sessions)))
                               :error-handler (fn [response] (pr response))}))}
           "Avslutt og lagre økta"]])])))

(defn plan-completed []
  [:div {:style {:margin-top 50}}
   [:div.pure-g
    [:h1.pure-u-1 "Plan completed!"]]
   [:div.pure-g
    [:p.pure-u-1.subtitle "Congratulations on completing your plan!
              All the data from your training is stored in the database.
              We're hard at work to create a statistics page where you soon can explore the details of your training."]]])

(defn generator-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       (if-let [session (session/get :movement-session)]
         [:div
          [:div.pure-g
           [:a.pure-u-1 {:style    {:text-decoration 'underline :text-align 'right
                                    :margin-right    5 :margin-top 20}
                         :on-click #(session/remove! :movement-session)} "clear session"]]
          [:div.content {:style {:margin-top "20px"}}
           (if (:plan-completed? session)
             (do
               (get-ongoing-plan)
               (plan-completed))
             [:div
              [:article.session
               (let [parts (:parts session)]
                 (doall
                   (for [i (range (count parts))]
                     ^{:key i} [part-component (get parts i) i])))]
              [:div.pure-g {:style {:margin-top '50}}
               [:div.pure-u-1-2 [date-component]]
               [:div.pure-u-1-2 (time-component)]]
              (comment-component)
              (image-upload-component)
              [finish-session-component]])]]
         [:div.content
          [blank-state-component]])])))