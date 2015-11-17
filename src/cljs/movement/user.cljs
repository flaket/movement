(ns movement.user
 (:require [reagent.core :refer [atom]]
           [reagent.session :as session]
           [movement.menu :refer [menu-component]]
           [movement.util :refer [POST text-input]]))

(defn update-password! [pass]
  (let [old-pass (:old-pass @pass)
        new-pass (:new-pass @pass)
        repeat-pass (:repeat-pass @pass)]
    (POST "/change-password!"
          {
           :params {:username (session/get-in [:profile :handle])
                    :password old-pass
                    :new-pass new-pass
                    :repeat-pass repeat-pass}
           :handler
                    (fn [_] (reset! pass {}))
           :error-handler
                    (fn [response]
                      (swap! pass assoc :error (get-in response [:response :error])))})))

(defn change-password []
  (let [pass (atom {})]
    (fn []
      [:div
       [:h2 "Password"]
       [:input {:type "password"
                :placeholder "password"
                :value (:old-pass @pass)
                :on-change #(swap! pass assoc :old-pass (-> % .-target .-value))}]
       [:br]
       [:input {:type "password"
                :placeholder "new password"
                :value (:new-pass @pass)
                :on-change #(swap! pass assoc :new-pass (-> % .-target .-value))}]
       [:br]
       [:input {:type "password"
                :placeholder "confirm password"
                :value (:repeat-pass @pass)
                :on-change #(swap! pass assoc :repeat-pass (-> % .-target .-value))}]
       [:br]
       (when-let [info (:info @pass)]
         [:div info])
       (when-let [error (:error @pass)]
         [:div.error error])
       (if (not= (:new-pass @pass) (:repeat-pass @pass))
         [:div.error "password mismatch"]
         (when (not-empty (:new-pass @pass))
           [:a.button.button-primary {:on-click #(update-password! pass)} "change password"]))])))

(defn user-component []
  (let []
    (fn []
      [:div#layout {:class (str "" (when (session/get :active?) "active"))}
       [menu-component]
       [:div.content
        [:h3 (str "Welcome " (session/get :user))]
        [:h2 "Logged sessions"]
        [:ul
         (doall
           (for [s (session/get :stored-sessions)]
             ^{:key s} [:li (str (:session/timestamp s) " - " (:session/name s) " - " (:session/comment s))]))]
        [:div
         [change-password]]]])))