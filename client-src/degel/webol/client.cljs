(ns degel.webol.client
  (:require [domina :as dom :refer [log]]
            [redmapel :as rml]
            [degel.utils.html :as dhtml]
            [degel.webol.page :as page]))


(def ^:export webol-tree (rml/make-redmapel-tree))

(defn fetch [key]
  (rml/fetch webol-tree key))

(defn put! [key value]
  (rml/put! webol-tree key value)
  value)

(defn update! [key f & args]
  (apply rml/update! webol-tree key f args)
  (fetch key))

(def memory-per-row 10)
(def memory-num-rows 10)


(defn clear-all []
  (doseq [n (range (* memory-per-row memory-num-rows))]
    (rml/put! webol-tree [:memory n] 0))
  (rml/put! webol-tree [:register :pc] 0)
  (rml/put! webol-tree [:register :sp] nil))

(defn register-name [index]
  (case index
    0 "Program counter"
    1 "Stack pointer"
    "Unknown"))

(defn register-key [index]
  (case index
    0 [:register :pc]
    1 [:register :sp]
    [:error]))

(defn or-empty [s]
  (str (or s "(empty)")))

(defn string-into [s n s1]
  (str (subs s 0 n) s1 (subs s (+ n (count s1)))))


(defn on-screen [fcn]
  (let [canvas (dom/by-id "sketchboard")
        context    (.getContext canvas "2d")
        width  (.-width canvas)
        height (.-height canvas)]
    (fcn {:canvas canvas :context context :width width :height height})))

(defn text-mode []
  (on-screen (fn [{:keys [context width height]}]
               (let [char-height 14]
                 (set! (.-globalCompositeOperation context) "copy")
                 (set! (.-fillStyle context) "black")
                 (set! (.-textBaseline context) "top")
                 (set! (.-font context) (str char-height "px Monospace"))
                 (let [char-width (-> context (.measureText "M") .-width)
                       width-in-chars (fix (/ width char-width))
                       height-in-lines (fix (/ height char-height))]
                   (log "TM D: " height-in-lines " " width-in-chars)
                   (.clearRect context 0 0 width height)
                   (put! [:screen :mode] :text)
                   (put! [:screen :line-height] char-height)
                   (put! [:screen :char-width] char-width)
                   (put! [:screen :width-in-chars] width-in-chars)
                   (put! [:screen :height-in-lines] height-in-lines)
                   (put! [:screen :text-x] 0)
                   (put! [:screen :text-y] 0)
                   (doseq [n (range height-in-lines)]
                       (put! [:screen :line n]
                           (clojure.string/join (repeat width-in-chars " ")))))))))

(defn text-pixel-pos
  ([]
     (text-pixel-pos (fetch [:screen :text-x]) (fetch [:screen :text-y])))
  ([text-x text-y]
     [(* text-x (fetch [:screen :char-width]))
      (* text-y (fetch [:screen :line-height]))]))


(defn text-scroll
  ([]
     (text-scroll 1))
  ([num-lines]
     (when (= (fetch [:screen :mode]) :text)
       (on-screen
        (fn [{:keys [canvas context width height]}]
          (log "scrolling")
          (let [height-in-lines (fetch [:screen :height-in-lines])
                line-height (fetch [:screen :line-height])
                rect-start (* line-height num-lines)
                rect-height (- height rect-start)]
            (log "height in lines: " height-in-lines)
            (log "line height: " line-height)
            (log "rect start: " rect-start)
            (log "rect height: " rect-height)
            (doseq [n (range height-in-lines)]
              (let [line (fetch [:screen :text n])]
                (.fillText context line 0 (* n line-height))))
            #_(.drawImage context
                        canvas 0 rect-start width rect-height
                        0 0 width rect-height)))))))

(defn text-out [text]
  (when (= (fetch [:screen :mode]) :text)
    (on-screen
     (fn [{:keys [context width height]}]
       (let [x (fetch [:screen :text-x])
             y (fetch [:screen :text-y])
             [canvas-x canvas-y] (text-pixel-pos x y)]
         (.fillText context text canvas-x canvas-y)
         (update! [:screen :text-x] + (count text))
         (log "TO F: y=" y " x=" x " text=" text)
         (update! [:screen :line y] identity text))))))


(defn newline-out []
  (when (= (fetch [:screen :mode]) :text)
    (on-screen
     (fn [{:keys [height]}]
       (put! [:screen :text-x] 0)
       (let [new-y (update! [:screen :text-y] inc)]
         (when (>= new-y (fetch [:screen :height-in-lines]))
           (text-scroll)
           (update! [:screen :text-y] dec)))))))


(defn ^:export init []
  (clear-all)
  (dom/set-html! (dom/by-id "page") (page/webol-page))
  (dom/set-html! (dom/by-id "memory")
    (dhtml/table memory-num-rows memory-per-row
                 :cell-fn #(let [loc (+ (* %1 memory-per-row) %2)]
                             (page/location-and-value
                              loc
                              (-> [:memory loc] fetch or-empty)))))
  (dom/set-html! (dom/by-id "registers")
    (dhtml/table 1 2
                 :cell-fn #(page/location-and-value
                            (register-name %2)
                            (->  (register-key %2) fetch or-empty))))
  (text-mode)
  (text-out "abc")
  (doseq [n (range 24)]
    (text-out (str "line " n))
    (newline-out)))
