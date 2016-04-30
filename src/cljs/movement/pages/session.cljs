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
    [movement.util :refer [handler-fn positions GET POST get-stored-sessions]]
    [movement.text :refer [text-edit-component text-input-component auto-complete-did-mount]]
    [movement.menu :refer [menu-component]]))

(defn replace-movement [event {:keys [kw movement part-number]}]
  (.preventDefault event)
  (cond
    (= :swap kw) (let [slot-category (:slot-category movement)
                       category (if slot-category
                                  (name (first (shuffle slot-category)))
                                  (name (first (shuffle (:category movement)))))]
                   (GET "movement-from-category" {:params        {:user-id  (:user-id (session/get :user))
                                                                  :category category}
                                                  :handler       (fn [new-movement]
                                                                   (let [old-movement (dissoc movement :next :previous) ; remove data that may not be overwritten by merging with the new movement
                                                                         part (session/get-in [:movement-session :parts part-number])
                                                                         pos (first (positions #{movement} part))
                                                                         new-part (assoc part pos (merge old-movement (first new-movement)))]
                                                                     (session/assoc-in! [:movement-session :parts part-number] new-part)))
                                                  :error-handler (fn [r] nil)}))
    (or (= :next kw)
        (= :previous kw)) (let [new-movement (first (shuffle (kw movement)))]
                            (GET "movement" {:params        {:user-id  (:user-id (session/get :user))
                                                             :name new-movement}
                                             :handler       (fn [new-movement]
                                                              (let [old-movement (dissoc movement :next :previous) ; remove data that may not be overwritten by merging with the new movement
                                                                    part (session/get-in [:movement-session :parts part-number])
                                                                    pos (first (positions #{movement} part))
                                                                    new-part (assoc part pos (merge old-movement new-movement))]
                                                                (session/assoc-in! [:movement-session :parts part-number] new-part)))
                                             :error-handler (fn [r] nil)}))
    :else nil))

(defn add-movement [category part-number]
  (GET "movement-from-category" {:params        {:user-id  (:user-id (session/get :user))
                                                 :category (name category)}
                                 :handler       (fn [[new-movement]]
                                                  (let [part (session/get-in [:movement-session :parts part-number])
                                                        new-part (conj part new-movement)]
                                                    (session/assoc-in! [:movement-session :parts part-number] new-part)))
                                 :error-handler (fn [r] nil)}))

(defn add-movement-from-search [name part-number]
  (GET "movement" {:params        {:user-id  (:user-id (session/get :user))
                                   :name name}
                   :handler       (fn [new-movement]
                                    (let [part (session/get-in [:movement-session :parts part-number])
                                          new-part (conj part  new-movement)]
                                      (session/assoc-in! [:movement-session :parts part-number] new-part)))
                   :error-handler (fn [r] nil)}))

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
        (if (empty? new-parts)
          (session/assoc-in! [:movement-session :parts] [[]])
          (session/assoc-in! [:movement-session :parts] new-parts)))
      (session/assoc-in! [:movement-session :parts part-number] new-part))))

(defn remove-session [event]
  (.preventDefault event)
  (session/remove! :movement-session))

(defn generate-placeholder-text [activity]
  (let [day (.getDay (DateTime.))
        nor-day ({1 "mandag" 2 "tirsdag" 3 "onsdag" 4 "torsdag" 5 "fredag" 6 "lørdag" 7 "søndag"} day)
        texts ["#du #kan #tagge #øktene #dine"
               (str "#" (str/lower-case activity))
               (str "Jeg liker lukten av bevegelse på " nor-day "er.")
               (str "Er det noe bedre enn litt " (str/lower-case activity) " på en " nor-day "?")
               "Hva tenker du om økta?"
               "Hvordan gikk økta?"
               "Det er her du skryter.."
               "Skryt av hva du gjorde i dag."
               (str "Hvordan gikk økta? #" nor-day "søkt")]]
    (first (shuffle texts))))

(defn generate-movement-session [event activity]
  (.preventDefault event)
  (GET "create-session"
       {:params        {:type    (:title activity)
                        :user-id (:user-id (session/get :user))}
        :handler       (fn [session]
                         (let [old-session (session/get :movement-session)]
                           (session/put! :movement-session
                                         (merge old-session session))))
        :error-handler (fn [r] (pr r))}))

(defn create-session-from-activity [event activity]
  (.preventDefault event)
  (session/put! :movement-session {:parts [[]] :activity activity}))

; brukes ikke lengre..
(defn preview-file []
  (let [file (.getElementById js/document "upload")
        reader (js/FileReader.)]
    (when-let [file (aget (.-files file) 0)]
      (set! (.-onloadend reader) #(session/update-in! [:movement-session] assoc :photo (-> % .-target .-result)))
      (.readAsDataURL reader file))))

(defn process-file [file]
  (let [reader (js/FileReader.)
        canvas (.getElementById js/document "session-image-canvas")
        ctx (.getContext canvas "2d")]
    (when-let [file (aget (.-files file) 0)]
      (set! (.-onload reader)
            (fn [e]
              (let [blob (js/Blob. (array (-> e .-target .-result)))
                    blob-url (.createObjectURL (.-URL js/window) blob)
                    image (js/Image.)]
                (session/update-in! [:movement-session] assoc :photo true)
                (set! (.-src image) blob-url)
                (set! (.-onload image)
                      (fn [e]
                        (let [max-w 1200
                              max-h 1200
                              w (.-width image)
                              h (.-height image)
                              [w h] (if (>= w h)
                                      (if (> w max-w)
                                        [max-w (* h (/ max-w w))]
                                        [w h])
                                      (if (> h max-h)
                                        [(* w (/ max-h h)) max-h]
                                        [w h])
                                      )]
                          (set! (.-width canvas) w)
                          (set! (.-height canvas) h)
                          (.drawImage ctx image 0 0 w h)))))))
      (.readAsArrayBuffer reader file))))

(defn add-photo-component []
  (if-let [photo (session/get-in [:movement-session :photo])]
    [:div.pure-g
     [:div.pure-u {:onClick    (fn [e] (.preventDefault e)
                                 (let [canvas (.getElementById js/document "session-image-canvas")
                                       ctx (.getContext canvas "2d")]
                                   (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
                                   (session/update-in! [:movement-session] dissoc :photo)))
                   :onTouchEnd (fn [e] (.preventDefault e)
                                 (let [canvas (.getElementById js/document "session-image-canvas")
                                       ctx (.getContext canvas "2d")]
                                   (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
                                   (session/update-in! [:movement-session] dissoc :photo)))
                   :style      {:color "red" :cursor 'pointer}} [:i.fa.fa-times.fa-2x]]]
    [:div.pure-g
     [:div.pure-u.pure-button.fileUpload
      [:span "Legg ved bilde"]
      [:input {:id "upload" :className "upload" :type "file"
               :on-change #(process-file (.getElementById js/document "upload"))}]]]))

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
   [:div.pure-u {:style {:color "#9999cc" :font-size "200%" :text-align 'center}} data]
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
         [:div.pure-u-1-5 {:onClick (fn [e] (.preventDefault e) (reset! expand (not @expand))) :onTouchEnd (fn [e] (.preventDefault e) (reset! expand (not @expand)))}
          [:img.graphic {:src (str "http://s3.amazonaws.com/mumrik-movement-images/" image) :title name :alt name}]]
         [:div.pure-u-2-5 {:onClick (fn [e] (.preventDefault e) (reset! expand (not @expand))) :onTouchEnd (fn [e] (.preventDefault e) (reset! expand (not @expand)))
                           :style   {:display 'flex :text-align 'center}}
          [:h3.title {:style {:margin 'auto}} name]]
         [:div.pure-u-1-5 {:onClick (fn [e] (.preventDefault e) (reset! expand (not @expand))) :onTouchEnd (fn [e] (.preventDefault e) (reset! expand (not @expand)))}

          [:div.pure-g [:div.pure-u-1
                        (if (pos? weight) (r-component {:data weight :name "kg"})
                                          [:div.pure-g {:style {:opacity 0.0}} [:div.pure-u {:style {:font-size "200%"}} 0]])]]
          [:div.pure-g [:div.pure-u-1
                        (when (pos? rep) (r-component {:data rep :name "reps"}))
                        (when (pos? distance) (r-component {:data distance :name "m"}))
                        (when (pos? duration) (r-component {:data duration :name "s"}))]]
          [:div.pure-g [:div.pure-u-1
                        (when (pos? rest) (r-component {:data rest :name "s pause"}))]]]

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
          (when (or performed-sets (> set 0))
            [:div.pure-g
             [:div.pure-u-1 [:div.center {:style {:margin-top (if performed-sets -24 -6)
                                                  :opacity    0.15}} "set"]]])]]

        (when @expand
          [:div
           [:div.pure-g
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Repetisjoner"]
             [:input {:style {:width 75}
                      :id    (str "rep-input" id) :type "number" :defaultValue rep :min 0}]]
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Avstand"]
             [:input {:style {:width 75}
                      :id    (str "distance-input" id) :type "number" :defaultValue distance :min 0}]
             [:span {:style {:margin-left 3}} "m"]]
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Tid"]
             [:input {:style {:width 75}
                      :id    (str "duration-input" id) :type "number" :defaultValue duration :min 0}]
             [:span {:style {:margin-left 3}} "sek"]]
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Vekt"]
             [:input {:style {:width 75}
                      :id    (str "weight-input" id) :type "number" :defaultValue weight :min 0 :step 0.5}]]
            [:div.pure-u {:style {:margin "5px 5px 5px 5px"}}
             [:label "Hvile"]
             [:input {:style {:width 75}
                      :id    (str "rest-input" id) :type "number" :defaultValue rest :min 0}]]
            [:a.pure-u-1-3.pure-button.pure-button-primary {:style   {:margin "5px 5px 5px 5px"}
                                                            :onClick (fn [e] (.preventDefault e) (update-movement {:id id :m m :parts parts :part-number part-number :pos pos}))
                                                            :onTouchEnd (fn [e] (.preventDefault e) (update-movement {:id id :m m :parts parts :part-number part-number :pos pos}))}
             "Oppdater"]]
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
               "Bytt med vanskeligere"])]])]])))

(defn all-movements [e show-search-input?]
  (.preventDefault e)
  (if (session/get :all-movements)
    (reset! show-search-input? (not @show-search-input?))
    (GET "movements" {:handler       (fn [movements] (session/put! :all-movements movements)
                                       (reset! show-search-input? true))
                      :error-handler (fn [] nil)})))

(defn add-movement-component []
  (let [show-search-input? (atom false)]
    (fn [movements part-number title]
      [:div.pure-g.movement.search
       [:div.pure-u-1.add-movement.center
        [:div
         (when-not (nil? title)                             ; when the session has no title (no session has been created from template): dont show +
           [:i.fa.fa-plus.fa-3x
            {:onClick    (fn [e]
                           (.preventDefault e)
                           (let [part (session/get-in [:movement-session :parts part-number])
                                 categories (shuffle (seq (apply clojure.set/union (map :slot-category part))))]
                             (add-movement (first categories) part-number)))
             :onTouchEnd (fn [e]
                           (.preventDefault e)
                           (let [part (session/get-in [:movement-session :parts part-number])
                                 categories (shuffle (seq (apply clojure.set/union (map :slot-category part))))]
                             (add-movement (first categories) part-number)))
             :style      {:margin-right '50 :cursor 'pointer}}])
         [:i.fa.fa-search-plus.fa-3x
          {:onClick #(all-movements % show-search-input?)
                    :onTouchEnd #(all-movements % show-search-input?)
                    :style {:cursor 'pointer}}]]
        (when @show-search-input?
          (let [id (str "mtags" part-number)
                movements-ac-comp (with-meta text-input-component
                                             {:component-did-mount #(auto-complete-did-mount
                                                                     (str "#" id)
                                                                     (vec (session/get :all-movements)))})]
            [movements-ac-comp {:style {:font-size "200%" :margin-top 20}
                                :id          id
                                :class       "edit"
                                :placeholder "type to find and add movement.."
                                :size        32
                                :auto-focus  true
                                :on-save     #(when (some #{%} (session/get :all-movements))
                                               (reset! show-search-input? false)
                                               (add-movement-from-search % part-number))}]))]])))

(defn part-component []
  (let []
    (fn [movements i title]
      [:div.pure-g.movements
       [:div.pure-u-1
        (for [m movements]
          ^{:key (str m (rand-int 100000))} [movement-component m i])
        [add-movement-component movements i title]]])))

(defn list-of-activities []
  (let [activites [{:title "Naturlig bevegelse" :graphic 'lightgreen}
                   {:title "Styrketrening" :graphic 'darkgreen}
                   {:title "Mobilitet" :graphic 'brown}
                   {:title "Løping" :graphic 'red}
                   {:title "Vandring" :graphic 'orange}
                   {:title "Sykling" :graphic 'darkgreen}
                   {:title "Yoga" :graphic 'purple}
                   {:title "Pilates" :graphic 'red}
                   {:title "Crossfit" :graphic 'brown}
                   {:title "Ski" :graphic 'lightblue}
                   {:title "Svømming" :graphic 'blue}
                   {:title "Annen aktivitet" :graphic 'lightgray}]]
    (fn []
      [:div.movements
       [:div.pure-g
        [:div.pure-u-1 {:style {:font-size "150%"}} "Logg en aktivitet"]]
       (doall
         (for [{:keys [title graphic] :as a} activites]
           ^{:key title}
           [:div.pure-g.activity {:style {:padding "25px 25px 25px 25px"}
                                  :onClick #(create-session-from-activity % a) :onTouchEnd #(create-session-from-activity % a)}
            [:div.pure-u {:style {:border-radius "50% 50% 50% 50%" :width 150 :height 150 :background-color graphic}}]
            [:div.pure-u {:style {:font-size "175%" :margin-left 20 :margin-top 50}} title]]))])))

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

(defn text-component [activity]
  [:div.pure-g {:style {:margin-top '25}}
   [:div.pure-u-1
    [:textarea {:rows      10 :cols 120
                :style     {:resize 'vertical} :placeholder (generate-placeholder-text (:title activity))
                :on-change #(session/assoc-in! [:movement-session :comment] (-> % .-target .-value))
                :value     (session/get-in [:movement-session :comment])}]]])

(defn store-session [event s & unique-movements]
  (.preventDefault event)
  (let [canvas (.getElementById js/document "session-image-canvas")
        image (.toDataURL canvas "image/jpeg" 0.9)
        session (session/get :movement-session)
        session (if-not (:comment session) (assoc session :comment "") session)
        new-parts (mapv (fn [part]
                          (mapv (fn [m]
                                  (-> m
                                      ((fn [m] (if (nil? (:performed-sets m))
                                                 (assoc m :performed-sets (:set m))
                                                 m)))
                                      (dissoc :category :slot-category :measurement :previous :next :set)
                                      ))
                                part))
                        (:parts session))
        new-parts (if (empty? (flatten new-parts)) [] new-parts) ; <--- worked fine when added this 29-04T13:33
        date (if-let [date (:date session)] date (date-string))
        time (time-string)
        date-time (str date "T" time)
        hash-tags (vec (re-seq #"#[\w]+" (:comment session)))
        session (assoc session :activity (:title (:activity session))
                               :photo image
                               :parts new-parts
                               :date-time date-time
                               :tags hash-tags
                               :unique-movements (map #(dissoc % :image) (flatten unique-movements)))
        session (dissoc session :date)]
    (POST "store-session"
          {:params        {:session session
                           :user-id (:user-id (session/get :user))}
           :handler       (fn [] (reset! s true))
           :error-handler (fn [r] (pr r))})))

(defn finish-session-component [& unique-movements]
  ;; Etter trykk på avslutt&lagre bør den oppdaterte feeden vises
  (let [s (atom false)]
    (fn [& unique-movements]
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
           {:onClick #(store-session % s unique-movements) :onTouchEnd #(store-session % s unique-movements)} "Logg økta"]])])))

(defn session-page []
  (let []
    (fn []
      [:div
       [menu-component]
       (if-let [session (session/get :movement-session)]
         [:div.content {:style {:margin-top 100}}
          [:a {:style      {:float 'right :margin-right 20 :margin-top 20
                            :color (:graphic (:activity session)) :opacity 1}
               :onClick    #(remove-session %)
               :onTouchEnd #(remove-session %)}
           [:i.fa.fa-times.fa-4x]]
          [:div.pure-g
           (when (or (= "Naturlig bevegelse" (:title (:activity session)))
                     (= "Styrketrening" (:title (:activity session)))
                     (= "Mobilitet" (:title (:activity session))))
             [:img.pure-u
              {:src        (str "images/mumrik.png") :title "Lag økt" :alt "Lag økt"
               :style      {:height       250
                            :margin-left  20
                            :margin-right 20 :cursor 'pointer}
               :onClick    #(generate-movement-session % (:activity session))
               :onTouchEnd #(generate-movement-session % (:activity session))}])
           (when-let [description (:description session)]
             [:div.pure-u (first (shuffle description))])]

          [:div
           (when-let [parts (:parts session)]
             [:article.session
              (doall
                (for [i (range (count parts))]
                  ^{:key i} [part-component (get parts i) i (:title session)]))])
           [:div.pure-g
            [:div.pure-u {:style {:font-size "200%"}} (str (:title (:activity session)) " i ")]
            [:div.pure-u {:style {:font-size "200%" :margin-left 10 :margin-right 10 :margin-bottom 10}} (time-component)]]
           (text-component (:activity session))
           [:div.pure-g
            [:div.pure-u-1-2
             (add-photo-component)]
            [:div.pure-u-1-2
             (date-component)]]
           [:canvas {:id "session-image-canvas"}]
           (if-let [parts (:parts session)]
             [(let [movements (flatten parts)

                    unique-movements (-> (for [m movements]
                                           (-> m
                                               (#(if (= 0 (:zone %)) (assoc % :zone 1) %))
                                               (dissoc :id :category :slot-category :measurement
                                                       :set :distance :duration :rep :movement :rest :weight
                                                       :natural-only? :performed-sets :next :previous)))
                                         set
                                         vec
                                         atom)]
                (fn []
                  [:div
                   (let [ms @unique-movements]
                     [:div.pure-g
                      (doall
                        (for [m ms]
                          ^{:key (rand-int 10000000)}
                          [:div.pure-u {:id    (str "unique-movement-" (:name m))
                                        :style {:border-bottom "1px solid"}}
                           [:img.graphic {:src (str "http://s3.amazonaws.com/mumrik-movement-images/" (:image m)) :title (:name m) :alt (:name m)}]
                           (let [m-pos (first (positions #{m} ms))]
                             (cond
                               (= 1 (:zone m))
                               [:div.center.dim
                                [:i.fa.fa-star.gold]
                                [:i.fa.fa-star-o.star {:onClick    (fn [e] (.preventDefault e) (swap! unique-movements assoc m-pos (assoc m :zone 2)))
                                                       :onTouchEnd (fn [e] (.preventDefault e) (swap! unique-movements assoc m-pos (assoc m :zone 2)))
                                                       :style      {:cursor 'pointer}}]
                                [:i.fa.fa-star-o.star {:onClick    (fn [e] (.preventDefault e) (swap! unique-movements assoc m-pos (assoc m :zone 3)))
                                                       :onTouchEnd (fn [e] (.preventDefault e) (swap! unique-movements assoc m-pos (assoc m :zone 3)))
                                                       :style   {:cursor 'pointer}}]]

                               (= 2 (:zone m))
                               [:div.center.dim
                                [:i.fa.fa-star.gold.star {:onClick    (fn [e] (.preventDefault e) (swap! unique-movements assoc m-pos (assoc m :zone 1)))
                                                          :onTouchEnd (fn [e] (.preventDefault e) (swap! unique-movements assoc m-pos (assoc m :zone 1)))
                                                          :style   {:cursor 'pointer}}]
                                [:i.fa.fa-star.gold]
                                [:i.fa.fa-star-o.star {:onClick    (fn [e] (.preventDefault e) (swap! unique-movements assoc m-pos (assoc m :zone 3)))
                                                       :onTouchEnd (fn [e] (.preventDefault e) (swap! unique-movements assoc m-pos (assoc m :zone 3)))
                                                       :style   {:cursor 'pointer}}]]

                               (= 3 (:zone m))
                               [:div.center.dim
                                [:i.fa.fa-star.gold.star {:onClick    (fn [e] (.preventDefault e) (swap! unique-movements assoc m-pos (assoc m :zone 1)))
                                                          :onTouchEnd (fn [e] (.preventDefault e) (swap! unique-movements assoc m-pos (assoc m :zone 1)))
                                                          :style   {:cursor 'pointer}}]
                                [:i.fa.fa-star.gold.star {:onClick    (fn [e] (.preventDefault e) (swap! unique-movements assoc m-pos (assoc m :zone 2)))
                                                          :onTouchEnd (fn [e] (.preventDefault e) (swap! unique-movements assoc m-pos (assoc m :zone 2)))
                                                          :style   {:cursor 'pointer}}]
                                [:i.fa.fa-star.gold]]))]))])
                   [finish-session-component @unique-movements]]))]
             [finish-session-component])]]
         [:div.content
          [list-of-activities]])])))