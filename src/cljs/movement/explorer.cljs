(ns movement.explorer
  (:require
    [reagent.core :refer [atom]]
    [reagent.session :as session]
    [movement.menu :refer [menu-component]]
    [movement.text :refer [text-input-component auto-complete-did-mount]]
    [movement.util :refer [GET]]))

(defn movement-image [equipment-name]
  (first (shuffle ["images/squat.png"
                   "images/push-up.png"
                   "images/high-bridge.png"
                   "images/frog-stand.png"
                   "images/broad-jump.png"
                   "images/elastic-band-overhead-pull-down.png"
                   "images/pull-up-reach.png"
                   "images/pull-up.png"
                   "images/stepping-over.png"
                   "images/side-swing.png"
                   "images/front-leg-swing.png"
                   "images/burpee.png"
                   "images/sit-up.png"
                   "images/sit-up-pike.png"
                   "images/jump-rope.png"
                   "images/pistol-single-leg-squat.png"
                   "images/hollow-body-rock.png"
                   "images/handstand-walk.png"
                   "images/handstand-push-up-kip.png"
                   "images/dip.png"
                   "images/russian-dip.png"
                   "images/kick-to-handstand.png"
                   "images/lying-roll.png"
                   "images/ring-l-sit.png"
                   "images/side-leg-swing.png"])))



(defn movement-component []
  (let []
    (fn [{:keys [id category graphic animation equipment] :as m}]
      (let [name (:movement/name m)
            graphic (movement-image "")
            description (:movement/category m)]
        [:div.pure-u.movement {:id        (str "m-")
                               :className ""}
         [:div.pure-g
          [:h3.pure-u.title name]]
         [:div.pure-g
          [:img.pure-u.graphic.pure-img-responsive {:src graphic :title name
                                                    :alt name}]]
         [:div.pure-g
          [:p.pure-u.pure-u-md-1-1.description description]]]))))

(defn explorer-component []
  (let [state (atom {})]
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content
        [:h1 "Movement Explorer"]
        [:div
         (let [id (str "tags")
               categories-ac-comp (with-meta text-input-component
                                             {:component-did-mount #(auto-complete-did-mount (str "#" id) (vec (session/get :all-movements)))})]
           [categories-ac-comp {:id      id
                                :class   "edit" :placeholder "Search for movement.."
                                :on-save (fn [m] (when (some #{m} (session/get :all-movements))
                                                   (GET "singlemovement"
                                                        {:params        {:categories ["Strength"]}
                                                         :handler       #(reset! state (first %))
                                                         :error-handler #(print "error getting single movement through add.")})))}])]

        [movement-component @state]
        [:div.content
         "This section allow users to discover the movements in the database
         view the animations more clearly, add their own comments to the movements
         and mark the subjective difficulty level of the movements."]]])))