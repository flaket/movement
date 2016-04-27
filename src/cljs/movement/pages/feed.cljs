(ns movement.pages.feed
  (:require [movement.menu :refer [menu-component]]
            [reagent.core :refer [atom]]
            [clojure.string :as str]
            [cljs.reader :refer [read-string]]
            [movement.util :refer [GET POST]]
            [reagent.session :as session]
            [secretary.core :include-macros true :refer [dispatch!]]))

(defn load-feed []
  (GET "feed" {:params        {:user-id (:user-id (session/get :user))}
               :handler       (fn [r] (if (empty? r) (session/put! :feed [])
                                                     (session/put! :feed r)))
               :error-handler (fn [r] (pr (str "error loading feed: " r)))}))

(defn load-user-only-feed []
  (GET "user-only-feed" {:params        {:user-id (:user-id (session/get :user))}
                         :handler       (fn [r] (session/put! :user-only-feed r))
                         :error-handler (fn [r] (pr (str "error loading feed: " r)))}))

(defn load-more [event]
  (.preventDefault event)
  (session/put! :feed (conj (session/get :feed) {:user-name    "Kårinator"
                                                 :user-image   "images/movements/pull-up.png"
                                                 :url          "17"
                                                 :text         "en finasdasd økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt"
                                                 :date         "3 timer siden"
                                                 :time         "45:00"
                                                 :activity     "Styrkeøkt"
                                                 :session-data [[{:name "Push Up" :rep 10 :set 3 :image "push-up.png"}
                                                                 {:name "Pull Up" :rep 5 :set 3 :image "pull-up.png"}]]
                                                 :comments     [{:comment "Ser bra ut!" :user "Bobby"}
                                                                {:comment "Oi, dette skal jeg prøve!" :user "Kari"}]
                                                 :likes        10
                                                 :image        "images/field.jpg"})))

(defn r-component [{:keys [data name]}]
  [:div.pure-g {:style {:margin 'auto}}
   [:div.pure-u
    [:div.pure-g
     [:div.pure-u {:style {:font-size "200%" :text-align 'right :padding-right 10}} data]
     [:div.pure-u {:style {:padding-top 10}} name]]]])

(defn movement-component []
  (let []
    (fn [{:keys [name image rep performed-sets set distance duration weight rest]}]
      [:div.pure-g.movement
       [:div.pure-u-1
        [:div.pure-g
         [:div.pure-u-1-5
          [:img.pure-img-responsive.graphic {:src (str "images/movements/" image) :title name :alt name}]]
         [:div.pure-u-2-5 {:style {:display 'flex :text-align 'center}}
          [:h3.title {:style {:margin 'auto}} name]]
         [:div.pure-u-1-5 {:style {:display 'flex}}
          (when (pos? rep) (r-component {:data rep :name "reps"}))
          (when (pos? distance) (r-component {:data distance :name "m"}))
          (when (pos? duration) (r-component {:data duration :name "s"}))
          (when (pos? weight) (r-component {:data weight :name "kg"}))
          (when (pos? rest) (r-component {:data rest :name "s"}))]
         [:div.pure-u-1-5
          [:div.pure-g {:style {:display 'flex}}
           [:div.pure-u {:style {:margin 'auto :margin-top 30 :opacity 0.85 :font-size "300%"}} performed-sets]]
          [:div.pure-g
           [:div.pure-u-1 [:div.center {:style {:margin-top 0 :opacity 0.85}} "set"]]]]]]])))

(defn part-component []
  (let []
    (fn [movements]
      [:div.pure-g.movements {:style {:margin-bottom "2rem"}}
       [:div.pure-u-1
        (doall
          (for [m movements]
            ^{:key (rand-int 10000000)}
            [movement-component m]))]])))

(defn add-comment [{:keys [adding-comment? comments session-url]}]
  (let [text (atom "")]
    (fn []
      [:div
       [:div.pure-g {:style {:margin-bottom 20}}
        [:div.pure-u-1
         [:textarea {:rows      5 :cols 120
                     :style     {:resize 'vertical}
                     :on-change #(reset! text (-> % .-target .-value))
                     :value     @text}]]]
       [:div.pure-g {:style {:margin-bottom 20}}
        [:a.pure-u-1.pure-button.pure-button-primary.button-xlarge
         {:onClick    (fn []
                        (reset! adding-comment? false)
                        (POST "comment" {:params        {:session-url session-url
                                                         :comments    (conj comments {:user (:name (session/get :user)) :comment @text})}
                                         :handler       (fn [r]
                                                          ; wait for a few ticks and dispatch to /feed to refresh
                                                          )
                                         :error-handler (fn [r] nil)}))
          :onTouchEnd (fn [] (reset! adding-comment? false) (POST "comment" {:params {:session-url session-url :comments (conj comments @text)} :handler (fn [r] (load-feed)) :error-handler (fn [r] nil)}))}
         "Kommenter"]]])))

(defn session-view []
  (let [show-session-data? (atom false)
        adding-comment? (atom false)]
    (fn [{:keys [url activity user-image user-id user-name date-time time comment comments image parts likes]
          :or   {user-image (first (shuffle ["images/field.jpg" "images/forest.jpg" "images/winter.jpg"]))}}]
      [:div {:style {:border-bottom "1px solid lightgray"}}

       ; user image, name and timestamp
       [:div.pure-g
        [:div.pure-u [:img {:src   user-image :width 80 :height 80
                                :style {:margin-top 15 :margin-left 40
                                        :cursor 'pointer :border-radius "50% 50% 50% 50%"}
                                ; onClick/onTouchEnd -> show profile
                                       }]]
        [:div.pure-u {:style {:margin-left 20}}
         [:div.pure-g [:h2 [:a.pure-u {
                                       ; onClick/onTouchEnd -> show profile
                                       } user-name]]]
         [:div.pure-g [:div.pure-u {:style {:margin-bottom 25}} date-time]]]]

       [:div {:style {:margin "0 40px 0 40px"}}

        ; photo
        [:div.center [:img {:src image :width "100%"}]]

        [:div.pure-g

         ; Activity and time
         [:h2.pure-u-5-6 (str activity (when time
                                         (let [time-string (str/split time #":")
                                               [h m s] (map #(read-string %) time-string)]
                                           (str " i "
                                                (cond
                                                  (= h 0) ""
                                                  (= h 1) (str h " time ")
                                                  :else (str h " timer "))
                                                (cond
                                                  (= m 0) ""
                                                  (= m 1) (str m " minutt ")
                                                  :else (str m " minutter "))
                                                (cond
                                                  (= s nil) ""
                                                  (= s 0) ""
                                                  (= s 1) (str s " sekund ")
                                                  :else (str s " sekunder "))))))]

         (when-not (empty? (flatten parts))
           (if @show-session-data?
             [:div.pure-u-1-6 [:i.fa.fa-minus-square.fa-4x {:onClick    #(reset! show-session-data? false)
                                                            :onTouchEnd #(reset! show-session-data? false)
                                                            :style      {:cursor       'pointer
                                                                         :float        'right
                                                                         :margin-right 15
                                                                         :color        'lightgray}}]]
             [:div.pure-u-1-6 [:i.fa.fa-plus-square.fa-4x {:onClick    #(reset! show-session-data? true)
                                                           :onTouchEnd #(reset! show-session-data? true)
                                                           :style      {:cursor       'pointer
                                                                        :float        'right
                                                                        :margin-right 15
                                                                        :color        'lightgray}}]]))]
        (when @show-session-data?
          [:article.session
           (doall
             (for [part parts]
               ^{:key (rand-int 1000000)}
               [part-component part]))])

        ; Username and session comment text if user typed a comment.
        (when comment
          [:div.pure-g {:style {:border-bottom "1px dotted"}}
           [:p.pure-u-1
            [:a.user {:onClick (fn [e]
                                 (.preventDefault e)
                                 (GET "user" {:params        {:user-id user-id}
                                              :handler       (fn [r]
                                                               (session/put! :viewing-user r)
                                                               (session/remove! :selected-menu-item)
                                                               (dispatch! "/user"))
                                              :error-handler (fn [r] nil)}))
                      :onTouchEnd #()}
             user-name]
            (str " " comment)]])

        ; If session has any likes -> show
        (when-not (empty? likes)
          [:div.pure-g
           [:p.pure-u-1 (str (count likes) (if (= 1 (count likes)) " tommel" " tomler") " opp")]])

        ; Possible additional comments from user or other users
        [:div {:style {:margin-top 10}}
         (doall
           (for [{:keys [comment user]} comments]
             ^{:key (str user comment)}
             [:div.pure-g {:style {:margin-bottom 10}}
              [:div.pure-u-1
               [:a.user {:onClick    (fn [e]
                                       (.preventDefault e)
                                       (GET "user" {:params        {:user-id user-id}
                                                    :handler       (fn [r]
                                                                     (session/put! :viewing-user r)
                                                                     (session/remove! :selected-menu-item)
                                                                     (dispatch! "/user"))
                                                    :error-handler (fn [r] nil)}))
                         :onTouchEnd #()} user]
               (str " " comment)]]))]

        ; Buttons for "liking" or "commenting"
        [:div.pure-g {:style {:margin-top 30 :margin-bottom 30}}
         (let [viewing-user-id (:user-id (session/get :user))]
           [:div.pure-u-1
            (when-not (= user-id viewing-user-id)
              [:i.fa.fa-thumbs-up.fa-2x {:style      {:cursor (when-not ((set likes) user-id) 'pointer) :color (if ((set likes) user-id) "#009900" 'lightgray)}
                                     :onClick    (fn []
                                                   ; "likes" lagres i databasen som en liste fordi 1.ddb kan ikke lagre tomme set init. 2.opplevde EDN-problemer med å sende set mellom server og klient.
                                                   ; Listen gjøres om til sett her for enklere logikk og tilbake til vektor for lagring.
                                                   (when-not ((set likes) user-id)
                                                     (POST "like" {:params        {:session-url url
                                                                                   :likers      (vec (conj (set likes) user-id))}
                                                                   :handler       (fn [r]
                                                                                    ; wait for a few ticks and dispatch to /feed to refresh
                                                                                    )
                                                                   :error-handler (fn [r] nil)})))
                                     :onTouchEnd (fn [] (when-not ((set likes) user-id) (POST "like" {:params  {:session-url url :likers (vec (conj (set likes) user-id))}
                                                                                                      :handler (fn [r] (load-feed)) :error-handler (fn [r] nil)})))}])

            [:i.fa.fa-comment.fa-2x {:onClick #(reset! adding-comment? true) :onTouchEnd #(reset! adding-comment? true)
                                     :style   {:margin-left (when-not (= user-id viewing-user-id) 40)
                                               :cursor      'pointer
                                               :color       'lightgray}}]])]
        (when @adding-comment?
          [add-comment {:adding-comment? adding-comment? :comments comments :session-url url}])]
       ])))

(defn feed-page []
  (let [
        _ (load-feed)
        ]
    (fn []
      [:div
       [menu-component]
       [:div#feed
        [:div.content
         (if-let [sessions (session/get :feed)]
           (if (empty? sessions)
             [:div.pure-g {:style {:margin-top 200}}
              [:div.pure-u-1.center
               "Ingen økter å vise. Logg en treningsøkt eller følg venner for å se økter her."]]
             (doall
               (for [session sessions]
                 ^{:key (:url session)}
                 [session-view session])))
           [:div.pure-g {:style {:margin-top 200}}
            [:div.pure-u-1.center
             [:i.fa.fa-spinner.fa-pulse.fa-4x]]])]
        #_(when-not (empty? (session/get :feed))
          [:div.pure-g [:div.pure-u-1.pure-button.x-large {:onClick    #(load-more %)
                                                           :onTouchEnd #(load-more %)} "Last flere"]])]])))