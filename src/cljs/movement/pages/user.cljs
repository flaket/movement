(ns movement.pages.user
  (:import [goog.date DateTime])
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [secretary.core :include-macros true :refer [dispatch!]]
            [movement.menu :refer [menu-component]]
            [movement.pages.feed :refer [load-feed load-user-only-feed session-view]]
            [movement.util :refer [POST text-input get-user-info]]))

(defn log-out [event]
  (.preventDefault event)
  (session/clear!)
  (dispatch! "/"))

(defn vec-remove
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(defn positions
  "Finds the integer positions of the elements in the collection, that matches the predicate."
  [pred coll]
  (keep-indexed (fn [idx x] (when (pred x) idx)) coll))

(defn remove-viewing-user [e]
  (.preventDefault e)
  (session/remove! :viewing-user)
  (session/put! :selected-menu-item :feed)
  (dispatch! "/feed"))

(defn unfollow-user [e user-id follow-id]
  (.preventDefault e)
  (POST "unfollow" {:params        {:user-id   user-id
                                    :follow-id follow-id}
                    :handler       (fn [r]
                                     (let [follows (:follows (session/get :user))
                                           pos (first (positions #{follow-id} follows))
                                           follows (vec-remove follows pos)]
                                       (session/assoc-in! [:user :follows] follows)))
                    :error-handler #(pr "error unfollowing")}))

(defn follow-user [e user-id follow-id]
  (.preventDefault e)
  (POST "follow" {:params {:user-id user-id
                           :follow-id follow-id}
                 :handler (fn [r]
                            (session/update-in! [:user :follows] conj follow-id))
                 :error-handler #(pr "error following")}))

(defn another-users-page [{:keys [name profile-text location sign-up-timestamp user-image badges user-id] :as viewing-user}]
  (let [selection (atom nil)]
    (fn []
      [:div.content
       [:div
        [:div.pure-g {:style {:border-bottom "1px solid lightgray"}}
         [:div.pure-u.pure-u-md-1-3
          [:div.pure-g
           [:div.pure-u-1
            [:img
             {:style {:padding "20px 20px 20px 20px"
                      :border-radius "50% 50% 50% 50%"}
              :width 275
              :height 275
              :src user-image}]]]]
         [:div.pure-u.pure-u-md-1-3
          [:div.pure-g {:style {:margin-top 10}}
           [:h2.pure-u-1 name]]
          [:div.pure-g {:style {:margin-top 10}}
           [:div.pure-u-1 profile-text]]
          [:div.pure-g {:style {:margin-top 10}}
           [:div.pure-u-1 location]]
          [:div.pure-g {:style {:margin-top 10}}
           [:div.pure-u-1 {:style {:font-size "80%" :opacity 0.5}} "Medlem siden " sign-up-timestamp]]
          [:div.pure-g {:style {:margin-top 10}}
           (doall
             (for [b badges]
               ^{:key b}
               [:div.pure-u {:style {:margin-right 5}} [:b b]]))]]
         [:div.pure-u.pure-u-md-1-3
          (if (get (set (:follows (session/get :user))) user-id)
            [:div.pure-g {:style {:margin-top 10}}
             [:a.pure-u-1.pure-button.button-success {:onClick    #(unfollow-user % (:user-id (session/get :user)) user-id)
                                                      :onTouchEnd #(unfollow-user % (:user-id (session/get :user)) user-id)} "Følger"]]
            [:div.pure-g {:style {:margin-top 10}}
             [:a.pure-u-1.pure-button.pure-button-primary {:onClick    #(follow-user % (:user-id (session/get :user)) user-id)
                                                           :onTouchEnd #(follow-user % (:user-id (session/get :user)) user-id)} "Følg"]])
          ]]]
       [:div
        [:div.pure-g [:div.pure-u-1 [:h2 (str name " sin treningsdagbok")]]]
        [:div.pure-g
         [:a.pure-u.pure-u-md-1-4
          {:onClick   (fn [] (load-user-only-feed user-id) (reset! selection :feed)) :onTouchEnd (fn [] (load-user-only-feed user-id) (reset! selection :feed))
           :className (str " pure-button" (when (= @selection :feed) " pure-button-primary"))} "Feed"]
         [:a.pure-u.pure-u-md-1-4
          {:style     {:opacity 0.25 :pointer-events 'none :cursor 'default}
           ;:onClick #(reset! selection :calendar) :onTouchEnd #(reset! selection :calendar)
           :className (str " pure-button" (when (= @selection :calendar) " pure-button-primary"))} "Kalender"]
         [:a.pure-u.pure-u-md-1-4
          {:style     {:opacity 0.25 :pointer-events 'none :cursor 'default}
           ;:onClick #(reset! selection :stat) :onTouchEnd #(reset! selection :stat)
           :className (str " pure-button" (when (= @selection :stat) " pure-button-primary"))} "Statistikk"]
         [:a.pure-u.pure-u-md-1-4
          {:style     {:opacity 0.25 :pointer-events 'none :cursor 'default}
           ;:onClick   #(reset! selection :tag) :onTouchEnd #(reset! selection :tag)
           :className (str " pure-button" (when (= @selection :tag) " pure-button-primary"))} "Mine hashtagger"]]]
       (case @selection
         :feed [:div
                (if-let [sessions (session/get :user-only-feed)]
                  [:div {:style {:margin-top 40}}
                   (doall
                     (for [session sessions]
                       ^{:key (:url session)}
                       [session-view session]))]
                  [:div.pure-g
                   [:div.pure-u-1.center
                    [:i.fa.fa-spinner.fa-pulse.fa-4x]]])]
         :calendar [:div "cal"]
         :stat [:div "stat"]
         :tag [:div "tag"]
         [:div])])))

(defn my-user-page []
  (let [local-state (atom :profile)
        pass (atom {:info "" :error ""})
        selection (atom nil)]
    (fn [{:keys [user-id name sign-up-timestamp badges user-image profile-text location] :as user} local-state]
      [:div.content
       [:div
        [:div.pure-g
         [:div.pure-u.pure-u-md-1-3
          [:div.pure-g
           [:div.pure-u-1
            [:img
             {:style {:padding "20px 20px 20px 20px"
                      :border-radius "50% 50% 50% 50%"}
              :width 275
              :height 275
              :src user-image}]]]]
         [:div.pure-u.pure-u-md-1-3
          [:div.pure-g {:style {:margin-top 10}}
           [:h2.pure-u-1 name]]
          [:div.pure-g {:style {:margin-top 10}}
           [:div.pure-u-1 profile-text]]
          [:div.pure-g {:style {:margin-top 10}}
           [:div.pure-u-1 location]]
          [:div.pure-g {:style {:margin-top 10}}
           [:div.pure-u-1 {:style {:font-size "80%" :opacity 0.5}} "Medlem siden " sign-up-timestamp]]
          [:div.pure-g {:style {:margin-top 10}}
           (doall
             (for [b badges]
               ^{:key b}
               [:div.pure-u {:style {:margin-right 5}} [:b b]]))]]
         [:div.pure-u.pure-u-md-1-3
          [:div.pure-g {:style {:margin-top 10}}
           [:a.pure-u-1.pure-button {:onClick    #(reset! local-state :change-profile)
                                     :onTouchEnd #(reset! local-state :change-profile)} "Endre profil"]]
          [:div.pure-g {:style {:margin-top 10}}
           [:a.pure-u-1.pure-button {:onClick    #(reset! local-state :change-profile)
                                     :onTouchEnd #(reset! local-state :change-profile)} "Endre innstillinger"]]
          [:div.pure-g {:style {:margin-top 10}}
           [:a.pure-u-1.pure-button {:onClick    #(reset! local-state :change-password)
                                     :onTouchEnd #(reset! local-state :change-password)} "Endre passord"]]
          [:div.pure-g {:style {:margin-top 10}}
           [:a.pure-u-1.pure-button {:onClick    #(log-out %)
                                     :onTouchEnd #(log-out %)} "Logg ut"]]]]]

       #_[:div {:style {:border-bottom "1px solid lightgray"}}

          [:div.pure-g
           [:div.pure-u-1-4 {:onClick #(reset! local-state :profile) :onTouchEnd #(reset! local-state :profile)}
            [:img {:width "100%" :src "images/movements/arch-up.png"}]]

           (case @local-state

             ; View for å endre profil/innstillinger.
             :change-profile
             [:div.pure-u-3-4

              [:div {:style {:margin-bottom 10}}
               [:div.pure-g [:div.pure-u-1 "Epost"]]
               [:div.pure-g [:div.pure-u-1 [:input {:id "change-email" :size 100 :type "text" :defaultValue (:email user)}]]]]

              [:div {:style {:margin-bottom 10}}
               [:div.pure-g [:div.pure-u-1 "Navn"]]
               [:div.pure-g [:div.pure-u-1 [:input {:id "change-name" :size 100 :type "text" :defaultValue (:name user)}]]]]

              [:div {:style {:margin-bottom 10}}
               [:div.pure-g [:div.pure-u-1 "Profiltekst"]]
               [:div.pure-g [:div.pure-u-1
                             [:textarea {:id           "change-text"
                                         :rows         2 :cols 100
                                         :style        {:resize 'vertical}
                                         ;:on-change #(session/assoc-in! [:movement-session :comment] (-> % .-target .-value))
                                         :defaultValue (:profile-text user)}]]]]

              [:div.pure-g [:a.pure-u-1.pure-button "Velg profilbilde"]]

              [:div.pure-g
               [:a.pure-u-1.pure-button.pure-button-primary
                {:onClick    #(reset! local-state :profile)
                 :onTouchEnd #(reset! local-state :profile)} "Lagre"]]]

             ; View for å se sin egen profil med knapper for å gjøre endringer og å logge ut.
             :profile
             [:div.pure-u-3-4 {:style {:position 'relative}}]

             :change-password
             [:div.pure-u-3-4 {:style {:position 'relative}}
              [:div.pure-g [:p.pure-u-1 "Endre passord"]]
              [:div
               [:div.pure-g
                [:input.pure-u.pure-u-md-1-2 {:type        "password"
                                              :placeholder "forrige passord"
                                              :value       (:old-pass @pass)
                                              :on-change   #(swap! pass assoc :old-pass (-> % .-target .-value))}]]
               [:div.pure-g
                [:input.pure-u.pure-u-md-1-2 {:type        "password"
                                              :placeholder "nytt passord"
                                              :value       (:new-pass @pass)
                                              :on-change   #(swap! pass assoc :new-pass (-> % .-target .-value))}]]

               [:div.pure-g
                [:input.pure-u.pure-u-md-1-2 {:type        "password"
                                              :placeholder "nytt passord igjen"
                                              :value       (:repeat-pass @pass)
                                              :on-change   #(swap! pass assoc :repeat-pass (-> % .-target .-value))}]]
               (when-let [info (:info @pass)]
                 [:div.pure-g [:div.pure-u {:style {:color 'green :font-size 24}} info]])
               (when-let [error (:error @pass)]
                 [:div.pure-g [:div.pure-u {:style {:color 'red :font-size 24}} error]])
               (if (not= (:new-pass @pass) (:repeat-pass @pass))
                 [:div.pure-g [:div.pure-u "passordene stemmer ikke"]]
                 (when (and (not-empty (:old-pass @pass)) (not-empty (:new-pass @pass)))
                   [:div.pure-g
                    [:a.pure-u.pure-u-md-1-2.pure-button.pure-button-primary
                     {:on-click #(POST "change-password"
                                       {:params        {:username     (session/get :user)
                                                        :password     (:old-pass @pass)
                                                        :new-password (:new-pass @pass)}
                                        :handler       (fn [response]
                                                         (reset! pass {:error "" :info response}))
                                        :error-handler (fn [response]
                                                         (reset! pass {:error (:response response) :info ""}))})}
                     "Endre passordet"]]))]])]

          [:div.pure-g
           [:div.pure-u-1-4.center {:style {:font-size "80%" :opacity 0.5}} "Medlem siden " (:sign-up-timestamp user)]
           [:div.pure-u-3-4
            [:div.pure-g {:style {:margin-top 40}}
             [:a.pure-u-1-4.pure-button {:onClick    #(reset! local-state :change-profile)
                                         :onTouchEnd #(reset! local-state :change-profile)} "Innstillinger"]
             [:a.pure-u-1-4.pure-button {:onClick    #(reset! local-state :change-password)
                                         :onTouchEnd #(reset! local-state :change-password)} "Passord"]
             [:div.pure-u-1-4]
             [:a.pure-u-1-4.pure-button {:onClick    #(log-out %)
                                         :onTouchEnd #(log-out %)} "Logg ut"]]]]]

       [:div
        [:div.pure-g [:div.pure-u-1 [:h2 "Min treningsdagbok"]]]
        [:div.pure-g
         [:a.pure-u.pure-u-md-1-4
          {:onClick   (fn [] (load-user-only-feed user-id) (reset! selection :feed)) :onTouchEnd (fn [] (load-user-only-feed user-id) (reset! selection :feed))
           :className (str " pure-button" (when (= @selection :feed) " pure-button-primary"))} "Feed"]
         [:a.pure-u.pure-u-md-1-4
          {:style     {:opacity 0.25 :pointer-events 'none :cursor 'default}
           ;:onClick #(reset! selection :calendar) :onTouchEnd #(reset! selection :calendar)
           :className (str " pure-button" (when (= @selection :calendar) " pure-button-primary"))} "Kalender"]
         [:a.pure-u.pure-u-md-1-4
          {:style     {:opacity 0.25 :pointer-events 'none :cursor 'default}
           ;:onClick #(reset! selection :stat) :onTouchEnd #(reset! selection :stat)
           :className (str " pure-button" (when (= @selection :stat) " pure-button-primary"))} "Statistikk"]
         [:a.pure-u.pure-u-md-1-4
          {:style     {:opacity 0.25 :pointer-events 'none :cursor 'default}
           ;:onClick   #(reset! selection :tag) :onTouchEnd #(reset! selection :tag)
           :className (str " pure-button" (when (= @selection :tag) " pure-button-primary"))} "Mine hashtagger"]]]

       (case @selection
         :feed
         [:div
          (if-let [sessions (session/get :user-only-feed)]
            [:div {:style {:margin-top 40}}
             (doall
               (for [session sessions]
                 ^{:key (:url session)}
                 [session-view session]))]
            [:div.pure-g
             [:div.pure-u-1.center
              [:i.fa.fa-spinner.fa-pulse.fa-4x]]])]

         :calendar
         [:div "cal"]

         :stat
         [:div "stat"]

         :tag
         [:div "tag"]

         [:div])

       ])))

(defn user-page []
  (let [viewing-user (session/get :viewing-user)
        menu (session/get :selected-menu-item)
        {:keys [user-id] :as user} (session/get :user)]
    (fn []
      [:div
       [menu-component]
       (if (and viewing-user (not= user-id (:user-id viewing-user)) (nil? menu))
         [another-users-page viewing-user]
         [my-user-page user]
         )])))

