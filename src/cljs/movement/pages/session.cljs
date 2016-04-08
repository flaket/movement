(ns movement.pages.session
  (:import [goog.events EventType]
           [goog.date Date DateTime])
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

(def test-template {:title "Test" :description "test" :background "test"
                    :part  [[{:category   #{:natural :balance}
                              :repetition [4 8 12] :distance [5 12 20] :duration 30 :set 4}
                             {:category   #{:natural :climb}
                              :repetition [2 4 6] :set 4}]]})

(defonce m-counter (atom 0))

(defn image-url [movement-name]
  (when-not (nil? movement-name)
    (str "images/movements/" (str/replace (str/lower-case movement-name) " " "-") ".png")))


(defn swap-movement [event movement part-number]
  (.preventDefault event)
  (let [category (name (first (shuffle (:category movement))))]
    (GET "movement-from-category" {:params        {:category category}
                                   :handler       (fn [new-movement]
                                                    (let [part (session/get-in [:movement-session :parts part-number])
                                                          pos (first (positions #{movement} part))
                                                          new-part (assoc part pos (first new-movement))]
                                                      (session/assoc-in! [:movement-session :parts part-number] new-part)))
                                   :error-handler (fn [r] nil)})))

(defn replace-movement [event {:keys [kw movement part-number]}]
  (.preventDefault event)
  (cond
    (= :swap kw) (let [slot-category (:slot-category movement)
                       category (name (first (shuffle (:category movement))))]
                   (GET "movement-from-category" {:params        {:category category}
                                                  :handler       (fn [new-movement]
                                                                   (let [new-movement (assoc (first new-movement) :slot-category slot-category)
                                                                         part (session/get-in [:movement-session :parts part-number])
                                                                         pos (first (positions #{movement} part))
                                                                         new-part (assoc part pos new-movement)]
                                                                     (session/assoc-in! [:movement-session :parts part-number] new-part)))
                                                  :error-handler (fn [r] nil)}))
    (or (= :next kw)
        (= :previous kw)) (let [slot-category (:slot-category movement)
                                    new-movement (first (shuffle (kw movement)))]
                            (GET "movement" {:params        {:name new-movement}
                                             :handler       (fn [new-movement]
                                                              (let [new-movement (assoc new-movement :slot-category slot-category)
                                                                    part (session/get-in [:movement-session :parts part-number])
                                                                    pos (first (positions #{movement} part))
                                                                    new-part (assoc part pos new-movement)]
                                                                (session/assoc-in! [:movement-session :parts part-number] new-part)))
                                             :error-handler (fn [r] nil)}))
    :else nil)
  )

(defn add-movement [])

(defn add-movement-from-search [])

(defn inc-set-completed [event m part-number]
  (.preventDefault event)
  (let [part (session/get-in [:movement-session :parts part-number])
        pos (positions #{m} part)
        new-part (assoc part (first pos) (update m :performed-sets inc))]
    (session/assoc-in! [:movement-session :parts part-number] new-part)))

(defn dec-set-completed [event m part-number]
  (.preventDefault event)
  (let [part (session/get-in [:movement-session :parts part-number])
        pos (positions #{m} part)
        new-part (assoc part (first pos)
                             (if (pos? (dec (:performed-sets m)))
                               (update m :performed-sets dec)
                               (dissoc m :performed-sets)))]
    (session/assoc-in! [:movement-session :parts part-number] new-part)))

(defn vec-remove
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(defn remove-movement [event m part-number]
  (.preventDefault event)
  (let [part (session/get-in [:movement-session :parts part-number])
        pos (first (positions #{m} part))
        new-part (vec-remove part pos)]
    (if (empty? new-part)
      (let [parts (session/get-in [:movement-session :parts])
            new-parts (vec-remove parts part-number)]
        (session/assoc-in! [:movement-session :parts] new-parts))
      (session/assoc-in! [:movement-session :parts part-number] new-part))))

(defn remove-session [event]
  (.preventDefault event)
  (session/remove! :movement-session))

(defn create-session-from-activity [event session-type]
  (.preventDefault event)
  (GET "create-session"
       {:params        {:type  session-type
                        :email (:email (session/get :user))}
        :handler       (fn [session]
                         (session/put! :movement-session
                                       (assoc session :activity session-type)))
        :error-handler (fn [r] (pr r))}))

(defn preview-file []
  (let [file (.getElementById js/document "upload")
        reader (js/FileReader.)]
    (when-let [file (aget (.-files file) 0)]
      (set! (.-onloadend reader) #(session/update-in! [:movement-session] assoc :photo (-> % .-target .-result str)))
      (.readAsDataURL reader file))))

(defn add-photo-component []
  (if-let [photo (session/get-in [:movement-session :photo])]
    [:div.pure-g
     [:div.pure-u [:img {:style {:height 200
                                 :border " 1px solid #000"
                                 :margin "10px 5px 0 0"}
                         :src   photo}]]
     [:div.pure-u {:on-click #(session/update-in! [:movement-session] dissoc :photo)
                   :style    {:color "red" :cursor 'pointer}} [:i.fa.fa-times.fa-2x]]]
    [:div.pure-g
     [:div.pure-u-1-3.pure-button.fileUpload
      [:span "Legg ved bilde"]
      [:input {:id "upload" :className "upload" :type "file" :on-change #(preview-file)}]]]))

;;;;;; Components ;;;;;;

(defn r-component [{:keys [data name]}]
  [:div.pure-g {:style {:margin 'auto}}
   [:div.pure-u
    [:div.pure-g
     [:div.pure-u {:style {:color "#9999cc" :font-size "200%" :text-align 'right :padding-right 10}} data]
     [:div.pure-u {:style {:padding-top 10}} name]]]])

(defn movement-component
  [{:keys [name image slot-category measurement previous next
           rep set performed-sets distance duration weight rest] :as m}
   part-number]
  (let [parts  (session/get-in [:movement-session :parts])
        pos (positions #{m} (get parts part-number))
        expand (atom false)]
    (fn []
      [:div.pure-g.movement #_{:id (str "m-" id)}
       [:div.pure-u-1
        [:div.pure-g {:style {:cursor 'pointer}}
         [:div.pure-u-1-5 {:onClick #(reset! expand (not @expand)) :onTouchEnd #(reset! expand (not @expand))}
          [:img.graphic {:src (str "images/movements/" image) :title name :alt name}]]
         [:div.pure-u-2-5 {:onClick #(reset! expand (not @expand)) :onTouchEnd #(reset! expand (not @expand))
                           :style   {:display 'flex :text-align 'center}}
          [:h3.title {:style {:margin 'auto}} name]]
         [:div.pure-u-1-5 {:onClick #(reset! expand (not @expand)) :onTouchEnd #(reset! expand (not @expand))
                           :style   {:display 'flex}}
          (when (pos? rep) (r-component {:data rep :name "reps"}))
          (when (pos? distance) (r-component {:data distance :name "m"}))
          (when (pos? duration) (r-component {:data duration :name "s"}))
          (when (pos? weight) (r-component {:data weight :name "kg"}))
          (when (pos? rest) (r-component {:data rest :name "s"}))]

         [:div.pure-u-1-5.set-area {:onClick    #(inc-set-completed % m part-number)
                                    :onTouchEnd #(inc-set-completed % m part-number)}
          [:div.pure-g
           [:div.pure-u-1
            [:i.fa.fa-minus {:onClick    #(dec-set-completed % m part-number)
                             :onTouchEnd #(dec-set-completed % m part-number)
                             :style      {:opacity    (when-not performed-sets 0)
                                          :color      (when performed-sets 'red)
                                          :margin-top 5 :margin-right 5
                                          :float      'right}}]]]
          (if set
            [:div.pure-g {:style {:display 'flex}}
             [:div.pure-u {:style {:margin 'auto :margin-top 10 :opacity 0.05 :font-size "300%"}} set]]
            [:div.pure-g {:style {:display 'flex}}
             [:div.pure-u {:style {:margin 'auto :margin-top 10 :opacity 0 :font-size "300%"}} 1]])
          (when performed-sets
            [:div.pure-g
             [:div.pure-u-1 [:h1.center {:style {:color 'red :margin-top -70 :font-size "350%"}} performed-sets]
              ]])
          [:div.pure-g
           [:div.pure-u-1 [:div.center {:style {:margin-top (if performed-sets -24 -6)
                                                :opacity    0.15}} "set"]]]]]

        (when @expand
          [:div
           [:div.pure-g
            [:a.pure-u.pure-button {:style   {:margin "5px 5px 5px 5px"}
                                    :onClick #(remove-movement % m part-number) :onTouchEnd #(remove-movement % m part-number)
                                    :title   "Fjern øvelse"}
             [:i.fa.fa-remove {:style {:color "#CC9999" :opacity 0.8}}]
             "Fjern øvelse"]
            [:a.pure-u.pure-button {:style      {:margin "5px 5px 5px 5px"} :title "Bytt øvelse"
                                    :onClick    #(replace-movement % {:kw :swap :movement m :part-number part-number})
                                    :onTouchEnd #(replace-movement % {:kw :swap :movement m :part-number part-number})} [:i.fa.fa-random {:style {:color "#99cc99" :opacity 0.8}}] "Bytt ut øvelse"]
            (when previous
              [:a.pure-u.pure-button {:style      {:margin "5px 5px 5px 5px"}
                                      :onClick    #(replace-movement % {:kw :previous :movement m :part-number part-number})
                                      :onTouchEnd #(replace-movement % {:kw :previous :movement m :part-number part-number}) :title "Bytt med enklere"}
               [:i.fa.fa-arrow-down {:style {:color "#99cc99" :opacity 0.8}}] "Bytt med enklere"])
            (when next
              [:a.pure-u.pure-button {:style      {:margin "5px 5px 5px 5px"}
                                      :onClick    #(replace-movement % {:kw :next :movement m :part-number part-number})
                                      :onTouchEnd #(replace-movement % {:kw :next :movement m :part-number part-number}) :title "Bytt med vanskeligere"}
               [:i.fa.fa-arrow-up {:style {:color "#99cc99" :opacity 0.8}}]
               "Bytt med vanskeligere"])]
           [:div.pure-g
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Repetisjoner"]
             [:input {:style {:width 75}
                      :type  "number" :value rep :min 0 :on-change (fn [v] (let [new-movement (assoc m :rep (int (-> v .-target .-value)))
                                                                                 new-part (assoc (get parts part-number) (int pos) new-movement)]
                                                                             (session/assoc-in! [:movement-session :parts part-number] new-part)))}]]
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Avstand"]
             [:input {:style {:width 75}
                      :type "number" :value distance :min 0 :on-change #(pr "changed!")}]]
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Tid"]
             [:input {:style {:width 75}
                      :type "number" :value duration :min 0 :on-change #(pr "changed!")}]]
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Vekt"]
             [:input {:style {:width 75}
                      :type "number" :value weight :min 0 :on-change #(pr "changed!")}]]
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Hvile"]
             [:input {:style {:width 75}
                      :type "number" :value rest :min 0 :on-change #(pr "changed!")}]]


            #_[rep-component rep m part-number]
              #_[distance-component distance m part-number]
              #_[duration-component duration m part-number]
              #_[weight-component weight m part-number]
              #_[rest-component rest m part-number]]])]])))

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
    (fn [movements i]
      [:div.pure-g.movements
       [:div.pure-u-1
        (for [m movements]
          ^{:key (str m (rand-int 100000))} [movement-component m i])
        [add-movement-component movements]]])))

(defn list-of-activities []
  (let [activites ["Naturlig bevegelse" "Styrke" "Løping" "Crossfit"
                   "Gym" "Yoga" "Gåtur" "Sport" "Sykling" "Ski"
                   "Svømming" "Annen aktivitet"]]
    (fn []
      [:div.movements
       [:div.pure-g
        [:div.pure-u-1 [:h1 "Logg en aktivitet"]]]
       (doall
         (for [a activites]
           ^{:key a}
           [:div.pure-g.activity {:onClick #(create-session-from-activity % a) :onTouchEnd #(create-session-from-activity % a)}
            [:div.pure-u [:img {:width 200 :height 'auto :src "images/movements/pull-up.png"}]]
            [:div.pure-u [:h2 {:style {:margin-top 85}} a]]]))])))

(defn time-component []
  (let [time-value (session/get-in [:movement-session :time])]
    [:input {:type      "time" :name "time" :step 1
             :on-change #(session/assoc-in! [:movement-session :time] (.-value (.-target %)))
             :value     (if time-value time-value "00:00:00")}]))

(defn date-string []
  (let [goog-date (DateTime.)
        year (.getFullYear goog-date)
        month (inc (.getMonth goog-date))
        month (if (> 10 month) (str 0 month) (str month))
        day (.getDate goog-date)
        day (if (> 10 day) (str 0 day) (str day))]
    (str year "-" month "-" day)))

(defn time-string []
  (let [goog-date (DateTime.)
        hours (.getHours goog-date)
        minutes (.getMinutes goog-date)
        minutes (if (> 10 minutes) (str 0 minutes) (str minutes))
        seconds (.getSeconds goog-date)
        seconds (if (> 10 seconds) (str 0 seconds) (str seconds))]
    (str hours ":" minutes ":" seconds)))

(defn date-component []
  (let [date-value (session/get-in [:movement-session :date])]
    (session/assoc-in! [:movement-session :date] date-value)
    [:input {:style     {:float 'right} :id "date" :name "date" :type "date"
             :value     (if date-value date-value (date-string))
             :on-change #(session/assoc-in! [:movement-session :date] (-> % .-target .-value))}]))

(defn text-component []
  [:div.pure-g {:style {:margin-top '25}}
   [:div.pure-u-1
    [:textarea {:rows      10 :cols 120
                :style     {:resize 'vertical} :placeholder "Hvordan gikk økta? #styrke #mandag"
                :on-change #(session/assoc-in! [:movement-session :comment] (-> % .-target .-value))
                :value     (session/get-in [:movement-session :comment])}]]])

(defn store-session [event s]
  (.preventDefault event)
  (let [session (session/get :movement-session)
        session (if-not (:comment session) (assoc session :comment "") session)
        new-parts (mapv (fn [part]
                          (mapv (fn [m]
                                  (dissoc m :category :slot-category :measurement :previous :next :image)) part))
                        (:parts session))
        date (if-let [date (:date session)] date (date-string))
        time (time-string)
        date-time (str date "T" time)
        hash-tags (vec (re-seq #"#[\w]+" (:comment session)))
        session (assoc session :parts new-parts :date-time date-time :tags hash-tags)]
    (POST "store-session"
            {:params        {:session session
                             :user-id (:user-id (session/get :user))}
             :handler       (fn [] (reset! s true))
             :error-handler (fn [r] (pr r))})))

(defn finish-session-component []
  ;; Etter trykk på avslutt&lagre bør den oppdaterte feeden vises
  (let [s (atom false)]
    (fn []
      [:div {:style {:margin-top '50}}
       (if @s
         (let []
           (go (<! (timeout 3000))
               (session/remove! :movement-session)
               (reset! s false))
           [:div.pure-g
            [:div.pure-u-1.center {:style {:color "green" :font-size 30}} "Økta er loggført!"]])
         [:div.pure-g
          [:a.pure-u-1.pure-button.pure-button-primary.button-xlarge
           {:onClick #(store-session % s) :onTouchEnd #(store-session % s)} "Logg økta"]])])))

(defn session-page []
  (let []
    (fn []
      [:div
       [menu-component]
       (if-let [session (session/get :movement-session)]
         (let [] #_[parts (mapv (fn [part] (mapv (fn [m] (swap! m-counter update inc) (assoc m :id @m-counter)) part))
                                (:parts session))
                    session (assoc session :parts parts)]
           [:div {:style {:margin-top "100px"}}
            [:div.pure-g
             [:a.pure-u {:style      {:margin-left 60 :margin-top 20}
                         :onClick    #(remove-session %)
                         :onTouchEnd #(remove-session %)}
              [:i.fa.fa-arrow-left.fa-4x]]]
            [:div.content {:style {:margin-top "20px"}}
             [:div
              [:article.session
               (let [parts (:parts session)]
                 (doall
                   (for [i (range (count parts))]
                     ^{:key i} [part-component (get parts i) i])))]
              [:div.pure-g
               [:div.pure-u-1 (date-component)]]
              [:h2.pure-g
               [:div.pure-u (str (:activity session) " i ")]
               [:div.pure-u (time-component)]]

              (text-component)

              [:div.pure-g {:style {:margin-top '10}}
               #_[:a.pure-u-1-3.pure-button "Legg ved bilde"]

               #_[:div.pure-u-1-3.center
                  [:a.pure-button "Sett geoposisjon"]]
               #_[:div.pure-u-1-3.center
                  [:a.pure-button "Del"]]]
              (add-photo-component)

              [finish-session-component]]]])
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