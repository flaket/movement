(ns movement.template
 (:require [reagent.core :refer [atom]]
           [reagent.session :as session]
           [reagent-forms.core :refer [bind-fields]]
           [secretary.core :include-macros true :refer [dispatch!]]
           [movement.nav :refer [nav-component]]))


(defn form-component []
  [:div
   [:div.container
    [:section
     [:div
      [:label "Title:"]
      [:input {:type "text"
               :on-click #()}]]
     [:div]]]])

;--------------------

#_(def counter (atom 0))
#_(def include-date (atom true))

#_(defn count-component []
  [:div
   "The value is: "
   @counter "."
   [:input {:type "button" :value "Click!"
            :on-click #(swap! counter inc)}]])

#_(defn input [value]
  [:input {:type "text" :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

#_(defn btn [val]
  [:input {:type     "button" :value val
           :on-click #(reset! include-date (if @include-date false true))}])

#_(defn title-component []
  (let [val (atom "My Favourite Movements")]
    (fn []
      [:div "The title of sessions created with this template is: " [input val]
       " and it " [btn (if @include-date "should" "should not")]
       " include the date in the title."])))

#_(def data (atom {:m 4 :category :strength}))

#_(defn slider [param value min max]
  [:input {:type      "range" :value value
           :min min :max max
           :style     {:width "100%"}
           :on-change (fn [e]
                        (swap! data assoc param (.-target.value e)))}])

#_(defn slider-component []
  (let [{:keys [m category]} @data]
    [:div
     [:div m
      [slider :m m 0 10]]]))

#_(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 input]])

#_(def form-template
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

#_(defn form-component []
  (let [doc (atom {:first-name "John" :last-name "Doe" :age 35
                   :email "john@doe.com" :comments "hello"})]
    (fn []
      [:div
       [:div.container
        (nav-component)
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