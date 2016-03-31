(ns movement.pages.session
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
    [movement.util :refer [handler-fn positions GET POST get-stored-sessions]]
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

(defn remove-movement [])
(defn swap-movement [])
(defn previous-movement [])
(defn next-movement [])

(defn add-movement [])
(defn add-movement-from-search [])

(defn remove-session-handler [event]
  (.preventDefault event)
  (session/remove! :movement-session))

(defn create-session-from-activity [event session-type]
  (.preventDefault event)
  (GET "create-session"
       {:params        {:type  session-type
                        :email (session/get :email)}
        :handler       (fn [session]
                         (session/put! :movement-session
                                       (assoc session :activity session-type)))
        :error-handler (fn [r] (pr r))}))

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
         [:div.pure-u-1-5 {:style {:border "2px solid lightgray"}}
          [:div.pure-g {:style {:display 'flex}}
           [:div.pure-u {:style {:margin 'auto :margin-top 30 :opacity 0.15 :font-size "300%"}} set]]
          [:div.pure-g
           [:div.pure-u-1 [:div.center {:style {:margin-top 0 :opacity 0.25}} "set"]]]]]
        (when @expand
          [:div
           [:div.pure-g
            [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"}
                                    :on-click #(remove-movement) :title "Fjern øvelse"}
             [:i.fa.fa-remove {:style {:color "#CC9999" :opacity 0.8}}]
             "Fjern øvelse"]
            [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"}
                                    :on-click #(swap-movement) :title "Bytt øvelse"}
             [:i.fa.fa-random {:style {:color "#99cc99" :opacity 0.8}}]
             "Bytt ut øvelse"]
            (when previous
              [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"}
                                      :on-click #(previous-movement) :title "Bytt med enklere"}
               [:i.fa.fa-arrow-down {:style {:color "#99cc99" :opacity 0.8}}]
               "Bytt med enklere"])
            (when next
              [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"}
                                      :on-click #(next-movement) :title "Bytt med vanskeligere"}
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
           {:on-click #(add-movement)
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

(defn list-of-activities []
  (let [activites ["Styrke" "Naturlig bevegelse" "Løp" "Crossfit"
                   "Yoga" "Gåtur" "Svømme" "Sport" "Ski"]]
    (fn []
      [:div.movements
       [:div.pure-g
        [:div.pure-u-1 [:h1 "Logg en aktivitet"]]]
       (doall
         (for [a activites]
           ^{:key a}
           [:div.pure-g.activity {:onClick #(create-session-from-activity % a) :onTouchEnd #(create-session-from-activity % a)}
            [:div.pure-u [:img {:width 200 :height 'auto :src "images/movements/pull-up.png"}]]
            [:div.pure-u [:h2 {:style {:margin-top 85}} a]]]))






       #_[:div.pure-g
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

(defn time-component []
  [:div.pure-u.pure-u-md-1-3
   [:div.pure-g
    [:label.pure-u-1-3 "timer"]
    [:label.pure-u-1-3 "minutter"]
    [:label.pure-u-1-3 "sekunder"]]
   [:div.pure-g
    [:input.pure-u-1-3 {:type      "number"
                        :value     (session/get-in [:movement-session :time :hours])
                        :min       0
                        :on-change #(try
                                     (let [value (-> % .-target .-value)]
                                       (session/assoc-in! [:movement-session :time :hours] value))
                                     (catch js/Error e
                                       (pr (str "Caught exception: " e))))}]
    [:input.pure-u-1-3 {:type      "number"
                        :value     (session/get-in [:movement-session :time :minutes])
                        :min       0
                        :on-change #(try
                                     (let [value (-> % .-target .-value)]
                                       (session/assoc-in! [:movement-session :time :minutes] value))
                                     (catch js/Error e
                                       (pr (str "Caught exception: " e))))}]
    [:input.pure-u-1-3 {:type      "number"
                        :value     (session/get-in [:movement-session :time :seconds])
                        :min       0
                        :on-change #(try
                                     (let [value (-> % .-target .-value)]
                                       (session/assoc-in! [:movement-session :time :seconds] value))
                                     (catch js/Error e
                                       (pr (str "Caught exception: " e))))}]]])

(defn date-component []
  (let [months {0 "januar" 1 "februar" 2 "mars" 3 "april" 4 "mai" 5 "juni"
                6 "juli" 7 "august" 8 "september" 9 "oktober" 10 "november" 11 "desember"}
        date (js/Date.)
        day (.getDate date)
        month (get months (.getMonth date))]
    ;; Benytt pikaday her? https://github.com/dbushell/Pikaday
    ;; ..og Moment.js er aktuelt, spesielt for norsk støtte http://momentjs.com/
    [:div.pure-u.pure-u-md-1-3
     [:div.pure-button (str day ". " month)]]))


(defn text-component []
  [:div.pure-g {:style {:margin-top '25}}
   [:div.pure-u-1
    [:textarea {
                :rows      10
                :cols      120
                :style {:resize 'vertical}
                :placeholder "Hvordan gikk økta? #styrke #mandag"
                :on-change #(session/assoc-in! [:movement-session :comment] (-> % .-target .-value))
                :value     (session/get-in [:movement-session :comment])}]]])

(defn image-geo-component []
  [:div.pure-g {:style {:margin-top '10}}
   [:div.pure-u-1-3.center
    [:a.pure-button "Last opp bilde"]]
   [:div.pure-u-1-3.center
    [:a.pure-button "Sett geoposisjon"]]
   [:div.pure-u-1-3.center
    [:a.pure-button "Last opp bilde"]]])

(defn finish-session-component []
  ;; Etter trykk på avslutt&lagre bør den oppdaterte feeden vises
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
                               :handler       (fn [response]
                                                (reset! session-stored-successfully? true)
                                                (get-stored-sessions))
                               :error-handler (fn [response] (pr response))}))}
           "Avslutt og lagre økta"]])])))

(defn session-page []
  (let []
    (fn []
      [:div
       [menu-component]
       (if-let [session (session/get :movement-session)]
         [:div {:style {:margin-top "100px"}}
          [:div.pure-g
           [:a.pure-u {:style    {:margin-left 60 :margin-top 20}
                       :onClick #(remove-session-handler %)
                       :onTouchEnd #(remove-session-handler %)}
            [:i.fa.fa-arrow-left.fa-4x ]]]
          [:div.content {:style {:margin-top "20px"}}
           [:div
            [:article.session
             (let [parts (:parts session)]
               (doall
                 (for [i (range (count parts))]
                   ^{:key i} [part-component (get parts i) i])))]
            [:div.pure-g {:style {:margin-top '50}}
             [:h2.pure-u.pure-u-md-1-3 (str (:activity session) " i ")]
             (time-component)
             (date-component)]
            (text-component)
            (image-geo-component)
            [finish-session-component]]]]
         [:div.content
          [list-of-activities]])])))


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