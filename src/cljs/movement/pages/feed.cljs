(ns movement.pages.feed
  (:require [movement.menu :refer [menu-component]]
            [reagent.core :refer [atom]]
            [clojure.string :as str]
            [movement.util :refer [GET POST]]))

(defonce feed-data (atom nil))

(defn load-feed [feed-data]
  (POST "feed" {:params        {:email ""}
                :handler       (fn [r] (reset! feed-data r))
                :error-handler (fn [r] (pr (str "error loading feed: " r)))}))

(defn load-more [event feed-data]
  (.preventDefault event)
  (swap! feed-data conj {:user-name    "Kårinator"
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
                         :image        "images/field.jpg"}))

(defn image-url [movement-name]
  (when-not (nil? movement-name)
    (str "images/movements/" (str/replace (str/lower-case movement-name) " " "-") ".png")))

(defn r-component [{:keys [data name]}]
  [:div.pure-g {:style {:margin 'auto}}
   [:div.pure-u
    [:div.pure-g
     [:div.pure-u {:style {:color "#9999cc" :font-size "200%" :text-align 'right :padding-right 10}} data]
     [:div.pure-u {:style {:padding-top 10}} name]]]])

(defn movement-component []
  (let []
    (fn [{:keys [name image rep set distance duration weight rest]}]
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
           [:div.pure-u {:style {:margin 'auto :margin-top 30 :opacity 0.05 :font-size "300%"}} set]]
          [:div.pure-g
           [:div.pure-u-1 [:div.center {:style {:margin-top 0 :opacity 0.25}} "set"]]]]]]])))

(defn part-component []
  (let []
    (fn [movements]
      [:div.pure-g.movements {:style {:margin-bottom "2rem"}}
       [:div.pure-u-1
        (doall
          (for [m movements]
            ^{:key (str (:movement-name m) (rand-int 100000))}
            [movement-component m]))]])))

(defn session-view []
  (let [show-session-data? (atom false)]
    (fn [{:keys [url activity user-image user-name date time text comments image session-data likes]
          :or   {user-image "images/movements/static-air-baby.png"}}]
      [:div {:style {:border-bottom "1px solid lightgray"}}
       [:div.pure-g
        [:div.pure-u-1-6.center [:img {:src   user-image :width "100px"
                                       :style {:cursor 'pointer}
                                       ; onClick/onTouchEnd -> show profile
                                       }]]
        [:div.pure-u-5-6
         [:div.pure-g [:h2 [:span.pure-u {:style {:cursor 'pointer
                                                  :margin-top 0}
                                       ; onClick/onTouchEnd -> show profile
                                       } user-name]]]
         [:div.pure-g [:div.pure-u {:style {:margin-bottom 25}} date]]]]
       [:div.center [:img {:src image :width "100%"}]]


       [:div {:style {:margin "0 40px 0 40px"}}
        [:div.pure-g
         [:h2.pure-u-5-6 (str activity (when time (str " i " time)))]
         (when-not (empty? session-data)
           (if @show-session-data?
             [:div.pure-u-1-6 [:i.fa.fa-minus-square.fa-4x {:onClick    #(reset! show-session-data? false)
                :onTouchEnd #(reset! show-session-data? false)
                :style      {:cursor       'pointer
                             :float        'right
                             :margin-right 15
                             :color        'lightgray}}]]
             [:div.pure-u-1-6 [:i.fa.fa-plus-square.fa-4x {:onClick    #(reset! show-session-data? true)
                                                           :onTouchEnd #(reset! show-session-data? true)
                                                           :style      {:cursor 'pointer
                                                                        :float        'right
                                                                        :margin-right 15
                                                                        :color  'lightgray}}]]))]
        (when @show-session-data?
          [:article.session
           (doall
             (for [part session-data]
               ^{:key (rand-int 1000)}
               [part-component part]))])
        [:div.pure-g
         [:p.pure-u-1 {:style {:padding-bottom 40 :border-bottom 'dotted}} [:a user-name] (str " " text)]]
        [:div.pure-g
         [:p.pure-u-1 (str likes " tomler opp")]]
        (doall
          (for [{:keys [comment user]} comments]
            ^{:key (str user comment)}
            [:div.pure-g {:style {:margin-bottom 10}}
             [:div.pure-u-1 [:a user] (str " " comment)]]))
        [:div.pure-g {:style {:margin-bottom 20}}
         [:div.pure-u-1
          [:i.fa.fa-heart.fa-2x {:style {:cursor 'pointer
                                         :color 'lightgray}}]
          [:i.fa.fa-comment.fa-2x {:style {:margin-left 40
                                           :cursor 'pointer
                                           :color 'lightgray}}]]]]])))

(defn feed-page []
  (let [
        _ (load-feed feed-data)
        ]
    (fn []
      [:div
       [menu-component]
       [:div#feed
        (if-let [sessions @feed-data]
          (doall
            (for [session sessions]
              ^{:key (:url session)}
              [session-view session]))
          [:div.pure-g {:style {:margin-top 200}}
           [:div.pure-u-1.center
            [:i.fa.fa-spinner.fa-pulse.fa-4x]]])
        (when @feed-data
          [:div.pure-g [:div.pure-u-1.pure-button {:onClick    #(load-more % feed-data)
                                                   :onTouchEnd #(load-more % feed-data)} "Last flere"]])]])))