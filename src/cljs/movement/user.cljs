(ns movement.user
 (:require [reagent.core :refer [atom]]
           [reagent.session :as session]
           [movement.menu :refer [menu-component]]
           [movement.util :refer [POST text-input]]))

(defn update-password! [pass]
  (let [old-pass (:old-pass @pass)
        new-pass (:new-pass @pass)
        repeat-pass (:repeat-pass @pass)]
    (POST "/change-password"
          {:params {:username     (session/get :user)
                    :password     old-pass
                    :new-password new-pass}
           :handler (fn [response]
                      (swap! pass assoc :info (:message response)))
           :error-handler (fn [response]
                            (swap! pass assoc :error (:message response)))})))

(defn change-password-component []
  (let [show-change-password? (atom false)
        pass (atom {})]
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
            [:div.pure-g [:div.pure-u info]])
          (when-let [error (:error @pass)]
            [:div.pure-g [:div.pure-u error]])
          (if (not= (:new-pass @pass) (:repeat-pass @pass))
            [:div.pure-g [:div.pure-u "new password mismatch"]]
            (when (and (not-empty (:old-pass @pass)) (not-empty (:new-pass @pass)))
              [:div.pure-g
               [:a.pure-u.pure-u-md-2-5.button.button-primary {:on-click #(update-password! pass)} "Change password"]]))])])))

(defn unsubscribe-component []
  (let [show-unsub-button? (atom false)]
    (fn []
      [:div
       [:div.pure-g
        [:div.button.pure-u.pure-u-md-2-5 {:on-click #(reset! show-unsub-button? true)} "Unsubscribe"]]
       (when @show-unsub-button?
         [:div.pure-g
          [:a.pure-u {:href "https://www.paypal.com/cgi-bin/webscr?cmd=_subscr-find&alias=7MLAKH5Y6KQA6"}
           [:img {:src    "https://www.paypalobjects.com/en_US/i/btn/btn_unsubscribe_LG.gif"
                  :border "0"}]]])])))

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

(defn user-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content
        [:div.pure-g
         [:h4.pure-u (str "Logged in as " (session/get :user))]]
        [logged-sessions-component]
        [change-password-component]
        [unsubscribe-component]]])))

(defn payment-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content
        [:div.pure-g
         [:form.pure-form {:action "https://www.paypal.com/cgi-bin/webscr"
                 :method "post"
                 :target "_top"}
          [:input {:type  "hidden"
                   :name  "cmd"
                   :value "_s-xclick"}]
          [:input {:type  "hidden"
                   :name  "hosted_button_id"
                   :value "9U8DQ9HYGV68S"}]
          [:input {:type   "image"
                   :src    "https://www.paypalobjects.com/en_US/NO/i/btn/btn_subscribeCC_LG.gif"
                   :border "0"
                   :name   "submit"
                   :alt    "PayPal - The safer, easier way to pay online!"}]
          [:img {:alt    ""
                 :border "0"
                 :src    "https://www.paypalobjects.com/no_NO/i/scr/pixel.gif"
                 :width  "1"
                 :height "1"}]]]]])))

