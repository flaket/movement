(ns movement.session
  (:import [goog.events EventType]
           [goog.date Date DateTime])
  (:require
    [reagent.session :as session]
    [reagent.core :refer [atom]]
    [cljs.reader :as reader]
    [clojure.string :as str]
    [movement.data :as data]
    [movement.util :refer [vec-remove handler-fn positions]]
    [movement.text :refer [text-edit-component text-input-component auto-complete-did-mount]]))

(defn replace-movement [e {:keys [kw movement part-number]}]
    (.preventDefault e)
    (cond
      (= :swap kw) (let [ slot-category (:slot-category movement)
                          category (if slot-category ; movement has slot-category if it has been created from a template
                                      (first (shuffle slot-category))
                                      (first (shuffle (:category movement))))
                          old-movement (dissoc movement :next :previous) ; remove data that may not be overwritten by merging with the new movement
                          part (session/get-in [:movement-session :parts part-number])
                          pos (first (positions #{movement} part))
                          new-movement (-> (filter #(contains? (:category %) category) data/all-movements) shuffle first)
                          new-part (assoc part pos (merge old-movement new-movement))]
                      (session/assoc-in! [:movement-session :parts part-number] new-part))
      (or (= :next kw)
          (= :previous kw))
          (let [new-movement-name (first (shuffle (kw movement)))
                old-movement (dissoc movement :next :previous) ; remove data that may not be overwritten by merging with the new movement
                part (session/get-in [:movement-session :parts part-number])
                pos (first (positions #{movement} part))
                new-movement (get (data/get-movements-map) new-movement-name)
                new-part (assoc part pos (merge old-movement new-movement))]
            (session/assoc-in! [:movement-session :parts part-number] new-part))
      :else nil))

(defn add-movement [category part-number]
  (let [new-movement (first (shuffle (data/all-movements)))
        part (session/get-in [:movement-session :parts part-number])
        new-part (conj part new-movement)]
    (session/assoc-in! [:movement-session :parts part-number] new-part)))

(defn add-movement-from-search [name part-number]
  (let [new-movement (get (data/get-movements-map) name)
        part (session/get-in [:movement-session :parts part-number])
        new-part (conj part new-movement)]
    (session/assoc-in! [:movement-session :parts part-number] new-part)))

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

(defn remove-movement [event m part-number]
  (.preventDefault event)
  (let [part (session/get-in [:movement-session :parts part-number])
        pos (first (positions #{m} part))
        new-part (vec-remove pos part)]
    (if (empty? new-part)
      (let [parts (session/get-in [:movement-session :parts])
            new-parts (vec-remove part-number parts)]
        (if (empty? new-parts)
          (session/assoc-in! [:movement-session :parts] [[]])
          (session/assoc-in! [:movement-session :parts] new-parts)))
      (session/assoc-in! [:movement-session :parts part-number] new-part))))

(defn remove-session [event]
  (.preventDefault event)
  (session/remove! :movement-session))

(defn reset-session [e]
  (session/put! :movement-session {:title "ABC" :parts [[]] :activity "Styrketrening"}))

(defn generate-session [e]
  (.preventDefault e)
  (let [n (inc (rand-int 8))
        movements (vec (take n (shuffle data/all-movements)))
        new-session {:title "ABC" :parts [movements] :activity "Styrketrening"}]
    (session/put! :movement-session new-session)))

(defn create-session-from-activity [event activity]
  (.preventDefault event)
  (session/put! :movement-session {:parts [[]] :activity activity}))

(defn update-movement [{:keys [id m parts part-number pos]}]
  (let [rep-input (-> (.getElementById js/document (str "rep-input" id)) .-value int)
        distance-input (-> (.getElementById js/document (str "distance-input" id)) .-value int)
        duration-input (-> (.getElementById js/document (str "duration-input" id)) .-value int)
        weight-input (-> (.getElementById js/document (str "weight-input" id)) .-value int)
        rest-input (-> (.getElementById js/document (str "rest-input" id)) .-value int)
        new-movement (assoc m :rep rep-input :distance distance-input :duration duration-input
                              :weight weight-input :rest rest-input)
        new-part (assoc (get parts part-number) (int (first pos)) new-movement)]
    (session/assoc-in! [:movement-session :parts part-number] new-part)))

;;;;;; Components ;;;;;;

(defn r-component [{:keys [data name]}]
  [:div.pure-g
   [:div.pure-u {:style {:color "#9999cc" :font-size "100%" :text-align 'center}} data]
   [:span.pure-u {:style {:padding-top 10}} name]])

(defn movement-component
  [{:keys [id name image slot-category measurement previous next
           rep set performed-sets distance duration weight rest] :as m}
   part-number]
  (let [parts (session/get-in [:movement-session :parts])
        pos (positions #{m} (get parts part-number))
        expand (atom false)]
    (fn []
      [:div.pure-g.movement {:id id}
       [:div.pure-u-1
        [:div.pure-g {:style {:cursor 'pointer}}
         [:div.pure-u-1-5 {:onClick    (fn [e] (.preventDefault e) (reset! expand (not @expand)))}
          [:img.graphic {:src   (str "http://s3.amazonaws.com/mumrik-movement-images/" image)
                         :title name :alt name}]]
         [:div.pure-u-2-5 {:onClick    (fn [e] (.preventDefault e) (reset! expand (not @expand)))
                           :style      {:display 'flex :text-align 'center}}
          [:h3.title {:style {:margin 'auto}} name]]
         [:div.pure-u-1-5 {:onClick    (fn [e] (.preventDefault e) (reset! expand (not @expand)))}

          [:div.pure-g
           [:div.pure-u-1
            (if (pos? weight)
              (r-component {:data weight :name "kg"})
              [:div.pure-g {:style {:opacity 0.0}}
               [:div.pure-u {:style {:font-size "200%"}} 0]])]]
          [:div.pure-g
           [:div.pure-u-1
            (when (pos? rep) (r-component {:data rep :name "reps"}))
            (when (pos? distance) (r-component {:data distance :name "m"}))
            (when (pos? duration) (r-component {:data duration :name "s"}))]]
          [:div.pure-g
           [:div.pure-u-1
            (when (pos? rest) (r-component {:data rest :name "s pause"}))]]]

         [:div.pure-u-1-5.set-area {:onClick    #(inc-set-completed % m part-number)}
          [:div.pure-g
           [:div.pure-u-1
            [:i.fa.fa-minus
             {:onClick    #(dec-set-completed % m part-number)
              :style      {:opacity    (when-not performed-sets 0)
                           :color      (when performed-sets 'red)
                           :margin-top 5 :margin-right 5
                           :float      'right}}]]]
          (if set
            [:div.pure-g {:style {:display 'flex}}
             [:div.pure-u {:style {:margin 'auto :margin-top 10 :opacity 0.05 :font-size "100%"}} set]]
            [:div.pure-g {:style {:display 'flex}}
             [:div.pure-u {:style {:margin 'auto :margin-top 10 :opacity 0 :font-size "100%"}} 1]])
          (when performed-sets
            [:div.pure-g
             [:div.pure-u-1 [:h1.center {:style {:color 'red :margin-top -70 :font-size "150%"}} performed-sets]
              ]])
          (when (or performed-sets (> set 0))
            [:div.pure-g
             [:div.pure-u-1 [:div.center {:style {:margin-top (if performed-sets -24 -6)
                                                  :opacity    0.15}} "set"]]])]]

        (when @expand
          [:div
           [:div.pure-g
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Repetisjoner"]
             [:input {:style {:margin-left 3 :width 75}
                      :id    (str "rep-input" id) :type "number" :defaultValue rep :min 0}]]
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Avstand"]
             [:input {:style {:margin-left 3 :margin-right 3 :width 75}
                      :id    (str "distance-input" id) :type "number" :defaultValue distance :min 0}]
             [:span "m"]]
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Tid"]
             [:input {:style {:margin-left 3 :margin-right 3 :width 75}
                      :id    (str "duration-input" id) :type "number" :defaultValue duration :min 0}]
             [:span "sek"]]
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Vekt"]
             [:input {:style {:margin-left 3 :width 75}
                      :id    (str "weight-input" id) :type "number" :defaultValue weight :min 0 :step 0.5}]]
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Hvile"]
             [:input {:style {:margin-left 3 :margin-right 3 :width 75}
                      :id    (str "rest-input" id) :type "number" :defaultValue rest :min 0}]
             [:span "sek"]]
            [:a.pure-u-1-3.pure-button.pure-button-primary {
                :style {:margin "5px 5px 5px 5px"}
                :onClick #(update-movement {:id id :m m :parts parts :part-number part-number :pos pos})
              } "Oppdater"]]
           [:div.pure-g
            [:a.pure-u.pure-button {:style   {:margin "5px 5px 5px 5px"}
                                    :onClick #(remove-movement % m part-number)
                                    :title   "Fjern øvelse"}
             [:i.fa.fa-remove {:style {:color "#CC9999" :opacity 0.8}}]
             "Fjern øvelse"]
            [:a.pure-u.pure-button {
                :style      {:margin "5px 5px 5px 5px"} :title "Bytt øvelse"
                :onClick    #(replace-movement % {:kw :swap :movement m :part-number part-number})
              }
              [:i.fa.fa-random {:style {:color "#99cc99" :opacity 0.8}}] "Bytt ut øvelse"]
            (when previous
              [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"}
                                      :onClick #(replace-movement % {:kw :previous :movement m :part-number part-number})
                                      :title "Bytt med enklere"}
               [:i.fa.fa-arrow-down {:style {:color "#99cc99" :opacity 0.8}}] "Bytt med enklere"])
            (when next
              [:a.pure-u.pure-button {:style {:margin "5px 5px 5px 5px"}
                                      :onClick #(replace-movement % {:kw :next :movement m :part-number part-number})}
               [:i.fa.fa-arrow-up {:style {:color "#99cc99" :opacity 0.8}}]
               "Bytt med vanskeligere"])]])]])))

(defn all-movements [e show-search-input?]
  (.preventDefault e)
  (reset! show-search-input? (not @show-search-input?)))

(defn add-movement-component []
  (let [show-search-input? (atom false)]
    (fn [movements part-number title]
      [:div.pure-g.movement.search
       [:div.pure-u-1.add-movement.center
        [:div
         (when-not (nil? title) ; when the session has no title (no session has been created from template): dont show +
           [:i.fa.fa-plus.fa-3x
            {:onClick    (fn [e]
                           (.preventDefault e)
                           (let [part (session/get-in [:movement-session :parts part-number])
                                 categories (shuffle (seq (apply clojure.set/union (map :slot-category part))))]
                             (add-movement (first categories) part-number)))
             :style      {:margin-right '50 :cursor 'pointer}}])
         [:i.fa.fa-search-plus.fa-3x
          {:onClick    #(all-movements % show-search-input?)
           :style      {:cursor 'pointer}}]]
        (when @show-search-input?
          (let [id (str "mtags" part-number)
                all-movement-names (mapv :name data/all-movements)
                movements-ac-comp (with-meta text-input-component
                                             {:component-did-mount #(auto-complete-did-mount (str "#" id) all-movement-names)})]
            [movements-ac-comp {:style       {:font-size "100%" :margin-top 20}
                                :id          id
                                :class       "edit"
                                :placeholder "type to find and add movement.."
                                :size        32
                                :auto-focus  true
                                :on-save     #(when (some #{%} all-movement-names)
                                               (reset! show-search-input? false)
                                               (add-movement-from-search % part-number))
                                }]))]])))

(defn part-component []
  (let []
    (fn [movements i title]
      [:div.pure-g.movements
       [:div.pure-u-1
        (for [m movements]
          ^{:key (str m (rand-int 100000))} [movement-component m i])
        [add-movement-component movements i title]]])))

(defn session-page []
  (let [_ (session/put! :movement-session {:parts [[]] :activity "Styrketrening"})]
    (fn []
      [:div
       (if-let [session (session/get :movement-session)]
         [:div.content {:style {:margin-top 0}}
          [:div.pure-g
            [:span.pure-u {:onClick #(generate-session %)} "Lag økt"]
            [:span.pure-u {:onClick #(reset-session %)} "Fjern økt"]
            (when-let [description (:description session)]
              [:div.pure-u (first (shuffle description))])]
          [:div
           (when-let [parts (:parts session)]
             [:article.session
              (doall
                (for [i (range (count parts))]
                  ^{:key i} [part-component (get parts i) i (:title session)]))])]])])))
