(ns movement.user)

(defn user-page []
  [:div
   [:section#header]
   [:section#nav]
   [:section#user
    [:div.container
     [:div "Hello"]]]])