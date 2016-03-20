(ns movement.session
  (:import [goog.events EventType])
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [reagent.session :as session]
    [reagent.core :refer [atom]]
    [cljs.core.async :as async :refer [timeout <!]]
    [cljs.reader :as reader]
    [clojure.string :as str]
    [cljs-pikaday.reagent :as pikaday]
    [movement.util :refer [handler-fn positions GET POST
                           get-stored-sessions]]
    [movement.text :refer [text-edit-component text-input-component auto-complete-did-mount]]
    [movement.menu :refer [menu-component]]))

(def test-template {:title       "Test"
                    :creator     "Andreas"
                    :description "test"
                    :background  "test"
                    :part        [[{:category   #{:natural :balance}
                                    :repetition [4 8 12] :distance [5 12 20] :duration 30 :set 4}
                                   {:category   #{:natural :climb}
                                    :repetition [2 4 6] :set 4}]]})

(defonce m-counter (atom 0))

(defn image-url [movement-name]
  (when-not (nil? movement-name)
    (str "images/movements/" (str/replace (str/lower-case movement-name) " " "-") ".png")))

(defn add-movement [movements]
  (let [
        ;parts (session/get-in [:movement-session :parts])
        ;position-in-parts (first (positions #{part-title} (map :title parts)))
        ;part (session/get-in [:movement-session :parts position-in-parts])
        ;movements (:movements part)
        ;part (apply dissoc part (for [[k v] part :when (nil? v)] k))
        ;part (dissoc part :title :movements)
        ]
    #_(GET "singlemovement"
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

(defn add-movement-from-search [movements movement-name]
  (let [
        ;parts (session/get-in [:movement-session :parts])
        ;position-in-parts (first (positions #{part-title} (map :title parts)))
        ;movements (session/get-in [:movement-session :parts position-in-parts :movements])
        ;part (session/get-in [:movement-session :parts position-in-parts])
        ;part (apply dissoc part (for [[k v] part :when (nil? v)] k))
        ;part (dissoc part :title :movements)
        ]
    #_(GET "movement"
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

#_{:name     "Balancing Backward Walk"
   :slot-category #{:balance :natural}
   :measurement :distance
   :previous ["Balancing Lateral Walk"]
   :distance 10
   :set 4}

(defn movement-component
  [{:keys [name slot-category measurement previous next
           rep set distance duration weight rest] :as m}
   title categories i]
  (let [;parts (session/get-in [:movement-session :parts])
        ;position-in-parts (first (positions #{title} (map :title parts)))
        expand (atom false)]
    (fn []
      [:div.pure-g.movement #_{:id (str "m-" id)}
       [:div.pure-u-1
        [:div.pure-g {:style {:cursor 'pointer}}
         [:div.pure-u-1-5 {:on-click #(reset! expand (not @expand))}
          [:img.pure-img-responsive.graphic {:src (image-url name) :title name :alt name}]]
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
            (when previous
              [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"}
                                      :on-click #(refresh-movement m title :easier) :title "Bytt med enklere"}
               [:i.fa.fa-arrow-down {:style {:color "#99cc99" :opacity 0.8}}]
               "Bytt med enklere"])
            (when next
              [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"}
                                      :on-click #(refresh-movement m title :harder) :title "Bytt med vanskeligere"}
               [:i.fa.fa-arrow-up {:style {:color "#99cc99" :opacity 0.8}}]
               "Bytt med vanskeligere"])]
          #_ [:div.pure-g
            [rep-component rep id position-in-parts]
            [distance-component distance id position-in-parts]
            [duration-component duration id position-in-parts]
            [weight-component weight id position-in-parts]
            [rest-component rest id position-in-parts]]])]])))

(defn add-movement-component []
  (let [show-search-input? (atom false)]
    (fn [movements]
      [:div.pure-g.movement.search
       [:div.pure-u-1
        [:div.pure-g.add-movement
         [:div.pure-u-2-5]
         [:div.pure-u
          [:i.fa.fa-plus.fa-3x
           {:on-click #(add-movement movements)
            :style    {:margin-right '50 :cursor 'pointer}}]]
         [:i.fa.fa-search-plus.fa-3x
          {:on-click #(handler-fn (reset! show-search-input? true))
           :style    {:cursor 'pointer}}]
         #_(if @show-search-input?
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
                                                  (add-movement-from-search movements %)))}])
           [:i.fa.fa-search-plus.fa-3x
            {:on-click #(handler-fn (reset! show-search-input? true))
             :style    {:cursor 'pointer}}])]]])))

#_[
   [
    {:category #{:natural :balance}
   :movement "Balancing Walk"
   :distance 10
   :set      4}

    {:category #{:natural :climb}
   :movement "Toes To Bar"
   :rep      5
   :set      4}]]

(defn part-component []
  (let []
    (fn [movements]
      [:div.pure-g.movements
       [:div.pure-u-1
        (for [m movements]
          ^{:key (str m (rand-int 100000))} [movement-component m])
        [add-movement-component movements]]])))

(defn blank-state-component []
  (let []
    (fn []
      [:div.blank-state
       [:div.pure-g.center {:style {:margin-bottom 50}}
        [:h1.pure-u-1 "Create your next Movement Session"]]
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-8]
        [:div.pure-u-1.pure-u-md-3-4
         [:div.pure-g
          [:div.pure-u-1.button.button-primary
           {:style    {:margin-bottom 5}
            :on-click #(GET "create-session"
                            {:params        {:type  "abc"
                                             :email (session/get :email)}
                             :handler       (fn [session] (session/put! :movement-session session))
                             :error-handler (fn [r] (pr r))}) #_pick-random-template} "From random template"]]]
        [:div.pure-u.pure-u-md-1-8]]])))

(defonce the-date (atom (js/Date.)))

(defn date-component []
  (let [months {0 "januar" 1 "februar" 2 "mars" 3 "april" 4 "mai" 5 "juni"
                6 "juli" 7 "august" 8 "september" 9 "oktober" 10 "november" 11 "desember"}
        date (js/Date.)
        day (.getDate date)
        month (get months (.getMonth date))
        set-new-date? (atom false)]
    (fn [{:keys [title description]}]
      [:div.pure-u-1
       [:div.pure-g
        [:div.pure-u-1-3 (str day ". " month)]
        [:div.pure-u-1-3
         [:a.pure-button {:on-click #(reset! set-new-date? (not @set-new-date?))}
          "Sett en annen dato"]
         (when @set-new-date?
           [pikaday/date-selector {:date-atom the-date}])]]])))

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
            [:div.pure-u-1.center {:style {:color "green" :font-size 30}} "Økta ble lagret!"]])
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

(defn session-page []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       (if-let [session (session/get :movement-session)]
         [:div
          [:div.pure-g
           [:a.pure-u-1 {:style    {:text-decoration 'underline :text-align 'right
                                    :margin-right    5 :margin-top 20}
                         :on-click #(session/remove! :movement-session)} "få en annen økt"]]
          [:div.content {:style {:margin-top "20px"}}
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
            [finish-session-component]]]]
         [:div.content
          [blank-state-component]])])))


#_(defn zone-data [val local-zone name]
    (cond
      (= :zone/one val) [:div.pure-u-1.center.dim
                         [:i.fa.fa-star.gold {:title "You're still in the learning phase with this movement"}]
                         [:i.fa.fa-star-o.star {:on-click #(POST "set-zone" {:params        {:email (session/get :email)
                                                                                             :name  name
                                                                                             :zone  :zone/two}
                                                                             :handler       (fn [r] (reset! local-zone :zone/two))
                                                                             :error-handler (fn [r] (pr "error setting zone data: " r))})
                                                :style    {:cursor 'pointer}
                                                :title    "Give two stars to indicate that you now know this movement well."}]
                         [:i.fa.fa-star-o.star {:on-click #(POST "set-zone" {:params        {:email (session/get :email)
                                                                                             :name  name
                                                                                             :zone  :zone/three}
                                                                             :handler       (fn [r] (reset! local-zone :zone/three))
                                                                             :error-handler (fn [r] (pr "error setting zone data: " r))})
                                                :style    {:cursor 'pointer}
                                                :title    "Give three stars to indicate that you have mastered this movement."}]]
      (= :zone/two val) [:div.pure-u-1.center.dim
                         [:i.fa.fa-star.gold.star {:on-click #(POST "set-zone" {:params        {:email (session/get :email)
                                                                                                :name  name
                                                                                                :zone  :zone/one}
                                                                                :handler       (fn [r] (reset! local-zone :zone/one))
                                                                                :error-handler (fn [r] (pr "error setting zone data: " r))})
                                                   :style    {:cursor 'pointer}
                                                   :title    "Go back to one star if you no longer can do this movement well."}]
                         [:i.fa.fa-star.gold {:title "You know this movement well, but it is not perfected. You're effective, but not efficient."}]
                         [:i.fa.fa-star-o.star {:on-click #(POST "set-zone" {:params        {:email (session/get :email)
                                                                                             :name  name
                                                                                             :zone  :zone/three}
                                                                             :handler       (fn [r] (reset! local-zone :zone/three))
                                                                             :error-handler (fn [r] (pr "error setting zone data: " r))})
                                                :style    {:cursor 'pointer}
                                                :title    "Give three stars to indicate that you have mastered this movement."}]]
      (= :zone/three val) [:div.pure-u-1.center.dim
                           [:i.fa.fa-star.gold.star {:on-click #(POST "set-zone" {:params        {:email (session/get :email)
                                                                                                  :name  name
                                                                                                  :zone  :zone/one}
                                                                                  :handler       (fn [r] (reset! local-zone :zone/one))
                                                                                  :error-handler (fn [r] (pr "error setting zone data: " r))})
                                                     :style    {:cursor 'pointer}
                                                     :title    "Go back to one star if you no longer can do this movement well."}]
                           [:i.fa.fa-star.gold.star {:on-click #(POST "set-zone" {:params        {:email (session/get :email)
                                                                                                  :name  name
                                                                                                  :zone  :zone/two}
                                                                                  :handler       (fn [r] (reset! local-zone :zone/two))
                                                                                  :error-handler (fn [r] (pr "error setting zone data: " r))})
                                                     :style    {:cursor 'pointer}
                                                     :title    "Go back to two stars if you no longer master this movement."}]
                           [:i.fa.fa-star.gold {:title "You have mastered this movement. You are both effective and efficient."}]]))

#_(defn explore-movement-component [name zone selected? category]
    (let []
      (fn [name zone selected?]
        [:div.pure-u.movement {:className (if selected? "explore-selected" "small")}
         [:h3.pure-g.center
          (if selected?
            [:div.pure-u-1 {:style {:cursor 'default}} name]
            [:div.pure-u-1 {:style    {:cursor 'pointer}
                            :on-click #(GET "explore-movement"
                                            {:params        {:unique-name name
                                                             :email       (session/get :email)}
                                             :handler       (fn [r] (do
                                                                      (pr r)
                                                                      (swap! explore-state dissoc :movements)
                                                                      (swap! explore-state assoc :selected-movement r)))
                                             :error-handler (fn [r] (pr "error exploring-movement: " r))})}
             name])]
         [:div.pure-g
          (let [val @zone]
            (zone-data val zone name))]
         [:div.center
          (if selected?
            [:img.graphic.pure-img-responsive {:src   (image-url name) :title name :alt name
                                               :style {:margin-bottom 10}}]
            [:img.graphic.pure-img-responsive {:className (if selected? "" "small-graphic")
                                               :src       (image-url name) :title name :alt name
                                               :style     {:margin-bottom 10
                                                           :cursor        'pointer}
                                               :on-click  #(GET "explore-movement"
                                                                {:params        {:unique-name name
                                                                                 :email       (session/get :email)}
                                                                 :handler       (fn [r] (do
                                                                                          (pr r)
                                                                                          (swap! explore-state dissoc :movements)
                                                                                          (swap! explore-state assoc :selected-movement r)))
                                                                 :error-handler (fn [r] (pr "error exploring-movement: " r))})}])]
         (when selected?
           (for [c category]
             [:div.pure-g
              [:div.pure-u-1.center.explore-link {:style    {:cursor 'pointer}
                                                  :on-click #(GET "movements-by-category"
                                                                  {:params        {:n        (:number-of-results @explore-state)
                                                                                   :category (:category/name c)}
                                                                   :handler       (fn [r] (do
                                                                                            (swap! explore-state assoc :selected-category (:category/name c))
                                                                                            (swap! explore-state dissoc :selected-movement)
                                                                                            (swap! explore-state assoc :movements r)))
                                                                   :error-handler (fn [r] (pr (str "error getting movements by category: " r)))})}
               (:category/name c)]]))])))

#_(defn explore-movements-component []
    (let []
      (fn []
        [:div {:style {:margin-top '20}}
         [:div.pure-g
          [:div.pure-u.pure-u-md-1-5
           [:div.pure-g
            [:span.pure-u {:style {:margin-bottom 10}} "See movements by category"]]
           (let [categories (sort (session/get :all-categories))]
             (doall
               (for [c categories]
                 ^{:key c}
                 [:div.pure-g {:style {:cursor           'pointer
                                       :color            (when (= c (:selected-category @explore-state)) "#fffff8")
                                       :background-color (when (= c (:selected-category @explore-state)) "gray")}}
                  [:span.pure-u-1.explore-link
                   {:style    {:color (when (and (= "Practical Movements" c)
                                                 (not (= c (:selected-category @explore-state))))
                                        "red")}
                    :on-click #(GET "movements-by-category"
                                    {:params        {:n        (:number-of-results @explore-state)
                                                     :category c}
                                     :handler       (fn [r] (do
                                                              (swap! explore-state assoc :selected-category c)
                                                              (swap! explore-state dissoc :selected-movement)
                                                              (swap! explore-state assoc :movements r)))
                                     :error-handler (fn [r] (pr (str "error getting movements by category: " r)))})} c]])))]
          [:div.pure-u.pure-u-md-4-5
           [:div.pure-g
            [:div.pure-u.pure-u-md-1-3
             [results-slider 1 30 1]]
            [:div.pure-u.pure-u-md-1-3
             (let [id (str "explore-mtags")
                   movements-ac-comp (with-meta text-input-component
                                                {:component-did-mount #(auto-complete-did-mount
                                                                        (str "#" id)
                                                                        (vec (session/get :all-movements)))})]
               [movements-ac-comp {:id          id
                                   :class       "edit"
                                   :placeholder "Search for movement"
                                   :size        32
                                   :on-save     #(when (some #{%} (session/get :all-movements))
                                                  (GET "movement"
                                                       {:params        {:name (str %)}
                                                        :handler       (fn [r] (do
                                                                                 (swap! explore-state dissoc :movements)
                                                                                 (swap! explore-state assoc :selected-movement r)))
                                                        :error-handler (pr "error getting single movement through add.")}))}])]
            [:div.pure-u.pure-u-md-1-3
             [:button.button.button-primary {:on-click #(GET "user-movements"
                                                             {:params        {:email (session/get :email)}
                                                              :handler       (fn [r] (do
                                                                                       (swap! explore-state dissoc :selected-movement)
                                                                                       (swap! explore-state assoc :movements r)))
                                                              :error-handler (fn [r] (pr (str "error getting user movements: " r)))})}
              "Movements I have done"]]]
           (when-not (nil? (:movements @explore-state))
             [:div.pure-g
              [:div.pure-u-1 (str "Showing " (count (:movements @explore-state)) " results")]])
           (let [movements (:movements @explore-state)]
             (if movements
               [:div.pure-g.movements
                (doall
                  (for [m movements]
                    ^{:key (:db/id m)}
                    [movement-component (if (nil? (:movement/name m)) (:movement/name m) (:movement/name m)) (atom (:db/ident (:movement/zone m))) false (:movement/category m)]))]
               (when-let [movement (:selected-movement @explore-state)]
                 (let [easier (:movement/easier movement)
                       harder (:movement/harder movement)]
                   [:div.movements
                    [:div.pure-g
                     [:div.pure-u-1-3
                      [:div.pure-g
                       [:div.pure-u-3-4
                        (for [m easier]
                          [:div.pure-g.center
                           [movement-component (if (nil? (:movement/name m)) (:movement/name m) (:movement/name m)) (atom (:db/ident (:movement/zone m))) false (:movement/category m)]])]
                       [:div.pure-u-1-4
                        (when-not (empty? easier)
                          [:div.explore-green [:i.fa.fa-arrow-right]])]]]

                     [:div.pure-u-1-3
                      [:div.pure-g
                       [movement-component (if (nil? (:movement/name movement)) (:movement/name movement) (:movement/name movement)) (atom (:db/ident (:movement/zone movement))) true (:movement/category movement)]]]

                     [:div.pure-u-1-3
                      [:div.pure-g
                       [:div.pure-u-1-4
                        (when-not (empty? harder)
                          [:div.explore-green [:i.fa.fa-arrow-right]])]
                       [:div.pure-u-3-4
                        (for [m harder]
                          [:div.pure-g
                           [:div.pure-u-1-5]
                           [movement-component (if (nil? (:movement/name m)) (:movement/name m) (:movement/name m)) (atom (:db/ident (:movement/zone m))) false (:movement/category m)]])]]]]]))))]]])))