(ns movement.generator
  (:import [goog.events EventType])
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [reagent.session :as session]
    [reagent.core :refer [atom]]
    [goog.events :as events]
    [clojure.string :as str]
    [movement.util :refer [GET POST get-stored-sessions]]
    [movement.text :refer [text-edit-component]]
    [movement.menu :refer [menu-component]]
    [movement.state :refer [movement-session handler-fn log-session]]))

(defonce m-counter (atom 0))

(defn image-url [name]
  (str "images/" (str/replace (str/lower-case name) " " "-") ".png"))

(defn positions
  "Finds the integer positions of the elements in the collection, that matches the predicate."
  [pred coll]
  (keep-indexed (fn [idx x]
                  (when (pred x) idx)) coll))

(defn add-movement [part-title]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        categories (:categories (first (filter #(= part-title (:title %)) parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])]
    (GET "singlemovement"
         {:params        {:categories categories}
          :handler       #(let [id (swap! m-counter inc)
                                new-movement (first %)
                                new-movement (assoc new-movement :id id)
                                new-movements (assoc movements id new-movement)]
                           (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
          :error-handler #(print "error getting single movement through add.")})))

(defn add-movement-from-search [part-title movement-name]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])]
    (GET "movement/"
         {:params         {:name movement-name}
          :handler       #(let [id (swap! m-counter inc)
                                new-movement (first %)
                                new-movement (assoc new-movement :id id)
                                new-movements (assoc movements id new-movement)]
                           (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
          :error-handler #(print "error getting single movement through add.")})))

(defn refresh-movement [m part-title]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        categories (:categories (first (filter #(= part-title (:title %)) parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])]
    (GET "singlemovement"
         {:params        {:categories categories}
          :handler       #(let [id (:id m)
                                new-movement (first %)
                                new-movement (assoc new-movement :id id)
                                new-movements (assoc movements id new-movement)]
                           (session/assoc-in! [:movement-session :parts position-in-parts :movements] new-movements))
          :error-handler #(print "error getting single movement through refresh.")})))

(defn remove-movement [m part-title]
  (let [parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{part-title} (map :title parts)))
        movements (session/get-in [:movement-session :parts position-in-parts :movements])
        movements (dissoc movements (:id m))]
    (session/assoc-in! [:movement-session :parts position-in-parts :movements] movements)))

(defn list-to-sorted-map [list-of-movements]
  (let [movements (atom (sorted-map))]
    (doseq [m list-of-movements
            :let [id (swap! m-counter inc)]]
      (swap! movements assoc id (assoc m :id id)))
    @movements))

(defn add-session-handler [session]
  (let [new-parts (mapv #(assoc % :movements (list-to-sorted-map (:movements %)))
                        (:parts session))
        new-session (assoc session :parts new-parts)
        new-session (assoc new-session :comment [])
        ]
    (session/put! :movement-session new-session)))

(defn create-session-from-template [template-name]
  (GET "template"
       {:params        {:template-name template-name
                        :user (session/get :user)}
        :handler       add-session-handler
        :error-handler (fn [] (print "error getting session data from server."))}))

(defn pick-random-template []
  (let [name (first (shuffle (session/get :templates)))]
    (create-session-from-template name)))

;;;;;; Components ;;;;;;

(defn movement-component [{:keys [id category distance rep set duration] :as m} {:keys [title]}]
  (let [name (:movement/name m)
        graphic (image-url name)
        parts (session/get-in [:movement-session :parts])
        position-in-parts (first (positions #{title} (map :title parts)))]
    (fn []
      [:div.pure-u.movement {:id (str "m-" id)}
       [:div.pure-g
        [:div.pure-u.refresh {:on-click #(refresh-movement m title) :title "Swap with another movement"}]
        [:div.pure-u.destroy {:on-click #(remove-movement m title) :title "Remove movement"}]
        [:h3.pure-u.title name]]
       [:img.graphic.pure-img-responsive {:src graphic :title name :alt name}]
       [:div.pure-g
        [:div.pure-u rep]
        [:div.pure-u "reps"]
        [:div.pure-u
         {:on-click #(session/update-in!
                      [:movement-session :parts position-in-parts :movements id :rep] inc)} "+"]
        [:div.pure-u
         {:on-click #(session/update-in!
                      [:movement-session :parts position-in-parts :movements id :rep] dec)} "-"]]
       [:div.pure-g
        [:div.pure-u set]
        [:div.pure-u "set"]
        [:div.pure-u
         {:on-click #(session/update-in!
                      [:movement-session :parts position-in-parts :movements id :set] inc)} "+"]
        [:div.pure-u
         {:on-click #(session/update-in!
                      [:movement-session :parts position-in-parts :movements id :set] dec)} "-"]]
       [:div.pure-g
        [:div.pure-u distance]
        [:div.pure-u "meters"]
        [:div.pure-u
         {:on-click #(session/update-in!
                      [:movement-session :parts position-in-parts :movements id :distance] (fn [e] (+ e 5)))} "+"]
        [:div.pure-u
         {:on-click #(session/update-in!
                      [:movement-session :parts position-in-parts :movements id :distance] (fn [e] (- e 5)))} "-"]]
       [:div.pure-g
        [:div.pure-u duration]
        [:div.pure-u "seconds"]
        [:div.pure-u
         {:on-click #(session/update-in!
                      [:movement-session :parts position-in-parts :movements id :duration] (fn [e] (+ e 10)))} "+"]
        [:div.pure-u
         {:on-click #(session/update-in!
                      [:movement-session :parts position-in-parts :movements id :duration] (fn [e] (- e 10)))} "-"]]])))

(defn part-component []
  (let [show-search-input (atom false)]
    (fn [{:keys [title movements] :as part}]
      [:div.part
       [:h2 title]
       [:div.pure-g
        [:p.pure-u-1-2 [:a.secondary-button {:on-click #(add-movement title)} "+"]]]
       [:div.pure-g
        (doall
          (for [m (vals movements)]
            ^{:key (str m (rand-int 100000))} [movement-component m part]))]])))

(defn header-component []
  (let [date (js/Date.)
        day (.getDate date)
        month (.getMonth date)]
    (fn [{:keys [title description]}]
      [:div
       [:div.pure-g
        [:div.pure-u.pure-u-md-1-5 (str day "/" month)]
        [:h1.pure-u.pure-u-md-3-5 title]]
       [:p.subtitle description]])))


(defn template-component []
  (let []
    (fn [name]
      [:a.pure-u.secondary-button {:on-click #(create-session-from-template name)} name])))

(defn blank-state-component []
  (let [templates-showing? (atom false)]
    (fn []
      [:div.blank-state
       [:div.pure-g
        [:h1.pure-u "Let's create your next Movement Session"]]
       [:div.pure-g
        [:h3.pure-u [:a.secondary-button {:on-click pick-random-template} "Get inspired by a random movement session."]]]
       [:div.pure-g
        [:h3.pure-u [:a.secondary-button {:on-click #(reset! templates-showing? true)} "Or generate a new session based on one of your templates"]]]
       [:div.pure-g
        (when @templates-showing?
          (doall
            (for [t (session/get :templates)]
              ^{:key t} [template-component t])))]])))

(defn top-menu-component []
  (let [templates-showing? (atom false)]
    (fn []
      [:div
       [:div.pure-g
        [:h3.pure-u.pure-u-md-1-4
         [:div.pure-g
          [:a.pure-u.secondary-button {:on-click pick-random-template} "Random session"]]]
        [:h3.pure-u.pure-u-md-1-4
         [:div.pure-g
          [:a.pure-u.secondary-button {:on-click #(handler-fn (reset! templates-showing? (not @templates-showing?)))} "Select template"]]]]
       [:div.pure-g
        (when @templates-showing?
          (doall
            (for [t (session/get :templates)]
              ^{:key t} [template-component t])))]])))

(defn comment-component []
  (let [adding-comment (atom false)]
    (fn [comments]
      [:div
       [:div.pure-g
        [:p.pure-u [:a.secondary-button {:on-click #(handler-fn (reset! adding-comment true))} "Add comments"]]]
       [:div.pure-g
        (when @adding-comment [text-edit-component {:class   "pure-u edit"
                                                    :on-save #(handler-fn (session/update-in! [:movement-session :comment] conj %))
                                                    :on-stop #(handler-fn (reset! adding-comment false))
                                                    :size    45}])]
       [:div.pure-u
        (for [c comments]
          ^{:key c} [:div.comment
                     [:p (str c)]])]])))

(defn finish-session-component []
  (let []
    (fn []
      [:div.pure-g
       [:h3.pure-u [:div.pure-g [:a.pure-u.log-button {:on-click #(do
                                                                   (POST "store-session"
                                                                         {:params        {:session (session/get :movement-session)
                                                                                          :user    (session/get :user)}
                                                                          :handler       (fn [response] (get-stored-sessions))
                                                                          :error-handler (fn [response] (print response))}))}
                                 "Finish movement session"]]]])))

(defn generator-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content
        (if-let [session (session/get :movement-session)]
          [:div {:style {:background-image (str "url(" (:background session) ")")}}
           [top-menu-component]
           [header-component session]
           (doall
             (for [p (:parts session)]
               ^{:key p} [part-component p]))
           [comment-component (:comment session)]
           [finish-session-component]]
          [blank-state-component])]])))