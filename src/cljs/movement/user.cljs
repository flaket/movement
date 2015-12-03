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

(defn change-password []
  (let [pass (atom {})]
    (fn []
      [:div
       [:div.pure-g
        [:h4.pure-u "Change password"]]
       [:div.pure-g
        [:input.pure-u {:type        "password"
                 :placeholder "old password"
                 :value       (:old-pass @pass)
                 :on-change   #(swap! pass assoc :old-pass (-> % .-target .-value))}]]
       [:div.pure-g
        [:input.pure-u {:type        "password"
                 :placeholder "new password"
                 :value       (:new-pass @pass)
                 :on-change   #(swap! pass assoc :new-pass (-> % .-target .-value))}]]

       [:div.pure-g
        [:input.pure-u {:type        "password"
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
            [:a.pure-u.button.button-primary {:on-click #(update-password! pass)} "Change password"]]))])))

(defn unsubscribe-component []
  [:a {:href "https://www.paypal.com/cgi-bin/webscr?cmd=_subscr-find&alias=7MLAKH5Y6KQA6"}
   [:img {:src "https://www.paypalobjects.com/en_US/i/btn/btn_unsubscribe_LG.gif"
          :border "0"}]])

(defn user-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content
        [:div.pure-g
         [:h4.pure-u (str "Logged in as " (session/get :user))]]
        [:div.pure-g
         [:h4.pure-u "Logged sessions"]]
        [:ul.pure-g
         (doall
           (for [s (session/get :stored-sessions)]
             ^{:key s}
             [:li.pure-u
              (str (:session/timestamp s) " - " (:session/name s) " - " (:session/comment s) "\t")
              [:a {:href (str "/session/" (:session/url s)) :target "_blank"} "View"]]))]
        [:div
         [change-password]]
        (unsubscribe-component)]])))

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

