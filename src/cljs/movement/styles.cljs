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
    [:body {:background-color (hsl 55 50 100)
            :color            (hsl 0 0 10)}]
    [:.content {:margin        "0 auto"
                :padding       "1rem 1rem"
                :max-width     (px 1000)
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
    [:.movement {:width (px 180)
                 :position 'relative
                 :margin-bottom (px 5)
                 :margin-right (px 5)
                 :padding (px 5)
                 :border-style 'solid
                 :border-width (px 1)
                 :border-color "#fffff4"
                 :background-color "#fff"
                 :box-shadow "0px 0px 1px rgba(50,50,50,0.5)"
                 :border-radius (px 25)}]

    [:.refresh {:position 'relative
                :left     (px 10)
                :width    (px 25)
                :height   (px 25)
                :margin   "auto 0"
                :font-size "1.8em"
                :color "#99cc99"
                :cursor 'pointer
                :-webkit-transition "all 0.2s"
                :transition "all 0.2s"}
     #_[:&:hover {:text-shadow "0 0 1px #000, 0 0 10px rgba(0,255,0,0.8"
                :-webkit-transform "scale(1.3)"
                :-ms-transform "scale(1.3)"
                :transform "scale(1.3)"}]
     #_[:&:after {:content "∴"}]]

    #_[:.destroy {:position 'absolute
                :top (px 10)
                :right (px 5)
                :width (px 25)
                :height (px 25)
                :margin "auto 0"
                :font-size "1.5em"
                :color "#CC9999"
                :cursor 'pointer
                  ;:-webkit-transition "all 0.2s"
                :transition "all 0.2s"}
     #_[:&:hover {:text-shadow "0 0 1px #000, 0 0 10px rgba(255,0,0,0.8"
                :-webkit-transform "scale(1.3)"
                :-ms-transform "scale(1.3)"
                :transform "scale(1.3)"}]
     #_[:&:destroy {:content "✖"}]]




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
                :margin "5px 10px 50px 10px"}]
    [:.icon {:width (px 25)
             :height (px 25)}]

    [:.sw {:position 'relative
           :bottom (px 40)}]
    [:.se {:position 'relative
           :bottom (px 40)}]


    [:.rep-text {:position 'absolute
                 :color "#68f"
                 :border-bottom "1px dashed #13f"}
     [:&:hover {:cursor 'pointer}]]
    [:.rep {:position 'absolute}]


    [:.set-text {:color "#68f"
                 :border-bottom "1px dashed #13f"
                 :position 'absolute}
     [:&:hover {:cursor 'pointer}]]
    [:.set {:position 'absolute}]
    [:.duration {:position 'absolute}]))

(insert-styles base-css)