(ns movement.styles
  (:require [garden.core :refer [css]]
            [garden.units :refer [px]]
            [garden.color :as color :refer [hsl rgb]]))

(defn insert-styles [styles]
  "Inserts Stylesheet into document head"
  (let [el (.createElement js/document "style")
        node (.createTextNode js/document styles)]
    (.appendChild el node)
    (.appendChild (.-head js/document) el)
    el))

(def base-css
  (css
    [:html {:font-size (px 15)}]
    [:body {:background-color "#fffff8"
            :color            (hsl 0 0 10)}]
    [:.content {:margin        "0 auto"
                :padding       "1rem 1rem"
                :max-width     (px 1200)
                :margin-bottom (px 50)}]
    [:h1 {:font-weight 400
          :margin-top "2rem"
          :margin-bottom "1.5rem"
          :font-size "3.2rem"
          :line-height 1}]
    [:p.subtitle {:margin-top "1rem"
                  :margin-bottom "5rem"
                  :font-size "1.6rem"
                  :display 'block
                  :line-height 1}]
    [:h2 {:font-weight 400
          :margin-top "2rem"
          :margin-bottom 0
          :font-size "2.4rem"
          :line-height 1}]
    [:h3 {:font-weight 400
          :font-size "1.5rem"
          :margin-top "1rem"
          :margin-bottom "1rem"
          :margin-left "0.5rem"
          :line-height 1}
     [:&:hover {:cursor 'pointer}]]
    [:h1 :h2 :p.subtitle {:text-align 'center}]
    [:.part {:margin-bottom "4rem"}]
    [:p :ol :ul {:font-size "1.2rem"}]
    [:p {:line-height "1.2rem"
         :margin-top "1.2rem"
         :margin-bottom "1.2rem"
         :padding-right 0
         :vertical-align 'baseline}]
    [:a {:text-decoration 'none
         :color (hsl 214 100 60)}
     [:&:hover {:cursor 'pointer}]]
    [:.blank-state {:margin-top "3rem"
                    :text-align 'center}]
    [:.primary-button {:color "#fff"
                       :background "#1b98f8"
                       :margin "1em 0"}]
    [:.secondary-button {:color "#333"
                         :background "#f4f4ff"
                         :margin "1em 0"
                         :padding "0.5em 2em"
                         :font-size "90%"}]
    [:.log-button {:color "#fff"
                   :background "#1b98f8"
                   :border "1px solid #ddd"
                   :padding "0.5em 2em"
                   :font-size "90%"}]
    [:.movement {:width (px 200)
                 :position 'relative
                 :margin-bottom (px 15)
                 :margin-right (px 15)
                 :padding (px 5)
                 :border-style 'solid
                 :border-width (px 1)
                 :border-color "#ffffa4"
                 :background-color (hsl 55 30 95)
                 :box-shadow "0px 0px 2px rgba(50,50,50,0.5)"
                 :border-radius (px 25)}]

    [:.refresh {:position 'relative
                :margin   "auto 0"
                :font-size "1.8em"
                :color "#99cc99"
                :cursor 'pointer
                :-webkit-transition "all 0.2s"
                :transition "all 0.2s"}]


    [:.description {:position 'relative
                    :padding 0
                    :margin "5px 0 0 15px"
                    :font-size "100%"}]
    [:.graphic {:display 'block
                :margin-left 'auto
                :margin-right 'auto
                :width (px 150)
                :height (px 150)
                :padding (px 0)
                :margin "15px 0px 0px 15px"}]
    [:.info {:position 'absolute
             :bottom (px 20)
             :right (px 20)}]
    [:.icon {:width (px 25)
             :height (px 25)}]
    [:.rep-text {:color "#68f"
                 :border-bottom "1px dashed #13f"}
     [:&:hover {:cursor 'pointer}]]
    [:.rep {}]
    [:.set-text {:color "#68f"
                 :border-bottom "1px dashed #13f"}
     [:&:hover {:cursor 'pointer}]]
    [:.set {}]
    [:.duration {}]))

(insert-styles base-css)