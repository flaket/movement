(ns movement.draggable
  (:import [goog.events EventType])
  (:require [goog.events :as events]
            [reagent.core :refer [atom]]))

(def draggable (atom {:x 100 :y 100}))
(def draggable-number (atom {}))

(defn get-client-rect [e]
  (let [r (.getBoundingClientRect (.-target e))]
    {:left (.-left r), :top (.-top r)}))

(defn draggable-number-component []
  (let [min 0
        max 100
        step 1
        hovering (atom false)
        dragging (atom false)
        mouse-move-handler (fn [offset]
                             (fn [e]
                               (let [x (int (- (.-clientX e) (:x offset)))
                                     x (int (/ (* x step) 10))
                                     x (Math/max x min)
                                     x (Math/min x max)
                                     y (int (- (.-clientY e) (:y offset)))]
                                 (reset! draggable-number {:x x
                                                           :y y}))))
        mouse-up-handler (fn [on-move]
                           (fn me [e]
                             (reset! dragging false)
                             (events/unlisten js/window EventType.MOUSEMOVE on-move)))]
    (fn []
      [:div
       [:span
        {:on-mouse-down  (fn [e]
                           (let [{:keys [left top]} (get-client-rect e)
                                 offset {:x (- (.-clientX e) left)
                                         :y (- (.-clientY e) top)}
                                 on-move (mouse-move-handler offset)]
                             (reset! dragging true)
                             (events/listen js/window EventType.MOUSEMOVE
                                            on-move)
                             (events/listen js/window EventType.MOUSEUP
                                            (mouse-up-handler on-move))))
         :on-mouse-enter #(reset! hovering true)
         :on-mouse-leave #(reset! hovering false)
         :className      (str "" (when @hovering " CursorDragHorizontal"))}
        (pr-str (:x @draggable-number))]])))

(defn draggable-button-component []
  (let [mouse-move-handler (fn [offset]
                             (fn [e]
                               (let [x (int (- (.-clientX e) (:x offset)))
                                     y (int (- (.-clientY e) (:y offset)))]
                                 (reset! draggable {:x x
                                                    :y y}))))
        mouse-up-handler (fn [on-move]
                           (fn me [e]
                             (events/unlisten js/window EventType.MOUSEMOVE on-move)))]
    (fn []
      [:div
       [:h1 (pr-str @draggable)]
       [:button
        {:style         {:position "absolute"
                         :left     (str (:x @draggable) "px") ;
                         :top      (str (:y @draggable) "px")}
         :on-mouse-down (fn [e]
                          (let [{:keys [left top]} (get-client-rect e)
                                offset {:x (- (.-clientX e) left)
                                        :y (- (.-clientY e) top)}
                                on-move (mouse-move-handler offset)]
                            (events/listen js/window EventType.MOUSEMOVE
                                           on-move)
                            (events/listen js/window EventType.MOUSEUP
                                           (mouse-up-handler on-move)))
                          )}
        "Drag me"]])))