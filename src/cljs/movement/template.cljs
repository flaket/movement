(ns movement.template
 (:require [reagent.core :refer [atom]]
           [reagent.session :as session]
           [reagent-forms.core :refer [bind-fields]]
           [secretary.core :include-macros true :refer [dispatch!]]))

(def counter (atom 0))
(def include-date (atom true))

(defn count-component []
  [:div
   "The value is: "
   @counter "."
   [:input {:type "button" :value "Click!"
            :on-click #(swap! counter inc)}]])

(defn input [value]
  [:input {:type "text" :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn btn [val]
  [:input {:type     "button" :value val
           :on-click #(reset! include-date (if @include-date false true))}])

(defn title-component []
  (let [val (atom "My Favourite Movements")]
    (fn []
      [:div "The title of sessions created with this template is: " [input val]
       " and it " [btn (if @include-date "should" "should not")]
       " include the date in the title."])))

(def data (atom {:m 4 :category :strength}))

(defn slider [param value min max]
  [:input {:type      "range" :value value
           :min min :max max
           :style     {:width "100%"}
           :on-change (fn [e]
                        (swap! data assoc param (.-target.value e)))}])

(defn slider-component []
  (let [{:keys [m category]} @data]
    [:div
     [:div m
      [slider :m m 0 10]]]))

(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 input]])

(def form-template
  [:div
   (row "first name"
        [:input.form-control {:field :text :id :first-name}])
   (row "last name"
        [:input.form-control {:field :text :id :last-name}])
   (row "age"
        [:input.form-control {:field :numeric :id :age}])
   (row "email"
        [:input.form-control {:field :email :id :email}])
   (row "comments"
        [:textarea.form-control {:field :textarea :id :comments}])])

(defn form-page []
  (let [doc (atom {:first-name "John" :last-name "Doe" :age 35
                   :email "john@doe.com" :comments "hello"})]
    (fn []
      [:div
       [:div.container
        [:section#header
         [:h1 "Movement Session"]]
        [:section#nav
         [:button.button {:on-click #(dispatch! "/")} "Session Generator"]
         [:button.button {:on-click #(dispatch! "/user")} "User Profile"]
         [:button.button {:on-click #(dispatch! "/template")} "Template Creator"]
         [:button.button {:on-click #(dispatch! "/movements")} "Movement Explorer"]]
        [:section#template
         [:div.page-header [:h1 "Reagent Form"]]
         [bind-fields
          form-template
          doc
          ; Optional event functions follow.
          ; These will be triggered whenever the doc is updated
          ; and are executed in the order they are listed.
          ; Events must take 3 params: id, value, document.
          ;#()
          ]
         [:label (str @doc)]]]])))