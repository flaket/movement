(ns movement.pages.discover
  (:require [reagent.core :refer [atom]]
            [movement.menu :refer [menu-component]]
            [movement.text :refer [text-edit-component]]
            [movement.pages.feed :refer [session-view]]))

(defn user-view []
  (let []
    (fn [{:keys [profile-image profile-name profile-text]
          :or   {profile-image "images/movements/static-air-baby.png"}}]
      [:div {:style {:border-bottom "1px solid lightgray"}}
       [:div.pure-g
        [:div.pure-u-1-6.center [:img {:src   profile-image :width "100px"
                                       :style {:cursor 'pointer}
                                       ; onClick/onTouchEnd -> show profile
                                       }]]
        [:div.pure-u-5-6
         [:div.pure-g [:h2 [:span.pure-u {:style {:cursor     'pointer
                                                  :margin-top 0}
                                          ; onClick/onTouchEnd -> show profile
                                          } profile-name]]]
         [:div.pure-g [:div.pure-u {:style {:margin-bottom 25}}]]]]])))

(defn discover-page []
  (let [selection (atom :user)
        user-search-results (atom [{:result 1
                               :profile-name "Kårinator"
                               :profile-image "images/movements/push-up.png"
                               :profile-text ""}
                                   {:result 2
                                    :profile-name "Bob"
                                    :profile-image "images/movements/arch-up.png"
                                    :profile-text "Hei jeg er Bob"}
                                   {:result 3
                                    :profile-name "Kari"
                                    :profile-image "images/movements/pull-up-reach.png"
                                    :profile-text "Kari4tw!"}])
        session-search-results (atom [{:result 2
                                    :user-name    "Kårinator"
                                    :user-image   "images/movements/pull-up.png"
                                    :url          "1"
                                    :text         "en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt en fin økt"
                                    :date         "3 timer siden"
                                    :time         "45:00"
                                    :activity     "Styrkeøkt"
                                    :session-data [[{:movement-name "Push Up" :rep 10 :set 3}
                                                    {:movement-name "Pull Up" :rep 5 :set 3}
                                                    {:movement-name "Push Up" :rep 10 :set 3}
                                                    {:movement-name "Pull Up" :rep 5 :set 3}]
                                                   [{:movement-name "Push Up" :rep 10 :set 3}
                                                    {:movement-name "Pull Up" :rep 5 :set 3}
                                                    {:movement-name "Push Up" :rep 10 :set 3}
                                                    {:movement-name "Pull Up" :rep 5 :set 3}]]
                                    :comments     [{:comment "Ser bra ut!" :user "Bobby"}
                                                   {:comment "Oi, dette skal jeg prøve!" :user "Kari"}]
                                    :likes        10
                                    :image        "images/field.jpg"}])]
    (fn []
      ;; Resultat fra søk på hashtagger sorteres med nyeste først.
      ;; Resultat fra søk på brukere sorteres etter en treffscore
      [:div
       [menu-component]
       #_[:div.content
        [:h1.pure-g
         [:a {:className (str "pure-u-1-2 pure-button" (when (= @selection :user) " pure-button-primary"))
                                       :onClick #(reset! selection :user)
                                       :onTouchEnd #(reset! selection :user)} "Brukere"]
         [:a {:className (str "pure-u-1-2 pure-button" (when (= @selection :session) " pure-button-primary"))
                                       :onClick #(reset! selection :session)
                                       :onTouchEnd #(reset! selection :session)} "Hashtagger"]]
        [:h1.pure-g
         [:div.pure-u-1
          [:input {:type "search" :name "search" :auto-focus true :placeholder ""
                   :size 60}]]]
        (case @selection
              :user (doall (for [result @user-search-results] ^{:key (:result result)}
                                                         [user-view result]))
              :session (doall (for [result @session-search-results] ^{:key (:result result)}
                                                            [session-view result])))]])))
