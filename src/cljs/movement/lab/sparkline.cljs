(ns movement.lab.sparkline)

(defn sparkline
  "
  'Sparklines are small, high resolution graphics embedded in a context of words, numbers and images.'
  - Edward Tufte

  This function takes a map that must include the HTML canvas element id and a list of data points to draw.
  Optionally the graphic can be drawn with a red colored endpoint,
  the color of the graph can be set and the type of graph to draw can be set to either
  a line graph :line or a bar chart :bar.
  "
  [{:keys [canvas_id data endpoint? color type]}]
  (let [c (.getElementById js/document canvas_id)
        ctx (.getContext c "2d")
        color (if color color "rgba(0,0,0,.5)")
        style (if type type :line)
        height (- (.-height c) 2)
        width (.-width c)
        total (count data)
        max (apply max data)
        xstep (/ width total)
        ystep (/ max height)
        x (atom 0)
        y (atom (- height (/ (first data) ystep)))]
    (.beginPath ctx)
    (set! (.-strokeStyle ctx) color)
    (.moveTo ctx @x @y)
    (doseq [i (range 1 total)]
      (reset! x (+ @x xstep))
      (reset! y (- height (/ (get data i) ystep)))
      (when (= style :bar) (.moveTo ctx @x height))
      (.lineTo ctx @x @y))
    (.stroke ctx)
    (when (and endpoint? (= style :line))
      (.beginPath ctx)
      (set! (.-fillStyle ctx) "rgba(255,0,0,.5)")
      (.arc ctx @x @y 1.5 0 (* (.-PI js/Math) 2))
      (.fill ctx))))

(def graph {:canvas_id "example1"
            :data [5 12 14 6 8 4 0 4 8 12 1 0 0 14 15]
            :endpoint? true
            :color "rgba(0,0,0,.5)"
            :type :line})

(def graph2 {:canvas_id "example2"
             :data [1345, 1267, 1178, 891, 1349, 1567, 1891, 1921, 2045, 2102, 2391,
                    2197, 1899, 1456, 1209, 781, 567, 344]
             :color "rgba(0,0,0,.5)"
             :type :line})

(def graph3 {:canvas_id "example3"
             :data [9, 37, 23, 28, 44, 26, 43, 43, 24, 33, 17, 18, 35, 31, 30, 49, 32, 25,
                    48, 37, 45, 20, 23, 34, 37, 26, 34, 38, 37, 34, 23, 26, 34, 37, 18, 39,
                    33, 28, 27, 24, 22, 35, 24, 38, 50, 45, 41, 18, 34, 19, 9]
             :endpoint? true
             :color "rgba(0,0,255,.5)"
             :type :bar})

;; -------------------------
;; Views

(defn sparklines-page []
  [:div
   [:div {:on-click #(do
                      (sparkline graph)
                      (sparkline graph2)
                      (sparkline graph3))} "Click to produce sparklines"]
   [:canvas {:id "example1" :height 25 :width 150}]
   [:canvas {:id "example2" :height 25 :width 150}]
   [:canvas {:id "example3" :height 25 :width 150}]])