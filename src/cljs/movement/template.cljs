(ns movement.template
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [reagent-forms.core :refer [bind-fields]]
            [cljs.core.async :as async :refer [timeout <!]]
            [secretary.core :include-macros true :refer [dispatch!]]
            [movement.menu :refer [menu-component]]
            [movement.util :refer [handler-fn text-input POST get-templates]]
            [movement.text :refer [text-input-component auto-complete-did-mount]]
            [movement.components.creator :refer [username heading title description error]]
            [clojure.string :as str]
            [cljs.reader :refer [read-string]]))

(def template-state (atom {:parts []}))

(defn preview-file []
  (let [file (.getElementById js/document "upload")
        reader (js/FileReader.)]
    (when-let [file (aget (.-files file) 0)]
      (set! (.-onloadend reader) #(swap! template-state assoc :background (-> % .-target .-result str)))
      (.readAsDataURL reader file))))

(defn upload-background-component []
  [:div.pure-g
   [:div.pure-u "Upload a custom background image for your template: "]
   [:input.pure-u {:id   "upload"
                   :type "file" :on-change #(preview-file)}]
   (when (:background @template-state)
     [:div.pure-u {:on-click #(swap! template-state dissoc :background)
                   :style    {:color "blue"}} "Remove custom background"])])

(defn movement-component
  ([categories data title image] (movement-component categories data title image nil))
  ([categories {:keys [rep set distance duration weight rest i]} title image m]
   [:div.pure-u.movement
    (when-not (nil? m)
      [:div.pure-g
       [:div.pure-u-1
        [:i.fa.fa-remove {:style {:margin "10px 0 0 15px" :cursor 'pointer :color "#CC9999" :opacity 0.8 :font-size "2.0em"}
                          :on-click #(let [specific-movements (vec (remove #{m} (get-in @template-state [:parts i :specific-movements])))]
                                      (swap! template-state assoc-in [:parts i :specific-movements] specific-movements))
                          :title    "Remove movement"}]]])
    [:h3.pure-g
     [:div.pure-u-1.center title]]
    (if-not (nil? m)
      [:img.graphic.pure-img-responsive {:src image}]
      [:div.pure-g
       (for [c categories]
         [:div.pure-u-1.center {:style {:margin-right 5}} c])
       [:img.graphic.pure-img-responsive {:src image}]])
    [:div
     [:div.pure-g
      [:div.pure-u-1-3.center {:className (str (when-not (and rep (< 0 rep)) " no-data"))} "Reps"]
      [:div.pure-u-1-3.center {:className (str (when-not (and set (< 0 set)) " no-data"))} "Set"]
      [:div.pure-u-1-3.center {:className (str (when-not (and rest (< 0 rest)) " no-data"))} "Rest"]]
     [:div.pure-g
      [:div.pure-u-1-3.rep-set.center (when (and rep (< 0 rep)) rep)]
      [:div.pure-u-1-3.rep-set.center (when (and set (< 0 set)) set)]
      [:div.pure-u-1-3.rep-set.center (when (and rest (< 0 rest)) rest)]]]
    [:div
     [:div.pure-g
      [:div.pure-u-1-3.center {:className (str (when-not (and distance (< 0 distance)) " no-data"))} "Meters"]
      [:div.pure-u-1-3.center {:className (str (when-not (and duration (< 0 duration)) " no-data"))} "Seconds"]
      [:div.pure-u-1-3.center {:className (str (when-not (and weight (< 0 weight)) " no-data"))} "Weight"]]
     [:div.pure-g
      [:div.pure-u-1-3.rep-set.center (when (and distance (< 0 distance)) distance)]
      [:div.pure-u-1-3.rep-set.center (when (and duration (< 0 duration)) duration)]
      [:div.pure-u-1-3.rep-set.center (when (and set (< 0 weight)) weight)]]]]))

(defn rep-set-distance-duration-component [i]
  [:div.pure-g
   [:div.pure-u-1
    [:div.pure-g.center
     [:div.pure-u-1 "Movements will be generated with the following data"]]
    [:div.pure-g
     [:input.pure-u {:style {:margin-right 5 :margin-top 5}
                     :type        "number"
                     :size        6
                     :placeholder "repetitions"
                     :value       (get-in @template-state [:parts i :rep])
                     :on-change   #(try
                                    (let [value (-> % .-target .-value read-string)]
                                      (if (or (nil? value) (and (integer? value) (< 0 value)))
                                        (swap! template-state assoc-in [:parts i :rep] value)))
                                    (catch js/Error e
                                      (pr (str "Caught exception: " e))))}]
     [:input.pure-u {:style {:margin-right 5 :margin-top 5}
                     :type      "number"
              :size      6
              :placeholder "sets"
              :value     (get-in @template-state [:parts i :set])
              :on-change #(try
                           (let [value (-> % .-target .-value read-string)]
                             (if (or (nil? value) (and (integer? value) (< 0 value)))
                               (swap! template-state assoc-in [:parts i :set] value)))
                           (catch js/Error e
                             (pr (str "Caught exception: " e))))}]
     [:input.pure-u {:style {:margin-right 5 :margin-top 5}
                     :type        "number"
                     :size        6
                     :placeholder "rest"
                     :value       (get-in @template-state [:parts i :rest])
                     :on-change   #(try
                                    (let [value (-> % .-target .-value read-string)]
                                      (if (or (nil? value) (and (integer? value) (< 0 value)))
                                        (swap! template-state assoc-in [:parts i :rest] value)))
                                    (catch js/Error e
                                      (pr (str "Caught exception: " e))))}]
     [:input.pure-u {:style {:margin-right 5 :margin-top 5}
              :type      "number"
              :value     (get-in @template-state [:parts i :distance])
              :size      6
              :placeholder "meters"
              :on-change #(try
                           (let [value (-> % .-target .-value read-string)]
                             (if (or (nil? value) (and (integer? value) (< 0 value)))
                               (swap! template-state assoc-in [:parts i :distance] value)))
                           (catch js/Error e
                             (pr (str "Caught exception: " e))))}]
     [:input.pure-u {:style {:margin-right 5 :margin-top 5}
              :type      "number"
              :value     (get-in @template-state [:parts i :duration])
              :size      6
              :placeholder "seconds"
              :on-change #(try
                           (let [value (-> % .-target .-value read-string)]
                             (if (or (nil? value) (and (integer? value) (< 0 value)))
                               (swap! template-state assoc-in [:parts i :duration] value)))
                           (catch js/Error e
                             (pr (str "Caught exception: " e))))}]
     [:input.pure-u {:style {:margin-right 5 :margin-top 5}
                     :type        "number"
                     :size        6
                     :placeholder "weight"
                     :value       (get-in @template-state [:parts i :weight])
                     :on-change   #(try
                                    (let [value (-> % .-target .-value read-string)]
                                      (if (or (nil? value) (and (integer? value) (< 0 value)))
                                        (swap! template-state assoc-in [:parts i :weight] value)))
                                    (catch js/Error e
                                      (pr (str "Caught exception: " e))))}]
     ]]])

(defn movement-input [i n]
  [:div.pure-g {:style {:margin-top 10}}
   [:div.pure-u
    (let [id (str "mtags" i)
          movements-ac-comp (with-meta text-input-component
                                       {:component-did-mount #(auto-complete-did-mount
                                                               (str "#" id)
                                                               (vec (session/get :all-movements)))})]
      [movements-ac-comp {:id          id
                          :class       "edit"
                          :placeholder "Add a specific movement.."
                          :size        32
                          :on-save     #(when (and (some #{%} (session/get :all-movements))
                                                   (not (some #{%} (get-in @template-state [:parts i :specific-movements]))))
                                         (swap! template-state update-in [:parts i :specific-movements] conj %))}])]
   [:div.pure-u {:style {:margin-left 5}}
    "" [:span {:style {:color "red" :font-size "24px"}} n] " random " (if (= n 1) "movement" "movements")]
   [:button.button.pure-u
    {:style {:margin-left 5}
     :on-click #(when (> n 0)
                 (swap! template-state update-in [:parts i :n] dec))} [:i.fa.fa-minus]]
   [:button.button.pure-u
    {:style {:margin-left 5}
     :on-click #(swap! template-state update-in [:parts i :n] inc)} [:i.fa.fa-plus]]])

(defn category-input [categories i showing-categories-list]
  [:div
   [:div.pure-g {:style {:margin-top 10 :margin-bottom 10}}
    [:a.pure-u {:style {:text-decoration 'underline}
                :on-click #(handler-fn (reset! showing-categories-list (not @showing-categories-list)))}
     (if @showing-categories-list "Hide categories list" "Show categories list")]]
   (when @showing-categories-list
     [:div.pure-g
      (for [c (sort (session/get :all-categories))]
        ^{:key c}
        [:div.pure-u
         {:style    {:cursor     'pointer :margin-right 10 :margin-bottom 5
                     :color      (when (some #{c} (get-in @template-state [:parts i :categories])) "#fffff8")
                     :background (when (some #{c} (get-in @template-state [:parts i :categories])) "gray")}
          :on-click #(if (some #{c} (get-in @template-state [:parts i :categories]))
                      (let [new-categories (remove #{c} (get-in @template-state [:parts i :categories]))]
                        (swap! template-state assoc-in [:parts i :categories] new-categories))
                      (swap! template-state update-in [:parts i :categories] conj c))} c])])
   [:div.pure-g
    (let [id (str "ctags" i)
          categories-ac-comp (with-meta text-input-component
                                        {:component-did-mount #(auto-complete-did-mount
                                                                (str "#" id)
                                                                (vec (session/get :all-categories)))})]
      [:div.pure-u
       [categories-ac-comp {:id          id
                            :class       "edit"
                            :placeholder "Add part categories.."
                            :size        32
                            :on-save     #(when (and (some #{%} (session/get :all-categories))
                                                     (not (some #{%} (get-in @template-state [:parts i :categories]))))
                                           (swap! template-state update-in [:parts i :categories] conj %))}]])
    (for [c categories]
      ^{:key (str c (rand-int 1000))}
      [:div.pure-u {:style {:margin-left 5}}
       [:span c]
       [:i.fa.fa-times {:style    {:color 'red :cursor 'pointer}
              :on-click #(let [categories (vec (remove #{c} (get-in @template-state [:parts i :categories])))]
                          (swap! template-state assoc-in [:parts i :categories] categories))}]])]])

(defn part-creator-component []
  (let [showing-categories-list (atom false)]
    (fn [{:keys [title n categories]} i data specific-movements n]
      [:div {:style {:margin-top "40px"
                     :border-top "dotted 1px"}}
       [:h2 [:input {:type        "text"
                     :placeholder "Part title"
                     :on-change   #(swap! template-state assoc-in [:parts i :title] (-> % .-target .-value))
                     :value       (get-in @template-state [:parts i :title])}]]
       (category-input categories i showing-categories-list)
       (movement-input i n)
       [:div.pure-g.movements
        (for [m specific-movements]
          ^{:key (rand-int 10000)} (movement-component categories data (str m) (str "images/movements/" (str/replace (str/lower-case m) " " "-") ".png") m))
        (for [i (range n)]
          ^{:key (rand-int 10000)} (movement-component categories data "Random movement from categories" "images/movements/static-air-baby.png"))]
       (rep-set-distance-duration-component i)])))

(defn adjust-parts-component []
  [:div.pure-g.center {:style {:margin-top "40px"}}
   [:div.pure-u-1 {:style {:margin-right "5px"}} "The session is divided into "
    [:span {:style {:color "red" :font-size "24px"}} (count (:parts @template-state))] (if (= 1 (count (:parts @template-state))) " part" " parts")
    (when (< 0 (count (:parts @template-state)))
      [:button.button
       {:style    {:margin-left 5}
        :on-click #(when (> (count (:parts @template-state)) 0)
                    (swap! template-state update-in [:parts] pop))} [:i.fa.fa-minus]])
    [:button.button
     {:style {:margin-left 5}
      :on-click #(swap! template-state update-in [:parts]
                        conj {:title              ""
                              :categories         []
                              :n                  0
                              :specific-movements []})} [:i.fa.fa-plus]]]])

(defn save-template-component [error-atom]
  (let [template-stored-successfully? (atom false)]
    (fn []
      (if @template-stored-successfully?
        (let []
          (go (<! (timeout 3000))
              (reset! template-stored-successfully? false)
              (reset! template-state {:parts []}))
          [:div.pure-g
           [:div.pure-u-1.center {:style {:margin-top 15 :font-size 24 :color "green"}} "Template stored successfully!"]])
        [:div.pure-g
         [:div.pure-u.pure-u-md-1-5]
         [::div.pure-u-1.pure-u-md-3-5.button.button-primary
          {:on-click #(let [title (:title @template-state)
                            parts (:parts @template-state)]
                       (cond
                         (nil? title) (reset! error-atom "The template needs a title")
                         (empty? parts) (reset! error-atom "A session must have 1 or more parts")
                         (< 0 (count (filter (fn [p] (empty? (:categories p))) parts))) (reset! error-atom "All parts must have at least one category to draw new movements from")
                         (< 0 (count (filter (fn [p] (str/blank? (:title p))) parts))) (reset! error-atom "All parts must have a title")

                         :else (let []
                                 (when (empty? (:categories @template-state)) (swap! template-state dissoc :categories))
                                 (when (empty? (:specific-movements @template-state)) (swap! template-state dissoc :specific-movements))
                                 (POST "template"
                                       {:params        {:email     (session/get :user)
                                                        :template (assoc @template-state
                                                                    :public? true
                                                                    :created-by (session/get :username))}
                                        :handler       (fn [response] (do
                                                                        (reset! error-atom "")
                                                                        (reset! template-stored-successfully? true)
                                                                        (get-templates)))
                                        :error-handler (fn [response] (do (pr response)
                                                                          (reset! error-atom response)))}))))}
          "Save Template"]
         [:div.pure-u.pure-u-md-1-5]]))))

(defn template-creator-component []
  (let [error-atom (atom "")
        months {0 "January" 1 "February" 2 "March" 3 "April" 4 "May" 5 "June"
                6 "July" 7 "August" 8 "September" 9 "October" 10 "November" 11 "December"}
        date (js/Date.)
        day (.getDate date)
        month (get months (.getMonth date))]
    (fn []
      [:div {:style {:margin-top "40px"}}
       [:div.pure-g [:div.pure-u-1.center (str month " " day)]]
       (title template-state "Title")
       (description template-state)
       (let [parts (:parts @template-state)]
         (for [i (range 0 (count parts))]
           ^{:key i}
           (let [r (get-in @template-state [:parts i :rep])
                 s (get-in @template-state [:parts i :set])
                 di (get-in @template-state [:parts i :distance])
                 du (get-in @template-state [:parts i :duration])
                 we (get-in @template-state [:parts i :weight])
                 re (get-in @template-state [:parts i :rest])
                 data {:rep r :set s :distance di :duration du :weight we :rest re :i i}
                 specific-movements (get-in @template-state [:parts i :specific-movements])
                 n (get-in @template-state [:parts i :n])]
             [part-creator-component (get parts i) i data specific-movements n])))
       (adjust-parts-component)
       (error @error-atom)
       (let [usr (session/get :username)]
         (if (nil? usr)
           (username "template")
           [save-template-component error-atom]))])))