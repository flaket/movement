(ns movement.pages.user
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [secretary.core :include-macros true :refer [dispatch!]]
            [movement.menu :refer [menu-component]]
            [movement.util :refer [POST text-input get-user-info]]))

(defn change-password-component []
  (let [show-change-password? (atom false)
        pass (atom {:info "" :error ""})]
    (fn []
      [:div
       [:div.pure-g
        [:div.button.pure-u.pure-u-md-2-5 {:on-click #(reset! show-change-password? true)} "Change password"]]
       (when @show-change-password?
         [:div
          [:div.pure-g
           [:input.pure-u.pure-u-md-2-5 {:type        "password"
                                         :placeholder "old password"
                                         :value       (:old-pass @pass)
                                         :on-change   #(swap! pass assoc :old-pass (-> % .-target .-value))}]]
          [:div.pure-g
           [:input.pure-u.pure-u-md-2-5 {:type        "password"
                                         :placeholder "new password"
                                         :value       (:new-pass @pass)
                                         :on-change   #(swap! pass assoc :new-pass (-> % .-target .-value))}]]

          [:div.pure-g
           [:input.pure-u.pure-u-md-2-5 {:type        "password"
                                         :placeholder "confirm new password"
                                         :value       (:repeat-pass @pass)
                                         :on-change   #(swap! pass assoc :repeat-pass (-> % .-target .-value))}]]
          (when-let [info (:info @pass)]
            [:div.pure-g [:div.pure-u {:style {:color 'green :font-size 24}} info]])
          (when-let [error (:error @pass)]
            [:div.pure-g [:div.pure-u {:style {:color 'red :font-size 24}} error]])
          (if (not= (:new-pass @pass) (:repeat-pass @pass))
            [:div.pure-g [:div.pure-u "new password mismatch"]]
            (when (and (not-empty (:old-pass @pass)) (not-empty (:new-pass @pass)))
              [:div.pure-g
               [:button.pure-u.pure-u-md-2-5.button.button-primary
                {:on-click #(POST "/change-password"
                                  {:params        {:username     (session/get :user)
                                                   :password     (:old-pass @pass)
                                                   :new-password (:new-pass @pass)}
                                   :handler       (fn [response]
                                                    (reset! pass {:error "" :info response}))
                                   :error-handler (fn [response]
                                                    (reset! pass {:error (:response response) :info ""}))})}
                "Change password"]]))])])))

(defn set-username-component []
  (let [username (atom {:info "" :error ""})]
    (fn []
      [:div
       [:p.pure-g
        [:input.pure-u.pure-u-md-2-5 {:type        "text"
                                      :placeholder "Select a new nickname"
                                      :value       (:new-username @username)
                                      :on-change   #(swap! username assoc :new-username (-> % .-target .-value))}]]
       (when-let [info (:info @username)]
         [:div.pure-g [:div.pure-u {:style {:color 'green :font-size 24}} info]])
       (when-let [error (:error @username)]
         [:div.pure-g [:div.pure-u {:style {:color 'red :font-size 24}} error]])
       (when (not-empty (:new-username @username))
         [:div.pure-g
          [:button.pure-u.pure-u-md-2-5.button.button-primary
           {:on-click #(POST "/change-username"
                             {:params        {:email    (:email (session/get :user))
                                              :username (:new-username @username)}
                              :handler       (fn [response]
                                               (let []
                                                 (swap! username assoc
                                                        :error ""
                                                        :info (:message response))
                                                 (session/put! :username (:username response))))
                              :error-handler (fn [response]
                                               (swap! username assoc
                                                      :error (:response response)
                                                      :info ""))})}
           "Set nickname"]])])))

(defn logged-sessions-component []
  (let [show-sessions? (atom false)
        sessions (session/get :stored-sessions)
        sorted-sessions (sort-by :session/timestamp sessions)]
    (fn []
      [:div
       [:div.pure-g
        [:div.button.pure-u.pure-u-md-2-5 {:on-click #(reset! show-sessions? true)} "Show logged sessions"]]
       (when @show-sessions?
         (doall
           (for [s sorted-sessions]
             ^{:key s}
             [:div.pure-g
              [:div.pure-u
               (str (:session/timestamp s) " - " (:session/title s) " - " (:session/comment s) "\t")
               [:a {:href (str "/session/" (:session/url s)) :target "_blank"} "View"]]])))])))

(defn logged-in-as []
  (let [set-new-username? (atom false)
        {:keys [email name]} (session/get :user)]
    (fn []
      [:div
       [:div.pure-g
        [:h4.pure-u "Logged in as " [:span {:style    {:text-decoration 'underline
                                                       :cursor 'pointer}
                                            :on-click #(reset! set-new-username? true)}
                                     (if name name email)]]]
       (when @set-new-username?
         [set-username-component])])))

(defn log-out [event]
  (.preventDefault event)
  (session/clear!)
  (dispatch! "/"))

(defn user-page []
  (let [selection (atom :feed)
        changing-settings? (atom false)]
    (fn []
      [:div
       [menu-component]
       [:div.content

        [:div.pure-g {:style {:border-bottom "1px solid lightgray"}}
         [:div.pure-u-1-4
          #_[:i.fa.fa-user.fa-4x]
          [:img {:width "100%" :src "images/movements/arch-up.png"}]]
         (if @changing-settings?
           [:div.pure-u-3-4

            [:div.pure-g [:div.pure-u-1 [:input {:size 60 :type "text" :placeholder "Brukernavn"}]]]
            [:div.pure-g [:div.pure-u-1 [:input {:size 60 :type "text" :placeholder "Profiltekst"}]]]
            [:div.pure-g [:div.pure-u-1 [:input {:size 60 :type "text" :placeholder "Profiltekst"}]]]
            [:div.pure-g [:div.pure-u-1 [:input {:size 60 :type "text" :placeholder "Profiltekst"}]]]
            [:div.pure-g [:a.pure-u-1.pure-button "Velg profilbilde"]]
            [:div.pure-g
             [:a.pure-u-1.pure-button.pure-button-primary
              {:onClick #(reset! changing-settings? false)
               :onTouchEnd #(reset! changing-settings? false)} "Lagre"]]]
           [:div.pure-u-3-4
            [:div.pure-g [:div.pure-u-1 {:on-click #(pr (session/get :user))} "Brukernavn"]]
            [:div.pure-g [:div.pure-u-1 "Profiltekst"]]
            [:div.pure-g
             [:a.pure-u-1-3.pure-button {:onClick #(reset! changing-settings? true)
                                         :onTouchEnd #(reset! changing-settings? true)} "Endre innstillinger"]
             [:div.pure-u-1-3]
             [:a.pure-u-1-3.pure-button {:onClick #(log-out %) :onTouchEnd #(log-out %)} "Logg ut"]]])]

        [:div
         [:div.pure-g [:div.pure-u-1 [:h2 "Min treningsdagbok"]]]
         [:div.pure-g
          [:a {:onClick #(reset! selection :feed) :onTouchEnd #(reset! selection :feed)
               :className (str "pure-u-1-4 pure-button" (when (= @selection :feed) " pure-button-primary"))} "Feed"]
          [:a {:onClick #(reset! selection :calendar) :onTouchEnd #(reset! selection :calendar)
               :className (str "pure-u-1-4 pure-button" (when (= @selection :calendar) " pure-button-primary"))} "Kalender"]
          [:a {:onClick #(reset! selection :stat) :onTouchEnd #(reset! selection :stat)
               :className (str "pure-u-1-4 pure-button" (when (= @selection :stat) " pure-button-primary"))} "Statistikk"]
          [:a {:onClick #(reset! selection :tag) :onTouchEnd #(reset! selection :tag)
               :className (str "pure-u-1-4 pure-button" (when (= @selection :tag) " pure-button-primary"))} "Mine hashtagger"]]]

        #_[logged-in-as]
        #_[logged-sessions-component]
        #_[change-password-component]

        ]])))

