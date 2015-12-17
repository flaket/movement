(ns movement.template
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [reagent-forms.core :refer [bind-fields]]
            [cljs.core.async :as async :refer [timeout <!]]
            [secretary.core :include-macros true :refer [dispatch!]]
            [movement.menu :refer [menu-component]]
            [movement.util :refer [text-input POST get-templates]]
            [movement.text :refer [text-input-component auto-complete-did-mount]]
            [movement.state :refer [handler-fn]]
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
  ([data title image] (movement-component data title image nil))
  ([{:keys [rep set distance duration i]} title image m]
   [:div.pure-u.movement
    (when-not (nil? m)
      [:div.pure-g
       [:div.pure-u.destroy
        [:i.fa.fa-remove {:on-click #(let [specific-movements (vec (remove #{m} (get-in @template-state [:parts i :specific-movements])))]
                                      (swap! template-state assoc-in [:parts i :specific-movements] specific-movements))
                          :title "Remove movement"}]]])
    [:h3.pure-g
     [:div.pure-u-1-12]
     [:div.pure-u.title title]]
    [:img.graphic.pure-img-responsive {:src image}]
    [:div
     [:div.pure-g
      [:div.pure-u-1-12]
      [:div.pure-u-5-12 {:className (str (when-not (and rep (< 0 rep)) " no-data"))}
       [:div.pure-u "Reps"]]
      [:div.pure-u-5-12 {:className (str (when-not (and set (< 0 set)) " no-data"))}
       [:div.pure-u "Set"]]
      [:div.pure-u-1-12]]
     [:div.pure-g
      [:div.pure-u-1-12]
      [:div.pure-u-5-12
       (when (and rep (< 0 rep))
         [:div.rep-set rep])]
      [:div.pure-u-5-12
       (when (and set (< 0 set))
         [:div.rep-set set])]
      [:div.pure-u-1-12]]]
    [:div
     [:div.pure-g
      [:div.pure-u-1-12]
      [:div.pure-u-5-12 {:className (str (when-not (and distance (< 0 distance)) " no-data"))}
       [:div "Meters"]]
      [:div.pure-u-5-12 {:className (str (when-not (and duration (< 0 duration)) " no-data"))}
       [:div "Seconds"]]
      [:div.pure-u-1-12]]
     [:div.pure-g
      [:div.pure-u-1-12]
      [:div.pure-u-5-12
       (when (and distance (< 0 distance))
         [:div.rep-set distance])]
      [:div.pure-u-5-12
       (when (and duration (< 0 duration))
         [:div.rep-set duration])]
      [:div.pure-u-1-12]]]]))

(defn rep-set-distance-duration-component [i]
  [:div
   [:div.pure-g
    [:label.pure-u "These movements should be done for: "]]
   [:div.pure-g
    [:div.pure-u [:input {:type        "text"
                          :size 10
                          :value       (get-in @template-state [:parts i :rep])
                          :on-change   #(try
                                         (let [value (-> % .-target .-value read-string)]
                                           (if (or (nil? value) (and (integer? value) (< 0 value)))
                                             (swap! template-state assoc-in [:parts i :rep] value)))
                                         (catch js/Error e
                                           (print (str "Caught exception: " e))))}]]

    [:div.pure-u "reps"]]
   [:div.pure-g
    [:div.pure-u [:input {:type        "text"
                          :size 10
                          :value       (get-in @template-state [:parts i :set])
                          :on-change   #(try
                                         (let [value (-> % .-target .-value read-string)]
                                           (if (or (nil? value) (and (integer? value) (< 0 value)))
                                             (swap! template-state assoc-in [:parts i :set] value)))
                                         (catch js/Error e
                                           (print (str "Caught exception: " e))))}]]
    [:div.pure-u "sets"]]
   [:div.pure-g
    [:div.pure-u [:input {:type        "text"
                          :value       (get-in @template-state [:parts i :distance])
                          :size 10
                          :on-change   #(try
                                         (let [value (-> % .-target .-value read-string)]
                                           (if (or (nil? value) (and (integer? value) (< 0 value)))
                                             (swap! template-state assoc-in [:parts i :distance] value)))
                                         (catch js/Error e
                                           (print (str "Caught exception: " e))))}]]
    [:div.pure-u "meters"]]
   [:div.pure-g
    [:div.pure-u [:input {:type        "text"
                          :value       (get-in @template-state [:parts i :duration])
                          :size 10
                          :on-change   #(try
                                         (let [value (-> % .-target .-value read-string)]
                                           (if (or (nil? value) (and (integer? value) (< 0 value)))
                                             (swap! template-state assoc-in [:parts i :duration] value)))
                                         (catch js/Error e
                                           (print (str "Caught exception: " e))))}]]
    [:div.pure-u "seconds"]]])

(defn part-creator-component []
  (let [showing-categories-list (atom false)]
    (fn [{:keys [title n categories]} i data specific-movements n]
      [:div {:style {:margin-top "10px"
                     :border-top "dotted 1px"}}
       [:div.pure-g
        [:h2.pure-u [:input {:type        "text"
                             :placeholder "Part title"
                             :on-change   #(swap! template-state assoc-in [:parts i :title] (-> % .-target .-value))
                             :value       (get-in @template-state [:parts i :title])}]]]

       [:div.pure-g.movements
        (for [m specific-movements]
          ^{:key (rand-int 10000)} (movement-component data
                                                       (str m) (str "images/" (str/replace (str/lower-case m) " " "-") ".png") m))]
       [:div.pure-g.movements
        (for [i (range n)]
          ^{:key (rand-int 10000)} (movement-component data "movement name" "images/push-up.png"))]
       [:div.pure-g
        [:div.pure-u "Movements generated in this part should be drawn from the following categories: "]]
       [:div.pure-g
        (for [c categories]
          ^{:key (str c (rand-int 1000))} [:div.pure-u
                                           [:div {:style {:margin-right "5px"}} c [:div {:style    {:cursor 'pointer}
                                                                                         :on-click #(let [categories (vec (remove #{c} (get-in @template-state [:parts i :categories])))]
                                                                                                     (swap! template-state assoc-in [:parts i :categories] categories))} [:i.fa.fa-times]]]])]
       [:div.pure-g
        (let [id (str "ctags" i)
              categories-ac-comp (with-meta text-input-component
                                            {:component-did-mount #(auto-complete-did-mount
                                                                    (str "#" id)
                                                                    (vec (session/get :all-categories)))})]
          [:div.pure-u
           [categories-ac-comp {:id          id
                                :class       "edit"
                                :placeholder "type to find and add category.."
                                :size        30
                                :on-save     #(when (and (some #{%} (session/get :all-categories))
                                                         (not (some #{%} (get-in @template-state [:parts i :categories]))))
                                               (swap! template-state update-in [:parts i :categories] conj %))}]])]
       [:div.pure-g
        [:div.button.pure-u {:on-click #(handler-fn (reset! showing-categories-list (not @showing-categories-list)))}
         (if @showing-categories-list "Hide list of categories" "Show list of categories")]]

       [:div.pure-g
        [:div.pure-u
         "This part has " [:span {:style {:color "red" :font-size "24px"}} n]
         " generated " (if (= n 1) "movement" "movements") " from the above categories"]
        [:div.button.pure-u
         {:on-click #(when (> n 0)
                      (swap! template-state update-in [:parts i :n] dec))} [:i.fa.fa-minus]]
        [:div.button.pure-u
         {:on-click #(swap! template-state update-in [:parts i :n] inc)} [:i.fa.fa-plus]]]

       (when @showing-categories-list
         (for [c (sort (session/get :all-categories))]
           ^{:key c} [:div.pure-g
                      [:div.pure-u {:style    {:cursor     'pointer
                                               :background (when (some #{c} (get-in @template-state [:parts i :categories]))
                                                             "yellow")}
                                    :on-click #(when (not (some #{c} (get-in @template-state [:parts i :categories])))
                                                (swap! template-state update-in [:parts i :categories] conj c))} c]]))

       [:div.pure-g
        [:label.pure-u "Additionally, the following exercises should always be included:"]]
       [:div.pure-g
        [:div.pure-u
         (let [id (str "mtags" i)
               movements-ac-comp (with-meta text-input-component
                                            {:component-did-mount #(auto-complete-did-mount
                                                                    (str "#" id)
                                                                    (vec (session/get :all-movements)))})]
           [movements-ac-comp {:id          id
                               :class       "edit"
                               :placeholder "type to find and add movement.."
                               :size        30
                               :on-save     #(when (and (some #{%} (session/get :all-movements))
                                                        (not (some #{%} (get-in @template-state [:parts i :specific-movements]))))
                                              (swap! template-state update-in [:parts i :specific-movements] conj %))}])]]
       (rep-set-distance-duration-component i)])))

(defn description-component []
  [:div.pure-g
   [:div.pure-u
    [:textarea {:rows        3
                :cols        58
                :placeholder "Optional description, e.g. outlining how to perform certain parts of the session, or which weights to use."
                :on-change   #(swap! template-state assoc :description (-> % .-target .-value))
                :value       (:description @template-state)}]]])

(defn title-component []
  [:div.pure-g
   [:h1.pure-u.pure-u-md-2-5
    [:input {:type        "text"
             :placeholder "Template Title"
             :on-change   #(swap! template-state assoc :title (-> % .-target .-value))
             :value       (:title @template-state)}]]])

(defn heading-component []
  [:div.pure-g
   [:h2.pure-u "Create a new Template"]])

(defn error-component [error-atom]
  [:div.pure-g
   [:h3.pure-u {:style {:color "red"}} @error-atom]])

(defn adjust-parts-component []
  [:div.pure-g {:style {:margin-top "10px"}}
   [:div.pure-u {:style {:margin-right "5px"}} "The session is divided into "
    [:span {:style {:color "red" :font-size "24px"}} (count (:parts @template-state))] (if (= 1 (count (:parts @template-state))) " part" " parts")]
   [:button.pure-u
    {:on-click #(when (> (count (:parts @template-state)) 0)
                 (swap! template-state update-in [:parts] pop))} [:i.fa.fa-minus]]
   [:button.pure-u
    {:on-click #(swap! template-state update-in [:parts]
                       conj {:title              ""
                             :categories         []
                             :n                  0
                             :specific-movements []})} [:i.fa.fa-plus]]])

(defn save-template-component [error-atom]
  (let [template-stored-successfully? (atom false)]
    (fn []
      (if @template-stored-successfully?
        (let []
          (go (<! (timeout 3000))
              (reset! template-stored-successfully? false)
              (reset! template-state {:parts []}))
          [:div.pure-g
           [:div.pure-u {:style {:margin-top 15 :font-size 24 :color "green"}} "Template stored successfully!"]])
        [:div.pure-g
         [:p.pure-u.pure-u-md-2-5.button.button-primary
          {:on-click #(let [title (:title @template-state)
                            parts (:parts @template-state)]
                       (cond
                         (nil? title) (reset! error-atom "The template needs a title.")
                         (empty? parts) (reset! error-atom "A session must have 1 or more parts.")
                         (< 0 (count (filter (fn [p] (str/blank? (:title p))) parts))) (reset! error-atom "All parts must have a title.")
                         :else (let []
                                 (when (empty? (:categories @template-state)) (swap! template-state dissoc :categories))
                                 (when (empty? (:specific-movements @template-state)) (swap! template-state dissoc :specific-movements))
                                 (POST "template"
                                       {:params        {:user     (session/get :user)
                                                        :template @template-state}
                                        :handler       (fn [response] (do
                                                                        (reset! error-atom "")
                                                                        (reset! template-stored-successfully? true)
                                                                        (get-templates)))
                                        :error-handler (fn [response] (do (print response)
                                                                          (reset! error-atom response)))}))))}
          "Save Template"]]))))

(defn template-creator-component []
  (let [error (atom "")]
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content {:style {:margin-top "20px"}}
        (heading-component)
        (error-component error)
        (title-component)
        (description-component)
        (adjust-parts-component)
        (let [parts (:parts @template-state)]
          (for [i (range 0 (count parts))]
            ^{:key (rand-int 10000)}
            (let [r (get-in @template-state [:parts i :rep])
                  s (get-in @template-state [:parts i :set])
                  di (get-in @template-state [:parts i :distance])
                  du (get-in @template-state [:parts i :duration])
                  data {:rep r :set s :distance di :duration du :i i}
                  specific-movements (get-in @template-state [:parts i :specific-movements])
                  n (get-in @template-state [:parts i :n])]
              [part-creator-component (get parts i) i data specific-movements n])))
        [save-template-component error]]])))