(ns movement.pages.user
  (:import [goog.date DateTime])
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [cljs.core.async :as async :refer [timeout <!]]
            [secretary.core :include-macros true :refer [dispatch!]]
            [movement.menu :refer [menu-component]]
            [movement.pages.feed :refer [load-feed load-user-only-feed session-view]]
            [movement.util :refer [POST text-input get-user-info]]
            [clojure.string :as str]))

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
  (POST "follow" {:params        {:user-id   user-id
                                  :follow-id follow-id}
                  :handler       (fn [r]
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
            (if user-image
              [:img
               {:style  {:padding       "20px 20px 20px 20px"
                         :border-radius "50% 50% 50% 50%"}
                :width  275
                :height 275
                :src    (str "http://s3.amazonaws.com/mumrik-user-profile-images/" user-id ".jpg")}]
              [:img
               {:style  {:padding       "20px 20px 20px 20px"
                         :border-radius "50% 50% 50% 50%"}
                :width  275
                :height 275
                :src    "images/profile-no-photo.png"}])]]]
         [:div.pure-u.pure-u-md-1-3
          [:div.pure-g {:style {:margin-top 10}}
           [:h2.pure-u-1 name]]
          (when profile-text
            [:div.pure-g {:style {:margin-top 10}}
             [:div.pure-u-1 profile-text]])
          (when location
            [:div.pure-g {:style {:margin-top 10}}
             [:div.pure-u-1 [:i.fa.fa-map-marker {:style {:margin-right 5}}] location]])
          [:div.pure-g {:style {:margin-top 10}}
           [:div.pure-u-1 {:style {:font-size "80%" :opacity 0.5}} "Medlem siden " sign-up-timestamp]]
          [:div.pure-g {:style {:margin-top 10}}
           (doall
             (for [b badges]
               ^{:key b}
               [:div.pure-u {:style {:margin-right 5}} [:b b]]))]]
         [:div.pure-u.pure-u-md-1-3
          (if (get (set (:follows (session/get :user))) user-id)
            [:div.pure-g {:style {:margin-top 10 :margin-bottom 10}}
             [:a.pure-u-1.pure-button.button-success {:onClick    #(unfollow-user % (:user-id (session/get :user)) user-id)
                                                      :onTouchEnd #(unfollow-user % (:user-id (session/get :user)) user-id)} "Følger"]]
            [:div.pure-g {:style {:margin-top 10 :margin-bottom 10}}
             [:a.pure-u-1.pure-button.pure-button-primary {:onClick    #(follow-user % (:user-id (session/get :user)) user-id)
                                                           :onTouchEnd #(follow-user % (:user-id (session/get :user)) user-id)} "Følg"]])
          ]]]
       [:div
        [:div.pure-g [:div.pure-u-1 [:h2 (str name " sin treningsdagbok")]]]
        [:div.pure-g
         [:a.pure-u.pure-u-md-1-4
          {:onClick    (fn [e] (.preventDefault e) (load-user-only-feed user-id) (reset! selection :feed))
           :onTouchEnd (fn [e] (.preventDefault e) (load-user-only-feed user-id) (reset! selection :feed))
           :className  (str " pure-button" (when (= @selection :feed) " pure-button-primary"))} "Feed"]
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

(defn change-password [e pass local-state]
  (.preventDefault e)
  (POST "change-password"
        {:params        {:user-id      (:user-id (session/get :user))
                         :password     (:old-pass @pass)
                         :new-password (:new-pass @pass)}
         :handler       (fn [r]
                          (reset! pass {:error "" :info r})
                          (go
                            (<! (timeout 1500))
                            (reset! pass {:info "" :error ""})
                            (reset! local-state nil)))
         :error-handler (fn [r]
                          (reset! pass {:error (:response r) :info ""}))}))

(defn change-profile [profile local-state]
  (let [p (into {} (for [[k v] @profile] (when-not (str/blank? v) [k v])))] ; remove empty strings and nils
    (POST "change-profile"
          {:params        {:user-id (:user-id (session/get :user))
                           :profile (dissoc p :info :error)}
           :handler       (fn [r]
                            (swap! profile assoc :error "" :info r)
                            (session/update-in! [:user] assoc
                                                :email (:email p)
                                                :profile-text (:profile-text p)
                                                :name (:name p)
                                                :location (:location p))
                            (go
                              (<! (timeout 1500))
                              (swap! profile dissoc :error :info)
                              (reset! local-state nil)))
           :error-handler (fn [r]
                            (swap! profile assoc :error (:r r)))})))

; bruker ikke denne metoden lengre..
(defn preview-file [profile]
  (let [file (.getElementById js/document "upload-profile-photo")
        reader (js/FileReader.)]
    (when-let [file (aget (.-files file) 0)]
      (set! (.-onloadend reader) #(swap! profile assoc :photo (-> % .-target .-result)))
      (.readAsDataURL reader file))))

(defn process-file [file profile]
  (let [reader (js/FileReader.)
        canvas (.getElementById js/document "image-canvas")
        ctx (.getContext canvas "2d")]
    (when-let [file (aget (.-files file) 0)]
      (set! (.-onload reader)
            (fn [e]
              (let [blob (js/Blob. (array (-> e .-target .-result)))
                    blob-url (.createObjectURL (.-URL js/window) blob)
                    image (js/Image.)]
                (swap! profile assoc :photo true)
                (set! (.-src image) blob-url)
                (set! (.-onload image)
                      (fn [e]
                        (let [max-w 400
                              max-h 400
                              w (.-width image)
                              h (.-height image)
                              [w h] (if (>= w h)
                                      (if (> w max-w)
                                        [max-w (* h (/ max-w w))]
                                        [w h])
                                      (if (> h max-h)
                                        [(* w (/ max-h h)) max-h]
                                        [w h])
                                      )]
                          (set! (.-width canvas) w)
                          (set! (.-height canvas) h)
                          (.drawImage ctx image 0 0 w h)))))))
      (.readAsArrayBuffer reader file))))

(defn my-user-page [{:keys [user-id email name sign-up-timestamp badges user-image profile-text location] :as user}]
  (let [local-state (atom nil)
        pass (atom {:info "" :error ""})
        profile (atom {:email email :name name :photo nil :profile-text profile-text :location location})
        selection (atom nil)]
    (fn [{:keys [user-id name email sign-up-timestamp badges user-image profile-text location] :as user}]
      (case @local-state
        :change-profile
        [:div.content
         [:div.pure-g
          [:div.pure-u-1-2
           [:div.pure-g
            [:div.pure-u.pure-button.fileUpload
             [:span "Last opp profilbilde"]
             [:input {:id "upload-profile-photo" :className "upload" :type "file" :on-change
                          #(process-file (.getElementById js/document "upload-profile-photo") profile)}]]]
           (when (:photo @profile)
             [:div {:onClick    (fn [e] (.preventDefault e)
                                  (let [canvas (.getElementById js/document "image-canvas")
                                        ctx (.getContext canvas "2d")]
                                    (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
                                    (swap! profile dissoc :photo)))
                    :onTouchEnd (fn [e] (.preventDefault e)
                                  (let [canvas (.getElementById js/document "image-canvas")
                                        ctx (.getContext canvas "2d")]
                                    (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
                                    (swap! profile dissoc :photo)))
                    :style      {:color "red" :cursor 'pointer}} [:i.fa.fa-times.fa-2x]])
           [:canvas {:id "image-canvas"}]]
          [:div.pure-u-1-2
           [:div.pure-g {:style {:margin-top 5}} [:div.pure-u-1 "Epost (vises ikke i profilen din)"]]
           [:div.pure-g
            [:input.pure-u-1 {:type        "text"
                              :placeholder email
                              :value       (:email @profile)
                              :on-change   #(swap! profile assoc :email (-> % .-target .-value))}]]

           [:div.pure-g {:style {:margin-top 5}} [:div.pure-u-1 "Navn"]]
           [:div.pure-g
            [:input.pure-u-1 {:type        "text"
                              :placeholder name
                              :value       (:name @profile)
                              :on-change   #(swap! profile assoc :name (-> % .-target .-value))}]]
           [:div.pure-g {:style {:margin-top 5}} [:div.pure-u-1 "Profiltekst"]]
           [:div.pure-g
            [:input.pure-u-1 {:type        "text"
                              :placeholder profile-text
                              :value       (:profile-text @profile)
                              :on-change   #(swap! profile assoc :profile-text (-> % .-target .-value))}]]

           [:div.pure-g {:style {:margin-top 5}} [:div.pure-u-1 "Sted"]]
           [:div.pure-g
            [:input.pure-u-1 {:type        "text"
                              :placeholder location
                              :value       (:location @profile)
                              :on-change   #(swap! profile assoc :location (-> % .-target .-value))}]]
           (when-let [info (:info @profile)]
             [:div.pure-g [:div.pure-u {:style {:color 'green :font-size 24}} info]])
           (when-let [error (:error @profile)]
             [:div.pure-g [:div.pure-u {:style {:color 'red :font-size 24}} error]])

           [:div.pure-g {:style {:margin-top 10}}
            [:a.pure-u-1.pure-button.pure-button-primary
             {:onClick    (fn [e] (.preventDefault e)
                            (let [canvas (.getElementById js/document "image-canvas")
                                  image (.toDataURL canvas "image/jpeg" 0.7)
                                  _ (swap! profile assoc :photo image)]
                              (change-profile profile local-state)))
              :onTouchEnd (fn [e] (.preventDefault e)
                            (let [canvas (.getElementById js/document "image-canvas")
                                  image (.toDataURL canvas "image/jpeg" 0.7)
                                  _ (swap! profile assoc :photo image)]
                              (change-profile profile local-state)))}
             "Lagre"]]
           [:div.pure-g {:style {:margin-top 10}}
            [:a.pure-u-1.pure-button {:onClick    (fn [e] (.preventDefault e) (reset! profile {}) (reset! local-state nil))
                                      :onTouchEnd (fn [e] (.preventDefault e) (reset! profile {}) (reset! local-state nil))} "Avbryt"]]

           ]
          ]

         ]
        :change-settings
        [:div.content
         [:div.pure-g {:style {:margin-top 10}}
          [:a.pure-u-1.pure-button {:onClick    (fn [e] (.preventDefault e) (reset! local-state nil))
                                    :onTouchEnd (fn [e] (.preventDefault e) (reset! local-state nil))} "Ferdig settings"]]]
        :change-password
        [:div.content
         [:div.pure-g
          [:div.pure-u-1 {:style {:position 'relative}}
           [:div.pure-g [:p.pure-u-1 "Endre passord"]]
           [:div
            [:div.pure-g
             [:input.pure-u-1.pure-u-md-1-2 {:type        "password"
                                             :placeholder "nåværende passord"
                                             :value       (:old-pass @pass)
                                             :on-change   #(swap! pass assoc :old-pass (-> % .-target .-value))}]]
            [:div.pure-g
             [:input.pure-u-1.pure-u-md-1-2 {:type        "password"
                                             :placeholder "nytt passord"
                                             :value       (:new-pass @pass)
                                             :on-change   #(swap! pass assoc :new-pass (-> % .-target .-value))}]]

            [:div.pure-g
             [:input.pure-u-1.pure-u-md-1-2 {:type        "password"
                                             :placeholder "nytt passord igjen"
                                             :value       (:repeat-pass @pass)
                                             :on-change   #(swap! pass assoc :repeat-pass (-> % .-target .-value))}]]
            (when-let [info (:info @pass)]
              [:div.pure-g [:div.pure-u {:style {:color 'green :font-size 24}} info]])
            (when-let [error (:error @pass)]
              [:div.pure-g [:div.pure-u {:style {:color 'red :font-size 24}} error]])
            (if (not= (:new-pass @pass) (:repeat-pass @pass))
              [:div.pure-g [:div.pure-u "de nye passordene er ikke like"]]
              (when (and (not-empty (:old-pass @pass)) (not-empty (:new-pass @pass)))
                [:div.pure-g {:style {:margin-top 10}}
                 [:a.pure-u.pure-u-md-1-2.pure-button.pure-button-primary
                  {:onClick #(change-password % pass local-state) :onTouchEnd #(change-password % pass local-state)}
                  "Endre passordet"]]))]]]
         [:div.pure-g {:style {:margin-top 10}}
          [:a.pure-u-1.pure-u-md-1-2.pure-button {:onClick    (fn [e] (.preventDefault e) (reset! local-state nil))
                                                  :onTouchEnd (fn [e] (.preventDefault e) (reset! local-state nil))} "Avbryt"]]]
        ; default
        [:div.content
         [:div
          [:div.pure-g
           [:div.pure-u.pure-u-md-1-3
            [:div.pure-g
             [:div.pure-u-1
              (if user-image
                [:img
                 {:style  {:padding       "20px 20px 20px 20px"
                           :border-radius "50% 50% 50% 50%"}
                  :width  275
                  :height 275
                  :src    (str "http://s3.amazonaws.com/mumrik-user-profile-images/" user-id ".jpg")}]
                [:img
                 {:style  {:padding       "20px 20px 20px 20px"
                           :border-radius "50% 50% 50% 50%"}
                  :width  275
                  :height 275
                  :src    "images/profile-no-photo.png"}])]]
            ]
           [:div.pure-u.pure-u-md-1-3
            [:div.pure-g {:style {:margin-top 10}}
             [:h2.pure-u-1 name]]
            (when profile-text
              [:div.pure-g {:style {:margin-top 10}}
               [:div.pure-u-1 profile-text]])
            (when location
              [:div.pure-g {:style {:margin-top 10}}
               [:div.pure-u-1 [:i.fa.fa-map-marker {:style {:margin-right 5}}] location]])
            [:div.pure-g {:style {:margin-top 10}}
             [:div.pure-u-1 {:style {:font-size "80%" :opacity 0.5}} "Medlem siden " sign-up-timestamp]]
            [:div.pure-g {:style {:margin-top 10}}
             (doall
               (for [b badges]
                 ^{:key b}
                 [:div.pure-u {:style {:margin-right 5}} [:b b]]))]]
           [:div.pure-u.pure-u-md-1-3
            [:div.pure-g {:style {:margin-top 10}}
             [:a.pure-u-1.pure-button {:onClick    (fn [e] (.preventDefault e) (reset! local-state :change-profile))
                                       :onTouchEnd (fn [e] (.preventDefault e) (reset! local-state :change-profile))} "Endre profil"]]
            [:div.pure-g {:style {:margin-top 10}}
             [:a.pure-u-1.pure-button {:style      {:opacity 0.25 :pointer-events 'none :cursor 'default}
                                       :onClick    (fn [e] (.preventDefault e) (reset! local-state :change-settings))
                                       :onTouchEnd (fn [e] (.preventDefault e) (reset! local-state :change-settings))} "Endre innstillinger"]]
            [:div.pure-g {:style {:margin-top 10}}
             [:a.pure-u-1.pure-button {:onClick    (fn [e] (.preventDefault e) (reset! local-state :change-password))
                                       :onTouchEnd (fn [e] (.preventDefault e) (reset! local-state :change-password))} "Endre passord"]]
            [:div.pure-g {:style {:margin-top 10}}
             [:a.pure-u-1.pure-button {:onClick    #(log-out %)
                                       :onTouchEnd #(log-out %)} "Logg ut"]]]]]
         [:div
          [:div.pure-g [:div.pure-u-1 [:h2 "Min treningsdagbok"]]]
          [:div.pure-g
           [:a.pure-u.pure-u-md-1-4
            {:onClick    (fn [e] (.preventDefault e) (load-user-only-feed user-id) (reset! selection :feed))
             :onTouchEnd (fn [e] (.preventDefault e) (load-user-only-feed user-id) (reset! selection :feed))
             :className  (str " pure-button" (when (= @selection :feed) " pure-button-primary"))} "Feed"]
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

           [:div])]))))

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

