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

(defn log-out [event]
  (.preventDefault event)
  (session/clear!)
  (dispatch! "/"))

(defn remove-viewing-user [e]
  (.preventDefault e)
  (session/remove! :viewing-user)
  (dispatch! "/feed"))

(defn user-page []
  (let [selection (atom nil)
        changing-settings? (atom false)
        changing-password? (atom false)
        new-user-settings (atom {})
        viewing-user (session/get :viewing-user)
        user (session/get :user)]
    (fn []
      [:div
       [menu-component]
       (if (and viewing-user (not= (:user-id user) (:user-id viewing-user)))
         ; Brukeren har trykket på en lenke til en annens brukerprofil
         [:div.content

          [:div.pure-g
           [:a.pure-u {:style      {:margin-left 20 :margin-top 0 :opacity 0.5}
                       :onClick #(remove-viewing-user %) :onTouchEnd #(remove-viewing-user %)}
            [:i.fa.fa-arrow-left.fa-4x]]]

          [:div.pure-g {:style {:border-bottom "1px solid lightgray"}}
           [:div.pure-u-1-4
            [:img {:width "100%" :src "images/movements/arch-up.png"}]]
           [:div.pure-u-3-4
            [:div.pure-g [:div.pure-u-1 {:on-click #(pr (session/get :user))} "Brukernavn"]]
            [:div.pure-g [:div.pure-u-1 "Profiltekst"]]
            [:div.pure-g
             [:a.pure-u-1-3.pure-button {:onClick #() :onTouchEnd #()} "Følg"]
             ]]]

          [:div
           [:div.pure-g [:div.pure-u-1 [:h2 (str (:name viewing-user) " sin treningsdagbok")]]]
           [:div.pure-g
            [:a {:onClick #(reset! selection :feed) :onTouchEnd #(reset! selection :feed)
                 :className (str "pure-u-1-4 pure-button" #_(when (= @selection :feed) " pure-button-primary"))} "Feed"]
            [:a {:onClick #(reset! selection :calendar) :onTouchEnd #(reset! selection :calendar)
                 :className (str "pure-u-1-4 pure-button" #_(when (= @selection :calendar) " pure-button-primary"))} "Kalender"]
            [:a {:onClick #(reset! selection :stat) :onTouchEnd #(reset! selection :stat)
                 :className (str "pure-u-1-4 pure-button" #_(when (= @selection :stat) " pure-button-primary"))} "Statistikk"]
            [:a {:onClick #(reset! selection :tag) :onTouchEnd #(reset! selection :tag)
                 :className (str "pure-u-1-4 pure-button" #_(when (= @selection :tag) " pure-button-primary"))} "Hashtagger"]]]]

         ; Brukerens profil
         [:div.content
          [:div {:style {:border-bottom "1px solid lightgray"}}

           [:div.pure-g
            [:div.pure-u-1-4
             [:img {:width "100%" :src "images/movements/arch-up.png"}]]

            (if @changing-settings?

              ; View for å endre profil/innstillinger.
              [:div.pure-u-3-4

               [:div {:style {:margin-bottom 10}}
                [:div.pure-g [:div.pure-u-1 "Navn"]]
                [:div.pure-g [:div.pure-u-1 [:input {:size 100 :type "text" :defaultValue (:name user)}]]]]

               [:div {:style {:margin-bottom 10}}
                [:div.pure-g [:div.pure-u-1 "Epost"]]
                [:div.pure-g [:div.pure-u-1 [:input {:size 100 :type "text" :defaultValue (:email user)}]]]]

               #_[change-password-component]

               #_[:div
                  [:div.pure-g [:div.pure-u-1 "Prioriteringer"]]
                  [:div.pure-g [:div.pure-u-1 [:input {:size 100 :type "text" :defaultValue (:priorities user)}]]]
                  [:div.pure-g [:div.pure-u-1 "Mål"]]
                  [:div.pure-g [:div.pure-u-1 [:input {:size 100 :type "text" :defaultValue (:goals user)}]]]]


               [:div.pure-g [:a.pure-u-1.pure-button "Velg profilbilde"]]

               [:div.pure-g
                [:a.pure-u-1.pure-button.pure-button-primary
                 {:onClick    #(reset! changing-settings? false)
                  :onTouchEnd #(reset! changing-settings? false)} "Lagre"]]]

              ; View for å se sin egen profil med knapper for å gjøre endringer og å logge ut.
              [:div.pure-u-3-4 {:style {:position 'relative}}
               [:div.pure-g [:p.pure-u-1 {:on-click #(pr user)} (:name user)]]
               [:div.pure-g [:p.pure-u-1 (:profile-text user)]]
               [:div.pure-g {:style {:margin-top 10}}
                (doall
                  (for [b (conj (:badges user) "Newbie" "Møssleup!" "Armhevingskongen")]
                    ^{:key b}
                    [:div.pure-u b]))]


               ])]


           [:div.pure-g
            [:div.pure-u-1-4 (str "Medlem siden " (:sign-up-timestamp user))]
            [:div.pure-u-3-4
             [:div.pure-g
              [:a.pure-u-1-4.pure-button {;:style      {:margin-top 40 :margin-right 5}
                                          :onClick    #(reset! changing-settings? true)
                                          :onTouchEnd #(reset! changing-settings? true)} "Innstillinger"]
              [:a.pure-u-1-4.pure-button {;:style      {:margin-top 40 :margin-right 5}
                                          :onClick    #(reset! changing-password? true)
                                          :onTouchEnd #(reset! changing-password? true)} "Passord"]
              [:div.pure-u-1-4]
              [:a.pure-u-1-4.pure-button {;:style      {:margin-top 40 :margin-right 5}
                                          :onClick    #(log-out %)
                                          :onTouchEnd #(log-out %)} "Logg ut"]]
             ]]


           ]

          [:div
           [:div.pure-g [:div.pure-u-1 [:h2 "Min treningsdagbok"]]]
           [:div.pure-g
            [:a.pure-u.pure-u-md-1-4 {
                                         :onClick   #(reset! selection :feed) :onTouchEnd #(reset! selection :feed)
                                         :className (str " pure-button" (when (= @selection :feed) " pure-button-primary"))} "Feed"]
            [:a.pure-u.pure-u-md-1-4 {
                                         :onClick   #(reset! selection :calendar) :onTouchEnd #(reset! selection :calendar)
                                         :className (str " pure-button" (when (= @selection :calendar) " pure-button-primary"))} "Kalender"]
            [:a.pure-u.pure-u-md-1-4 {
                                         :onClick   #(reset! selection :stat) :onTouchEnd #(reset! selection :stat)
                                         :className (str " pure-button" (when (= @selection :stat) " pure-button-primary"))} "Statistikk"]
            [:a.pure-u.pure-u-md-1-4 {
                                         :onClick   #(reset! selection :tag) :onTouchEnd #(reset! selection :tag)
                                         :className (str " pure-button" (when (= @selection :tag) " pure-button-primary"))} "Mine hashtagger"]]]



          ])])))

